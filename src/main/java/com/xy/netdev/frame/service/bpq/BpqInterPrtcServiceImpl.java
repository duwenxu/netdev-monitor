package com.xy.netdev.frame.service.bpq;


import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.entity.TransportEntity;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.transit.IDataReciveService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * 39所Ku&L下变频器接口协议解析
 *
 * @author luo
 * @date 2021-03-05
 */
public class BpqInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {

    @Autowired
    SocketMutualService socketMutualService;
    @Autowired
    IDataReciveService dataReciveService;

    /**
     * 查询设备接口
     * @param  reqInfo    请求参数信息
     */
    @Override
    public void queryPara(FrameReqData reqInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append(BpqPrtcServiceImpl.SEND_START_MARK).append(reqInfo.getDevNo()).append("/")
                .append(reqInfo.getCmdMark());
        String command = sb.toString();
        TransportEntity transportEntity = new TransportEntity();
        BaseInfo baseInfo = BaseInfoContainer.getDevInfoByNo(reqInfo.getDevNo());
        transportEntity.setDevInfo(baseInfo);
        transportEntity.setParamMark(reqInfo.getCmdMark());
        transportEntity.setParamBytes(command.getBytes());
        socketMutualService.request(transportEntity, ProtocolRequestEnum.QUERY);
    }

    /**
     * 查询设备接口响应
     * @param  transportEntity   数据传输对象
     * @return
     */
    @Override
    public FrameRespData queryParaResponse(TransportEntity transportEntity) {
        String respStr = new String(transportEntity.getParamBytes());
        int startIdx = respStr.indexOf("_");
        int endIdx = respStr.indexOf("\\n");
        String str = respStr.substring(startIdx+1,endIdx);
        String[] params = str.split("\\r");
        FrameRespData respData = new FrameRespData();
        List<FrameParaData> frameParaList = new ArrayList<>();
        for (String param : params) {
            String cmdMark = param.split("_")[0];
            String value = param.split("_")[1];
            FrameParaData paraInfo = new FrameParaData();
            FrameParaInfo frameParaDetail = BaseInfoContainer.getParaInfoByCmd(transportEntity.getDevInfo().getDevType(),transportEntity.getParamMark());
            paraInfo.setParaNo(frameParaDetail.getParaNo());
            paraInfo.setDevType(frameParaDetail.getDevType());
            paraInfo.setDevNo(frameParaDetail.getDevNo());
            paraInfo.setParaVal(value);
            frameParaList.add(paraInfo);
        }
        respData.setFrameParaList(frameParaList);
        respData.setCmdMark(transportEntity.getParamMark());
        respData.setDevNo(transportEntity.getDevInfo().getDevNo());
        respData.setDevType(transportEntity.getDevInfo().getDevType());
        respData.setOperType(SysConfigConstant.OPREATE_QUERY_RESP);
        respData.setAccessType(SysConfigConstant.ACCESS_TYPE_INTERF);
        dataReciveService.paraQueryRecive(respData);
        return respData;
    }


}
