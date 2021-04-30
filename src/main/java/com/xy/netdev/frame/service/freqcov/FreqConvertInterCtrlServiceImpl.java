package com.xy.netdev.frame.service.freqcov;

import cn.hutool.core.util.HexUtil;
import com.xy.common.exception.BaseException;
import com.xy.netdev.admin.entity.SysParam;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.frame.service.modem.ModemInterPrtcServiceImpl;
import com.xy.netdev.frame.service.modemscmm.ModemScmmPrtcServiceImpl;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReciveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.xy.netdev.common.util.ByteUtils.byteArrayCopy;

/**
 * 6914变频器接口控制实现 (多参数控制)
 *
 * @author duwenxu
 * @create 2021-04-14 9:56
 */
@Service
@Slf4j
public class FreqConvertInterCtrlServiceImpl implements ICtrlInterPrtclAnalysisService {
    @Autowired
    private ModemScmmPrtcServiceImpl modemScmmPrtcService;
    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private IDataReciveService dataReciveService;
    @Autowired
    private ISysParamService sysParamService;

    @Override
    public void ctrlPara(FrameReqData reqData) {
        List<FrameParaData> paraList = reqData.getFrameParaList();
        byte[] bytes = new byte[]{};
        for (FrameParaData paraData : paraList) {
            byte[] frameBytes = modemScmmPrtcService.doGetFrameBytes(paraData);
            bytes = ByteUtils.bytesMerge(bytes, frameBytes);
        }
        reqData.setParamBytes(bytes);
        log.info("6914变频器发送控制帧标识字：[{}]，内容：[{}]",reqData.getCmdMark(), HexUtil.encodeHexStr(bytes));
        socketMutualService.request(reqData, ProtocolRequestEnum.CONTROL);
    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        byte[] bytes = respData.getParamBytes();
        //该响应消息所对应的请求消息帧ID
        String requestId = HexUtil.encodeHexStr(Objects.requireNonNull(byteArrayCopy(bytes, 10, 2))).toUpperCase();
        String resCode = HexUtil.encodeHexStr(Objects.requireNonNull(byteArrayCopy(bytes, 12, 1))).toUpperCase();
        if ("00".equals(resCode)){
            String errCode = HexUtil.encodeHexStr(Objects.requireNonNull(byteArrayCopy(bytes, 13, 1))).toUpperCase();
            String errMsg = getErr(errCode).getParaName();
            log.warn("6914变频器查询失败：失败帧标识：[{}],失败code:[{}],失败错误类型：[{}]", requestId, errCode, errMsg);
        }
        respData.setCmdMark(requestId);
        respData.setRespCode(resCode);
        dataReciveService.paraCtrRecive(respData);
        return respData;
    }

    /**
     * 根据响应错误代码获取 配置的响应参数
     *
     * @param errCode 错误代码
     * @return 响应参数
     */
    public SysParam getErr(String errCode) {
        List<SysParam> errParams = sysParamService.queryParamsByParentId(SysConfigConstant.ERR_PARENT_ID_6914);
        List<SysParam> list = errParams.stream().filter(param -> errCode.equals(param.getRemark1())).collect(Collectors.toList());
        if (!list.isEmpty()) {
            return list.get(0);
        } else {
            throw new BaseException("6914变频器查询响应解析异常：非法的错误类型ID:" + errCode);
        }
    }
}
