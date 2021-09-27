package com.xy.netdev.frame.service.pam;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.common.util.BeanFactoryUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.ParamCodec;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReceiveService;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.xy.netdev.common.util.ByteUtils.*;
import static com.xy.netdev.monitor.constant.MonitorConstants.BYTE;
import static com.xy.netdev.monitor.constant.MonitorConstants.DOUBLE;

/**
 * Ku400w功放 参数查询响应 帧协议解析层
 *
 * @author duwenxu
 * @create 2021-03-22 15:30
 */
@Service
@Slf4j
public class PowerAmpPrtcServiceImpl implements IParaPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private IDataReceiveService dataReciveService;
    /**
     * 分隔符 ASCII码为 0x2C
     */
    private static final String SPLIT = ",";

    @Override
    public void queryPara(FrameReqData reqInfo) {
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        return null;
    }

    /**
     * Ku400w功放 参数控制
     *
     * @param reqInfo 请求参数信息
     */
    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        List<FrameParaData> paraList = reqInfo.getFrameParaList();
        if (paraList == null || paraList.isEmpty()) {
            return;
        }
        FrameParaData paraData = paraList.get(0);
        FrameParaInfo paraInfoByNo = BaseInfoContainer.getParaInfoByNo(paraData.getDevType(), paraData.getParaNo());
        String dataType = paraInfoByNo.getDataType();
        byte[] bytes = HexUtil.decodeHex(reqInfo.getCmdMark());
        //此处只有 衰减可设置
        switch (dataType) {
            case DOUBLE:
                ParamCodec codec = BeanFactoryUtil.getBean(paraInfoByNo.getNdpaRemark2Data());
                byte[] valBytes = codec.encode(make0Str(paraData.getParaVal(),2,2));
                bytes = bytesMerge(bytes, valBytes);
                break;
            case BYTE:
                //功放开关设置 直接发送状态位 80关/81开
                String byteVal = "0".equals(paraData.getParaVal()) ? "80" : "81";
                String dataBody = reqInfo.getCmdMark() + byteVal;
                bytes = HexUtil.decodeHex(dataBody);
                break;
            default:
                break;
        }
        reqInfo.setParamBytes(bytes);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    /**
     * Ku400w功放 参数控制响应
     *
     * @param respData 协议解析响应数据
     * @return 响应结果
     */
    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        byte[] paramBytes = respData.getParamBytes();
        //参数标识
        Byte cmd = bytesToNum(paramBytes, 0, 1, ByteBuf::readByte);
        String hexCmd = numToHexStr(Long.valueOf(cmd));
        //响应标识
        Byte response = bytesToNum(paramBytes, 1, 1, ByteBuf::readByte);
        String hexResponse = numToHexStr(Long.valueOf(response));
        respData.setCmdMark(hexCmd);
        respData.setRespCode(hexResponse);
        dataReciveService.paraCtrRecive(respData);
        return respData;
    }

    /**
     * 数值型字符串补0
     * @return
     */
    private String make0Str(String value,int len, int smallLen){
        if(StringUtils.isNotBlank(value)){
            if(value.contains(".")){
                //小数补0
                String[] valueList = value.split("\\.");
                String small = StringUtils.rightPad(valueList[1],2,"0");
                if(small.length()>smallLen){
                    small = small.substring(0,smallLen);
                }
                value = StringUtils.leftPad(valueList[0],len,"0")+"."+small;
            }else{
                //整数补小数及0
                value = StringUtils.leftPad(value,len,"0")+"."+StringUtils.leftPad("",smallLen,"0");
            }
        }
        return value;
    }
}



