package com.xy.netdev.frame.service.msct;

import cn.hutool.core.util.HexUtil;
import com.xy.common.exception.BaseException;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.BeanFactoryUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.ParamCodec;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.constant.MonitorConstants;
import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.xy.netdev.frame.service.gf.GfPrtcServiceImpl.isFloat;

/**
 * @author luo
 * @date 2021/4/21
 */
@Service
public class MsctCtrlInterPrtcServiceImpl implements ICtrlInterPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private ISysParamService sysParamService;


    @Override
    public void ctrlPara(FrameReqData reqData) {
        List<byte[]> byteList = new ArrayList<>();
        Interface intf = BaseInfoContainer.getInterLinkInterface(reqData.getDevType(),reqData.getCmdMark());
        String[] format = intf.getItfDataFormat().split(",");
        for (String str : format) {
            Integer paraId = Integer.parseInt(str);
            for (FrameParaData frameParaData : reqData.getFrameParaList()) {
                FrameParaInfo param = BaseInfoContainer.getParaInfoByNo(reqData.getDevType(),frameParaData.getParaNo());
                if(paraId.equals(param.getParaId())){
                    String cmdMark = param.getCmdMark();
                    boolean isStr = false;
                    if(cmdMark.equals("AA")){
                         isStr = false;
                    }else{
                         isStr = MonitorConstants.STRING_CODE.equals(param.getDataType());
                    }
                    String value = frameParaData.getParaVal();
                    if (isStr) {
                        String configClass = param.getNdpaRemark2Data();
                        if(StringUtils.isNotBlank(configClass)) {
                            ParamCodec handler = BeanFactoryUtil.getBean(configClass);
                            byteList.add(handler.encode(value));
                        }else{
                            byteList.add(value.getBytes());
                        }
                    } else {
                        String desc1 = param.getNdpaRemark1Desc();
                        String data1 = param.getNdpaRemark1Data();
                        if(StringUtils.isNotEmpty(desc1) && desc1.equals("倍数") && StringUtils.isNotEmpty(data1)){
                            Integer multiple = Integer.parseInt(data1);
                            float temp = Float.parseFloat(value)*multiple;
                            value = String.valueOf(temp);
                        }
                        byteList.add(ByteUtils.objToBytes(value, frameParaData.getLen(), isFloat(sysParamService, param.getDataType())));
                    }
                }
            }
        }
        reqData.setParamBytes(ByteUtils.listToBytes(byteList));
        socketMutualService.request(reqData, ProtocolRequestEnum.CONTROL);
    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        String data = HexUtil.encodeHexStr(respData.getParamBytes());
        String controlSuccessCode = sysParamService.getParaRemark1(SysConfigConstant.CONTROL_SUCCESS);
        String controlFailCode = sysParamService.getParaRemark1(SysConfigConstant.CONTROL_FAIL);
        if (controlSuccessCode.equals(data)) {
            respData.setRespCode(controlSuccessCode);
        } else if (controlFailCode.equals(data)) {
            respData.setRespCode(controlFailCode);
        } else {
            throw new BaseException("多体制卫星信道终端参数设置响应异常，数据字节：" + data);
        }
        return respData;
    }




}
