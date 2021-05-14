package com.xy.netdev.transit.impl;

import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.fastjson.JSONArray;
import com.xy.common.exception.BaseException;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.*;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.monitor.bo.DevStatusInfo;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.bo.ParaViewInfo;
import com.xy.netdev.monitor.bo.TransRule;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.rpt.bo.RptBodyDev;
import com.xy.netdev.rpt.bo.RptHeadDev;
import com.xy.netdev.rpt.enums.AchieveClassNameEnum;
import com.xy.netdev.rpt.enums.StationCtlRequestEnums;
import com.xy.netdev.rpt.service.IDevStatusReportService;
import com.xy.netdev.rpt.service.StationControlHandler;
import com.xy.netdev.rpt.service.impl.DevStatusReportService;
import com.xy.netdev.rpt.service.impl.IDownRptPrtclAnalysisServiceImpl;
import com.xy.netdev.transit.IDataReciveService;
import com.xy.netdev.websocket.send.DevIfeMegSend;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static com.xy.netdev.common.constant.SysConfigConstant.IS_DEFAULT_TRUE;
import static com.xy.netdev.monitor.constant.MonitorConstants.SUB_MODEM;


/**
 * <p>
 * [transit]中转层收[frame]协议解析层实现
 * </p>
 *
 * @author tangxl
 * @since 2021-03-11
 */
@Service
@Slf4j
public class DataReciveServiceImpl implements IDataReciveService {

    @Autowired
    IDevStatusReportService devStatusReportService;

    @Autowired
    private DevStatusReportService statusReportService;

    @Autowired
    private IDownRptPrtclAnalysisServiceImpl downRptPrtclAnalysisService;

    @Autowired
    private StationControlHandler stationControlHandler;

    private ExecutorService rptDevExecutor= ThreadUtil.newExecutor(4,4,10);

    /**
     * 参数查询接收
     * @param  respData   协议解析响应数据
     */
    public void paraQueryRecive(FrameRespData respData) {
        //respData.setAccessType(SysConfigConstant.ACCESS_TYPE_PARAM);
        respData.setOperType(SysConfigConstant.OPREATE_QUERY_RESP);
        if(DevParaInfoContainer.handlerRespDevPara(respData)){
            DevIfeMegSend.sendParaToDev(respData.getDevNo());//如果设备参数变化,websocet推前台
            sendCtrlInter(respData);
            //站控主动上报
            rptDevExecutor.submit(()->stationRptParamsByDev(respData));
        }
        DevLogInfoContainer.handlerRespDevPara(respData);//记录日志
        DevIfeMegSend.sendLogToDev(respData.getDevNo());//操作日志websocet推前台
        handlerAlertInfo(respData);//处理报警信息
    }

    /**
     * 主动上报推送
     * @param respData 上报数据
     */
    private void stationRptParamsByDev(FrameRespData respData) {
        RptHeadDev headDev = rptParamsByDev(respData);
        stationControlHandler.queryParaResponse(headDev);
    }

    /**
     * 当前设备数据变化时   主动上报当前设备参数
     * @param respData  当前变化的设备参数响应帧
     */
    private RptHeadDev rptParamsByDev(FrameRespData respData) {
        String devNo = respData.getDevNo();
        //创建设备基础状态参数
        DevStatusInfo devStatusInfo = statusReportService.createDevStatusInfo(devNo);
        RptHeadDev headDev = initDevHead(devStatusInfo);
        RptBodyDev rptBodyDev = new RptBodyDev();
        //获取指定设备当前可读且可以对外上报的参数列表
        List<ParaViewInfo> devParaViewList = DevParaInfoContainer.getDevParaViewList(devStatusInfo.getDevNo()).stream()
                .filter(paraView -> !SysConfigConstant.ONLY_WRITE.equals(paraView.getAccessRight()) && IS_DEFAULT_TRUE.equals(paraView.getNdpaOutterStatus()))
                .collect(Collectors.toList());

        //当前设备的查询响应参数列表
        List<FrameParaData> resFrameParaList = new ArrayList<>();
        //获取单个参数信息
        devParaViewList.forEach(paraView -> {
            FrameParaData frameParaData = downRptPrtclAnalysisService.frameParaDataWrapper(paraView);
            resFrameParaList.add(frameParaData);
        });
        rptBodyDev.setDevNo(devNo);
        rptBodyDev.setDevTypeCode(respData.getDevType());
        rptBodyDev.setDevParaTotal(devParaViewList.size()+"");
//        rptBodyDev.setDevParamLen();
        rptBodyDev.setDevParaList(resFrameParaList);
        headDev.setParam(rptBodyDev);
        return headDev;
    }

