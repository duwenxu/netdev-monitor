package com.xy.netdev.frame.serivce.impl.device;

import com.xy.netdev.frame.bo.DataBodyPara;
import com.xy.netdev.frame.base.AbsDeviceSocketHandler;
import org.mockito.internal.util.collections.Sets;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 2.4米卫通天线控制
 * @author cc
 */
@Service
public class AntennaControlImpl extends AbsDeviceSocketHandler<DataBodyPara> {

    @Override
    public Set<String> queryMark() {
        return Sets.newSet("10");
    }

    @Override
    public Set<String> queryResultMark() {
        return Sets.newSet("10");
    }

    @Override
    public void doQuery() {
        super.doQuery();
    }

}
