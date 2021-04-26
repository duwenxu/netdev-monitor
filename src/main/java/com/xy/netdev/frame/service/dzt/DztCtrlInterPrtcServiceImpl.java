package com.xy.netdev.frame.service.dzt;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.xy.common.exception.BaseException;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.xy.netdev.rpt.service.StationControlHandler.setQueryResponseHead;

/**
 * 动中通--控制接口协议解析
 * @author luo
 * @date 2021/3/26
 */

@Service
@Slf4j
public class DztCtrlInterPrtcServiceImpl implements ICtrlInterPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private ISysParamService sysParamService;


    @Override
    public void ctrlPara(FrameReqData reqData) {
        BaseInfo devInfo = BaseInfoContainer.getDevInfoByNo(reqData.getDevNo());
        //车载卫星天线本控状态不能进行设置
        /*if(devInfo.getDevType().equals(SysConfigConstant.DEVICE_CAR_ANTENNA)){
            FrameParaInfo paraInfo = BaseInfoContainer.getParaInfoByCmd(devInfo.getDevType(),"56_7");
            if(paraInfo.getParaVal().equals("1")){
                throw new BaseException("本控状态下，站控设备的设置命令无效！");
            }
        }*/
        List<byte[]> byteList = new ArrayList<>();
        Interface intf = BaseInfoContainer.getInterLinkInterface(reqData.getDevType(),reqData.getCmdMark());
        String[] format = intf.getItfDataFormat().split(",");
        for (String str : format) {
            Integer paraId = Integer.parseInt(str);
            for (FrameParaData frameParaData : reqData.getFrameParaList()) {
                FrameParaInfo param = BaseInfoContainer.getParaInfoByNo(reqData.getDevType(),frameParaData.getParaNo());
                if(paraId.equals(param.getParaId())){
                    String value = frameParaData.getParaVal();
                    String desc = param.getNdpaRemark2Desc();
                    String data = param.getNdpaRemark3Data();
                    if(StringUtils.isNotEmpty(desc) && desc.equals("倍数") && StringUtils.isNotEmpty(data)){
                        Integer multiple = Integer.parseInt(data);
                        float temp = Float.parseFloat(value)*multiple;
                        value = String.valueOf(temp);
                    }
                    byteList.add(ByteUtils.objToBytes(value,frameParaData.getLen()));
                }
            }
        }
        reqData.setParamBytes(ByteUtils.listToBytes(byteList));
        socketMutualService.request(reqData, ProtocolRequestEnum.CONTROL);
    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        String keywords = HexUtil.encodeHexStr(respData.getParamBytes()).substring(0,2);
        String result = HexUtil.encodeHexStr(respData.getParamBytes()).substring(2);
        String controlSuccessCode = sysParamService.getParaRemark1(SysConfigConstant.CONTROL_SUCCESS);
        String controlFailCode = sysParamService.getParaRemark1(SysConfigConstant.CONTROL_FAIL);
        String ctrlDesc = getCtrlDesc(keywords);
        if (controlSuccessCode.equals(result)) {
            respData.setRespCode(controlSuccessCode);
        } else if (controlFailCode.equals(result)) {
            respData.setRespCode(controlFailCode);
        } else {
            throw new BaseException("车载卫星天线"+ctrlDesc+"异常，数据字节：" + result);
        }
        return respData;
    }


    private String getCtrlDesc(String keywords){
        String ctrlDesc = "";
        switch (keywords){
            case "30":
                ctrlDesc = "设置卫星参数";
                break;
            case "31":
                ctrlDesc = "换星";
                break;
            case "40":
                ctrlDesc = "设置待命";
                break;
            case "41":
                ctrlDesc = "设置自动跟踪";
                break;
            case "42":
                ctrlDesc = "收藏";
                break;
            case "43":
                ctrlDesc = "设置单轴转动";
                break;
        }
        return ctrlDesc;
    }

}
