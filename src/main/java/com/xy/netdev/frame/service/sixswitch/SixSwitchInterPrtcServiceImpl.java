package com.xy.netdev.frame.service.sixswitch;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReceiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 华达电源监控
 *
 * @author luo
 * @date 2021-03-05
 */
@Component
@Slf4j
public class SixSwitchInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {

    @Autowired
    SocketMutualService socketMutualService;
    @Autowired
    IDataReceiveService dataReciveService;


    /**
     * 查询设备接口
     * @param  reqInfo    请求参数信息
     */
    @Override
    public void queryPara(FrameReqData reqInfo) {
        StringBuilder sb = new StringBuilder();
        String localAddr = "255";
        sb.append(SixSwitchPrtcServiceImpl.SEND_START_MARK).append(localAddr).append("/");
        String cmdMark = reqInfo.getCmdMark();
        if(!cmdMark.equals("ADDR")){
            sb.append(reqInfo.getCmdMark());
        }
         sb.append("_?").append(StrUtil.CRLF);
        String command = sb.toString();
        reqInfo.setParamBytes(command.getBytes());
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    /**
     * 查询设备接口响应
     * @param  respData   协议解析响应数据
     * @return
     */
    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        String respStr = new String(respData.getParamBytes());
        String addr = respStr.substring(1,4);
        int stIdx = respStr.indexOf("/");
        int edIdx = respStr.indexOf(StrUtil.LF);
        String dataStr = respStr.substring(stIdx+1,edIdx);
        String[] datas = dataStr.split(",");
        List<FrameParaData> frameParaList = new ArrayList<>();
        for (int i = 0; i < datas.length; i++) {
            if(i==0){
                String cmdMk1 = datas[i].split("_")[0];
                String val1 = datas[i].split("_")[1];
                FrameParaData paraInfo = new FrameParaData();
                FrameParaInfo frameParaDetail = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(),cmdMk1);
                BeanUtil.copyProperties(frameParaDetail, paraInfo, true);
                paraInfo.setParaVal(val1);
                frameParaList.add(paraInfo);
            }else{
                String[] devData = datas[i].split(":")[1].split("_");
                for (int j = 0; j < devData.length; j++) {
                    String cmdMk = "";

                    if(j==0){
                         cmdMk = "CH"+String.valueOf(i);
                    }
                    if(j==1){
                         cmdMk = "VOL"+String.valueOf(i);
                    }
                    if(j==2){
                        cmdMk = "ECU"+String.valueOf(i);
                    }
                    String val = devData[i];
                    FrameParaData paraInfo = new FrameParaData();
                    FrameParaInfo frameParaDetail = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(),cmdMk);
                    BeanUtil.copyProperties(frameParaDetail, paraInfo, true);
                    paraInfo.setParaVal(val);
                    frameParaList.add(paraInfo);
                }
            }
        }
        respData.setFrameParaList(frameParaList);
        dataReciveService.paraQueryRecive(respData);
        return respData;
    }

}