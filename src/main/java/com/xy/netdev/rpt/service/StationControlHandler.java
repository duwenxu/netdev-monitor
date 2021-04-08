package com.xy.netdev.rpt.service;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.util.BeanFactoryUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.network.NettyUtil;
import com.xy.netdev.rpt.bo.RptBodyDev;
import com.xy.netdev.rpt.bo.RptHeadDev;
import com.xy.netdev.rpt.enums.AchieveClassNameEnum;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.xy.netdev.common.util.ByteUtils.*;

/**
 * 站控支持
 * @author cc
 */
@Component
@Slf4j
public class StationControlHandler implements IUpRptPrtclAnalysisService{

    @Autowired
    @Qualifier("IDownRptPrtclAnalysisServiceImpl")
    private IDownRptPrtclAnalysisService iDownRptPrtclAnalysisService;

    @Autowired
    private ISysParamService sysParamService;

    @Setter
    @Getter
   public static class StationControlHeadEntity {

        private BaseInfo baseInfo;

        private String cmdMark;

        private Integer length;

        private byte[] paramData;

    }

    /**
     * 收站控数据
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
        int len = bytesToNum(bytes, 2, 2, ByteBuf::readShort);
        byte[] paramData = byteArrayCopy(bytes, 8, len);
        StationControlHeadEntity stationControlHeadEntity = new StationControlHeadEntity();
        stationControlHeadEntity.setBaseInfo(devInfo);
        stationControlHeadEntity.setCmdMark(Integer.toHexString(cmdMark));
        stationControlHeadEntity.setLength(len);
        stationControlHeadEntity.setParamData(paramData);
        return Optional.of(stationControlHeadEntity);
    }


    /**
     * 收站控socket数据
     * @param stationControlHeadEntity 站控对象
     */
    private void receiverSocket(StationControlHeadEntity stationControlHeadEntity){
        RptHeadDev rptHeadDev = new RptHeadDev();
        rptHeadDev.setDevNo(stationControlHeadEntity.getBaseInfo().getDevNo());
        rptHeadDev.setCmdMarkHexStr(stationControlHeadEntity.getCmdMark());
        setAchieveClass(rptHeadDev);
        ResponseService responseService = BeanFactoryUtil.getBean(rptHeadDev.getAchieveClassNameEnum().getClassName());
        //数据解析
        rptHeadDev = responseService.unpackBody(stationControlHeadEntity, rptHeadDev);
        //执行设备查询/设置流程
        iDownRptPrtclAnalysisService.doAction(rptHeadDev);
        //等待一秒获取缓存更新结果
        RptHeadDev finalRptHeadDev = rptHeadDev;
        ThreadUtil.execute(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
                //重新获取缓存
                RptHeadDev headDev = iDownRptPrtclAnalysisService.queryNewCache(finalRptHeadDev);
                //调用数据外发
                this.queryParaResponse(headDev);
            } catch (InterruptedException e) {
                log.error("站控等待返回缓存结果异常中断, 中断原因:{}", e.getMessage(), e);
            }
        });
    }


    @Override
    public void queryParaResponse(RptHeadDev headDev) {
        setAchieveClass(headDev);
        BaseInfo stationInfo = BaseInfoContainer.genRptBaseInfo();
        RequestService requestService = BeanFactoryUtil.getBean(headDev.getAchieveClassNameEnum().getClassName());
        byte[] bodyBytes = requestService.pack(headDev);
        int port = Integer.parseInt(stationInfo.getDevPort());
        int cmd = Integer.parseInt(headDev.getCmdMarkHexStr(), 16);
        //拼数据头
        byte[] bytes = ArrayUtil.addAll(
                //信息类别
                  ByteUtils.objToBytes(cmd, 2)
                //数据字段长度
                , ByteUtils.objToBytes(bodyBytes.length, 2)
                //预留
                , ByteUtils.objToBytes(0, 4)
                //数据字段
                , bodyBytes);
        NettyUtil.sendMsg(bytes, port, stationInfo.getDevIpAddr(), port, Integer.parseInt(sysParamService.getParaRemark1(stationInfo.getDevNetPtcl())));
        log.info("发送站控数据, 目标地址:{}:{}, 数据体:{}",  stationInfo.getDevIpAddr(), port, HexUtil.encodeHexStr(bytes));
    }


    /**
     * 响应头设置
     * @param rptHeadDev 结构化数据
     * @param tempList 存储list
     */
    public static void setQueryResponseHead(RptHeadDev rptHeadDev, List<byte[]> tempList) {
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
     * @param rptHeadDev
     * @param function
     * @return
     */
    public static RptHeadDev unpackCommonHead(StationControlHeadEntity stationControlHeadEntity,
                                              RptHeadDev rptHeadDev, Function<byte[], List<RptBodyDev>> function) {
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
        rptHeadDev.setStationNo(String.valueOf(stationNo));
        rptHeadDev.setCmdMarkHexStr(Integer.toHexString(cmdMark));
        rptHeadDev.setParam(rptBodyDevs);
        rptHeadDev.setDevNum(devNum);
        rptHeadDev.setDevNo(stationControlHeadEntity.getBaseInfo().getDevNo());
        return rptHeadDev;
    }


    /**
     * 参数赋值并获取下一个起始位下标
     * @param list
     * @param rptBodyDev
     * @param paramBytes
     * @param length
     * @param offset
     * @return
     */
    public static int getIndex(List<RptBodyDev> list, RptBodyDev rptBodyDev, byte[] paramBytes, int length, int offset) {
        //参数解析
        List<FrameParaData> devParaList = new ArrayList<>(length);
        for (byte paramByte : paramBytes) {
            FrameParaData frameParaData = new FrameParaData();
            frameParaData.setParaNo(String.valueOf(paramByte));
            devParaList.add(frameParaData);
        }
        rptBodyDev.setDevParaList(devParaList);
        list.add(rptBodyDev);
        return length + offset;
    }

    /**
     * 公共拆包方法
     * @param rptHeadDev
     * @param consumer
     * @return
     */
    @SuppressWarnings("unchecked")
    public static byte[] commonPack(RptHeadDev rptHeadDev, BiConsumer<List<FrameParaData>, List<byte[]>> consumer){
        List<RptBodyDev> rptBodyDevs = (List<RptBodyDev>) rptHeadDev.getParam();
        List<byte[]> tempList = new ArrayList<>();
        setQueryResponseHead(rptHeadDev, tempList);
        rptBodyDevs.forEach(rptBodyDev -> {
            queryHeadNext(tempList, rptBodyDev);
            List<FrameParaData> devParaList = rptBodyDev.getDevParaList();
            int parmaSize = devParaList.size();
            //设备参数数量
            tempList.add(ByteUtils.objToBytes(parmaSize, 1));
            consumer.accept(devParaList, tempList);
        });
        return listToBytes(tempList);
    }


    /**
     * 设置站控class
     * @param rptHeadDev
     */
    private static void setAchieveClass(RptHeadDev rptHeadDev){
        int cmd = Integer.parseInt(rptHeadDev.getCmdMarkHexStr(), 16);
        AchieveClassNameEnum achieveClassNameEnum = null;
        switch (cmd){
            case 1:
                achieveClassNameEnum = AchieveClassNameEnum.REPORT_STATUS;
                break;
            case 2:
                achieveClassNameEnum = AchieveClassNameEnum.REPORT_WARN;
                break;
            case 3:
            case 4:
                achieveClassNameEnum = AchieveClassNameEnum.PARAM_QUERY;
                break;
            case 5:
            case 6:
                achieveClassNameEnum = AchieveClassNameEnum.PARAM_SET;
                break;
            case 7:
            case 8:
                achieveClassNameEnum = AchieveClassNameEnum.PARAM_WARN;
                break;
            default:break;
        }
        rptHeadDev.setAchieveClassNameEnum(achieveClassNameEnum);
    }

}
