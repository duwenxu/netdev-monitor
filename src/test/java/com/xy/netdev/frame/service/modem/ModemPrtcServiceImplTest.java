package com.xy.netdev.frame.service.modem;

import com.xy.netdev.NetdevApplicationTest;
import com.xy.netdev.frame.bo.FrameReqData;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
*
* @Description: 调制解调器请求外发TEST
* @Date 9:31 2021/3/17
* @Author duwx
*/

@Slf4j
public class ModemPrtcServiceImplTest extends NetdevApplicationTest {

    @Autowired
    private ModemPrtcServiceImpl modemPrtcService;

    @Test
    public void testCtrlPara() {
        //测试数据 ODU参考 04H   80H:开/81H:关
        FrameReqData frameReqData = FrameReqData.builder()
                .cmdMark("04")
                .devType("0020008")
                .accessType("0025001")
                .operType("0026003")
                .build();
        modemPrtcService.ctrlPara(frameReqData);
    }
}