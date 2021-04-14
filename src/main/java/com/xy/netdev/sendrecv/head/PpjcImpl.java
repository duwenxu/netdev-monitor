package com.xy.netdev.sendrecv.head;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.ppjc.PpjcInterPrtcServiceImpl;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static com.xy.netdev.common.constant.SysConfigConstant.OPREATE_QUERY_RESP;
import static com.xy.netdev.common.util.ByteUtils.byteArrayCopy;
import static com.xy.netdev.common.util.ByteUtils.bytesToNum;

/**
 * 频谱监测设备
 */
@Service
@Slf4j
public class PpjcImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> {

    @Autowired
    private PpjcInterPrtcServiceImpl ppjcInterPrtcService;
    /**查询/控制响应命令标识*/
    private static final String QUERY_RES = "83";
    private static final String QUERY_CMD ="82";

    /**
     * 回滚
     *
     * @param frameRespData
     * @param iParaPrtclAnalysisService
     * @param iQueryInterPrtclAnalysisService
     * @param ctrlInterPrtclAnalysisService
     */
    @Override
    public void callback(FrameRespData frameRespData, IParaPrtclAnalysisService iParaPrtclAnalysisService, IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService, ICtrlInterPrtclAnalysisService ctrlInterPrtclAnalysisService) {
        switch (frameRespData.getOperType()) {
            case OPREATE_QUERY_RESP:  //查询响应
                ppjcInterPrtcService.queryParaResponse(frameRespData);
                break;
            default:
                log.warn("设备{}命令{}未知频谱监测设备响应类型...", frameRespData.getDevNo(), frameRespData.getCmdMark());
                break;
        }
    }

    /**
     * 拆包
     *
     * @param socketEntity
     * @param frameRespData
     * @return
     */
    @Override
    public FrameRespData unpack(SocketEntity socketEntity, FrameRespData frameRespData) {
        byte[] bytes = socketEntity.getBytes();
        if (bytes.length <= 5) {
            log.warn("频谱监测设备响应数据长度错误, 未能正确解析, 数据体长度:{}, 数据体:{}", bytes.length, HexUtil.encodeHexStr(bytes));
            return frameRespData;
        }
        //目前只有查询响应
        //获取16进制命令字
        String hexRespType = HexUtil.toHex(bytesToNum(bytes, 2, 1, ByteBuf::readUnsignedByte));
        //判断操作类型赋值
        if (QUERY_RES.equals(hexRespType)){
            frameRespData.setCmdMark(QUERY_CMD);
            frameRespData.setOperType(OPREATE_QUERY_RESP);
        }
        //数据体
        byte[] paramBytes = byteArrayCopy(bytes, 3, bytes.length - 5);
        frameRespData.setParamBytes(paramBytes);
        return frameRespData;
    }

    /**
     * 打包
     * 目前只有查询帧且帧协议固定
     * @param frameReqData
     * @return
     */
    @Override
    public byte[] pack(FrameReqData frameReqData) {
        byte[] frameByte = new byte[]{0x02,0x02, (byte) 0x82,0x00,0x0a};
        return frameByte;
    }
}
