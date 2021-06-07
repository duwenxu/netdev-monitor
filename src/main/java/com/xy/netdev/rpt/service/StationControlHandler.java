package com.xy.netdev.rpt.service;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.xy.common.exception.BaseException;
import com.xy.netdev.admin.service.ISysDepartService;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.util.BeanFactoryUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.rpt.enums.StationCtlRequestEnums;
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
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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

    private static ISysParamService sysParamService;

    @Autowired
    @Qualifier("IDownRptPrtclAnalysisServiceImpl")
    private IDownRptPrtclAnalysisService iDownRptPrtclAnalysisService;

    @Autowired
    private ISysParamService iSysParamService;

    @PostConstruct
    void init(){
        sysParamService = iSysParamService;
    }

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
        unpackHead(socketEntity, BaseInfoContainer.getDevInfo(socketEntity.getRemoteAddress()).get(0))
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
        byte[] paramData = new byte[0];
        try {
            paramData = byteArrayCopy(bytes, 8, len);
        } catch (Exception e) {
            log.error("bytes：{},len:{}",bytes,len);
        }
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
        ResponseService responseService = BeanFactoryUtil.getBean(rptHeadDev.getAchieveClassNameEnum().getClazzName());
        //数据解析
        rptHeadDev = responseService.unpackBody(stationControlHeadEntity, rptHeadDev);
        //执行设备查询/设置流程
        iDownRptPrtclAnalysisService.doAction(rptHeadDev);
        //等待一秒获取缓存更新结果
        RptHeadDev finalRptHeadDev = rptHeadDev;
        ThreadUtil.execute(() -> {
            try {
                //等等缓存更新
                TimeUnit.SECONDS.sleep(1);
                //重新获取缓存
                RptHeadDev headDev = iDownRptPrtclAnalysisService.queryNewCache(finalRptHeadDev);
                //调用数据外发
                String msgType = headDev.getCmdMarkHexStr();
                if(headDev.getCmdMarkHexStr().equals("4")){
                    this.queryParaResponse(headDev, StationCtlRequestEnums.PARA_QUERY_RESPONSE);
                }else if(headDev.getCmdMarkHexStr().equals("8")) {
                    this.queryParaResponse(headDev, StationCtlRequestEnums.PARA_WARNING_QUERY_RESP);
                }else {
                        this.queryParaResponse(headDev, StationCtlRequestEnums.PARA_SET_RESPONSE);
                    }
            } catch (InterruptedException e) {
                log.error("站控等待返回缓存结果异常中断, 中断原因:{}", e.getMessage(), e);
            }
        });
    }


    @Override
    public synchronized void queryParaResponse(RptHeadDev headDev,StationCtlRequestEnums stationCtlRequestEnums) {
        BaseInfo stationInfo = null;
        byte[] bodyBytes = new byte[0];
        int port = 0;
        int cmd = 0;
        try {
            setAchieveClass(headDev);
            stationInfo = BaseInfoContainer.genRptBaseInfo();
            if (headDev.getAchieveClassNameEnum()==null){
                headDev.setAchieveClassNameEnum(AchieveClassNameEnum.PARAM_QUERY);
            }
            RequestService requestService = BeanFactoryUtil.getBean(headDev.getAchieveClassNameEnum().getClazzName());
            bodyBytes = requestService.pack(headDev,stationCtlRequestEnums);
            port = Integer.parseInt(stationInfo.getDevPort());
            cmd = Integer.parseInt(headDev.getCmdMarkHexStr(), 16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int localPort = port;
        if (StrUtil.isNotBlank(stationInfo.getDevLocalPort())){
            localPort = Integer.parseInt(stationInfo.getDevLocalPort());
        }
        NettyUtil.sendMsg(bodyBytes, localPort, stationInfo.getDevIpAddr(), port, Integer.parseInt(iSysParamService.getParaRemark1(stationInfo.getDevNetPtcl())));
        log.debug("发送站控数据, 本地端口：{}，  目标地址:{}:{}, 数据体:{}", localPort, stationInfo.getDevIpAddr(), port, HexUtil.encodeHexStr(bodyBytes));
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
        String sn = StringUtils.isEmpty(rptHeadDev.getSN()) ? "0" : rptHeadDev.getSN();
        tempList.add(ByteUtils.objToBytes(Integer.parseInt(sn, 16), 1));
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
    //todo luo
    public static void queryHeadNext(List<byte[]> tempList, RptBodyDev rptBodyDev) {
        String devCode = "";
        if(rptBodyDev.getDevTypeCode().length()==7 && rptBodyDev.getDevTypeCode().startsWith("0020")){
            devCode = sysParamService.getParaRemark1(rptBodyDev.getDevTypeCode());
        }else{
            devCode = rptBodyDev.getDevTypeCode();
        }
        byte codeByte = objToBytes(devCode, 1)[0];
        byte[] bytes = {0x39, codeByte};
        //设备型号
        tempList.add(bytes);
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
        //查询标识和设置标号  todo 这里解析到的查询标识始终是:0
        int sn = ByteUtils.byteToNumber(paramData, 4, 1).intValue();
        //站号
        int stationNo = ByteUtils.byteToNumber(paramData, 5, 1).intValue();
        //设备数量
        int devNum = ByteUtils.byteToNumber(paramData, 6, 1).intValue();
        //参数
        byte[] dataBytes = ByteUtils.byteArrayCopy(paramData, 7, paramData.length - 7);
        List<RptBodyDev> rptBodyDevs = function.apply(dataBytes);
        rptHeadDev.setStationNo(String.valueOf(stationNo));
        rptHeadDev.setSN(String.valueOf(sn));
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
    public static List<byte[]> commonPack(RptHeadDev rptHeadDev, BiConsumer<List<FrameParaData>, List<byte[]>> consumer){
        List<RptBodyDev> rptBodyDevs = (List<RptBodyDev>) rptHeadDev.getParam();
        List<byte[]> tempList = new ArrayList<>();
        //通用设置响应头
        setQueryResponseHead(rptHeadDev, tempList);
        rptBodyDevs.forEach(rptBodyDev -> {
            queryHeadNext(tempList, rptBodyDev);
            List<FrameParaData> devParaList = rptBodyDev.getDevParaList();
            int parmaSize = devParaList.size();
            //设备参数数量
            tempList.add(ByteUtils.objToBytes(parmaSize, 1));
            consumer.accept(devParaList, tempList);
        });
        return tempList;
    }


    /**
     * 设置站控class
     * @param rptHeadDev
     */
    private static void setAchieveClass(RptHeadDev rptHeadDev){
        if (rptHeadDev.getCmdMarkHexStr().equals("ffff8967")){
            log.info("收到站控心跳包...........");
            return;
        }
        int cmd = 0;
        try {
            cmd = Integer.parseInt(rptHeadDev.getCmdMarkHexStr(), 16);
        } catch (Exception e) {
            log.error("站控接收帧转换cmd关键字错误，cmdStr:{}",rptHeadDev.getCmdMarkHexStr());
        }
        AchieveClassNameEnum achieveClassNameEnum = null;
        switch (cmd){
            case 1:
                achieveClassNameEnum = AchieveClassNameEnum.REPORT_STATUS;
                break;
            case 2:
                achieveClassNameEnum = AchieveClassNameEnum.REPORT_WARN;
                break;
            case 3:
                       break;
            case 4:
                achieveClassNameEnum = AchieveClassNameEnum.PARAM_QUERY;
                break;
            case 5:
                achieveClassNameEnum = AchieveClassNameEnum.PARAM_SET;
                break;
            case 6:
                achieveClassNameEnum = AchieveClassNameEnum.PARAM_SET_RESP;
                break;
            case 7:
                achieveClassNameEnum = AchieveClassNameEnum.PARAM_WARN;
                break;
            case 8:
                achieveClassNameEnum = AchieveClassNameEnum.PARAM_WARN;
                break;
            default:break;
        }
        rptHeadDev.setAchieveClassNameEnum(achieveClassNameEnum);
    }

}
