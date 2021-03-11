package com.xy.netdev.frame.service.impl;

import com.xy.netdev.common.util.BeanFactoryUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.entity.PrtclFormat;
import org.springframework.stereotype.Service;

/**
 * 数据交互实现类
 * @author cc
 */
@Service
public class SocketMutualServiceImpl implements SocketMutualService {

    @Override
    public void request(FrameReqData frameReqData, ProtocolRequestEnum requestEnum) {
        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterfaceOrPara(frameReqData.getDevType(), frameReqData.getCmdMark());
        AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> socketHandler =
                BeanFactoryUtil.getBean(prtclFormat.getFmtHandlerClass());
        socketHandler.socketRequest(frameReqData, requestEnum);
    }
}
