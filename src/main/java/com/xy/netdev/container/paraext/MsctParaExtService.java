package com.xy.netdev.container.paraext;

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
    private Map<String,List<ParaViewInfo>> modeMap = new HashMap<>();

    @Override
    public void setCacheDevParaViewInfo(String devNo) {
        devCmdSendService.paraQuerySend(devNo,"AA");
        initModeParaMap(devNo);
    }

    @Override
    public List<ParaViewInfo> getCacheDevParaViewInfo(String devNo) {
        String mode = getDevCurrMode(devNo);
        return modeMap.get(mode);
    }

    /**
     * 获取设备当前工作模式
     * @return
     */
    private String getDevCurrMode(String devNo){
        BaseInfo baseInfo = BaseInfoContainer.getDevInfoByNo(devNo);
        FrameParaInfo frameParaInfo = BaseInfoContainer.getParaInfoByCmd(baseInfo.getDevType(),"AA");
        String mode = frameParaInfo.getParaVal();
        return mode;
    }

    /**
     * 初始化模式和接口对应关系
     * @param devNo
     */
    private void  initModeParaMap(String devNo){
        BaseInfo baseInfo = BaseInfoContainer.getDevInfoByNo(devNo);
        List<Interface> interfaces = BaseInfoContainer.getInterfacesByDevType(baseInfo.getDevType());
        Map<String, Set<String>> modeParas = new HashMap<>();
        for (Interface anInterface : interfaces) {
            String cmdMark = anInterface.getItfCmdMark();
            String itfType = anInterface.getItfType();
            String mode = "";
            if(itfType.equals(MonitorConstants.SINGLE_QUERY) || itfType.equals(MonitorConstants.SUB_QUERY)){
                mode = cmdMark.substring(0,2);
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
        }
        List<ParaViewInfo> paras = DevParaInfoContainer.getDevParaExtViewList(devNo);
        for (ParaViewInfo para : paras) {
            Integer paraId = para.getParaId();
            for (Map.Entry<String, Set<String>> paraSet : modeParas.entrySet()) {
                String key = paraSet.getKey();
                if(modeMap.containsKey(key)){
                    modeMap.get(key).add(para);
                }else{
                    List<ParaViewInfo> paraList = new ArrayList<>();
                    paraList.add(para);
                    modeMap.put(key,paraList);
                }
            }
        }
    }
}