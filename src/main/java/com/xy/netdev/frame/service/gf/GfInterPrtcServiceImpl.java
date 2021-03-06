package com.xy.netdev.frame.service.gf;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.impl.DataReciveServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.xy.netdev.common.util.ByteUtils.byteToNumber;
import static com.xy.netdev.frame.service.gf.GfPrtcServiceImpl.isUnsigned;

/**
 * 40W功放接口协议解析
 * @author cc
 */
@Service
@Slf4j
public class GfInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {

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
        List<FrameParaInfo> frameParaInfos = BaseInfoContainer
                .getInterLinkParaList(respData.getDevType(), respData.getCmdMark());
        byte[] bytes = respData.getParamBytes();
        if (ObjectUtil.isNull(bytes)){
            log.warn("40W功放查询响应异常, 未获取到数据体, 信息:{}", JSON.toJSONString(respData));
            return respData;
        }
        List<FrameParaData> frameParaDataList = frameParaInfos.stream()
                .filter(Objects::nonNull)
                .map(frameParaInfo -> {
                    FrameParaData paraInfo = new FrameParaData();
                    BeanUtil.copyProperties(frameParaInfo, paraInfo, true);
                    BeanUtil.copyProperties(respData, paraInfo, true);
                    paraInfo.setLen(Integer.parseInt(frameParaInfo.getParaByteLen()));
                    String s = byteToNumber(bytes, frameParaInfo.getParaStartPoint(),
                            Integer.parseInt(frameParaInfo.getParaByteLen()), isUnsigned(sysParamService,
                                    frameParaInfo.getAlertPara())).toString();

                    String paraNo = frameParaInfo.getParaNo();
                    if (paraNo.equals("13")||paraNo.equals("14")||paraNo.equals("26")||paraNo.equals("27")){
                        s = Integer.parseInt(s) * 0.1+"";
                    }
                    paraInfo.setParaVal(s);
                    return paraInfo;
                }).collect(Collectors.toList());
        respData.setFrameParaList(frameParaDataList);
        dataReciveService.interfaceQueryRecive(respData);
        return respData;
    }

    public static void setParamBytes(FrameReqData reqInfo) {
        if (reqInfo.getFrameParaList() == null){
            return;
        }
        List<byte[]> bytes = reqInfo.getFrameParaList().stream()
                .filter(Objects::nonNull)
                .filter(frameParaData -> StrUtil.isNotBlank(frameParaData.getParaVal()))
                .map(frameParaData -> frameParaData.getParaNo().equals("14") ? ByteUtils.objToBytes(Integer.valueOf(frameParaData.getParaVal())*10, frameParaData.getLen()): ByteUtils.objToBytes(frameParaData.getParaVal(), frameParaData.getLen()))
                .collect(Collectors.toList());
        reqInfo.setParamBytes(ByteUtils.listToBytes(bytes));
    }

//
//    /**
//     * 对于步进参数  解析值后乘以步进
//     * @param devType 設備類型
//     * @param cmdMark 參數標識
//     * @param devNo 設備編號
//     * @param val 值
//     * @param flag  0: 解碼  1：編碼
//     * @return
//     */
//    public static String stepConvert(String devType, String cmdMark, String devNo, String val, boolean flag){
//        FrameParaInfo infoByCmd = BaseInfoContainer.getParaInfoByCmd(devType, cmdMark);
//        ParaViewInfo devParaView = DevParaInfoContainer.getDevParaView(devNo, infoByCmd.getParaNo());
//        String step = devParaView.getParaValStep();
//        double convertVal = Integer.parseInt(val);
//        if (StringUtils.isNotBlank(step)){
//            double v = Double.parseDouble(step);
//            if (flag){
//                convertVal = convertVal * v;
//            }else {
//                convertVal = convertVal / v;
//            }
//        }
//        return convertVal+"";
//    }
}
