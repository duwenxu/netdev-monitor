package com.xy.netdev.frame.service.modem;

import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.transit.IDataReciveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author duwenxu
 * @create 2021-03-11 14:26
 */
@Slf4j
@Component
public class ModemInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {

    @Autowired
    SocketMutualService socketMutualService;
    @Autowired
    ISysParamService sysParamService;
    @Autowired
    private IDataReciveService dataReciveService;
    /**查询应答帧 分隔符*/
    private static final String SPLIT = "5f";

    @Override
    public void queryPara(FrameReqData reqInfo) {
        reqInfo.setAccessType(SysConfigConstant.ACCESS_TYPE_INTERF);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    /**
     * 接口查询响应协议
     * @param  respData   协议解析响应数据
     * @return 协议相应解析数据
     */
    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
//        String orignData = respData.getReciveOrignData();
        String bytesData = new String(respData.getParamBytes());
        String[] dataList = bytesData.toLowerCase().split(SPLIT.toLowerCase());
        String devType = respData.getDevType();
        //拆分后根据关键字获取参数
        List<FrameParaData> frameParaDataList = new ArrayList<>();
        for (String data : dataList) {
            String paraCmk = data.substring(0, 2);
            String paraValue = data.substring(2);
            FrameParaInfo paraInfoByCmd = BaseInfoContainer.getParaInfoByCmd(devType, paraCmk);
            if (paraInfoByCmd == null){ continue;}
            FrameParaData frameParaData = FrameParaData.builder()
                    .devType(devType)
                    .devNo(respData.getDevNo())
                    .paraVal(paraValue)
                    .paraNo(paraInfoByCmd.getParaNo())
                    .build();
            frameParaDataList.add(frameParaData);
        }
        respData.setFrameParaList(frameParaDataList);
        //参数查询响应结果接收
        dataReciveService.paraQueryRecive(respData);
        return respData;
    }
}
