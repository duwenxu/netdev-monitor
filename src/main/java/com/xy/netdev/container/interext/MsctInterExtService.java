package com.xy.netdev.container.interext;

import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.container.DevCtrlInterInfoContainer;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.bo.InterfaceViewInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.transit.IDevCmdSendService;
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
    private Map<String,List<InterfaceViewInfo>> modeMap = new HashMap<>();

    @Override
    public void setCacheDevInterViewInfo(String devNo) {
        devCmdSendService.paraQuerySend(devNo,"AA");
        initModeParaMap(devNo);
    }

    @Override
    public List<InterfaceViewInfo> getCacheDevInterViewInfo(String devNo) {
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
        List<InterfaceViewInfo> intfs = DevCtrlInterInfoContainer.getDevCtrInterExtList(devNo);
        for (InterfaceViewInfo intf : intfs) {
            String cmdMark = intf.getItfCmdMark();
            String mode = cmdMark.substring(0,2);
            if(mode.equals("80")){
                mode = cmdMark.substring(2,4);
                if(modeMap.containsKey(mode)){
                    modeMap.get(mode).add(intf);
                }else{
                    List<InterfaceViewInfo> modeList = new ArrayList<>();
                    modeList.add(intf);
                }
            }
        }
    }

}
