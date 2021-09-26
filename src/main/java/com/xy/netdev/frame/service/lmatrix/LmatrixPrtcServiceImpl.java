package com.xy.netdev.frame.service.lmatrix;

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
import org.springframework.stereotype.Service;

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
@Service
public class LmatrixPrtcServiceImpl implements IParaPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private IDataReceiveService dataReciveService;
    @Autowired
    private ISysParamService sysParamService;

    private static final String CTRL_SUCCESS_CODE = "20";
    private static final String CTRL_ERROR_CODE = "21";
    private static final String DEV_TYPE_UP = "0020041";


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
            //参数标识
            String cmdMarkStr = paraInfoByNo.getCmdMark();
                list.add(ByteUtils.objToBytes(Integer.parseInt(cmdMarkStr), 1));
                list.add(ByteUtils.objToBytes(1, 1));
                //参数值
                String paraValStr = frameParaData.getParaVal();
                list.add(ByteUtils.objToBytes(Integer.parseInt(paraValStr), 1));
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
        if (controCode.equals(CTRL_SUCCESS_CODE)) {
            respData.setRespCode(CTRL_SUCCESS_CODE);
        } else if (controCode.contains(CTRL_ERROR_CODE)) {
            respData.setRespCode(CTRL_ERROR_CODE);
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
