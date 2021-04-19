package com.xy.netdev.frame.service.czp;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReciveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * C中频切换矩阵
 *
 * @author sunchao
 * @date 2021-04-01
 */
@Slf4j
@Component
public class CzpPrtcServiceImpl  implements IParaPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private IDataReciveService dataReciveService;
    @Autowired
    private ISysParamService sysParamService;

    @Override
    public void queryPara(FrameReqData reqInfo) {

    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        return null;
    }

    /**
     * 设置设备参数
     * @param  reqInfo   请求参数信息
     */
    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        if(reqInfo.getFrameParaList() == null && reqInfo.getFrameParaList().isEmpty()){
            log.info("C中频切换矩阵无参数，设置设备参数取消！");
            return ;
        }
        List<byte[]> list = new ArrayList<>();
        reqInfo.getFrameParaList().forEach(frameParaData->{
            FrameParaInfo paraInfoByNo = BaseInfoContainer.getParaInfoByNo(frameParaData.getDevType(), frameParaData.getParaNo());
            String newVal = frameParaData.getParaVal().replaceAll("[^0-9]","");
            //赋值处理后的参数值
            frameParaData.setParaVal(newVal);
            String dataBody = paraInfoByNo.getCmdMark() + newVal;
            //处理复杂参数:利用正则表达式过滤数字
            list.add(HexUtil.decodeHex(dataBody));
        });
        reqInfo.setParamBytes(ByteUtils.listToBytes(list));
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    /**
     * 设置设备参数响应
     * @param  respData   数据传输对象
     * @return
     */
    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        byte[] bytes =respData.getParamBytes();
        String data = HexUtil.encodeHexStr(bytes);
        String controlSuccessCode = sysParamService.getParaRemark1(SysConfigConstant.CONTROL_SUCCESS);
        String controlFailCode = sysParamService.getParaRemark1(SysConfigConstant.CONTROL_FAIL);
        if (data.contains(controlSuccessCode)) {
            respData.setRespCode(controlSuccessCode);
        } else if (data.contains(controlFailCode)) {
            respData.setRespCode(controlFailCode);
        } else {
            throw new IllegalStateException("C中频切换矩阵控制响应异常，数据字节：" + data);
        }
        //参数列表
        FrameParaInfo frameParaInfo = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(),respData.getCmdMark());
        if (StringUtils.isNotEmpty(frameParaInfo.getParaNo())){
            FrameParaData frameParaData = FrameParaData.builder()
                    .devType(respData.getDevType())
                    .devNo(respData.getDevNo())
                    .paraNo(frameParaInfo.getParaNo())
                    .build();
            respData.setFrameParaList(Arrays.asList(frameParaData));
        }
        dataReciveService.paraCtrRecive(respData);
        return respData;
    }
}
