package com.xy.netdev.frame.serivce.impl;

import com.xy.netdev.common.util.BeanFactoryUtil;
import com.xy.netdev.frame.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.bo.DataBodyPara;
import com.xy.netdev.frame.serivce.SocketMutualService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据交互实现类
 * @author cc
 */
@Service
public class SocketMutualServiceImpl implements SocketMutualService {


    @Override
    public <T extends DataBodyPara> void request(T t) {
        AbsDeviceSocketHandler<T> socketHandler = BeanFactoryUtil.getBean("");
        socketHandler.request(t);
    }

    @Override
    public <T extends DataBodyPara> void callBack(List<T> list) {

    }
}
