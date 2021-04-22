package com.xy.netdev.frame.service.modemscmm;

import cn.hutool.core.util.HexUtil;
import com.alibaba.fastjson.JSON;
import com.xy.netdev.admin.service.ISysParamService;
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
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.sendrecv.head.ModemImpl;
import com.xy.netdev.sendrecv.head.ModemScmmImpl;
import com.xy.netdev.transit.IDataReciveService;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.xy.netdev.common.util.ByteUtils.*;
/**
 * SCMM-2300调制解调器 参数协议内容解析
 *
 * @author duwenxu
 * @create 2021-03-30 13:51
 */
@Service
@Slf4j
public class ModemScmmPrtcServiceImpl implements IParaPrtclAnalysisService {
    @Autowired
    private IDataReciveService dataReciveService;
    @Autowired
    private SocketMutualService socketMutualService;

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
        byte[] valueBytes = doGetFrameBytes(paraData);
        byte[] bytes = HexUtil.decodeHex(reqInfo.getCmdMark());
        byte[] bytesMerge = bytesMerge(bytes, valueBytes);
        reqInfo.setParamBytes(bytesMerge);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    /**
     * 获取参数相对应的字节数组表示
     * @param paraData 数据帧参数结构
     * @return 参数字节
     */
    public byte[] doGetFrameBytes(FrameParaData paraData) {
        FrameParaInfo paraInfoByNo = BaseInfoContainer.getParaInfoByNo(paraData.getDevType(), paraData.getParaNo());
        String configClass = paraInfoByNo.getNdpaRemark2Data();
        String configParams = paraInfoByNo.getNdpaRemark3Data();
        ParamCodec handler = new DirectParamCodec();
        Object[] params = new Object[0];
        ExtParamConf paramConf = new ExtParamConf();
        if (!StringUtils.isBlank(configParams)) {
            paramConf = JSON.parseObject(configParams, ExtParamConf.class);
        }
        //构造参数
        if (paramConf.getPoint() != null && paramConf.getStart() != null) {
            params = new Integer[]{paramConf.getStart(), paramConf.getPoint()};
        } else if (paramConf.getExt() != null){
            params =paramConf.getExt().toArray();
        }
        if (StringUtils.isNotBlank(configClass)){
            handler = BeanFactoryUtil.getBean(configClass);
        }
        return handler.encode(paraData.getParaVal(), params);
    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        byte[] paramBytes = respData.getParamBytes();
        log.info("接收到控制响应帧内容:[{}]", HexUtil.encodeHexStr(paramBytes));
        /**此处忽略响应中多出的80  此处控制响应中只有单元标识，没有参数标识*/
        //单元标识
        Byte unit = bytesToNum(paramBytes, 1, 1, ByteBuf::readByte);
        //设置响应
        Byte res = bytesToNum(paramBytes, 2, 1, ByteBuf::readByte);
        respData.setCmdMark(numToHexStr(Long.valueOf(unit)));
        respData.setRespCode(numToHexStr(Long.valueOf(res)));
        dataReciveService.interfaceCtrlRecive(respData);
        return respData;
    }
}
