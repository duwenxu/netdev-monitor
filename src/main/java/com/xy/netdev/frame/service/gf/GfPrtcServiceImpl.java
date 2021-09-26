package com.xy.netdev.frame.service.gf;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.primitives.Ints;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.transit.impl.DataReciveServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.xy.netdev.common.util.ByteUtils.byteToNumber;
import static com.xy.netdev.frame.service.gf.GfInterPrtcServiceImpl.setParamBytes;

/**
 * 40W 功放
 * @author cc
 */
@Service
@Slf4j
public class GfPrtcServiceImpl implements IParaPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;

    @Autowired
    private ISysParamService sysParamService;

    @Autowired
    private DataReciveServiceImpl dataReciveService;

    @Override
    public void queryPara(FrameReqData reqInfo) {
        setParamBytes(reqInfo);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        FrameParaInfo frameParaInfo = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(), respData.getCmdMark());
        FrameRespData frameRespData = setRespData(respData, frameParaInfo, frameParaInfo.getParaStartPoint() - 1);
        dataReciveService.paraQueryRecive(frameRespData);
        return frameRespData;
    }

    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        setParamBytes(reqInfo);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        FrameParaInfo frameParaInfo = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(), respData.getCmdMark());
        String data = HexUtil.encodeHexStr(respData.getParamBytes());
        String success = sysParamService.getParaRemark1(SysConfigConstant.CONTROL_SUCCESS);
        String fail = sysParamService.getParaRemark1(SysConfigConstant.CONTROL_FAIL);
        respData.setRespCode(data);
        if (success.equals(data)){
            respData.setRespCode(success);
        }
        if (fail.equals(data)){
            respData.setRespCode(fail);
        }
        FrameRespData frameRespData = setRespData(respData, frameParaInfo, 0);
        dataReciveService.paraQueryRecive(frameRespData);
        return frameRespData;
    }

    private FrameRespData setRespData(FrameRespData respData, FrameParaInfo frameParaInfo, int offset) {
        byte[] bytes = respData.getParamBytes();
        FrameParaData paraInfo = new FrameParaData();
        BeanUtil.copyProperties(frameParaInfo, paraInfo, true);
        BeanUtil.copyProperties(respData, paraInfo, true);
        paraInfo.setLen(Integer.parseInt(frameParaInfo.getParaByteLen()));
        /*paraInfo.setParaVal(byteToNumber(bytes
                , offset
                , Integer.parseInt(frameParaInfo.getParaByteLen())
                , isUnsigned(sysParamService, frameParaInfo.getAlertPara())).toString());*/
        respData.setFrameParaList(Lists.list(paraInfo));
        return respData;
    }

    /**
     * 有无符号
     * @param sysParamService 系统参数
     * @param alertPara 参数编号
     * @return true 有符号, false 无符号
     */
    public static boolean isUnsigned(ISysParamService sysParamService, String alertPara){
        String isUnsigned = sysParamService.getParaRemark1(alertPara);
        if (StrUtil.isBlank(isUnsigned)){
            return true;
        }
        return Integer.parseInt(isUnsigned) == 1;
    }

    /**
     * 是否浮点型参数
     * @param sysParamService 系统参数
     * @param alertPara 参数编号
     * @return true 浮点型, false 整型
     */
    public static boolean isFloat(ISysParamService sysParamService, String alertPara){
        String isFloat = sysParamService.getParaRemark3(alertPara);
        if (StrUtil.isBlank(isFloat)){
            return false;
        }
        return Integer.parseInt(isFloat) == 2;
    }

    public static void main(String[] args) {
        byte b = (byte) 128;
        System.out.println(b);
    }
}
