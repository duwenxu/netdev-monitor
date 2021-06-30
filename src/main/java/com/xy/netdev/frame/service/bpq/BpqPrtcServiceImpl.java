package com.xy.netdev.frame.service.bpq;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.service.IBaseInfoService;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReciveService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 39所Ku&L下变频器参数协议解析
 *
 * @author luo
 * @date 2021-03-05
 */
@Component
public class BpqPrtcServiceImpl implements IParaPrtclAnalysisService {


    /**用户命令起始标记*/
    public final static String SEND_START_MARK = "<";
    /**设备响应开始标记*/
    public final static String RESP_START_MARK = ">";
    /**设备物理地址设置*/
    public final static String SET_ADDR_CMD = "SPA";
    /**设备物理广播地址*/
    public final static String BROADCAST_ADDR = "255";

    public final static String FORMAT = "#";


    @Autowired
    SocketMutualService socketMutualService;
    @Autowired
    IDataReciveService dataReciveService;
    @Autowired
    IBaseInfoService baseInfoService;


    /**
     * 查询设备参数
     * @param  reqInfo   请求参数信息
     */
    @Override
    public void queryPara(FrameReqData reqInfo) {
        StringBuilder sb = new StringBuilder();
        String localAddr = "001";
        BaseInfo baseInfo = BaseInfoContainer.getDevInfoByNo(reqInfo.getDevNo());
        //Ka/c下变频器没有切换单元
        if(baseInfo.getDevType().equals(SysConfigConstant.DEVICE_KAC_BPQ)){
            localAddr = baseInfo.getDevLocalAddr();
        }else{
            List<BaseInfo> subDevs = BaseInfoContainer.getDevInfoByParentNo(baseInfo.getDevParentNo());
            for (BaseInfo subDev : subDevs) {
                if(subDev.getDevType().equals(SysConfigConstant.DEVICE_QHDY)){
                    localAddr = subDev.getDevLocalAddr();
                }
            }
        }
        sb.append(SEND_START_MARK).append(localAddr).append("/")
                .append(reqInfo.getCmdMark());
        String command = sb.toString();
        reqInfo.setParamBytes(command.getBytes());
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    /**
     * 查询设备参数响应
     * @param  respData   数据传输对象
     * @return
     */
    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        String respStr = new String(respData.getParamBytes());
        respStr = respStr.substring(1);
        String[] respArr = null;
        if(respStr.contains(RESP_START_MARK)){
            respArr = respStr.split(RESP_START_MARK);
        }else{
            respArr = new String[]{respStr};
        }
        List<FrameParaData> frameParas = new ArrayList<>();
        for (int i = 0; i < respArr.length; i++) {
            String addr = respArr[i].substring(0,3);
            int beginIdx = respArr[i].indexOf("/");
            int endIdx = respArr[i].indexOf("_");
            String value = respStr.substring(endIdx+1,respStr.indexOf(StrUtil.CRLF));
            FrameParaInfo frameParaInfo = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(),respData.getCmdMark());
            FrameParaData frameParaData = new FrameParaData();
            String cmdMark = respData.getCmdMark();
            BeanUtil.copyProperties(frameParaInfo, frameParaData, true);
            frameParaData.setDevNo(getDevNo(addr));
            frameParaData.setParaVal(value);
            frameParas.add(frameParaData);
        }
        respData.setFrameParaList(frameParas);
        //切换单元的参数需要改变设备编号
        FrameParaData para = frameParas.get(0);
        if(para.getDevType().equals(SysConfigConstant.DEVICE_QHDY)){
            respData.setDevNo(frameParas.get(0).getDevNo());
            respData.setDevType(SysConfigConstant.DEVICE_QHDY);
        }
        dataReciveService.paraQueryRecive(respData);
        return respData;
    }

    /**
     * 设置设备参数
     * @param  reqInfo   请求参数信息
     */
    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        StringBuilder sb = new StringBuilder();
        String localAddr = "001";
        BaseInfo baseInfo = BaseInfoContainer.getDevInfoByNo(reqInfo.getDevNo());
        List<BaseInfo> subDevs = BaseInfoContainer.getDevInfoByParentNo(baseInfo.getDevParentNo());
        for (BaseInfo subDev : subDevs) {
            if(subDev.getDevType().equals(SysConfigConstant.DEVICE_QHDY)){
                localAddr = subDev.getDevLocalAddr();
            }
        }
        sb.append(SEND_START_MARK).append(localAddr).append("/").append(reqInfo.getCmdMark())
                .append("_").append(reqInfo.getFrameParaList().get(0).getParaVal());
        String command = sb.toString();
        reqInfo.setParamBytes(command.getBytes());
        String cmdMark = reqInfo.getCmdMark();
        if(cmdMark.equals(SET_ADDR_CMD)) {
            setDevLocalAddr(reqInfo);
        }
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    /**
     * 设置设备参数响应
     * @param  respData   数据传输对象
     * @return
     */
    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        String respStr = new String(respData.getParamBytes());
        respStr = respStr.substring(1);
        String[] respArr = null;
        if(respStr.contains(RESP_START_MARK)){
            respArr = respStr.split(RESP_START_MARK);
        }else{
            respArr = new String[]{respStr};
        }
        List<FrameParaData> frameParas = new ArrayList<>();
        for (int i = 0; i < respArr.length; i++) {
            String addr = respArr[i].substring(0,3);
            int beginIdx = respArr[i].indexOf("/");
            int endIdx = respArr[i].indexOf("_");
            String value = respStr.substring(endIdx+1,respStr.indexOf(StrUtil.CRLF));
            FrameParaInfo frameParaInfo = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(),respData.getCmdMark());
            FrameParaData frameParaData = new FrameParaData();
            String cmdMark = respData.getCmdMark();
            BeanUtil.copyProperties(frameParaInfo, frameParaData, true);
            frameParaData.setDevNo(getDevNo(addr));
            frameParaData.setParaVal(value);
            frameParas.add(frameParaData);
        }
        respData.setFrameParaList(frameParas);
        //切换单元的参数需要改变设备编号
        FrameParaData para = frameParas.get(0);
        if(para.getDevType().equals(SysConfigConstant.DEVICE_QHDY)){
            respData.setDevNo(frameParas.get(0).getDevNo());
            respData.setDevType(SysConfigConstant.DEVICE_QHDY);
        }
        dataReciveService.paraQueryRecive(respData);
        return respData;
    }

    /**
     * 获取设备物理地址
     * @param reqInfo
     * @return
     */
    protected String getDevLocalAddr(FrameReqData reqInfo){
        BaseInfo baseInfo = BaseInfoContainer.getDevInfoByNo(reqInfo.getDevNo());
        String localAddr = baseInfo.getDevLocalAddr();
        if(StringUtils.isEmpty(localAddr)){
            if(reqInfo.getCmdMark().equals(SET_ADDR_CMD)){
                localAddr = BROADCAST_ADDR;
            }
        }
        return localAddr;
    }

    /**
     * 设置设备物理地址
     * @param reqInfo
     */
    private void setDevLocalAddr(FrameReqData reqInfo){
        String devNo = reqInfo.getDevNo();
        BaseInfo baseInfo = new BaseInfo();
        baseInfo.setDevNo(devNo);
        baseInfo.setDevLocalAddr(reqInfo.getFrameParaList().get(0).getParaVal());
        baseInfoService.updateById(baseInfo);
        BaseInfoContainer.updateBaseInfo(devNo);
    }

