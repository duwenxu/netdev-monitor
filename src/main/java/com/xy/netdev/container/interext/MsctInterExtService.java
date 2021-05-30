package com.xy.netdev.container.interext;

import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.container.DevCtrlInterInfoContainer;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.frame.service.msct.MsctInterPrtcServiceImpl;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.bo.InterfaceViewInfo;
import com.xy.netdev.monitor.bo.ParaViewInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.transit.IDevCmdSendService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author luo
 * @date 2021/4/25
 */
@Service
public class MsctInterExtService implements InterExtService {


    @Autowired
    private IDevCmdSendService devCmdSendService;
    @Autowired
    private MsctInterPrtcServiceImpl msctInterPrtcService;
    private Map<String,Map<String,List<InterfaceViewInfo>>> devModeMap = new HashMap<>();
    //当前工作模式查询接口名来字
    public static final String CURRENT_MODE_CMD = "8205AA";


    @Override
    public void setCacheDevInterViewInfo(String devNo) {
        Interface intf = BaseInfoContainer.getInterLinkInterface(devNo,CURRENT_MODE_CMD);
        if(intf.getItfId()!=null){
            devCmdSendService.interfaceQuerySend(devNo,CURRENT_MODE_CMD);
            initModeIntfMap(devNo);
        }
    }

    @Override
    public List<InterfaceViewInfo> getCacheDevInterViewInfo(String devNo) {
        String mode = getDevCurrMode(devNo);
        List<InterfaceViewInfo> intfs = new ArrayList<>();
        intfs.addAll(devModeMap.get(devNo).get("AA"));
        intfs.addAll(devModeMap.get(devNo).get(mode));
        return intfs;
    }

    /**
     * 获取设备当前工作模式
     * @return
     */
    private String getDevCurrMode(String devNo){

        BaseInfo baseInfo = BaseInfoContainer.getDevInfoByNo(devNo);
        FrameParaInfo frameParaInfo = BaseInfoContainer.getParaInfoByCmd(baseInfo.getDevType(),"AA");
        ParaViewInfo paraViewInfo = DevParaInfoContainer.getDevParaView(devNo,frameParaInfo.getParaNo());
        String mode = paraViewInfo.getParaVal();
        if(StringUtils.isEmpty(mode)){
            mode = "00";
        }
        return mode;
    }

    /**
     * 初始化模式和接口对应关系
     * @param devNo
     */
    private void  initModeIntfMap(String devNo){
        if(!devModeMap.containsKey(devNo)){
            Map<String,List<InterfaceViewInfo>> modeMap = new HashMap<>();
            devModeMap.put(devNo,modeMap);
        }
        List<InterfaceViewInfo> intfs = DevCtrlInterInfoContainer.getDevCtrInterExtList(devNo);
        for (InterfaceViewInfo intf : intfs) {
            String cmdMark = intf.getItfCmdMark();
            String mode = cmdMark.substring(0,2);
            if(mode.equals("80")){
                mode = cmdMark.substring(2,4);
                if(devModeMap.get(devNo).containsKey(mode)){
                    devModeMap.get(devNo).get(mode).add(intf);
                }else{
                    List<InterfaceViewInfo> intfList = new ArrayList<>();
                    intfList.add(intf);
                    devModeMap.get(devNo).put(mode,intfList);
                }
            }
        }
    }
}
