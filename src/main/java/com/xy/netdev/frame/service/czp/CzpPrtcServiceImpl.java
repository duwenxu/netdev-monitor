package com.xy.netdev.frame.service.czp;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReceiveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static com.xy.netdev.common.constant.SysConfigConstant.*;

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
    private IDataReceiveService dataReciveService;
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
            log.info("C中频切换矩阵无参数，控制设备参数取消！");
            return ;
        }
        List<byte[]> list = new ArrayList<>();
        reqInfo.getFrameParaList().forEach(frameParaData->{
            //缓存获取参数详情
            FrameParaInfo paraInfoByNo = BaseInfoContainer.getParaInfoByNo(frameParaData.getDevType(), frameParaData.getParaNo());
            String newVal = frameParaData.getParaVal();
            if(frameParaData.getParaNo().equals("24") || frameParaData.getParaNo().equals("25")){
                newVal = remove0(Integer.toHexString(Integer.parseInt(newVal)));
            }
            String dataBody = remove0(paraInfoByNo.getCmdMark()) + newVal;
            //赋值处理后的参数值
            frameParaData.setParaVal(newVal);
            if(PARA_COMPLEX_LEVEL_SUB.equals(paraInfoByNo.getCmplexLevel())){
                //如当前参数是子参数，则设置其父参数的值
                FrameParaInfo paraInfoByNoParent = BaseInfoContainer.getParaInfoByNo(frameParaData.getDevType(), paraInfoByNo.getParentParaNo());
                dataBody = paraInfoByNoParent.getCmdMark()+dataBody;
            }
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
        String controlSuccessCode = sysParamService.getParaRemark1(CONTROL_SUCCESS);
        String controlFailCode = sysParamService.getParaRemark1(CONTROL_FAIL);
        if (data.contains(controlSuccessCode)) {
            respData.setRespCode(controlSuccessCode);
        } else if (data.contains(controlFailCode)) {
            respData.setRespCode(controlFailCode);
        } else {
            throw new IllegalStateException("C中频切换矩阵控制响应异常，数据字节：" + data);
        }
        //生成参数帧
        FrameParaInfo frameParaInfo = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(),respData.getCmdMark());
        if (StringUtils.isNotEmpty(frameParaInfo.getParaNo())){
            FrameParaData frameParaData = FrameParaData.builder()
                    .devType(respData.getDevType())
                    .devNo(respData.getDevNo())
                    .paraNo(frameParaInfo.getParaNo())
                    .build();
            respData.setFrameParaList(Arrays.asList(frameParaData));
        }
        //调用参数控制接收后续处理办法
        dataReciveService.paraCtrRecive(respData);
        return respData;
    }

    /**
     * 移除0（只保留两位）
     * @param value
     * @return
     */
    private String remove0(String value){
        int length = value.length();
        if(length<2){
            value = StringUtils.leftPad(value,2,"0");
        }else if(length>2){
            value = value.substring(length-2,length);
        }
        return value;
    }
}
