package com.xy.netdev.frame.service.hdpm;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.service.IBaseInfoService;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReciveService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;


/**
 * 华达电源监控
 *
 * @author luo
 * @date 2021-03-05
 */
@Component
public class HdpmPrtcServiceImpl implements IParaPrtclAnalysisService {


    /**用户命令起始标记*/
    public final static String SEND_START_MARK = "<";
    /**设备响应开始标记*/
    public final static String RESP_START_MARK = ">";
    /**设备物理地址设置*/
    public final static String SET_ADDR_CMD = "SPA";
    /**设备物理广播地址*/
    public final static String BROADCAST_ADDR = "255";

    public final static String FORMAT = "#";


    @Autowired
    SocketMutualService socketMutualService;
    @Autowired
    IDataReciveService dataReciveService;
    @Autowired
    IBaseInfoService baseInfoService;


    /**
     * 查询设备参数
     * @param  reqInfo   请求参数信息
     */
    @Override
    public void queryPara(FrameReqData reqInfo) {
        StringBuilder sb = new StringBuilder();
        String localAddr = "255";
        sb.append(SEND_START_MARK).append(localAddr).append("/");
        String cmdMk = reqInfo.getCmdMark();
        sb.append(cmdMk).append("_?").append(StrUtil.CRLF);
        String command = sb.toString();
        reqInfo.setParamBytes(command.getBytes());
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    /**
     * 查询设备参数响应
     * @param  respData   数据传输对象
     * @return
     */
    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        String respStr = new String(respData.getParamBytes());
        respStr =  respStr.split(RESP_START_MARK)[1];
        int stIndex = respStr.indexOf("_");
        String cmdMk = respStr.substring(0,stIndex);
        int edIndex = respStr.indexOf(StrUtil.LF);
        String val = respStr.substring(stIndex+1,edIndex);
        List<FrameParaData> frameParas = new ArrayList<>();
        if(cmdMk.equals("LAA") || cmdMk.equals("LAAT")){
            val.replace("P1","电源1告警");
            val.replace("P2","电源2告警");
            val.replace("I1","设备1告警");
            val.replace("I2","设备2告警");
            val.replace("LK","内部通信告警");
        }
        FrameParaInfo frameParaInfo = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(),respData.getCmdMark());
        if(cmdMk.equals("FA")){
            List<FrameParaInfo> subParaList = frameParaInfo.getSubParaList();
            for (int i = 0; i <subParaList.size() ; i++) {
                subParaList.get(i).setParaVal(String.valueOf(val.charAt(1)));
            }
        }
        FrameParaData frameParaData = new FrameParaData();
        BeanUtil.copyProperties(frameParaInfo, frameParaData, true);
        frameParaData.setParaVal(val);
        frameParas.add(frameParaData);
        respData.setFrameParaList(frameParas);
        dataReciveService.paraQueryRecive(respData);
        return respData;
    }

    /**
     * 设置设备参数
     * @param  reqInfo   请求参数信息
     */
    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        StringBuilder sb = new StringBuilder();
        String localAddr = "001";
        sb.append(SEND_START_MARK).append(localAddr).append("/").append(reqInfo.getCmdMark())
                .append("_").append(reqInfo.getFrameParaList().get(0).getParaVal());
        String val = reqInfo.getFrameParaList().get(0).getParaVal();
        String cmdMk = reqInfo.getCmdMark();
        switch (cmdMk){
            case "REM_ON":
                sb.append(cmdMk).append(StrUtil.CRLF);
                break;
            case "REM_CUT":
                sb.append(cmdMk).append(StrUtil.CRLF);
                break;
            case "CH1":
                cmdMk = "CH_01:"+ val;
                sb.append(cmdMk).append(StrUtil.CRLF);
                break;
            case "CH2":
                cmdMk = "CH_02:"+ val;
                sb.append(cmdMk).append(StrUtil.CRLF);
                break;
            case "ACLR":
                sb.append(cmdMk).append(StrUtil.CRLF);
                break;
            default:
                sb.append(cmdMk).append("_").append(val)
                        .append(StrUtil.CRLF);
        }
        String command = sb.toString();
        reqInfo.setParamBytes(command.getBytes());
        String cmdMark = reqInfo.getCmdMark();
        if(cmdMark.equals(SET_ADDR_CMD)) {
            setDevLocalAddr(reqInfo);
        }
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    /**
     * 设置设备参数响应
     * @param  respData   数据传输对象
     * @return
     */
    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        String respStr = new String(respData.getParamBytes());
        respStr =  respStr.split(RESP_START_MARK)[1];
        int stIndex = respStr.indexOf("_");
        String cmdMk = respStr.substring(0,stIndex);
        int edIndex = respStr.indexOf(StrUtil.LF);
        String val = respStr.substring(stIndex+1,edIndex);
        List<FrameParaData> frameParas = new ArrayList<>();
        if(cmdMk.equals("CH")){
            String[] rst = val.split(":");
            if(rst[0].equals("01")){
                cmdMk = "CH1";
            }else{
                cmdMk = "CH2";
            }
            val = rst[1];
        }
        FrameParaInfo frameParaInfo = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(),respData.getCmdMark());
        FrameParaData frameParaData = new FrameParaData();
        BeanUtil.copyProperties(frameParaInfo, frameParaData, true);
        frameParaData.setParaVal(val);
        frameParas.add(frameParaData);
        respData.setFrameParaList(frameParas);
        dataReciveService.paraQueryRecive(respData);
        return respData;
    }


    /**
     * 设置设备物理地址
     * @param reqInfo
     */
    private void setDevLocalAddr(FrameReqData reqInfo){
        String devNo = reqInfo.getDevNo();
        BaseInfo baseInfo = new BaseInfo();
        baseInfo.setDevNo(devNo);
        baseInfo.setDevLocalAddr(reqInfo.getFrameParaList().get(0).getParaVal());
        baseInfoService.updateById(baseInfo);
        BaseInfoContainer.updateBaseInfo(devNo);
    }





}
