package com.xy.netdev.sendrecv.base;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.SpringContextUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.factory.CtrlInterPrtcllFactory;
import com.xy.netdev.factory.ParaPrtclFactory;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.PrtclFormat;
import com.xy.netdev.network.NettyUtil;
import com.xy.netdev.sendrecv.base.service.ProtocolPackService;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import com.xy.netdev.sendrecv.enums.CallbackTypeEnum;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataSendService;
import io.netty.channel.ChannelFuture;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.xy.netdev.common.constant.SysConfigConstant.ACCESS_TYPE_PARAM;
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
    public void socketResponse(SocketEntity socketEntity,BaseInfo devInfo) {
            R r = (R)new FrameRespData();
            r.setDevType(devInfo.getDevType());
            r.setDevNo(devInfo.getDevNo());
            //数据拆包
            R unpackBytes = unpack((Q) socketEntity, r);
            //转16进制，用来获取协议解析类
            String cmdHexStr = cmdMarkConvert(r);
            r.setCmdMark(cmdHexStr);
            //根据cmd和设备类型获取具体的数据处理类
            PrtclFormat prtclFormat = null;
            if (ACCESS_TYPE_PARAM.equals(unpackBytes.getAccessType())){
                prtclFormat = BaseInfoContainer.getPrtclByPara(r.getDevType(), cmdHexStr);
            }else{
                prtclFormat = BaseInfoContainer.getPrtclByInterfaceOrPara(r.getDevType(), cmdHexStr);
            }
            if (prtclFormat.getFmtId() == null){
                //log.warn("设备:{}, 未找到数据体处理类", r.getDevNo());
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
            ICtrlInterPrtclAnalysisService iCtrlInterPrtclAnalysisService = null;
            if (prtclFormat.getIsPrtclParam() == 0){
                r.setAccessType(ACCESS_TYPE_PARAM);
                iParaPrtclAnalysisService = ParaPrtclFactory.genHandler(prtclFormat.getFmtHandlerClass());
            }else {
                switch (getCallbackType(unpackBytes)){
                    case ICTRLINTER_PRTCL:
                        iCtrlInterPrtclAnalysisService = CtrlInterPrtcllFactory.genHandler(prtclFormat.getFmtHandlerClass());
                        break;
                    default:
                        Object handler = SpringContextUtils.getBean(prtclFormat.getFmtHandlerClass());
                        if (handler instanceof IQueryInterPrtclAnalysisService){
                            r.setOperType(SysConfigConstant.OPREATE_QUERY_RESP);
                            queryInterPrtclAnalysisService = (IQueryInterPrtclAnalysisService)handler;
                        }else if (handler instanceof ICtrlInterPrtclAnalysisService){
                            r.setOperType(SysConfigConstant.OPREATE_CONTROL_RESP);
                            iCtrlInterPrtclAnalysisService = (ICtrlInterPrtclAnalysisService)handler;
                        }else if (handler instanceof IParaPrtclAnalysisService){
                            iParaPrtclAnalysisService = (IParaPrtclAnalysisService)handler;
                        }
                        r.setAccessType(SysConfigConstant.ACCESS_TYPE_INTERF);
                        break;
                }
            }
            setReceiveOriginalData(r, socketEntity.getBytes());
            //执行回调方法
            this.callback(unpackBytes, iParaPrtclAnalysisService, queryInterPrtclAnalysisService, iCtrlInterPrtclAnalysisService);
            log.debug("设备数据已发送至对应模块, 数据体:{}", JSON.toJSONString(unpackBytes));
        }

    /**
     * 不同协议 cmd 关键字转换处理 默认不做转换
     * @param frameRespData 响应数据结构
     * @return 转换的cmd
     */
    public String cmdMarkConvert(FrameRespData frameRespData){return frameRespData.getCmdMark();}


    /**
     * 获取头部返回值类型
     * @param r 返回字节
     * @return 返回值
     */
    protected CallbackTypeEnum getCallbackType(R r){
        String cmdMark = r.getCmdMark();
        switch (cmdMark){
            //81：车载卫星天线接口设置响应命令字
            case "81":
                return CallbackTypeEnum.ICTRLINTER_PRTCL;
            default:
                return CallbackTypeEnum.DEFAULT;
        }

    }


    /**
     * 回调别的模块数据
     * @param r 设备数据已发送至对应模块
     * @param iParaPrtclAnalysisService 参数对象
     * @param iQueryInterPrtclAnalysisService 接口对象
     * @param ctrlInterPrtclAnalysisService
     */
    public abstract void callback(R r, IParaPrtclAnalysisService iParaPrtclAnalysisService,
                                  IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService, ICtrlInterPrtclAnalysisService ctrlInterPrtclAnalysisService);


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
        int localPort = port;
        if (StrUtil.isNotBlank(devInfo.getDevLocalPort())){
            localPort = Integer.parseInt(devInfo.getDevLocalPort());
        }
        return NettyUtil.sendMsg(bytes, localPort, devInfo.getDevIpAddr(), port, Integer.parseInt(sysParamService.getParaRemark1(devInfo.getDevNetPtcl())));
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
