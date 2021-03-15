package com.xy.netdev.rpt.service;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.xy.netdev.common.util.BeanFactoryUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.PrtclFormat;
import com.xy.netdev.network.NettyUtil;
import com.xy.netdev.rpt.bo.RptBodyDev;
import com.xy.netdev.rpt.bo.RptHeadDev;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.xy.netdev.common.util.ByteUtils.*;

/**
 * 站控支持
 * @author cc
 */
@Component
public class StationControlHandler implements IUpRptPrtclAnalysisService{


    @Setter
    @Getter
   public static class StationControlHeadEntity {

        private BaseInfo baseInfo;

        private String cmdMark;

        private Integer length;

        private byte[] paramData;

        private String className;

    }

    /**
     * 上报外部协议
     * @param socketEntity 数据体
     */
    public void stationControlReceive(SocketEntity socketEntity){
        unpackHead(socketEntity, BaseInfoContainer.getDevInfo(socketEntity.getRemoteAddress()))
                .ifPresent(this::receiverSocket);
    }

    /**
     * 数据头解析
     * @param socketEntity socket数据信息
     * @param devInfo 设备信息
     * @return 站控对象
     */
    private Optional<StationControlHeadEntity> unpackHead(SocketEntity socketEntity, BaseInfo devInfo){
        byte[] bytes = socketEntity.getBytes();
        int cmdMark = bytesToNum(bytes, 0, 2, ByteBuf::readShort);
        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterfaceOrPara(devInfo.getDevType(), Integer.toHexString(cmdMark));
        if (StrUtil.isBlank(prtclFormat.getFmtHandlerClass())){
            return Optional.empty();
        }
        int len = bytesToNum(bytes, 2, 2, ByteBuf::readShort);
        byte[] paramData = byteArrayCopy(bytes, 8, len - 8);

        StationControlHeadEntity stationControlHeadEntity = new StationControlHeadEntity();
        stationControlHeadEntity.setBaseInfo(devInfo);
        stationControlHeadEntity.setCmdMark(Integer.toHexString(cmdMark));
        stationControlHeadEntity.setLength(len);
        stationControlHeadEntity.setParamData(paramData);
        stationControlHeadEntity.setClassName(prtclFormat.getFmtHandlerClass());
        return Optional.of(stationControlHeadEntity);
    }


    /**
     * 收站控socket数据
     * @param stationControlHeadEntity 站控对象
     */
    private void receiverSocket(StationControlHeadEntity stationControlHeadEntity){
        ResponseService responseService = BeanFactoryUtil.getBean(stationControlHeadEntity.getClassName());
        //数据解析
        Object param = responseService.unpackBody(stationControlHeadEntity);
        //数据发送中心
        RptHeadDev rptHeadDev = new RptHeadDev();
        rptHeadDev.setDevNo(stationControlHeadEntity.getBaseInfo().getDevNo());
        rptHeadDev.setCmdMarkHexStr(stationControlHeadEntity.getCmdMark());
        rptHeadDev.setParam(param);
        responseService.callback(rptHeadDev);
        //调用应答
        ThreadUtil.execute(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
                //数据发送
                this.queryParaResponse(rptHeadDev);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }


    @Override
    public void queryParaResponse(RptHeadDev headDev) {
        BaseInfo stationInfo = BaseInfoContainer.getDevInfoByNo(headDev.getDevNo());
        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterfaceOrPara(stationInfo.getDevType(), headDev.getCmdMarkHexStr());
        RequestService requestService = BeanFactoryUtil.getBean(prtclFormat.getFmtHandlerClass());
        byte[] bodyBytes = requestService.pack(headDev);
        int port = Integer.parseInt(stationInfo.getDevPort());
        //拼数据头
        int cmd = Integer.parseInt(headDev.getCmdMarkHexStr(), 16);

        //拼成完整帧格式
        byte[] bytes = ArrayUtil.addAll(
                //信息类别
                  ByteUtils.objToBytes(cmd, 2)
                //数据字段长度
                , ByteUtils.objToBytes(bodyBytes.length, 2)
                //预留
                , ByteUtils.objToBytes(0, 4)
                //数据字段
                , bodyBytes);

        NettyUtil.sendMsg(bytes, port, stationInfo.getDevIpAddr(), port, Integer.parseInt(stationInfo.getDevNetPtcl()));
    }


    /**
     * 被动查询 参数头拼装
     * @param rptHeadDev 结构化数据
     * @param tempList 存储list
     */
    public static void queryHead(RptHeadDev rptHeadDev, List<byte[]> tempList) {
        //保留
        tempList.add(placeholderByte(4));
        //查询标志
        tempList.add(ByteUtils.objToBytes(Integer.parseInt(rptHeadDev.getCmdMarkHexStr(), 16), 1));
        //站号
        tempList.add(ByteUtils.objToBytes(rptHeadDev.getStationNo(), 1));
        //设备数量
        tempList.add(ByteUtils.objToBytes(rptHeadDev.getDevNum(), 1));
    }

    /**
     * 被动查询 参数头拼装下一步
     * @param tempList
     * @param rptBodyDev
     */
    public static void queryHeadNext(List<byte[]> tempList, RptBodyDev rptBodyDev) {
        //设备型号
        tempList.add(ByteUtils.objToBytes(rptBodyDev.getDevTypeCode(), 1));
        //设备编号
        tempList.add(ByteUtils.objToBytes(rptBodyDev.getDevNo(), 1));
    }

    /**
     * 查询/设置 公共解析头
     * @param stationControlHeadEntity
     * @param function
     * @return
     */
    public static RptHeadDev unpackCommonHead(StationControlHandler.StationControlHeadEntity stationControlHeadEntity,
                                              Function<byte[],  List<RptBodyDev>> function) {
        byte[] paramData = stationControlHeadEntity.getParamData();
        //查询标识
        int cmdMark = ByteUtils.byteToNumber(paramData, 4, 1).intValue();
        //站号
        int stationNo = ByteUtils.byteToNumber(paramData, 5, 1).intValue();
        //设备数量
        int devNum = ByteUtils.byteToNumber(paramData, 6, 1).intValue();
        //参数
        byte[] dataBytes = ByteUtils.byteArrayCopy(paramData, 7, paramData.length - 7);
        List<RptBodyDev> rptBodyDevs = function.apply(dataBytes);
        RptHeadDev rptHeadDev = new RptHeadDev();
        rptHeadDev.setStationNo(String.valueOf(stationNo));
        rptHeadDev.setCmdMarkHexStr(Integer.toHexString(cmdMark));
        rptHeadDev.setParam(rptBodyDevs);
        rptHeadDev.setDevNum(devNum);
        rptHeadDev.setDevNo(stationControlHeadEntity.getBaseInfo().getDevNo());
        return rptHeadDev;
    }
}
