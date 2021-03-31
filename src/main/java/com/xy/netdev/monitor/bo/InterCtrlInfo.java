package com.xy.netdev.monitor.bo;

import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.monitor.entity.ParaInfo;
import lombok.Data;

import java.util.List;

/**
 * @author luo
 * @date 2021/3/29
 */

@Data
public class InterCtrlInfo {

    /**
     * 设备编号
     */
    private String devNo;

    /**
     * 接口Id
     */
    private String cmdMark;

    /**
     * 参数列表
     */
    List<FrameParaData> paraInfos;

}
