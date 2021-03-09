package com.xy.netdev.frame.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.ParaInfo;
import com.xy.netdev.monitor.service.IBaseInfoService;
import com.xy.netdev.monitor.service.IParaInfoService;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 39所Ku&L下变频器参数协议解析
 *
 * @author admin
 * @date 2021-03-05
 */
public class FreqConverterPrtcParser implements IParaPrtclAnalysisService {


    /**用户命令起始标记*/
    private final static String SEND_START_MARK = "<";
    /**设备响应开始标记*/
    private final static String RESP_START_MARK = ">";
    /**用户命令结尾标记*/
    private final static String SEND_END_MARK = "'cr'";
    /**设备响应结尾标记*/
    private final static String RESP_END_MARK = "'cr''lf']";

    @Autowired
    IParaInfoService paraInfoService;
    @Autowired
    IBaseInfoService baseInfoService;


    @Override
    public void queryPara(BaseInfo devInfo, ParaInfo paraInfo) {
        devInfo = getDevInfoDetail(devInfo);
        paraInfo = getParaInfoDetail(devInfo,paraInfo);
        StringBuilder sb = new StringBuilder();
        sb.append(SEND_START_MARK).append(devInfo.getDevIpAddr()).append("/")
                .append(paraInfo.getNdpaCode()).append("_").append(SEND_END_MARK);
        String command = sb.toString();
    }

    @Override
    public ParaInfo queryParaResponse(BaseInfo devInfo, ParaInfo paraInfo) {
        devInfo = getDevInfoDetail(devInfo);
        paraInfo = getParaInfoDetail(devInfo,paraInfo);
        StringBuilder sb = new StringBuilder();
        sb.append(RESP_START_MARK).append(devInfo.getDevIpAddr()).append("/")
                .append(paraInfo.getNdpaCode()).append("_").append(RESP_END_MARK);
        String command = sb.toString();
        return null;
    }

    @Override
    public void ctrlPara(BaseInfo devInfo, ParaInfo paraInfo) {
        devInfo = getDevInfoDetail(devInfo);
        paraInfo = getParaInfoDetail(devInfo,paraInfo);
        StringBuilder sb = new StringBuilder();
        sb.append(SEND_START_MARK).append(devInfo.getDevIpAddr()).append("/").append(paraInfo.getNdpaCode())
                .append("_").append(paraInfo.getParaVal()).append(SEND_END_MARK);
        String command = sb.toString();
    }

    @Override
    public ParaInfo ctrlParaResponse(BaseInfo devInfo, ParaInfo paraInfo) {
        devInfo = getDevInfoDetail(devInfo);
        paraInfo = getParaInfoDetail(devInfo,paraInfo);
        StringBuilder sb = new StringBuilder();
        sb.append(RESP_START_MARK).append(devInfo.getDevIpAddr()).append("/").append(paraInfo.getNdpaCode())
                .append("_").append(paraInfo.getParaVal()).append(RESP_END_MARK);
        String command = sb.toString();
        return null;
    }

    /**
     * 获取参数详细信息
     * @param devInfo
     * @param paraInfo
     * @return
     */
    private ParaInfo getParaInfoDetail(BaseInfo devInfo, ParaInfo paraInfo){
        String devType = devInfo.getDevType();
        Integer paraId = paraInfo.getNdpaId();
        QueryWrapper<ParaInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ParaInfo::getDevType,devType);
        queryWrapper.lambda().eq(ParaInfo::getNdpaId,paraId);
        return paraInfoService.getOne(queryWrapper);
    }

    /**
     * 获取设备详细信息
     * @param devInfo
     * @return
     */
    private BaseInfo getDevInfoDetail(BaseInfo devInfo){
        String devType = devInfo.getDevType();
        QueryWrapper<BaseInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(BaseInfo::getDevType,devType);
        return baseInfoService.getOne(queryWrapper);
    }
}
