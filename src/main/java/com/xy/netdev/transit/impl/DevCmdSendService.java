package com.xy.netdev.transit.impl;

import com.xy.common.exception.BaseException;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.bo.InterCtrlInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.transit.IDataSendService;
import com.xy.netdev.transit.IDevCmdSendService;
import lombok.extern.slf4j.Slf4j;
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
        dataSendService.paraCtrSend(frameReqData);
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
     * @param  interCtrlInfo 接口参数信息
     */
    @Override
    public void interfaceCtrSend(InterCtrlInfo interCtrlInfo) {
        BaseInfo devInfo = BaseInfoContainer.getDevInfoByNo(interCtrlInfo.getDevNo());
        if(devInfo.getDevType().equals("0020011")){

        }
        FrameReqData frameReqData = genFrameReqData(interCtrlInfo.getDevNo(),interCtrlInfo.getCmdMark());
        String devType = frameReqData.getDevType();
        for (FrameParaData para : interCtrlInfo.getParaInfos()) {
            FrameParaInfo detail = BaseInfoContainer.getParaInfoByNo(devType,para.getDevNo());
            para.setLen(Integer.parseInt(detail.getParaByteLen()));
            para.setDevNo(interCtrlInfo.getDevNo());
            para.setDevType(devType);
        }
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
