package com.xy.netdev.frame.service.modem;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.transit.IDataReciveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 650型号 调制解调器
 *
 * @author duwenxu
 * @create 2021-03-11 14:23
 */
@Slf4j
@Service
public class ModemPrtcServiceImpl implements IParaPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    ISysParamService sysParamService;
    @Autowired
    private IDataReciveService dataReciveService;
    /**查询应答帧 分隔符*/
    private static final String SPLIT = "5f";

    @Override
    public void queryPara(FrameReqData reqInfo) {
        //暂时是单个参数查询 cmdMark为单个参数的命令标识
        byte[] bytes = HexUtil.decodeHex(reqInfo.getCmdMark());
        reqInfo.setParamBytes(bytes);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        String bytesData = HexUtil.encodeHexStr(respData.getParamBytes());
        String[] dataList = bytesData.toLowerCase().split(SPLIT.toLowerCase());
        String devType = respData.getDevType();
        //拆分后根据关键字获取参数
        List<FrameParaData> frameParaDataList = new ArrayList<>();
        for (String data : dataList) {
            String paraCmk = data.substring(0, 2);
            String paraValue = data.substring(2);
            FrameParaInfo paraInfoByCmd = BaseInfoContainer.getParaInfoByCmd(devType, paraCmk);
            if (paraInfoByCmd == null|| StringUtils.isEmpty(paraInfoByCmd.getParaNo())){ continue;}
            FrameParaData frameParaData = FrameParaData.builder()
                    .devType(devType)
                    .devNo(respData.getDevNo())
                    .paraVal(paraValue.toUpperCase())
                    .paraNo(paraInfoByCmd.getParaNo())
                    .build();
            frameParaDataList.add(frameParaData);
        }
        respData.setFrameParaList(frameParaDataList);
        //参数查询响应结果接收
        dataReciveService.paraQueryRecive(respData);
        return respData;
    }

    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        List<FrameParaData> paraList = reqInfo.getFrameParaList();
        if (paraList == null||paraList.isEmpty()){ return; }
        String paraVal = paraList.get(0).getParaVal();
        String dataBody = reqInfo.getCmdMark() + paraVal;
        byte[] bytes = HexUtil.decodeHex(dataBody);
        reqInfo.setParamBytes(bytes);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    /**
     * 调制解调器参数控制响应
     * @param  respData   协议解析响应数据
     * @return 响应结果数据
     */
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
            throw new IllegalStateException("调制解调器控制响应异常，数据字节：" + data);
        }
        return respData;
    }
}
