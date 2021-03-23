package com.xy.netdev.frame.service.gf;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
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

    @Override
    public void queryPara(FrameReqData reqInfo) {
        setParamBytes(reqInfo);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        FrameParaInfo frameParaInfo = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(), respData.getCmdMark());
        return setRespData(respData, frameParaInfo,frameParaInfo.getParaStartPoint() - 1);
    }

    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        setParamBytes(reqInfo);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        FrameParaInfo frameParaInfo = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(), respData.getCmdMark());
        return setRespData(respData, frameParaInfo,0);
    }

    private FrameRespData setRespData(FrameRespData respData, FrameParaInfo frameParaInfo, int offset) {
        byte[] bytes = respData.getParamBytes();
        FrameParaData paraInfo = new FrameParaData();
        BeanUtil.copyProperties(frameParaInfo, paraInfo, true);
        BeanUtil.copyProperties(respData, paraInfo, true);
        paraInfo.setLen(Integer.parseInt(frameParaInfo.getParaByteLen()));
        paraInfo.setParaVal(byteToNumber(bytes
                , offset
                , Integer.parseInt(frameParaInfo.getParaByteLen())
                , isUnsigned(sysParamService, frameParaInfo.getParaNo())).toString());
        respData.setFrameParaList(Lists.list(paraInfo));
        return respData;
    }

    /**
     * 有无符号
     * @param sysParamService 系统参数
     * @param paraNo 参数编号
     * @return true 有符号, false 无符号
     */
    public static boolean isUnsigned(ISysParamService sysParamService, String paraNo){
        String isUnsigned = sysParamService.getParaRemark1(paraNo);
        if (StrUtil.isBlank(isUnsigned)){
            return true;
        }
        return Integer.parseInt(isUnsigned) == 1;
    }

    /**
     * 是否浮点型参数
     * @param sysParamService 系统参数
     * @param paraNo 参数编号
     * @return true 浮点型, false 整型
     */
    public static boolean isFloat(ISysParamService sysParamService, String paraNo){
        String isFloat = sysParamService.getParaRemark3(paraNo);
        if (StrUtil.isBlank(isFloat)){
            return false;
        }
        return Integer.parseInt(isFloat) == 2;
    }
}
