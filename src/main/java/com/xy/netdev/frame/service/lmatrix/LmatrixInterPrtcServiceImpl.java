package com.xy.netdev.frame.service.lmatrix;

import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReceiveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.xy.netdev.common.util.ByteUtils.byteToInt;
import static com.xy.netdev.common.util.ByteUtils.byteToNumber;

/**
 * L频段4x4开关矩阵
 *
 * @author sunchao
 * @create 2021-04-28 15:30
 */
@Service
@Slf4j
public class LmatrixInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private IDataReceiveService dataReciveService;

    /**
     * 查询设备参数
     * @param  reqInfo   请求参数信息
     */
    @Override
    public void queryPara(FrameReqData reqInfo) {
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    /**
     * 查询设备参数响应
     * @param  respData   数据传输对象
     * @return
     */
    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        byte[] bytes = respData.getParamBytes();
        //全查询：按容器中的参数顺序解析
        String devType = respData.getDevType();
        List<FrameParaInfo> frameParaInfos = BaseInfoContainer.getInterLinkParaList(devType,respData.getCmdMark());
        List<FrameParaData> frameParaDataList = new ArrayList<>();
        for (FrameParaInfo frameParaInfo : frameParaInfos){
            byte[] paraValBytes = ByteUtils.byteArrayCopy(bytes,frameParaInfo.getParaStartPoint(),Integer.valueOf(frameParaInfo.getParaByteLen()));
            String data = String.valueOf(byteToInt(paraValBytes));
            FrameParaInfo currentPara = BaseInfoContainer.getParaInfoByCmd(devType, frameParaInfo.getCmdMark());
            if (StringUtils.isEmpty(currentPara.getParaNo())){ continue;}
            FrameParaData frameParaData = FrameParaData.builder()
                    .devType(devType)
                    .devNo(respData.getDevNo())
                    .paraNo(currentPara.getParaNo())
                    .paraOrigByte(paraValBytes)
                    .build();
            frameParaData.setParaVal(data);
            frameParaDataList.add(frameParaData);
        }
        respData.setFrameParaList(frameParaDataList);
        //接口查询响应结果接收
        dataReciveService.interfaceQueryRecive(respData);
        return respData;
    }
}