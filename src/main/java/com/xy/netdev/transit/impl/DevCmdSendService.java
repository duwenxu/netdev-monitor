package com.xy.netdev.transit.impl;

import com.xy.common.exception.BaseException;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.service.snmp.SnmpReqDTO;
import com.xy.netdev.frame.service.snmp.SnmpTransceiverServiceImpl;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.bo.InterfaceViewInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.transit.IDataSendService;
import com.xy.netdev.transit.IDevCmdSendService;
import com.xy.netdev.transit.ISnmpDataReceiveService;
import com.xy.netdev.transit.schedule.ScheduleQuery;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 设备命令发送服务
 * </p>
 *
 * @author tangxl
 * @since 2021-03-12
 */
@Service
public class DevCmdSendService implements IDevCmdSendService {

    @Autowired
    private IDataSendService dataSendService;
    @Autowired
    private SnmpTransceiverServiceImpl snmpTransceiverService;
    @Autowired
    private ScheduleQuery scheduleQuery;

    /**
     * 参数查询发送
     * @param  devNo   设备编号
     * @param  cmdMark 命令标识
     */
    public void paraQuerySend(String devNo,String cmdMark) {
        validateInputPara(devNo,cmdMark);
        FrameReqData frameReqData = genFrameReqData(devNo,cmdMark);
        dataSendService.paraQuerySend(frameReqData);
    }

    /**
     * 参数控制发送
     * @param  devNo   设备编号
     * @param  cmdMark 命令标识
     * @param  paraVal 参数值
     */
    public void paraCtrSend(String devNo,String cmdMark,String paraVal) {
        validateInputPara(devNo,cmdMark);
        FrameReqData frameReqData = genFrameReqData(devNo,cmdMark);
        FrameParaInfo paraInfo = BaseInfoContainer.getParaInfoByCmd(frameReqData.getDevType(),cmdMark);
        if(!paraInfo.getNdpaAccessRight().equals(SysConfigConstant.CMD_RIGHT)){
            if(StringUtils.isEmpty(paraVal)){
                throw new BaseException("传入的paraVal为空!");
            }
        }
        List<FrameParaData>  paraDataList = new ArrayList<>();
        FrameParaData  frameParaData = new FrameParaData();
        frameParaData.setDevNo(devNo);
        frameParaData.setDevType(frameReqData.getDevType());
        frameParaData.setParaNo(paraInfo.getParaNo());
        frameParaData.setParaVal(paraVal);
        if(!StringUtils.isEmpty(paraInfo.getParaByteLen())){
            frameParaData.setLen(Integer.parseInt(paraInfo.getParaByteLen()));
        }
        paraDataList.add(frameParaData);
        frameReqData.setFrameParaList(paraDataList);
        //参数控制发送聚合
        paraCtrl(devNo,frameReqData);
    }

    /**
     * 参数控制执行
     * @param devNo 设备编号
     * @param frameReqData 控制参数体
     */
    private void paraCtrl(String devNo, FrameReqData frameReqData) {
        frameReqData.setAccessType(SysConfigConstant.ACCESS_TYPE_PARAM);
        frameReqData.setOperType(SysConfigConstant.OPREATE_CONTROL);
        BaseInfo devInfoByNo = BaseInfoContainer.getDevInfoByNo(devNo);
        if (devInfoByNo!=null && !StringUtils.isBlank(devInfoByNo.getDevNo())){
            String devNetPtcl = devInfoByNo.getDevNetPtcl();
            String devIpAddr = devInfoByNo.getDevIpAddr();
            if (SysConfigConstant.SNMP.equals(devNetPtcl)){
                SnmpReqDTO snmpReqDTO = scheduleQuery.frameReq2SnmpReq(frameReqData);
                snmpTransceiverService.paramCtrl(snmpReqDTO,devIpAddr);
            }else {
                dataSendService.paraCtrSend(frameReqData);
            }
        }else {
            throw new BaseException("参数控制设备编号为"+devNo+"的设备不存在");
        }
    }

    /**
     * 接口查询发送
     * @param  devNo   设备编号
     * @param  cmdMark 命令标识
     */
    public void interfaceQuerySend(String devNo,String cmdMark) {
        validateInputPara(devNo,cmdMark);
        FrameReqData frameReqData = genFrameReqData(devNo,cmdMark);
        dataSendService.interfaceQuerySend(frameReqData);
    }

    /**
     * 接口设置发送
     * @param  interfaceViewInfo 接口参数信息
     */
    @Override
    public void interfaceCtrSend(InterfaceViewInfo interfaceViewInfo) {
        FrameReqData frameReqData = genFrameReqData(interfaceViewInfo.getDevNo(),interfaceViewInfo.getItfCmdMark());
        List<FrameParaData>  paraDataList = new ArrayList<>();
        interfaceViewInfo.getSubParaList().forEach(paraViewInfo -> {
            if(!paraViewInfo.getAccessRight().equals(SysConfigConstant.CMD_RIGHT)){
                if(StringUtils.isEmpty(paraViewInfo.getParaVal())){
                    throw new BaseException("传入的paraVal为空!");
                }
            }
            FrameParaData  frameParaData = new FrameParaData();
            frameParaData.setDevNo(paraViewInfo.getDevNo());
            frameParaData.setDevType(paraViewInfo.getDevType());
            frameParaData.setParaNo(paraViewInfo.getParaNo());
            frameParaData.setParaVal(paraViewInfo.getParaVal());
            if(!StringUtils.isEmpty(paraViewInfo.getParaByteLen())){
                frameParaData.setLen(Integer.parseInt(paraViewInfo.getParaByteLen()));
            }
            paraDataList.add(frameParaData);
        });
        frameReqData.setFrameParaList(paraDataList);
        dataSendService.interfaceCtrlSend(frameReqData);
    }

    /**
     * 生成协议解析请求数据
     * @param  devNo   设备编号
     * @return  协议解析请求数据
     */
    private  FrameReqData  genFrameReqData(String devNo,String cmdMark){
        BaseInfo devInfo = BaseInfoContainer.getDevInfoByNo(devNo);
        FrameReqData frameReqData = new FrameReqData();
        frameReqData.setDevNo(devNo);
        frameReqData.setCmdMark(cmdMark);
        frameReqData.setDevType(devInfo.getDevType());
        return frameReqData;
    }

    private void validateInputPara(String devNo,String cmdMark){
        if(StringUtils.isEmpty(devNo)){
            throw new BaseException("传入的devNo为空!");
        }
        if(StringUtils.isEmpty(cmdMark)){
            throw new BaseException("传入的cmdMark为空!");
        }
    }

}
