package com.xy.netdev.frame.service.msct;

import cn.hutool.core.util.HexUtil;
import com.xy.common.exception.BaseException;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.BeanFactoryUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.ParamCodec;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.bo.ParaViewInfo;
import com.xy.netdev.monitor.constant.MonitorConstants;
import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDevCmdSendService;
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
    @Autowired
    private IDevCmdSendService devCmdSendService;
    //当前工作模式设置接口
    public static final String SET_MODE_CMD = "8005AA";
    //当前工作模式查询接口
    public static final String CURRENT_MODE_CMD = "8205AA";
    //当前工作模式，默认为TDMA
    public static String currMode = "00";


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
                         currMode = reqData.getFrameParaList().get(0).getParaVal();
                    }else{
                         isStr = MonitorConstants.STRING_CODE.equals(param.getDataType()) || MonitorConstants.IP_ADDRESS.equals(param.getDataType());
                    }
                    String value = handleParaVal(cmdMark,frameParaData.getParaVal());
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
                            Double temp = Double.parseDouble(value)*multiple;
                            if(isFloat(sysParamService, param.getDataType())){
                                value = String.valueOf(temp);
                            }else{
                                if(temp>Integer.MAX_VALUE){
                                    value = String.valueOf(temp.longValue());
                                }else{
                                    value = String.valueOf(temp.intValue());
                                }
                            }
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
        //如果我当前模式设置，需要刷新设置后的缓存
        if(respData.getCmdMark().equals(SET_MODE_CMD)){
            FrameParaInfo frameParaInfo = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(),"AA");
            ParaViewInfo paraViewInfo =  DevParaInfoContainer.getDevParaView(respData.getDevNo(),frameParaInfo.getParaNo());
            paraViewInfo.setParaVal(currMode);
        }
        if (controlSuccessCode.equals(data)) {
            respData.setRespCode(controlSuccessCode);
        } else if (controlFailCode.equals(data)) {
            respData.setRespCode(controlFailCode);
        } else {
            throw new BaseException("多体制卫星信道终端参数设置响应异常，数据字节：" + data);
        }
        return respData;
    }

    /***
     * @Description 处理TDMA模式“参考编码方式”
     * @Date 10:44 2021/7/27
     * @Param [cmdMark, val]
     * @return java.lang.String
     * @author luo
     **/
    private String handleParaVal(String cmdMark,String val){
        if(cmdMark.equals("D4-9")){
            switch (val){
                case "66":
                    val = "2";
                    break;
                case "146":
                    val = "3";
                    break;
                case "226":
                    val = "4";
                    break;
                default:
                    break;
            }
        }
        return val;
    }


}
