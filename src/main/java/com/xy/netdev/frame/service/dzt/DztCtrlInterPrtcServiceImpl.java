package com.xy.netdev.frame.service.dzt;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.xy.common.exception.BaseException;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.xy.netdev.rpt.service.StationControlHandler.setQueryResponseHead;

/**
 * 动中通--控制接口协议解析
 * @author luo
 * @date 2021/3/26
 */

@Service
@Slf4j
public class DztCtrlInterPrtcServiceImpl implements ICtrlInterPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private ISysParamService sysParamService;


    @Override
    public void ctrlPara(FrameReqData reqData) {
        List<byte[]> byteList = new ArrayList<>();
        Interface intf = BaseInfoContainer.getInterLinkInterface(reqData.getDevType(),reqData.getCmdMark());
        String[] format = intf.getItfDataFormat().split(",");
        for (String str : format) {
            Integer paraId = Integer.parseInt(str);
            for (FrameParaData frameParaData : reqData.getFrameParaList()) {
                FrameParaInfo paraInfo = BaseInfoContainer.getParaInfoByNo(reqData.getDevType(),frameParaData.getParaNo());
                if(paraId.equals(paraInfo.getParaId())){
                    byteList.add(ByteUtils.objToBytes(frameParaData.getParaVal(),frameParaData.getLen()));
                }
            }
        }
        reqData.setParamBytes(ByteUtils.listToBytes(byteList));
        socketMutualService.request(reqData, ProtocolRequestEnum.CONTROL);
    }

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
            throw new BaseException("车载卫星天线参数设置响应异常，数据字节：" + data);
        }
        return respData;
    }







}
