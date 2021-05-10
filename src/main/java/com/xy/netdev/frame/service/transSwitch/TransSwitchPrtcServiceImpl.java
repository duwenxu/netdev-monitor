package com.xy.netdev.frame.service.transSwitch;

import cn.hutool.core.util.HexUtil;
import com.xy.common.exception.BaseException;
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
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.xy.netdev.common.constant.SysConfigConstant.PARA_COMPLEX_LEVEL_COMPOSE;

/**
 * 1:1 转换开关
 *
 * @author sunchao
 * @date 2021-04-01
 */
@Slf4j
@Component
public class TransSwitchPrtcServiceImpl implements IParaPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private IDataReciveService dataReciveService;
    @Autowired
    private ISysParamService sysParamService;
    /**比特位固定字节**/
    private String BIT_STR = "1111";

    @Override
    public void queryPara(FrameReqData reqInfo) {}

    /**
     * 查询设备参数响应
     * @param  respData   数据传输对象
     * @return
     */
    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) { return null; }

    /**
     * 设置设备参数
     * @param  reqInfo   请求参数信息
     */
    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        if(reqInfo.getFrameParaList() == null && reqInfo.getFrameParaList().isEmpty()){
            log.info("1:1 转换开关无参数，设置设备参数取消！");
            return ;
        }
        List<byte[]> list = new ArrayList<>();
        reqInfo.getFrameParaList().forEach(frameParaData->{
            FrameParaInfo paraInfoByNo = BaseInfoContainer.getParaInfoByNo(frameParaData.getDevType(), frameParaData.getParaNo());
            String paraValStr = frameParaData.getParaVal();
            if(paraInfoByNo.getCmplexLevel().equals(PARA_COMPLEX_LEVEL_COMPOSE)){
                paraValStr = BIT_STR + frameParaData.getParaVal().replaceAll("[^0-9]","");
                paraValStr = BitToHexStr(paraValStr);
            }
            frameParaData.setParaVal(paraValStr);
            String dataBody = paraInfoByNo.getCmdMark() + frameParaData.getParaVal();
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
            throw new IllegalStateException("1:1 转换开关控制响应异常，数据字节：" + data);
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

    /**
     * Bit转Byte
     */
    private String BitToHexStr(String byteStr) {
        int re, len;
        if (null == byteStr) {
            throw new BaseException("比特位长度异常，请检查");
        }
        len = byteStr.length();
        if (len != 4 && len != 8) {
            throw new BaseException("比特位长度异常，请检查");
        }
        if (len == 8) {// 8 bit处理
            if (byteStr.charAt(0) == '0') {// 正数
                re = Integer.parseInt(byteStr, 2);
            } else {// 负数
                re = Integer.parseInt(byteStr, 2) - 256;
            }
        } else {//4 bit处理
            re = Integer.parseInt(byteStr, 2);
        }
        return HexUtil.encodeHexStr(new byte[]{(byte) re}) ;
    }
}
