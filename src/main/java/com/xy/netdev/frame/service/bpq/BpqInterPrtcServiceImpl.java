package com.xy.netdev.frame.service.bpq;


import cn.hutool.core.bean.BeanUtil;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 39所Ku&L下变频器接口协议解析
 *
 * @author luo
 * @date 2021-03-05
 */
@Component
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
        reqInfo.setAccessType(SysConfigConstant.ACCESS_TYPE_PARAM);
        reqInfo.setParamBytes(command.getBytes());
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    /**
     * 查询设备接口响应
     * @param  respData   协议解析响应数据
     * @return
     */
    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        String respStr = new String(respData.getParamBytes());
        int startIdx = respStr.indexOf("_");
        int endIdx = respStr.indexOf("\\n");
        String str = respStr.substring(startIdx+3,endIdx);
        String[] params = str.split("\\\\r");
        List<FrameParaData> frameParaList = new ArrayList<>();
        for (String param : params) {
            String cmdMark = param.split("_")[0];
            String value = param.split("_")[1];
            FrameParaData paraInfo = new FrameParaData();
            FrameParaInfo frameParaDetail = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(),cmdMark);
            BeanUtil.copyProperties(frameParaDetail, paraInfo, true);
            paraInfo.setParaVal(value);
            paraInfo.setDevNo(respData.getDevNo());
            frameParaList.add(paraInfo);
        }
        respData.setFrameParaList(frameParaList);
        respData.setCmdType(1);
        dataReciveService.paraQueryRecive(respData);
        return respData;
    }


}
