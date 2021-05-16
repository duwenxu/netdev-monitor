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
import com.xy.netdev.monitor.service.IParaInfoService;
import com.xy.netdev.rpt.bo.RptBodyDev;
import com.xy.netdev.rpt.bo.RptHeadDev;
import com.xy.netdev.rpt.enums.StationCtlRequestEnums;
import com.xy.netdev.rpt.service.IDownRptPrtclAnalysisService;
import com.xy.netdev.transit.impl.DevCmdSendService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static com.xy.netdev.common.constant.SysConfigConstant.IS_DEFAULT_TRUE;
import static com.xy.netdev.common.constant.SysConfigConstant.PARA_SHOW_MODEL;

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
    /**上报  外发查询响应转换标识*/
    private static final int IN_TO_OUT = 0;
    /**上报  内发设置响应转换标识*/
    private static final int OUT_TO_IN = 1;

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
     *
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
        //过滤参数长度不为空的设置参数
        List<RptBodyDev> realRptBody = new CopyOnWriteArrayList<>();
        for (RptBodyDev bodyDev : rptBodyDev) {
            List<FrameParaData> currentList = bodyDev.getDevParaList().stream().filter(param -> param.getLen() != null && param.getLen() != 0).collect(Collectors.toList());
            bodyDev.setDevParaList(currentList);
            realRptBody.add(bodyDev);
        }
        //阻塞式的进行参数设置调用
        realRptBody.forEach(rptBody -> {
            //请求间隔
            Integer intervalTime = BaseInfoContainer.getDevInfoByNo(rptBody.getDevNo()).getDevIntervalTime();
            //参数设置请求初始化
            List<FrameParaData> canBeWriteFrameDataList = initParaSetAction(rptBody);
            for (FrameParaData paraData : canBeWriteFrameDataList) {
                try {
                    Thread.sleep(intervalTime);
                } catch (InterruptedException e) {
                    log.error("线程+{}+休眠发生异常！", Thread.currentThread().getName());
                }
                String cmkMark = BaseInfoContainer.getParaInfoByNo(paraData.getDevType(), paraData.getParaNo()).getCmdMark();
                ParaViewInfo devParaView = DevParaInfoContainer.getDevParaView(paraData.getDevNo(), paraData.getParaNo());
                devParaView.setParaVal(paraData.getParaVal());
                //外部设置值转换
                String paramVal = transParamVal(devParaView, OUT_TO_IN);
                //参数控制
                devCmdSendService.paraCtrSend(rptBody.getDevNo(), cmkMark, paramVal);
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
        //初始化设置响应
        devParaList.forEach(para -> DevLogInfoContainer.initParaRespStatus(para.getDevNo(), para.getParaNo()));
        devParaList = devParaList.stream().filter(para -> {
            String paraNo = para.getParaNo();
            FrameParaInfo targetParaInfo = BaseInfoContainer.getParaInfoByNo(para.getDevType(), paraNo);
            String accessRight = targetParaInfo.getNdpaAccessRight();
            //若为只读参数则不可写
            boolean canBeWrite = SysConfigConstant.READ_WRITE.equals(accessRight) || SysConfigConstant.ONLY_WRITE.equals(accessRight);
            if (!canBeWrite) {
                DevLogInfoContainer.setParaRespIllegalStatus(para.getDevNo(), paraNo);
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
            //获取指定设备当前可读且可以对外上报的参数列表
            List<ParaViewInfo> devParaViewList = DevParaInfoContainer.getDevParaViewList(rptBody.getDevNo()).stream()
                    .filter(paraView -> !SysConfigConstant.ONLY_WRITE.equals(paraView.getAccessRight()) && IS_DEFAULT_TRUE.equals(paraView.getNdpaOutterStatus()))
                    .collect(Collectors.toList());
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

    public FrameParaData frameParaDataWrapper(ParaViewInfo paraView) {
        String paramVal = transParamVal(paraView,IN_TO_OUT);
        FrameParaData.FrameParaDataBuilder frameParaDataBuilder = FrameParaData.builder()
                .paraNo(paraView.getParaNo())
                .paraVal(paramVal)
                .devType(paraView.getDevType())
                .devNo(paraView.getDevNo());
        if (StrUtil.isNotBlank(paraView.getParaByteLen())){
            frameParaDataBuilder.len(Integer.parseInt(paraView.getParaByteLen()));
        }
        return frameParaDataBuilder.build();
    }

    /**
     * 对下拉框显示且需要进行转换的参数进行转换(0:内转外 1:外转内)
     *
     * @param paraView 参数实体类
     * @return 转换后的参数值
     */
    private String transParamVal(ParaViewInfo paraView,int sign) {
        FrameParaInfo infoByNo = BaseInfoContainer.getParaInfoByNo(paraView.getDevType(), paraView.getParaNo());
        String paramVal = paraView.getParaVal();
        String showMode = paraView.getParahowMode();
        String transRule = infoByNo.getTransRule();
        /**参数为下拉框 且 存在转换规则*/
        if (PARA_SHOW_MODEL.equals(showMode) && StringUtils.isNoneBlank(transRule)) {
            if (sign == IN_TO_OUT){
                Map<String, String> intoOutMap = infoByNo.getTransIntoOutMap();
                if (intoOutMap.containsKey(paramVal)){
                    paramVal = intoOutMap.get(paramVal);
                }
            }else {
                Map<String, String> outInMap = infoByNo.getTransOuttoInMap();
                if (outInMap.containsKey(paramVal)){
                    paramVal = outInMap.get(paramVal);
                }
            }
        }
        return paramVal;
    }

    /**
     * 站控 参数告警查询
     *
     * @param headDev 参数查询结构体
     * @return 参数查询响应结果
     */
    private RptHeadDev doParaWarnQueryAction(RptHeadDev headDev) {
        List<RptBodyDev> rptBodyDev = (List<RptBodyDev>) headDev.getParam();
        List<AlertInfo> alertInfoList = new ArrayList<>();
        headDev.setCmdMarkHexStr(StationCtlRequestEnums.PARA_WARNING_QUERY_RESP.getCmdCode());
        rptBodyDev.forEach(rptBody -> {
            //获取指定设备的报警信息
            List<AlertInfo> alertInfoLists = DevAlertInfoContainer.getDevAlertInfoList(rptBody.getDevNo());
            alertInfoList.addAll(alertInfoLists);
        });
        headDev.setParam(alertInfoList);
        return headDev;
    }

}
