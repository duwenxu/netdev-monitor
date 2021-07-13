package com.xy.netdev.frame.service.msct;

import cn.hutool.core.util.HexUtil;
import com.alibaba.fastjson.JSON;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.BeanFactoryUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.factory.SingletonFactory;
import com.xy.netdev.frame.bo.ExtParamConf;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.ParamCodec;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.frame.service.codec.DirectParamCodec;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.bo.ParaViewInfo;
import com.xy.netdev.monitor.constant.MonitorConstants;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReciveService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
            if(paraInfo.getParaName().equals("预留")){
                DevParaInfoContainer.setIsShow(respData.getDevNo(), paraInfo.getParaNo(), false);
            }
            Integer len = Integer.parseInt(paraInfo.getParaByteLen());
            Integer endIndex = startIndex + len;
            if (StringUtils.isEmpty(paraInfo.getParaNo())) {
                continue;
            }
            String val = "";
            //解析简单参数
            FrameParaData frameParaData = FrameParaData.builder()
                    .devType(devType)
                    .devNo(respData.getDevNo())
                    .paraNo(paraInfo.getParaNo())
                    .build();
            boolean isStr = MonitorConstants.STRING_CODE.equals(paraInfo.getDataType());
            if (isStr) {
                //默认直接转换
                ParamCodec codec = SingletonFactory.getInstance(DirectParamCodec.class);
                //获取参数解析配置信息
                String confClass = paraInfo.getNdpaRemark2Data();
                //按配置的解析方式解析
                if (!StringUtils.isBlank(confClass)) {
                    codec = BeanFactoryUtil.getBean(confClass);
                }
                try {
                    val = codec.decode(ByteUtils.byteArrayCopy(paraValBytes, startIndex, len), null);
                } catch (Exception e) {
                    log.error("参数解析异常：{}",paraInfo);
                }
            }else{
                Number paraVal = 0;
                try{
                    paraVal = byteToNumber(paraValBytes, startIndex, len
                            , isUnsigned(sysParamService, paraInfo.getDataType())
                            , isFloat(sysParamService, paraInfo.getDataType())).floatValue();
                }catch (Exception e){
                    log.error("参数解析异常，参数关键字：{}",paraInfo.getCmdMark());
                }
                String desc = paraInfo.getNdpaRemark1Desc();
                String data = paraInfo.getNdpaRemark1Data();
                if (StringUtils.isNotEmpty(desc) && desc.equals("倍数") && StringUtils.isNotEmpty(data)) {
                    Float multiple = Float.parseFloat(data);
                    //单个参数值转换
                    DecimalFormat myFormatter = new DecimalFormat(getDecimal(multiple));
                    val = myFormatter.format(paraVal.floatValue() / multiple);
                } else {
                    val = String.valueOf(paraVal.intValue());
                }
            }
            //设置TDMA模式工作模式
            if(paraInfo.getCmdMark().equals("B0-1")){
                FrameParaInfo paraDetail = BaseInfoContainer.getParaInfoByCmd(paraInfo.getDevType(),"A1");
                ParaViewInfo paraViewInfo =  DevParaInfoContainer.getDevParaView(respData.getDevNo(), paraDetail.getParaNo());
                String  temp = val;
                if(val.equals("1")){
                    temp = "2";
                }else if(val.equals("2")){
                    temp = "1";
                }
                paraViewInfo.setParaVal(temp);
            }
            frameParaData.setParaVal(val);
            startIndex = endIndex;
            frameParaDataList.add(frameParaData);
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
    private String getDecimal(Float multiple){
        String decimal = "";
        String mutiStr = String.valueOf(multiple);
        switch (mutiStr){
            case "10":
                decimal =  "##0.0#";
                break;
            case "100":
                decimal =  "##0.00#";
                break;
            case "1000":
                decimal =  "##0.000#";
                break;
            default:
                decimal =  "##0";
                break;
        }
        return decimal;
    }

    /**
     * 获取卫星信道终端当前工作模式
     */
    public void initDeviceModel(){
        List<BaseInfo> baseInfos = BaseInfoContainer.getDevInfosByType(SysConfigConstant.DEVICE_MSCT);
        for (BaseInfo baseInfo : baseInfos) {
            FrameReqData reqInfo = new FrameReqData();
            reqInfo.setCmdMark("05AA");
            reqInfo.setDevNo(baseInfo.getDevNo());
            reqInfo.setDevType(SysConfigConstant.DEVICE_MSCT);
            reqInfo.setAccessType(SysConfigConstant.ACCESS_TYPE_INTERF);
            reqInfo.setOperType(SysConfigConstant.OPREATE_QUERY);
            socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
        }
    }

}
