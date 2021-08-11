package com.xy.netdev.frame.service.kapam;

import cn.hutool.core.util.HexUtil;
import com.xy.common.exception.BaseException;
import com.xy.netdev.admin.entity.SysParam;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.frame.service.modemscmm.ModemScmmPrtcServiceImpl;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.sendrecv.head.KaPowerAmpImpl;
import com.xy.netdev.transit.IDataReceiveService;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Ka频段100W发射机 参数协议
 *
 * @author duwenxu
 * @create 2021-04-30 10:59
 */
@Service
@Slf4j
public class KaPowerAmpPrtcServiceImpl implements IParaPrtclAnalysisService {
    @Autowired
    private ModemScmmPrtcServiceImpl modemScmmPrtcService;
    @Autowired
    private IDataReceiveService dataReciveService;
    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private ISysParamService sysParamService;

    @Override
    public void queryPara(FrameReqData reqInfo) {
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        return null;
    }

    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        List<FrameParaData> paraList = reqInfo.getFrameParaList();
        if (paraList == null || paraList.isEmpty()) { return; }
        //控制参数信息拼接
        FrameParaData paraData = paraList.get(0);
        byte[] valueBytes = modemScmmPrtcService.doGetFrameBytes(paraData);
        reqInfo.setParamBytes(valueBytes);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        byte[] bytes = respData.getParamBytes();
        String cmdMark = respData.getCmdMark();
        log.info("Ka100W发射机收到控制响应帧内容：[{}]", HexUtil.encodeHexStr(bytes));
        //命令码
        String resCode = ByteUtils.bytesToNum(bytes,0,1, ByteBuf::readUnsignedByte)+"";
        if (!KaPowerAmpImpl.REQUEST_CMD.contains(cmdMark)){
            log.error("Ka100W发射机控制响应帧标识错误：[{}]", cmdMark);
        }
        SysParam param = getErr(resCode);
        String errMsg = param.getParaName();
        log.info("Ka频段100W发射机控制响应：命令标识：[{}],响应code:[{}],响应信息：[{}]", cmdMark, resCode, errMsg);
        respData.setRespCode(param.getRemark2());
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
        List<SysParam> errParams = sysParamService.queryParamsByParentId(SysConfigConstant.ERR_PARENT_ID_GF);
        List<SysParam> list = errParams.stream().filter(param -> errCode.equals(param.getRemark1())).collect(Collectors.toList());
        if (!list.isEmpty()) {
            return list.get(0);
        } else {
            throw new BaseException("Ka频段100W发射机查询响应解析异常：非法的错误类型ID:" + errCode);
        }
    }
}
