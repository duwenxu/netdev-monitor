package com.xy.netdev.frame.service.acu;


import cn.hutool.core.util.StrUtil;
import com.xy.common.exception.BaseException;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.transit.IDataReciveService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;


/**
 * ACU卫通天线接口协议解析
 *
 * @author luo
 * @date 2021-03-05
 */
@Component
public class AcuPrtcServiceImpl implements IParaPrtclAnalysisService {

    @Autowired
    SocketMutualService socketMutualService;

    @Autowired
    IDataReciveService dataReciveService;


    @Override
    public void queryPara(FrameReqData reqInfo) {

    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        return null;
    }

    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        FrameParaData frameParaData = reqInfo.getFrameParaList().get(0);
        String paraVal = frameParaData.getParaVal().replaceAll(" ","");
        //当参数为位置模式时特殊处理
        if(frameParaData.getParaNo().equals("3")){
            String x = make0Str(paraVal.split("]")[0].substring(4),3,2);
            String y = make0Str(paraVal.split("]")[1].substring(4),3,2);
            String z = make0Str(paraVal.split("]")[2].substring(4),3,2);
            paraVal = "{X}["+x+"]{Y}["+y+"]{Z}["+z+"]";
            //如果为位置模式，则判断当前俯仰角大于20度时才执行
            if(Float.valueOf(DevParaInfoContainer.getDevParaView(frameParaData.getDevNo(),"12").getParaVal())<20){
                throw new BaseException("俯仰角小于20°不能执行位置模式命令！");
            }
        }else if(frameParaData.getParaNo().equals("6")){
            //当参数为接收机本振频率
            paraVal = paraVal.split("]")[0]+"]{F}["+make0Str(paraVal.split("]")[1].substring(4),5,2)+"]";
        }
        else if(reqInfo.getFrameParaList().get(0).getLen() != null){
            //当长度不为0时补0
            paraVal = make0Str(paraVal,frameParaData.getLen(),1);
        }
        String replace = paraVal.replace("{", "")
                .replace("}","")
                .replace("[","")
                .replace("]","");
        String command = "<" + reqInfo.getCmdMark() + replace + ">";
        reqInfo.setParamBytes(StrUtil.bytes(command));
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        String str = StrUtil.str(respData.getParamBytes(), Charset.defaultCharset());
        respData.setReciveOriginalData(str);
        dataReciveService.paraCtrRecive(respData);
        return respData;
    }

    /**
     * 数值型字符串补0
     * @return
     */
    private String make0Str(String value,int len, int smallLen){
        if(StringUtils.isNotBlank(value)){
            if(value.contains(".")){
                //小数补0
                String[] valueList = value.split("\\.");
                String small = StringUtils.rightPad(valueList[1],2,"0");
                if(small.length()>smallLen){
                    small = small.substring(0,smallLen);
                }
                value = StringUtils.leftPad(valueList[0],len,"0")+"."+small;
            }else{
                //整数补小数及0
                value = StringUtils.leftPad(value,len,"0")+"."+StringUtils.leftPad("",smallLen,"0");
            }
        }
        return value;
    }
}
