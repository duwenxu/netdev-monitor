package com.xy.netdev.frame.service.bpq;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.xy.common.exception.BaseException;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseContainerLoader;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReciveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 39所Ku&L下变频器接口协议解析
 *
 * @author luo
 * @date 2021-03-05
 */
@Component
@Slf4j
public class BpqInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {

    @Autowired
    SocketMutualService socketMutualService;
    @Autowired
    IDataReciveService dataReciveService;
    @Autowired
    BpqPrtcServiceImpl bpqPrtcService;

    public final static String WORK_STATUS_CMD = "RAS";

    /**
     * 查询设备接口
     * @param  reqInfo    请求参数信息
     */
    @Override
    public void queryPara(FrameReqData reqInfo) {
        StringBuilder sb = new StringBuilder();
        String localAddr = "001";
        BaseInfo baseInfo = BaseInfoContainer.getDevInfoByNo(reqInfo.getDevNo());
        //Ka/c下变频器没有切换单元
        if(baseInfo.getDevType().equals(SysConfigConstant.DEVICE_KAC_BPQ)){
            localAddr = baseInfo.getDevLocalAddr();
        }else {
            List<BaseInfo> subDevs = BaseInfoContainer.getDevInfoByParentNo(baseInfo.getDevParentNo());
            for (BaseInfo subDev : subDevs) {
                if (subDev.getDevType().equals(SysConfigConstant.DEVICE_QHDY)) {
                    localAddr = subDev.getDevLocalAddr();
                }
            }
        }
        sb.append(BpqPrtcServiceImpl.SEND_START_MARK).append(localAddr).append("/")
                .append(reqInfo.getCmdMark());
        String command = sb.toString();
        reqInfo.setParamBytes(command.getBytes());
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    /**
     * 查询设备接口响应
     * @param  respData   协议解析响应数据
     * @return
     */
    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        String respStr = new String(respData.getParamBytes());
        String addr = respStr.substring(1,4);
        respData.setDevNo(getDevNo(addr));
        int startIdx = respStr.indexOf("_");
        int endIdx = respStr.indexOf(StrUtil.LF);
        if(endIdx==-1){
            endIdx = respStr.length();
        }
        String[] params = null;
        try{
            String str = respStr.substring(startIdx+2,endIdx);
            params = str.split(StrUtil.CR);
        }catch (Exception e){
            log.error("接口响应数据异常！源数据：{}",respStr);
            throw new BaseException("接口响应数据异常！");
        }
        List<FrameParaData> frameParaList = new ArrayList<>();
        for (String param : params) {
            String cmdMark = convertCmdMark(param.split("_")[0],respData.getCmdMark());
            //上下变频器信号命令标识有区别，这里做下转换
            if(cmdMark.equals("TX")){
                cmdMark = "RX";
            }
            String value = "";
            String[] values = param.split("_");
            if(values.length>1){
                value = values[1];
            }
            FrameParaData paraInfo = new FrameParaData();
            FrameParaInfo frameParaDetail = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(),cmdMark);
            BeanUtil.copyProperties(frameParaDetail, paraInfo, true);
            if(cmdMark.equals("POUT") && value.length()>1){
                value = value.substring(0,value.length()-3);
            }
            if(cmdMark.equals("POW") && value.length()>1){
                value = value.substring(0,value.length()-1);
            }
            paraInfo.setParaVal(value);
            paraInfo.setDevNo(getDevNo(addr));
            frameParaList.add(paraInfo);
        }
        respData.setFrameParaList(frameParaList);
        dataReciveService.paraQueryRecive(respData);
        return respData;
    }

    private String convertCmdMark(String cmdMark,String intfCmdMark){
        if(intfCmdMark.toUpperCase().equals(WORK_STATUS_CMD)){
            cmdMark = cmdMark+="-S";
        }
        return cmdMark;
    }

    /**
     * 获取变频器内部地址映射关系
     * @return
     */
    private Map<String, BaseInfo> getBPQAddrMap(){
        List<BaseInfo> baseInfos = new ArrayList<>();
        baseInfos.addAll(Optional.ofNullable(BaseInfoContainer.getDevInfosByType(SysConfigConstant.DEVICE_BPQ)).orElse(new ArrayList<>()));
        baseInfos.addAll(Optional.ofNullable(BaseInfoContainer.getDevInfosByType(SysConfigConstant.DEVICE_KAC_BPQ)).orElse(new ArrayList<>()));
        baseInfos.addAll(Optional.ofNullable(BaseInfoContainer.getDevInfosByType(SysConfigConstant.DEVICE_QHDY)).orElse(new ArrayList<>()));
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

}
