package com.xy.netdev.rpt.service.impl;

import cn.hutool.core.util.StrUtil;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.container.DevAlertInfoContainer;
import com.xy.netdev.container.DevLogInfoContainer;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.bo.ParaViewInfo;
import com.xy.netdev.monitor.entity.AlertInfo;
import com.xy.netdev.rpt.bo.RptBodyDev;
import com.xy.netdev.rpt.bo.RptHeadDev;
import com.xy.netdev.rpt.enums.StationCtlRequestEnums;
import com.xy.netdev.rpt.service.IDownRptPrtclAnalysisService;
import com.xy.netdev.transit.impl.DevCmdSendService;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 站控 参数查询/设置 实现
 *
 * @author duwenxu
 * @create 2021-03-15 11:19
 */
@Service
@Slf4j
public class IDownRptPrtclAnalysisServiceImpl implements IDownRptPrtclAnalysisService {

    @Autowired
    private DevCmdSendService devCmdSendService;

    /**
     * 标识：查询所有参数
     */
    private static final String ALL_PARAS_QUERY = "0";

    @Override
    public RptHeadDev doAction(RptHeadDev headDev) {
        //站控命令标识
        String cmdMarkHexStr = headDev.getCmdMarkHexStr();
        RptHeadDev resBody = new RptHeadDev();
        try {
            switch (cmdMarkHexStr) {
                case "0005":
                    doParaSetAction(headDev);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("站控指令请求执行异常...devNo={},cmdMark={}", headDev.getDevNo(), headDev.getCmdMarkHexStr());
        }
        return resBody;
    }


    @Override
    public RptHeadDev queryNewCache(RptHeadDev headDev) {
        String cmdMarkHexStr = headDev.getCmdMarkHexStr();
        RptHeadDev resBody = new RptHeadDev();
        try {
            switch (Integer.parseInt(cmdMarkHexStr)) {
                case 3:
                    resBody = doQueryNewCache(headDev);
                    break;
                case 5:
                    resBody = doQuerySetCache(headDev);
                    break;
                case 7:
                    resBody = doParaWarnQueryAction((headDev));
                    break;
                default:
                    resBody = headDev;
                    break;
            }
        } catch (Exception e) {
            log.error("站控指令响应查询异常...devNo={},cmdMark={}, 异常原因:{}", headDev.getDevNo(), headDev.getCmdMarkHexStr(), e);
        }
        return resBody;
    }


    /**
     * 查询控制响应结果
     * @param rptHeadDev 参数设置结构体
     * @return 控制响应结果
     */
    private RptHeadDev doQuerySetCache(RptHeadDev rptHeadDev) {
        List<RptBodyDev> rptBodyDev = (List<RptBodyDev>) rptHeadDev.getParam();
        //遍历参数设置值
        rptBodyDev.forEach(body -> {
            body.getDevParaList().forEach(para -> {
                String respStatus = DevLogInfoContainer.getDevParaRespStatus(body.getDevNo(), para.getParaNo());
                para.setParaSetRes(respStatus);
            });
        });
        rptHeadDev.setCmdMarkHexStr(StationCtlRequestEnums.PARA_SET_RESPONSE.getCmdCode());
        return rptHeadDev;
    }

    /**
     * 站控 参数设置
     *
     * @param headDev 参数设置结构体
     */
    private void doParaSetAction(RptHeadDev headDev) {
        Thread.currentThread().setName(headDev.getDevNo() + "doParaSetAction-thread");
        List<RptBodyDev> rptBodyDev = (List<RptBodyDev>) headDev.getParam();
        //阻塞式的进行参数设置调用
        rptBodyDev.forEach(rptBody -> {
            String devNo = rptBody.getDevNo();
            //请求间隔
            Integer intervalTime = BaseInfoContainer.getDevInfoByNo(devNo).getDevIntervalTime();
            //参数设置请求初始化
            List<FrameParaData> canBeWriteFrameDataList = initParaSetAction(rptBody);
            for (FrameParaData paraData : canBeWriteFrameDataList) {
                try {
                    Thread.sleep(intervalTime);
                } catch (InterruptedException e) {
                    log.error("线程+{}+休眠发生异常！", Thread.currentThread().getName());
                }
                String cmkMark = BaseInfoContainer.getParaInfoByNo(paraData.getDevType(), paraData.getParaNo()).getCmdMark();
                //参数控制
                devCmdSendService.paraCtrSend(devNo, cmkMark, paraData.getParaVal());
            }
        });
    }

    /**
     * 初始化设置响应状态 不可写的设置为 不合法|其它先设置为 未响应
     *
     * @param rptBody 单个设备的上报对象
     * @return 可设置的参数列表
     */
    private List<FrameParaData> initParaSetAction(RptBodyDev rptBody) {
        List<FrameParaData> devParaList = rptBody.getDevParaList();
        String devNo = rptBody.getDevNo();
        //初始化设置响应
        devParaList.forEach(para -> DevLogInfoContainer.initParaRespStatus(devNo, para.getParaNo()));
        devParaList = devParaList.stream().filter(para -> {
            String paraNo = para.getParaNo();
            FrameParaInfo targetParaInfo = BaseInfoContainer.getParaInfoByNo(para.getDevType(), paraNo);
            String accessRight = targetParaInfo.getNdpaAccessRight();
            //若为只读参数则不可写
            boolean canBeWrite = SysConfigConstant.READ_WRITE.equals(accessRight) || SysConfigConstant.ONLY_WRITE.equals(accessRight);
            if (!canBeWrite) {
                DevLogInfoContainer.setParaRespIllegalStatus(devNo, paraNo);
            }
            return canBeWrite;
        }).collect(Collectors.toList());
        return devParaList;
    }

    /**
     * 站控 参数查询
     *
     * @param headDev 参数查询结构体
     * @return 参数查询响应结果
     */
    private RptHeadDev doQueryNewCache(RptHeadDev headDev) {
        List<RptBodyDev> rptBodyDev = (List<RptBodyDev>) headDev.getParam();
        rptBodyDev.forEach(rptBody -> {
            //获取指定设备当前可读的参数列表
            List<ParaViewInfo> devParaViewList = DevParaInfoContainer.getDevParaViewList(rptBody.getDevNo()).stream()
                    .filter(paraView -> !SysConfigConstant.ONLY_WRITE.equals(paraView.getAccessRight())).collect(Collectors.toList());
            //当前设备的查询响应参数列表
            List<FrameParaData> resFrameParaList = new ArrayList<>();
            for (FrameParaData para : rptBody.getDevParaList()) {
                String paraNo = para.getParaNo();
                //参数编号为0,查询所有
                if (ALL_PARAS_QUERY.equals(paraNo)) {
                    devParaViewList.forEach(paraView -> {
                        FrameParaData frameParaData = frameParaDataWrapper(paraView);
                        resFrameParaList.add(frameParaData);
                    });
                    break;
                }
                //获取单个参数信息
                devParaViewList.stream()
                        .filter(paraView -> paraNo.equals(paraView.getParaNo())).findFirst()
                        .ifPresent(paraView -> {
                            FrameParaData frameParaData = frameParaDataWrapper(paraView);
                            resFrameParaList.add(frameParaData);
                        });
            }
            rptBody.setDevParaList(resFrameParaList);
        });
        headDev.setParam(rptBodyDev);
        headDev.setCmdMarkHexStr(StationCtlRequestEnums.PARA_QUERY_RESPONSE.getCmdCode());
        return headDev;
    }

    private FrameParaData frameParaDataWrapper(ParaViewInfo paraView) {
        FrameParaData.FrameParaDataBuilder frameParaDataBuilder = FrameParaData.builder()
                .paraNo(paraView.getParaNo())
                .paraVal(paraView.getParaVal())
                .devType(paraView.getDevType())
                .devNo(paraView.getDevNo());
        if (StrUtil.isNotBlank(paraView.getParaStrLen())){
            frameParaDataBuilder.len(Integer.parseInt(paraView.getParaStrLen()));
        }
        return frameParaDataBuilder.build();
    }

    private FrameParaData frameParaDataWrapper(AlertInfo alertInfo) {
        return FrameParaData.builder()
                .paraNo(alertInfo.getNdpaNo())
                .devType(alertInfo.getDevType())
                .devNo(alertInfo.getDevNo())
                .build();
    }


    /**
     * 站控 参数告警查询
     *
     * @param headDev 参数查询结构体
     * @return 参数查询响应结果
     */
    private RptHeadDev doParaWarnQueryAction(RptHeadDev headDev) {
        List<RptBodyDev> rptBodyDev = (List<RptBodyDev>) headDev.getParam();
        rptBodyDev.forEach(rptBody -> {
            String devNo = rptBody.getDevNo();
            //获取指定设备当前可读的参数列表
            List<AlertInfo> alertInfoList = DevAlertInfoContainer.getDevAlertInfoList(devNo);
            //当前设备的查询响应参数列表
            List<FrameParaData> resFrameParaList = new ArrayList<>();
            //当前设备的查询响应参数列表
            for (FrameParaData para : rptBody.getDevParaList()) {
                String paraNo = para.getParaNo();
                //参数编号为0,查询所有
                if (ALL_PARAS_QUERY.equals(paraNo)) {
                    alertInfoList.forEach(alertInfo -> {
                        FrameParaData frameParaData = frameParaDataWrapper(alertInfo);
                        resFrameParaList.add(frameParaData);
                    });
                    break;
                }
                //获取单个参数信息
                alertInfoList.stream()
                        .filter(alertInfo -> paraNo.equals(alertInfo.getNdpaNo())).findFirst()
                        .ifPresent(alertInfo -> {
                            FrameParaData frameParaData = frameParaDataWrapper(alertInfo);
                            resFrameParaList.add(frameParaData);
                        });
            }
            rptBody.setDevParaList(resFrameParaList);
        });
        headDev.setParam(rptBodyDev);
        return headDev;
    }

}
