package com.xy.netdev.frame.service.lpdSwitch;

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
 * L频段4x4开关矩阵
 *
 * @author sunchao
 * @create 2021-04-28 15:30
 */
@Slf4j
@Component
public class LpdSwitchPrtcServiceImpl implements IParaPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private IDataReciveService dataReciveService;
    @Autowired
    private ISysParamService sysParamService;

    @Override
    public void queryPara(FrameReqData reqInfo) {}

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
            log.info("L频段4x4开关矩阵无参数，设置参数取消！");
            return ;
        }
        List<byte[]> list = new ArrayList<>();
        reqInfo.getFrameParaList().forEach(frameParaData->{
            FrameParaInfo paraInfoByNo = BaseInfoContainer.getParaInfoByNo(frameParaData.getDevType(), frameParaData.getParaNo());
            String paraValStr = paraInfoByNo.getTransIntoOutMap().get(frameParaData.getParaVal());
            //设置转换后的值
            /*frameParaData.setParaVal(paraValStr);*/
            String dataBody = paraInfoByNo.getCmdMark() + paraValStr;
            list.add(HexUtil.decodeHex(dataBody));
        });
        reqInfo.setParamBytes(ByteUtils.listToBytes(list));
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    /**
     * 设置设备参数响应(修改)
     * @param  respData   数据传输对象
     * @return
     */
    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        String data = HexUtil.encodeHexStr(respData.getParamBytes());
        String controCode = data.substring(data.length()-2);
        String controlSuccessCode = sysParamService.getParaRemark1(SysConfigConstant.LPD_CTRL_SUCCESS);
        if (controCode.equals(controlSuccessCode)) {
            respData.setRespCode(controlSuccessCode);
        } else if (data.contains("6")) {
            //不包含成功但包含6则失败
            respData.setRespCode(controCode);
        } else {
            throw new IllegalStateException("L频段4x4开关矩阵控制响应异常，数据字节：" + data);
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
