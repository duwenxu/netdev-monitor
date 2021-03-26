package com.xy.netdev.sendrecv;

import com.xy.netdev.common.util.BeanFactoryUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.SocketMutualService;
import org.springframework.stereotype.Service;

/**
 * 数据交互实现类
 * @author cc
 */
@Service
public class SocketMutualServiceImpl implements SocketMutualService {

    @Override
    public void request(FrameReqData frameReqData, ProtocolRequestEnum requestEnum) {
        String classByDevType = BaseInfoContainer.getClassByDevType(frameReqData.getDevType());
        AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> socketHandler = BeanFactoryUtil.getBean(classByDevType);
        socketHandler.socketRequest(frameReqData, requestEnum);
    }
}
