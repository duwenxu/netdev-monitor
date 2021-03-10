package com.xy.netdev.frame.entity;

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

    private List<ParaInfo> dataBodyParas;

    public static<T extends TransportEntity> T setList(List<ParaInfo> dataBodyParas){
        TransportEntity transportEntity = new TransportEntity();
        transportEntity.setDataBodyParas(dataBodyParas);
        return (T)transportEntity;
    }
}
