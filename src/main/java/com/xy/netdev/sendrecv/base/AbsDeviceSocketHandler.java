package com.xy.netdev.sendrecv.base;

import cn.hutool.core.util.HexUtil;
import com.alibaba.fastjson.JSON;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.factory.ParaPrtclFactory;
import com.xy.netdev.factory.QueryInterPrtcllFactory;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.PrtclFormat;
import com.xy.netdev.network.NettyUtil;
import com.xy.netdev.sendrecv.base.service.ProtocolPackService;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
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
    @Autowired
    private ISysParamService sysParamService;

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

    /**
     * 设置发送原始数据十六进制字符串
     * @param t t
     */
    protected void setSendOriginalData(T t, byte[] bytes){
        t.setSendOriginalData(HexUtil.encodeHexStr(bytes).toUpperCase());
    }

    /**
     * 设置接收原始数据十六进制字符串
     * @param r
     */
    protected void setReceiveOriginalData(R r, byte[] bytes){
        r.setReciveOriginalData(HexUtil.encodeHexStr(bytes).toUpperCase());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void socketResponse(SocketEntity socketEntity) {
        //获取设备参数信息
        BaseInfo devInfo = getDevInfo(socketEntity.getRemoteAddress());
        R r = (R)new FrameRespData();
        r.setDevType(devInfo.getDevType());
        r.setDevNo(devInfo.getDevNo());

        //数据拆包
        R unpackBytes = unpack((Q) socketEntity, r);
        //转16进制，用来获取协议解析类
        String cmdHexStr = cmdMarkConvert(r);

        //根据cmd和设备类型获取具体的数据处理类
        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterfaceOrPara(r.getDevType(), cmdHexStr);
        if (prtclFormat == null){
            log.warn("设备:{}, 未找到数据体处理类", r.getDevNo());
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
            r.setAccessType(SysConfigConstant.ACCESS_TYPE_PARAM);
            iParaPrtclAnalysisService = ParaPrtclFactory.genHandler(prtclFormat.getFmtHandlerClass());
        }else {
            r.setAccessType(SysConfigConstant.ACCESS_TYPE_INTERF);
            queryInterPrtclAnalysisService = QueryInterPrtcllFactory.genHandler(prtclFormat.getFmtHandlerClass());
        }
        setReceiveOriginalData(r, socketEntity.getBytes());
        r.setCmdMark(cmdHexStr);

        //执行回调方法
        this.callback(unpackBytes, iParaPrtclAnalysisService, queryInterPrtclAnalysisService);
        log.debug("设备数据已发送至对应模块, 数据体:{}", JSON.toJSONString(unpackBytes));
    }

    /**
     * 不同协议 cmd 关键字转换处理 默认不做转换
     * @param frameRespData 响应数据结构
     * @return 转换的cmd
     */
    public String cmdMarkConvert(FrameRespData frameRespData){return frameRespData.getCmdMark();}


    /**
     * 回调别的模块数据
     * @param r 设备数据已发送至对应模块
     * @param iParaPrtclAnalysisService 参数对象
     * @param iQueryInterPrtclAnalysisService 接口对象
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
        return NettyUtil.sendMsg(bytes, port, devInfo.getDevIpAddr(), port, Integer.parseInt(sysParamService.getParaRemark1(devInfo.getDevNetPtcl())));
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
            //设置发送原始数据
            setSendOriginalData(t, bytes);
            //回调
            dataSendService.notifyNetworkResult(t);
        }));
    }

}
