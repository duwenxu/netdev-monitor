package com.xy.netdev.frame.service.modemscmm;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.util.BeanFactoryUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.ParamCodec;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.frame.service.codec.DirectParamCodec;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReciveService;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.xy.netdev.common.util.ByteUtils.byteArrayCopy;
import static com.xy.netdev.common.util.ByteUtils.bytesToNum;

/**
 * SCMM-2300调制解调器 接口协议内容解析
 *
 * @author duwenxu
 * @create 2021-03-30 14:30
 */
@Service
@Slf4j
public class ModemScmmInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {
    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private ISysParamService sysParamService;
    @Autowired
    private IDataReciveService dataReciveService;

    @Override
    public void queryPara(FrameReqData reqInfo) {
        //按单元查询参数
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        byte[] bytes = respData.getParamBytes();
        if (ObjectUtil.isNull(bytes)) {
            log.warn("400W功放查询响应异常, 未获取到数据体, 信息:{}", JSON.toJSONString(respData));
            return respData;
        }
        //单元信息
        Short unit = bytesToNum(bytes, 0, 1, ByteBuf::readShort);
        String hexUnit = HexUtil.toHex(unit);
        //获取接口单元的参数信息
        List<FrameParaInfo> frameParaInfos = BaseInfoContainer.getInterLinkParaList(respData.getDevType(), hexUnit);
        frameParaInfos.stream().filter(Objects::nonNull)
                .map(param->{
                    Integer startPoint = param.getParaStartPoint();
                    String byteLen = param.getParaByteLen();
                    int paraByteLen = 0;
                    if (StringUtils.isNotBlank(byteLen)) {
                        paraByteLen = Integer.parseInt(byteLen);
                    }
                    byte[] targetBytes = byteArrayCopy(bytes, startPoint, paraByteLen);
                    String[] config = param.getNdpaRemark2Data().split(",");
                    //默认直接转换
                    ParamCodec codec = new DirectParamCodec();
                    if (config.length>1){
                        codec = BeanFactoryUtil.getBean(config[0]);
//                        config = System.arraycopy(config,1,new String[], 0, config.length-1);
                    }
                    return codec.decode(bytes, config);
                }).collect(Collectors.toList());
        return null;
    }
}
