package com.xy.netdev.frame.service.shipAcu;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.xy.common.exception.BaseException;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.common.util.SpringContextUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.ParamCodec;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReciveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import static com.xy.netdev.common.constant.SysConfigConstant.PARA_COMPLEX_LEVEL_COMPOSE;
import static com.xy.netdev.common.constant.SysConfigConstant.PARA_DATA_TYPE_INT;
import static com.xy.netdev.container.DevLogInfoContainer.PARA_REPS_STATUS_SUCCEED;

/**
 * 1.5米ACU天线查询实现(船载)
 *
 * @author sunchao
 * @create 2021-05-16 11:08
 */
@Service
@Slf4j
public class ShipAcuPrtcServiceImpl implements IQueryInterPrtclAnalysisService {
    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private IDataReciveService dataReciveService;

    /**
     * 状态上报包帧头标识
     */
    private static final String RPT_IDS = "7c";

    @Override
    public void queryPara(FrameReqData reqInfo) {
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        byte[] bytes = respData.getParamBytes();
        if (ObjectUtil.isNull(bytes)) {
            log.warn("1.5米ACU查询响应异常, 未获取到数据体, 设备编号：[{}], 信息:[{}]", respData.getDevNo(), JSON.toJSONString(respData));
            return respData;
        }
        //响应标识 帧头
        String cmdMark = respData.getCmdMark();
        List<FrameParaData> frameParaDataList = new ArrayList<>();
        /**查询上报*/
        if (RPT_IDS.equals(cmdMark)) {
            setFrameDataList(respData, bytes, cmdMark, frameParaDataList);
            respData.setRespCode(PARA_REPS_STATUS_SUCCEED);
            /**错误应答信息*/
        } else {
            throw new BaseException("1.5米ACU查询响应解析异常：非法的帧头:" + cmdMark);
        }
        respData.setFrameParaList(frameParaDataList);
        dataReciveService.interfaceQueryRecive(respData);
        return respData;
    }

    private void setFrameDataList(FrameRespData respData, byte[] bytes, String cmdMark, List<FrameParaData> frameParaDataList) {
        bytes = ArrayUtil.remove(bytes,30);
        //获取接口单元的参数信息
        List<FrameParaInfo> frameParaInfos = BaseInfoContainer.getInterLinkParaList(respData.getDevType(), cmdMark);
        for (FrameParaInfo param : frameParaInfos) {
            byte[] byte1 = ByteUtils.byteArrayCopy(bytes,param.getParaStartPoint(),Integer.valueOf(param.getParaByteLen()));
            FrameParaData frameParaData = null;
            if (PARA_COMPLEX_LEVEL_COMPOSE.equals(param.getCmplexLevel())) {
                String paraValueStr = ByteUtils.byteToBinary(byte1[00]);
                StringBuffer sb = new StringBuffer(paraValueStr);
                sb.insert(4,"_");
                sb.insert(7,"_");
                paraValueStr = sb.toString();
                //改变子参数的数据
                String[] paraList = paraValueStr.split("_");
                for(int i=0; i< param.getSubParaList().size();i++){
                    FrameParaData subFrame = genFramePara(param.getSubParaList().get(i),respData.getDevNo(),paraList[i]);
                    frameParaDataList.add(subFrame);
                }
                frameParaData = genFramePara(param,respData.getDevNo(),paraValueStr.substring(0,Integer.valueOf(param.getParaStrLen())));
            }else if(StringUtils.isNotBlank(param.getNdpaRemark1Data())){
                ParamCodec handler = SpringContextUtils.getBean(param.getNdpaRemark1Data());
                if(PARA_DATA_TYPE_INT.equals(param.getDataType())){
                    frameParaData = genFramePara(param,respData.getDevNo(),String.valueOf(handler.decode(byte1,param.getNdpaRemark2Data())));
                }else{
                    frameParaData = genFramePara(param,respData.getDevNo(),String.valueOf(handler.decode(byte1,null)));
                }
            }else{
                frameParaData = genFramePara(param,respData.getDevNo(), HexUtil.encodeHexStr(byte1));
            }
            frameParaDataList.add(frameParaData);
        }
    }

    /**
     * 生成FrameParaData类
     * @param currentPara
     * @param devNo
     * @param paraValueStr
     * @return
     */
    private  FrameParaData genFramePara(FrameParaInfo currentPara,String devNo,String paraValueStr){
        FrameParaData frameParaData = FrameParaData.builder()
                .devType(currentPara.getDevType())
                .paraNo(currentPara.getParaNo())
                .devNo(devNo)
                .build();
        frameParaData.setParaVal(paraValueStr);
        return frameParaData;
    }
}
