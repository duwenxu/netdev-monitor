package com.xy.netdev.frame.service.msct;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.constant.MonitorConstants;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReciveService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.xy.netdev.common.util.ByteUtils.byteToNumber;
import static com.xy.netdev.frame.service.gf.GfPrtcServiceImpl.isFloat;
import static com.xy.netdev.frame.service.gf.GfPrtcServiceImpl.isUnsigned;

/**
 * MSCT-102C多体制卫星信道终端站控协议  接口查询响应 帧协议解析层
 *
 * @author luo
 * @date 2021/4/8
 */
@Service
public class MsctInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    ISysParamService sysParamService;
    @Autowired
    private IDataReciveService dataReciveService;

    @Override
    public void queryPara(FrameReqData reqInfo) {
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        String devType = respData.getDevType();
        String paraValueStr = HexUtil.encodeHexStr(respData.getParamBytes());
        byte[] paraValBytes = HexUtil.decodeHex(paraValueStr);
        String cmdMark = respData.getCmdMark();
        List<FrameParaData> frameParaDataList = new ArrayList<>();
        List<FrameParaInfo> paraInfos = BaseInfoContainer.getInterLinkParaList(devType, cmdMark);
        Integer startIndex = 0;
        for (FrameParaInfo paraInfo : paraInfos) {
            Integer len = Integer.parseInt(paraInfo.getParaByteLen());
            Integer endIndex = startIndex + len;
            if (StringUtils.isEmpty(paraInfo.getParaNo())) {
                continue;
            }
            //解析简单参数
            FrameParaData frameParaData = FrameParaData.builder()
                    .devType(devType)
                    .devNo(respData.getDevNo())
                    .paraNo(paraInfo.getParaNo())
                    .build();
            boolean isStr = MonitorConstants.STRING_CODE.equals(paraInfo.getDataType());
            if (isStr) {
                frameParaData.setParaVal(paraValueStr.substring(startIndex * 2, endIndex * 2));
            } else {
                Number paraVal = byteToNumber(paraValBytes, startIndex, len
                        , isUnsigned(sysParamService, paraInfo.getDataType())
                        , isFloat(sysParamService, paraInfo.getDataType())).floatValue();
                String desc = paraInfo.getNdpaRemark1Desc();
                String data = paraInfo.getNdpaRemark1Data();
                String val = "";
                if (StringUtils.isNotEmpty(desc) && desc.equals("倍数") && StringUtils.isNotEmpty(data)) {
                    Integer multiple = Integer.parseInt(data);
                    //单个参数值转换
                    DecimalFormat myFormatter = new DecimalFormat(getDecimal(multiple));
                    val = myFormatter.format(paraVal.floatValue() / multiple);
                } else {
                    val = String.valueOf(paraVal.intValue());
                }
                frameParaData.setParaVal(val);
                startIndex = endIndex;
                frameParaDataList.add(frameParaData);
            }
        }
        respData.setFrameParaList(frameParaDataList);
        //参数查询响应结果接收
        dataReciveService.interfaceQueryRecive(respData);
        return respData;
    }

    /**
     * 根据倍数获取保留小数点后几位格式
     * @param multiple
     * @return
     */
    private String getDecimal(Integer multiple){
        String decimal = "";
        switch (multiple){
            case 10:
                decimal =  "###.0#";
                break;
            case 100:
                decimal =  "###.00#";
                break;
            case 1000:
                decimal =  "###.000#";
                break;
            default:
                decimal =  "###";
                break;
        }
        return decimal;
    }

    public void initDeviceModel(){

        List<BaseInfo> baseInfos = BaseInfoContainer.getDevInfoByNo()
        FrameReqData reqInfo = new FrameReqData();
        reqInfo.setCmdMark("05AA");
        reqInfo.setDevType(SysConfigConstant.DEVICE_MSCT);
        reqInfo.setDevNo("51");
        reqInfo.setAccessType();
    }


}
