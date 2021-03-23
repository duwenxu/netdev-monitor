package com.xy.netdev.frame.base;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.factory.ParaPrtclFactory;
import com.xy.netdev.factory.QueryInterPrtcllFactory;
import com.xy.netdev.frame.base.service.ProtocolPackService;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.monitor.constant.MonitorConstants;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.PrtclFormat;
import com.xy.netdev.network.NettyUtil;
import com.xy.netdev.transit.IDataSendService;
import io.netty.channel.ChannelFuture;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.xy.netdev.container.BaseInfoContainer.getDevInfo;

/**
 * 设备数据流程处理基类
 * @author cc
 */
@Component
@Slf4j
public abstract class AbsDeviceSocketHandler<Q extends SocketEntity, T extends FrameReqData, R extends FrameRespData>
        extends DeviceSocketBaseHandler<T, R> implements ProtocolPackService<Q, T, R> {

    @Autowired
    private IDataSendService dataSendService;

    @Override
    public void socketRequest(T t, ProtocolRequestEnum requestEnum) {
        switch (requestEnum){
            case CONTROL:
                doControl(t);
                break;
            case QUERY_RESULT:
                doQueryResult(t);
                break;
            case CONTROL_RESULT:
                doControlResult(t);
                break;
            default:
                doQuery(t);
                break;
        }
    }

    @SneakyThrows
    @Override
    public void doQuery(T t) {
        //数据封包
        byte[] packBytes = pack(t);
        //是否发送成功
        sendMsg(t, packBytes);
    }

    @Override
    public void doControl(T t) {
        this.doQuery(t);
    }

    @Override
    public void doQueryResult(T t) {
        this.doQuery(t);
    }

    @Override
    public void doControlResult(T t) {
        this.doQuery(t);
    }



    @Override
    @SuppressWarnings("unchecked")
    public void socketResponse(SocketEntity socketEntity) {
        //获取设备参数信息
        BaseInfo devInfo = getDevInfo(socketEntity.getRemoteAddress());
        FrameRespData frameRespData = new FrameRespData();
        frameRespData.setDevType(devInfo.getDevType());
        frameRespData.setDevNo(devInfo.getDevNo());

        //数据拆包
        R unpackBytes = unpack((Q) socketEntity, (R) frameRespData);
        //转16进制，用来获取协议解析类
        String cmdHexStr = frameRespData.getCmdMark();

        //获取设备CMD信息, '/'为调制解调器特殊格式, 因为调制解调器cmd为字符串, 不能进行十六进制转换, 所以特殊区分
        if (!MonitorConstants.SUB_MODEM.equals(frameRespData.getDevType())){
            if (!StrUtil.contains(frameRespData.getCmdMark(), '/')){
                cmdHexStr = Integer.toHexString(Integer.parseInt(frameRespData.getCmdMark(),16));
            }else {
                cmdHexStr = StrUtil.removeAll(frameRespData.getCmdMark(), '/');
            }
        }

        //根据cmd和设备类型获取具体的数据处理类
        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterfaceOrPara(frameRespData.getDevType(), cmdHexStr);
        if (prtclFormat == null){
            log.warn("设备:{}, 未找到数据体处理类", frameRespData.getDevNo());
            return;
        }
        if (prtclFormat.getIsPrtclParam() == null){
            log.warn("数据解析失败, 未获取到处理类,  发数据地址:{}:{}, cmd:{}, 数据体:{}", socketEntity.getRemoteAddress(),
                    socketEntity.getRemotePort(), cmdHexStr, HexUtil.encodeHexStr(socketEntity.getBytes()));
            return;
        }

        //初始化协议和接口
        IParaPrtclAnalysisService iParaPrtclAnalysisService = null;
        IQueryInterPrtclAnalysisService queryInterPrtclAnalysisService = null;
        if (prtclFormat.getIsPrtclParam() == 0){
            frameRespData.setAccessType(SysConfigConstant.ACCESS_TYPE_PARAM);
            iParaPrtclAnalysisService = ParaPrtclFactory.genHandler(prtclFormat.getFmtHandlerClass());
        }else {
            frameRespData.setAccessType(SysConfigConstant.ACCESS_TYPE_INTERF);
            queryInterPrtclAnalysisService = QueryInterPrtcllFactory.genHandler(prtclFormat.getFmtHandlerClass());
        }
        frameRespData.setReciveOrignData(HexUtil.encodeHexStr(socketEntity.getBytes()).toUpperCase());
        frameRespData.setCmdMark(cmdHexStr);

        //执行回调方法
        this.callback(unpackBytes, iParaPrtclAnalysisService, queryInterPrtclAnalysisService);
        log.debug("设备数据已发送至对应模块, 数据体:{}", JSON.toJSONString(unpackBytes));
    }

    /**
     * 回调别的模块数据
     * @param r 设备数据已发送至对应模块
     * @param iParaPrtclAnalysisService
     * @param iQueryInterPrtclAnalysisService
     */
    public abstract void callback(R r, IParaPrtclAnalysisService iParaPrtclAnalysisService,
                                  IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService);


    /**
     * 数据发送
     * @param t t
     * @param bytes 原始数据
     * @return 回调结果
     */
    @SneakyThrows
    protected Optional<ChannelFuture> sendData(T t, byte[] bytes) {
        BaseInfo devInfo = BaseInfoContainer.getDevInfoByNo(t.getDevNo());
        int port = Integer.parseInt(devInfo.getDevPort());
        return NettyUtil.sendMsg(bytes, port, devInfo.getDevIpAddr(), port, Integer.parseInt(devInfo.getDevNetPtcl()));
    }

    /**
     * 消息发送
     * @param t t
     * @param bytes 目标字节
     */
    private void sendMsg(T t, byte[] bytes) {
        sendData(t, bytes).ifPresent(channelFuture -> channelFuture.addListener(future -> {
            //设置发送结果
            if (future.isSuccess()){
                t.setIsOk("0");
            }else {
                t.setIsOk("1");
            }
            //设置发送原始数据十六进制字符串
            t.setSendOrignData(HexUtil.encodeHexStr(bytes).toUpperCase());
            //回调
            dataSendService.notifyNetworkResult(t);
        }));
    }

//    public static void main(String[] args) {
//        String str = "7F 31 00 10 01 01 03 00 04 00 00 01 01 01 04 02 BC 02 02 01 20 00 2F 01 E1 01 01 01 01 01 00 00" +
//                " 02 BC 01 01 02 17 00 06 01 E0 01 01 01 01 01 3F 7D";
//        System.out.println(str.replaceAll("\\s+", ""));
//
//    }
}