//    private String getDevNo(String addr){
//        String devNo = "";
//        switch (addr){
//            case "001":
//                devNo = "42";
//                break;
//            case "010":
//                devNo = "40";
//                break;
//            case "011":
//                devNo = "41";
//                break;
//            default:
//                devNo = "42";
//                break;
//        }
//        return devNo;
//    }

    /**
     * 获取变频器内部地址映射关系
     * @return
     */
    private Map<String, BaseInfo> getBPQAddrMap(){
        List<BaseInfo> baseInfos = new ArrayList<>();
        baseInfos.addAll(BaseInfoContainer.getDevInfosByType(SysConfigConstant.DEVICE_BPQ));
        baseInfos.addAll(BaseInfoContainer.getDevInfosByType(SysConfigConstant.DEVICE_KAC_BPQ));
        baseInfos.addAll(BaseInfoContainer.getDevInfosByType(SysConfigConstant.DEVICE_QHDY));
        Map<String, BaseInfo> addrMap = new HashMap<>();
        for (BaseInfo baseInfo : baseInfos) {
            String localAddr = baseInfo.getDevLocalAddr();
            if(StringUtils.isNotEmpty(localAddr)){
                addrMap.put(localAddr,baseInfo);
            }
        }
        return addrMap;
    }

    /**
     * 获取设备编号
     * @param addr
     * @return
     */
    private String getDevNo(String addr){
        String devNo = "";
        Map<String, BaseInfo> addrMap =  getBPQAddrMap();
        if(addrMap.get(addr) != null){
            devNo = addrMap.get(addr).getDevNo();
        }
        return devNo;
    }



}
