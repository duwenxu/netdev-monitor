package com.xy.netdev.frame.service.comtech;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.monitor.service.impl.ParaInfoServiceImpl;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReciveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.xy.netdev.monitor.constant.MonitorConstants.SINGLE_QUERY;

/**
 * Comtech多参数组合查询
 *
 * @author duwenxu
 * @create 2021-05-20 15:14
 */
@Slf4j
@Service
public class ComtechInterPrtclAnalyseService implements IQueryInterPrtclAnalysisService {
    /**Comtech高级查询命令字*/
    private static final String ADVANCE_QUERY_CMK = "?";

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private IDataReciveService dataReciveService;
    @Autowired
    private ParaInfoServiceImpl paraInfoService;
    @Autowired
    private ComtechPrtclAnalyseService comtechPrtclAnalyseService;

    @Override
    public void queryPara(FrameReqData reqInfo) {
        String devType = reqInfo.getDevType();
        //此处固定组装需要高级查询的参数命令字符
        List<Interface> interfacesByDevType = BaseInfoContainer.getInterfacesByDevType(devType);
        List<Interface> interfaces = interfacesByDevType.stream().filter(inter -> inter.getDevType().equals(devType) && SINGLE_QUERY.equals(inter.getItfType())).collect(Collectors.toList());
        Assert.notEmpty(interfaces);
        //高级查询接口
        Interface anInterface = interfaces.get(0);
        String[] paraNoList = anInterface.getItfDataFormat().split(",");
        StringBuilder advanceCmk = new StringBuilder(ADVANCE_QUERY_CMK);
        for (String paraNo : paraNoList) {
            String ndpaNo = paraInfoService.getById(paraNo).getNdpaNo();
            FrameParaInfo paraInfo = BaseInfoContainer.getParaInfoByNo(devType, ndpaNo);
            String cmdMark = paraInfo.getCmdMark();
            if (paraInfo.getParaId()==null|| StringUtils.isEmpty(cmdMark)){
                log.error("Comtech功放参数配置错误：参数编号：[{}]",paraNo);
                continue;
            }
            advanceCmk.append(cmdMark);
        }
        reqInfo.setCmdMark(advanceCmk.toString());
        log.info("Comtech高级查询参数体：[{}]",advanceCmk);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        byte[] paramBytesWithCom = respData.getParamBytes();
        byte[] paramBytes = ByteUtils.byteArrayCopy(paramBytesWithCom, 1, paramBytesWithCom.length - 1);
        String cmdMark = respData.getCmdMark();

        //区分成功和失败响应的处理
        if (respData.getRespCode().equals("1")){
            respData = comtechPrtclAnalyseService.rejectCodeHandler(paramBytes,respData);
        }else{
            List<FrameParaInfo> frameParaInfos = BaseInfoContainer.getInterLinkParaList(respData.getDevType(), cmdMark);
            List<FrameParaData> frameParaDataList = new ArrayList<>();
            //对接口参数按照参数长度解析
            for (FrameParaInfo paraInfo : frameParaInfos) {
                String paraByteLen = paraInfo.getParaByteLen();
                if (StringUtils.isBlank(paraByteLen)){
                    log.error("Comtech功放参数字节长度为空：参数编号：[{}]",paraInfo.getParaNo());
                }
                int byteLen = Integer.parseInt(paraByteLen);
                byte[] bytes = new byte[0];
                try {
                    bytes = ByteUtils.byteArrayCopy(paramBytes, paraInfo.getParaStartPoint(), byteLen);
                } catch (Exception e) {
                    log.error("Comtech高级查询参数长度异常：参数编号：[{}]---参数名称：[{}]",paraInfo.getParaNo(),paraInfo.getParaName());
                }
                String paraVal = StrUtil.str(bytes, StandardCharsets.UTF_8);

                FrameParaData paraData = new FrameParaData();
                BeanUtil.copyProperties(paraInfo, paraData, true);
                BeanUtil.copyProperties(respData, paraData, true);
                paraData.setParaVal(paraVal);
                frameParaDataList.add(paraData);
            }
            respData.setFrameParaList(frameParaDataList);
        }
        dataReciveService.interfaceQueryRecive(respData);
        return respData;
    }
}