    /**
     * 设置初始化站控头参数
     * @return  站控头参数
     */
    private RptHeadDev initDevHead(DevStatusInfo devStatusInfo) {
        RptHeadDev headDev = new RptHeadDev();
        headDev.setCmdMarkHexStr(StationCtlRequestEnums.DEV_AUTO_REPORT.getCmdCode());
        headDev.setDevNo(devStatusInfo.getDevNo());
        headDev.setDevNum(1);
        headDev.setStationNo(devStatusInfo.getStationId());
        return headDev;
    }

    /**
     * 参数控制接收
     * @param  respData   协议解析响应数据
     */
    public void paraCtrRecive(FrameRespData respData) {
        respData.setAccessType(SysConfigConstant.ACCESS_TYPE_PARAM);
        respData.setOperType(SysConfigConstant.OPREATE_CONTROL_RESP);
        if(DevParaInfoContainer.handlerRespDevPara(respData)){
            DevIfeMegSend.sendParaToDev(respData.getDevNo());//如果设备参数变化,websocet推前台
            sendCtrlInter(respData);
            //站控主动上报
            rptDevExecutor.submit(()->stationRptParamsByDev(respData));
        }
        DevLogInfoContainer.handlerRespDevPara(respData);//记录日志
        DevIfeMegSend.sendLogToDev(respData.getDevNo());//操作日志websocet推前台
        handlerAlertInfo(respData);//处理报警信息
    }

    /**
     * 接口查询接收
     * @param  respData   协议解析响应数据
     */
    public void interfaceQueryRecive(FrameRespData respData) {
        respData.setAccessType(SysConfigConstant.ACCESS_TYPE_INTERF);
        respData.setOperType(SysConfigConstant.OPREATE_QUERY_RESP);
        if(!StringUtils.isEmpty(respData.getPageQueryJsonStr())){
            if(PageInfoContainer.addPageInfo(respData.getDevNo(),respData.getCmdMark(),respData.getPageQueryJsonStr())){
                DevIfeMegSend.sendPageInfoToDev(respData.getDevNo(),respData.getCmdMark());//如果页面查询接口数据发送变化,推送前台数据
            }
        }else{
            if(DevParaInfoContainer.handlerRespDevPara(respData)){
                DevIfeMegSend.sendParaToDev(respData.getDevNo());//如果设备参数变化,websocet推前台
                sendCtrlInter(respData);
                //站控主动上报
                rptDevExecutor.submit(()->stationRptParamsByDev(respData));
            }
        }
        DevLogInfoContainer.handlerRespDevPara(respData);//记录日志
        DevIfeMegSend.sendLogToDev(respData.getDevNo());//操作日志websocet推前台
        handlerAlertInfo(respData);//处理报警信息
    }

