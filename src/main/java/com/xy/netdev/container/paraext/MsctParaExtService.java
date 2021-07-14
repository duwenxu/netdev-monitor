package com.xy.netdev.container.paraext;

import com.xy.netdev.common.util.ParaHandlerUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.container.DevCtrlInterInfoContainer;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.bo.InterfaceViewInfo;
import com.xy.netdev.monitor.bo.ParaViewInfo;
import com.xy.netdev.monitor.constant.MonitorConstants;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.transit.IDevCmdSendService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author luo
 * @date 2021/4/25
 */
@Service
public class MsctParaExtService implements IParaExtService {


    @Autowired
    private IDevCmdSendService devCmdSendService;
    private Map<String,Map<String,List<ParaViewInfo>>> devModeMap = new HashMap<>();
    //当前工作模式查询接口名来字
    public static final String CURRENT_MODE_CMD = "8205AA";

    @Override
    public void setCacheDevParaViewInfo(String devNo) {
        BaseInfo devInfo = BaseInfoContainer.getDevInfoByNo(devNo);
        Interface intf = BaseInfoContainer.getInterLinkInterface(devInfo.getDevType(),CURRENT_MODE_CMD);
        if(intf.getItfId()!=null) {
            devCmdSendService.interfaceQuerySend(devNo, CURRENT_MODE_CMD);
            initModeParaMap(devNo);
        }
    }

    @Override
    public List<ParaViewInfo> getCacheDevParaViewInfo(String devNo) {
        String mode = getDevCurrMode(devNo);
        List<ParaViewInfo> paras = new ArrayList<>();
        paras.addAll(devModeMap.get(devNo).get("05"));
        paras.addAll(devModeMap.get(devNo).get(mode));
        return paras;
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
    private void  initModeParaMap(String devNo){
        if(!devModeMap.containsKey(devNo)){
            Map<String,List<ParaViewInfo>> modeMap = new HashMap<>();
            devModeMap.put(devNo,modeMap);
        }
        BaseInfo baseInfo = BaseInfoContainer.getDevInfoByNo(devNo);
        List<Interface> interfaces = BaseInfoContainer.getInterfacesByDevType(baseInfo.getDevType());
        Map<String, Set<String>> modeParas = new HashMap<>();
        for (Interface anInterface : interfaces) {
            String cmdMark = anInterface.getItfCmdMark();
            String itfType = anInterface.getItfType();
            String mode = "";
            mode = cmdMark.substring(0,2);
            if(itfType.equals(MonitorConstants.SINGLE_QUERY)){
                mode = cmdMark.substring(0,2);
            }
            if(itfType.equals(MonitorConstants.SUB_QUERY) && anInterface.getItfCmdMark().length()==6){
                mode = cmdMark.substring(2,4);
            }
            String format = anInterface.getItfDataFormat();
            if(format.length()>0){
                String[] paras = format.split(",");
                for (int i = 0; i < paras.length; i++) {
                    if(modeParas.containsKey(mode)){
                        modeParas.get(mode).add(paras[i]);
                    }else{
                        Set<String> paraSet = new HashSet<>();
                        paraSet.add(paras[i]);
                        modeParas.put(mode,paraSet);
                    }
                }
            }
        }
        List<ParaViewInfo> paras = DevParaInfoContainer.getDevParaExtViewList(devNo);
        for (Map.Entry<String, Set<String>> paraSet : modeParas.entrySet()) {
            String key = paraSet.getKey();
            Set<String> val = paraSet.getValue();
            for (ParaViewInfo para : paras) {
                for (String s : val) {
                    if(para.getParaId()==Integer.parseInt(s)){
                            if(devModeMap.get(devNo).containsKey(key)){
                                devModeMap.get(devNo).get(key).add(para);
                            }else{
                                List<ParaViewInfo> paraList = new ArrayList<>();
                                paraList.add(para);
                                devModeMap.get(devNo).put(key,paraList);
                            }
                    }
                }
            }
        }
    }
}
