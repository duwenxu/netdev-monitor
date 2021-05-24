package com.xy.netdev.frame.service.comtech;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.xy.common.exception.BaseException;
import com.xy.netdev.admin.entity.SysParam;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.frame.service.modemscmm.ModemScmmInterPrtcServiceImpl;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.rpt.enums.ComtechSpeComEnum;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReciveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static com.xy.netdev.common.constant.SysConfigConstant.PARA_COMPLEX_LEVEL_COMPOSE;

/**
 * Comtech功率放大器 单参数 查询控制
 *
 * @author duwenxu
 * @create 2021-05-20 15:12
 */
@Service
@Slf4j
public class ComtechPrtclAnalyseService implements IParaPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private IDataReciveService dataReciveService;
    @Autowired
    private ModemScmmInterPrtcServiceImpl modemScmmInterPrtcService;
    @Autowired
    private ISysParamService sysParamService;
    /**
     * 故障历史 字段标识
     */
    private static int FAULT_HISTORY_COMMAND = 4;

    @Override
    public void queryPara(FrameReqData reqInfo) {
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        byte[] bytes = respData.getParamBytes();
        log.info("接收到Comtech查询响应帧：[{}]", HexUtil.encodeHexStr(bytes));
        String devType = respData.getDevType();
        String cmdMark = respData.getCmdMark();

        FrameParaInfo paraInfo = BaseInfoContainer.getParaInfoByCmd(devType, cmdMark);
        FrameParaData paraData = new FrameParaData();
        BeanUtil.copyProperties(paraInfo, paraData, true);
        BeanUtil.copyProperties(respData, paraData, true);
        String ndpaRemark1Data = paraInfo.getNdpaRemark1Data();
        //命令字长度
        int commandLen = 0;
        if (!StringUtils.isBlank(ndpaRemark1Data)) {
            commandLen = Integer.parseInt(ndpaRemark1Data);
        }

        //TODO 目前先关注转换成协议上显示的 String内容后的解析  例如：2 A X CBBBBBBPDS 3 C
        String content = new String(bytes);
        int paraByteLen = 0;
        if (!StringUtils.isBlank(paraInfo.getParaByteLen())) {
            paraByteLen = Integer.parseInt(paraInfo.getParaByteLen());
        }
        CopyOnWriteArrayList<FrameParaData> paraList = new CopyOnWriteArrayList<>();
        try {
            //处理特殊参数
            if (content.startsWith(ComtechSpeComEnum.PBM.getReqCommand()) || content.startsWith(ComtechSpeComEnum.PBW.getReqCommand())) {
                String paraVal = content.substring(commandLen, commandLen + paraByteLen);
                paraData.setParaVal(paraVal);
                paraList.add(paraData);
                //故障状态 字符串循环
            } else if (paraInfo.getCmdMark().equals(FAULT_HISTORY_COMMAND)) {

                //处理复杂参数
            } else if (PARA_COMPLEX_LEVEL_COMPOSE.equals(paraInfo.getCmplexLevel())) {
                List<FrameParaInfo> subParaList = paraInfo.getSubParaList();
                //此处按命令字长度截取数据体内容
                String paraVal = content.substring(commandLen, commandLen + paraByteLen);
                byte[] dataBytes = StrUtil.bytes(paraVal);
                for (FrameParaInfo frameParaInfo : subParaList) {
                    String value = modemScmmInterPrtcService.doGetValue(frameParaInfo, dataBytes);
                    FrameParaData subParaData = new FrameParaData();
                    BeanUtil.copyProperties(frameParaInfo, paraData, true);
                    subParaData.setParaVal(value);
                    paraList.add(subParaData);
                }
            } else {
                String paraVal = content.substring(commandLen, commandLen + paraByteLen);
                paraData.setParaVal(paraVal);
                paraList.add(paraData);
            }
        } catch (Exception e) {
            log.error("Comtech数据解析异常：设备类型：{}---参数编号：{}---参数标识字：{}", devType, paraInfo.getParaNo(), paraInfo.getCmdMark());
        }
        respData.setFrameParaList(paraList);
        //响应结果向下流转
        dataReciveService.paraQueryRecive(respData);
        return respData;
    }

    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        List<FrameParaData> paraList = reqInfo.getFrameParaList();
        if (paraList == null || paraList.isEmpty()) {
            return;
        }
        //控制参数信息拼接
        FrameParaData paraData = paraList.get(0);
        String paraVal = paraData.getParaVal();
        byte[] dataBytes = null;
        if (!StringUtils.isBlank(paraVal)) {
            dataBytes = StrUtil.bytes(paraVal);
        }
        reqInfo.setParamBytes(dataBytes);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        byte[] bytes = respData.getParamBytes();
        String cmdMark = respData.getCmdMark();
        log.info("接收到Comtech控制响应帧：[{}]", HexUtil.encodeHexStr(bytes));

        //TODO 解析控制响应内容
        String content = new String(bytes);

        FrameParaInfo paraInfo = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(), cmdMark);
        String ndpaRemark1Data = paraInfo.getNdpaRemark1Data();
        //命令字长度
        int commandLen = 0;
        if (!StringUtils.isBlank(ndpaRemark1Data)) {
            commandLen = Integer.parseInt(ndpaRemark1Data);
        }

        //TODO 获取到响应字后拿到响应信息   这里暂时用截取代替  是否校验
        String errCode = content.substring(commandLen, commandLen + 1);
        if (StringUtils.isBlank(errCode)) {

            respData.setRespCode("0");
        } else {
            SysParam errParam = getErr(errCode);
            String errMsg = errParam.getParaName();
            log.info("Comtech控制响应：命令标识：[{}],响应code:[{}],响应信息：[{}]", cmdMark, content, errMsg);
            respData.setRespCode(errParam.getRemark2());
        }
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
        List<SysParam> errParams = sysParamService.queryParamsByParentId(SysConfigConstant.ERR_PARENT_COMTECH);
        List<SysParam> list = errParams.stream().filter(param -> errCode.equals(param.getRemark1())).collect(Collectors.toList());
        if (!list.isEmpty()) {
            return list.get(0);
        } else {
            throw new BaseException("Comtech功放控制响应解析异常：非法的错误类型CODE:" + errCode);
        }
    }
}