    /**
     * 接口控制接收
     * @param  respData   协议解析响应数据
     */
    public void interfaceCtrlRecive(FrameRespData respData) {
        respData.setAccessType(SysConfigConstant.ACCESS_TYPE_INTERF);
        respData.setOperType(SysConfigConstant.OPREATE_CONTROL_RESP);
        if(DevParaInfoContainer.handlerRespDevPara(respData)){
            DevIfeMegSend.sendParaToDev(respData.getDevNo());//如果设备参数变化,websocet推前台
            sendCtrlInter(respData);
            //站控主动上报
            rptDevExecutor.submit(()->stationRptParamsByDev(respData));
        }
        DevLogInfoContainer.handlerRespDevPara(respData);//记录日志
        DevIfeMegSend.sendLogToDev(respData.getDevNo());//操作日志websocet推前台
        handlerAlertInfo(respData);//处理报警信息
    }
    /**
     * webSokcet推送接口控制数据
     * @param  respData   协议解析响应数据
     */
    private  void sendCtrlInter(FrameRespData respData){
        if(!BaseInfoContainer.getCtrlItfInfo(respData.getDevNo()).isEmpty()){
            DevIfeMegSend.sendDevCtrlItfInfosToDev(respData.getDevNo());
        }
    }

    /**
     * 处理报警、主备信息
     * @param  respData   协议解析响应数据
     */
    private void handlerAlertInfo(FrameRespData respData){
        List<FrameParaData> params = respData.getFrameParaList();
        if (params == null || params.isEmpty()){
            log.warn("处理报警信息失败, 参数列表为空, 数据体:{}", JSONArray.toJSONString(respData));
            return;
        }
        //处理调制解调器主备使用信息
        if (respData.getDevType().equals(SysConfigConstant.DEVICE_TRANS_SWITCH)){
            handlerMasterOfModem(respData);
            DevIfeMegSend.sendDevStatusToDev();
            return;
        }
        params.forEach(param->{
            FrameParaInfo paraInfo = BaseInfoContainer.getParaInfoByNo(param.getDevType(),param.getParaNo());
            if(!paraInfo.getAlertPara().equals(SysConfigConstant.PARA_ALERT_TYPE_NULL)){
                String ruleStr =  paraInfo.getTransRule();
                List<TransRule> rules;
                //读取参数配置的状态转换规则
                try {
                    rules = JSONArray.parseArray(ruleStr, TransRule.class);
                }catch(Exception e) {
                    throw new BaseException("参数状态转换规则有误");
                }
                if(rules!=null){
                    devStatusReportService.reportWarningAndStaus(rules,param);
                }
            }

        });
    }

    private static final Map<String,List<String>> modemPair = new ConcurrentHashMap<>();
    static {
        //初始化转换开关和调制解调器的对应关系
        modemPair.put("30",Arrays.asList("11","12"));
        modemPair.put("31",Arrays.asList("13","14"));
    }

    private static void handlerMasterOfModem(FrameRespData respData) {
        List<FrameParaData> paraList = respData.getFrameParaList();
        paraList.forEach(param-> {
            FrameParaInfo paraInfo = BaseInfoContainer.getParaInfoByNo(param.getDevType(), param.getParaNo());
            String alertField = paraInfo.getAlertPara();
            if (!alertField.equals(SysConfigConstant.PARA_ALERT_TYPE_NULL)){
                String ruleStr =  paraInfo.getTransRule();
                List<TransRule> rules;
                //读取参数配置的状态转换规则
                try {
                    rules = JSONArray.parseArray(ruleStr, TransRule.class);
                }catch(Exception e) {
                    throw new BaseException("参数状态转换规则有误");
                }
                if (rules!=null){
                    rules.forEach(rule-> {
                        if(rule.getInner().equals(param.getParaVal())) {
                            String outerStatus = rule.getOuter();
                            List<BaseInfo> modemBaseInfo = modemPair.get(respData.getDevNo())
                                    .stream().map(BaseInfoContainer::getDevInfoByNo).collect(Collectors.toList());
                            if (outerStatus.equals("0")){
                                synchronized (param){
                                    DevStatusContainer.setModemUse(modemBaseInfo.get(0).getDevNo(),"0");
                                    DevStatusContainer.setModemUse(modemBaseInfo.get(1).getDevNo(),"1");
                                }
                            }else {
                                synchronized (param){
                                    DevStatusContainer.setModemUse(modemBaseInfo.get(0).getDevNo(),"1");
                                    DevStatusContainer.setModemUse(modemBaseInfo.get(1).getDevNo(),"0");
                                }
                            }
                        }
                    });
                }
            }
        });
    }
}
