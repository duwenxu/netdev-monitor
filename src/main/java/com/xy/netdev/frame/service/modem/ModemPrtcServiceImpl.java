package com.xy.netdev.frame.service.modem;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.HexUtil;
import com.alibaba.fastjson.JSON;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.BeanFactoryUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.ExtParamConf;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.ParamCodec;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.frame.service.codec.DirectParamCodec;
import com.xy.netdev.frame.service.modemscmm.ModemScmmPrtcServiceImpl;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReciveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.xy.netdev.common.util.ByteUtils.bytesMerge;

/**
 * 650型号 调制解调器参数 查询控制 实现
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
    @Autowired
    private ModemScmmPrtcServiceImpl modemScmmPrtcService;
    //TODO 暂时不进行单参数查询

    @Override
    public void queryPara(FrameReqData reqInfo) {
        //暂时是单个参数查询 cmdMark为单个参数的命令标识
        byte[] bytes = HexUtil.decodeHex(reqInfo.getCmdMark());
        reqInfo.setParamBytes(bytes);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        //转换为当前字节
        byte[] paraValBytes = respData.getParamBytes();
        FrameParaInfo currentPara = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(), respData.getCmdMark());
        FrameParaData paraInfo = new FrameParaData();
        BeanUtil.copyProperties(currentPara, paraInfo, true);
        BeanUtil.copyProperties(respData, paraInfo, true);

        //获取参数解析配置信息
        String confClass = currentPara.getNdpaRemark2Data();
        String confParams = currentPara.getNdpaRemark3Data();
        //默认直接转换
        ParamCodec codec = new DirectParamCodec();
        ExtParamConf paramConf = new ExtParamConf();
        Object[] params = new Object[0];
        if (!StringUtils.isBlank(confParams)) {
            paramConf = JSON.parseObject(confParams, ExtParamConf.class);
        }
        //按配置的解析方式解析
        if (!StringUtils.isBlank(confClass)) {
            codec = BeanFactoryUtil.getBean(confClass);
        }
        //构造参数
        if (paramConf.getPoint() != null && paramConf.getStart() != null) {
            params = new Integer[]{paramConf.getStart(), paramConf.getPoint()};
        } else if (paramConf.getExt() != null){
            params =paramConf.getExt().toArray();
        }
        String value = null;
        try {
            value = codec.decode(paraValBytes, params);
        } catch (Exception e) {
            log.error("参数解析异常：{}",paraInfo);
        }
        paraInfo.setParaVal(value);

        CopyOnWriteArrayList<FrameParaData> paraData = new CopyOnWriteArrayList<>();
        paraData.add(paraInfo);
        respData.setFrameParaList(paraData);
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
        FrameParaData paraData = paraList.get(0);
        byte[] frameBytes = modemScmmPrtcService.doGetFrameBytes(paraData);
        byte[] bytes = HexUtil.decodeHex(reqInfo.getCmdMark());
        byte[] bytesMerge = bytesMerge(bytes, frameBytes);
        reqInfo.setParamBytes(bytesMerge);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    /**
     * 调制解调器参数控制响应
     *
     * @param respData 协议解析响应数据
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
        dataReciveService.paraCtrRecive(respData);
        return respData;
    }
}
