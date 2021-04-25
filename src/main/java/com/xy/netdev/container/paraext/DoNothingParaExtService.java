package com.xy.netdev.container.paraext;


import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.monitor.bo.ParaViewInfo;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

/**
 * 参数缓存扩展
 *
 * @author tangxl
 * @date 2021-04-25
 */
@Service
public class DoNothingParaExtService implements IParaExtService{


    /**
     * 设置显示设备参数
     * @param devNo            设备编号
     */
    public void setCacheDevParaViewInfo(String devNo) {

    }

    /**
     * 获取设备显示参数
     * @param devNo            设备编号
     */
   public List<ParaViewInfo> getCacheDevParaViewInfo(String devNo) {
       return DevParaInfoContainer.getDevParaExtViewList(devNo) ;
   }
}
