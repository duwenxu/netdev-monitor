package com.xy.netdev.frame.service.freqcov;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.xy.common.exception.BaseException;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.frame.service.modem.ModemInterPrtcServiceImpl;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReciveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.xy.netdev.common.util.ByteUtils.byteArrayCopy;
import static com.xy.netdev.container.DevLogInfoContainer.PARA_REPS_STATUS_SUCCEED;

/**
 * 6914变频器接口查询实现
 *
 * @author duwenxu
 * @create 2021-04-14 9:53
 */
@Service
@Slf4j
public class FreqConvertInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {
    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private IDataReciveService dataReciveService;
    @Autowired
    private ModemInterPrtcServiceImpl modemInterPrtcService;
    @Autowired
    private FreqConvertInterCtrlServiceImpl freqConvertInterCtrlService;
    /**状态查询后可能会存在 上报/错误应答包 两种方式的响应信息*/
    /**
     * 错误应答包帧ID标识
     */
    private static final List<String> RES_IDS = Arrays.asList("FDFD", "FEFE");
    /**
     * 状态上报包帧ID标识
     */
    private static final List<String> RPT_IDS = Arrays.asList("0006", "000F");

    @Override
    public void queryPara(FrameReqData reqInfo) {
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        byte[] bytes = respData.getParamBytes();
        if (ObjectUtil.isNull(bytes)) {
            log.warn("6914变频器查询响应异常, 未获取到数据体, 设备编号：[{}], 信息:[{}]", respData.getDevNo(), JSON.toJSONString(respData));
            return respData;
        }
        //响应标识 帧ID
        String cmdMark = respData.getCmdMark();
        List<FrameParaData> frameParaDataList = new ArrayList<>();
        /**查询上报*/
        if (RPT_IDS.contains(cmdMark)) {
            //获取接口单元的参数信息
            List<FrameParaInfo> frameParaInfos = BaseInfoContainer.getInterLinkParaList(respData.getDevType(), cmdMark);
            for (FrameParaInfo param : frameParaInfos) {
                if (Objects.nonNull(param)) {
                    //构造返回信息体 paraInfo
                    Integer startPoint = param.getParaStartPoint();
                    int byteLen = Integer.parseInt(param.getParaByteLen());
                    byte[] targetBytes = byteArrayCopy(bytes, startPoint, byteLen);
                    FrameParaData paraData = modemInterPrtcService.doGetParam(respData, targetBytes, param);
                    frameParaDataList.add(paraData);
                }
            }
            respData.setRespCode(PARA_REPS_STATUS_SUCCEED);
            /**错误应答信息*/
        } else if (RES_IDS.contains(cmdMark)) {
            //该响应消息所对应的请求消息帧ID
            String requestId = HexUtil.encodeHexStr(Objects.requireNonNull(byteArrayCopy(bytes, 0, 2))).toUpperCase();
            String resCode = HexUtil.encodeHexStr(Objects.requireNonNull(byteArrayCopy(bytes, 2, 1))).toUpperCase();
            String realCode = "00".equals(resCode)?"1":"0";
            String errCode = HexUtil.encodeHexStr(Objects.requireNonNull(byteArrayCopy(bytes, 3, 1))).toUpperCase();
            String errMsg = freqConvertInterCtrlService.getErr(errCode).getParaName();
            log.warn("6914变频器查询失败：失败帧标识：[{}],失败code:[{}],失败错误类型：[{}]", requestId, errCode, errMsg);
            respData.setCmdMark(cmdMark);
            respData.setRespCode(realCode);
        } else {
            throw new BaseException("6914变频器查询响应解析异常：非法的帧标识ID:" + cmdMark);
        }
        respData.setFrameParaList(frameParaDataList);
        dataReciveService.interfaceQueryRecive(respData);
        return respData;
    }
}
