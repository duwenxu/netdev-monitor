package com.xy.netdev.frame.entity;

import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.ParaInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 数据传输类
 * @author cc
 */
@Setter
@Getter
public class TransportEntity {

    private BaseInfo devInfo;

    private String paramMark;

    private byte[] paramBytes;
}
