package com.xy.netdev.frame.service.impl;


import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.monitor.entity.ParaInfo;

import java.util.List;

/**
 * 39所Ku&L下变频器接口协议解析
 *
 * @author admin
 * @date 2021-03-05
 */
public class FreqConverterInterPrtcParser implements IQueryInterPrtclAnalysisService {

    @Override
    public void queryPara(BaseInfo devInfo, Interface interInfo) {

    }

    @Override
    public List<ParaInfo> queryParaResponse(BaseInfo devInfo, List<ParaInfo> paraList) {
        return null;
    }
}
