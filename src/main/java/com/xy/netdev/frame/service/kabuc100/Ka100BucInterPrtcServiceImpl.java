package com.xy.netdev.frame.service.kabuc100;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.xy.common.exception.BaseException;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReciveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.xy.netdev.common.constant.SysConfigConstant.PARA_COMPLEX_LEVEL_COMPOSE;

import java.util.*;

/**
 * Ka100WBUC功放
 *
 * @author luo
 * @date 2021-03-05
 */
@Component
@Slf4j
public class Ka100BucInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {

    @Autowired
    SocketMutualService socketMutualService;
    @Autowired
    IDataReciveService dataReciveService;

//    public final static String WORK_STATUS_CMD = "STATUS";

    /**
     * 查询设备接口
     * @param  reqInfo    请求参数信息
     */
    @Override
    public void queryPara(FrameReqData reqInfo) {
        StringBuilder sb = new StringBuilder();
        String localAddr = "001";
        sb.append(Ka100BucPrtcServiceImpl.SEND_START_MARK).append(localAddr).append("/")
                .append(reqInfo.getCmdMark())
                .append("_?")
                .append(StrUtil.CRLF);
        String command = sb.toString();
        reqInfo.setParamBytes(command.getBytes());
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    /**
     * 查询设备接口响应
     * @param  respData   协议解析响应数据
     * @return
     */
    public FrameRespData queryParaResponse(FrameRespData respData) {
        String respStr = new String(respData.getParamBytes());
        String addr = respStr.substring(1,4);
//        respData.setDevNo(getDevNo(addr));
        int startIdx = respStr.indexOf("_");
        int endIdx = respStr.indexOf(StrUtil.LF);
        if(endIdx==-1){
            endIdx = respStr.length();
        }
        String[] params = null;
        try{
            String str = respStr.substring(startIdx+4,endIdx);
            params = str.split(",");
        }catch (Exception e){
            log.error("接口响应数据异常！源数据：{}",respStr);
            throw new BaseException("接口响应数据异常！");
        }

        List<FrameParaData> frameParaList = new ArrayList<>();
        for (String param : params) {
            String cmdMark = param.split("_")[0];
            String value = "";
            String[] values = param.split("_");
            if(values.length>1){
                value = values[1];
            }
            if (cmdMark.equals("MUTE") || cmdMark.equals("UNMUTE")){
                value = cmdMark;
                cmdMark = "MU";
            }
            FrameParaData paraData= new FrameParaData();
            FrameParaInfo frameParaDetail = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(),cmdMark);
            BeanUtil.copyProperties(frameParaDetail, paraData, true);

            if(cmdMark.equals("FA") && value.length()>1){
                value = value.substring(0,value.length()-2);
                if(PARA_COMPLEX_LEVEL_COMPOSE.equals(frameParaDetail.getCmplexLevel())){
                    List<FrameParaInfo> subList  = frameParaDetail.getSubParaList();
                    subList.sort(Comparator.comparing(frameParaInfo1 -> Integer.valueOf(frameParaInfo1.getParaNo())));
                    for (int i = 0; i < subList.size(); i++) {
                        FrameParaData frameParaData = genFramepara(subList.get(i),value.charAt(i) + "",respData);
                        frameParaList.add(frameParaData);
                    }
                }
            }
            paraData.setParaVal(value);
            frameParaList.add(paraData);
        }
        respData.setFrameParaList(frameParaList);
        dataReciveService.interfaceQueryRecive(respData);
        return respData;
    }

    private  FrameParaData genFramepara(FrameParaInfo currentpara,String paraValueStr,FrameRespData respData){
        FrameParaData frameParaData = FrameParaData.builder()
                .devType(currentpara.getDevType())
                .paraNo(currentpara.getParaNo())
                .devNo(respData.getDevNo())
                .build();
        frameParaData.setParaVal(paraValueStr);
        return  frameParaData;
    }




}
