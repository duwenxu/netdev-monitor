package com.xy.netdev.frame.service.bpq;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.xy.common.exception.BaseException;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.transit.IDataReciveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 39所Ku&L下变频器接口协议解析
 *
 * @author luo
 * @date 2021-03-05
 */
@Component
@Slf4j
public class BpqInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {

    @Autowired
    SocketMutualService socketMutualService;
    @Autowired
    IDataReciveService dataReciveService;
    @Autowired
    BpqPrtcServiceImpl bpqPrtcService;

    public final static String WORK_STATUS_CMD = "RAS";

    /**
     * 查询设备接口
     * @param  reqInfo    请求参数信息
     */
    @Override
    public void queryPara(FrameReqData reqInfo) {
        StringBuilder sb = new StringBuilder();
        String localAddr = bpqPrtcService.getDevLocalAddr(reqInfo);
        sb.append(BpqPrtcServiceImpl.SEND_START_MARK).append(localAddr).append("/")
                .append(reqInfo.getCmdMark());
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
        int startIdx = respStr.indexOf("_");
        int endIdx = respStr.indexOf(StrUtil.LF);
        String[] params = null;
        try{
            String str = respStr.substring(startIdx+2,endIdx);
            params = str.split(StrUtil.CR);
        }catch (Exception e){
            log.error("接口响应数据异常！源数据：{}",respStr);
            throw new BaseException("接口响应数据异常！");
            
        }
        List<FrameParaData> frameParaList = new ArrayList<>();
        for (String param : params) {
            String cmdMark = convertCmdMark(param.split("_")[0],respData.getCmdMark());
            String value = param.split("_")[1];
            FrameParaData paraInfo = new FrameParaData();
            FrameParaInfo frameParaDetail = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(),cmdMark);
            BeanUtil.copyProperties(frameParaDetail, paraInfo, true);
            paraInfo.setParaVal(value);
            paraInfo.setDevNo(respData.getDevNo());
            frameParaList.add(paraInfo);
        }
        respData.setFrameParaList(frameParaList);
        dataReciveService.paraQueryRecive(respData);
        return respData;
    }

    private String convertCmdMark(String cmdMark,String intfCmdMark){
        if(intfCmdMark.toUpperCase().equals(WORK_STATUS_CMD)){
            cmdMark = cmdMark+="-S";
        }
        return cmdMark;
    }

}
