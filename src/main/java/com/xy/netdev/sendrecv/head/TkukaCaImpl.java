package com.xy.netdev.sendrecv.head;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.shipAcu.ShipAcuInterPrtcServiceImpl;
import com.xy.netdev.frame.service.shipAcu.ShipAcuPrtcServiceImpl;
import com.xy.netdev.frame.service.tkuka.TkukaCaPrtcServiceImpl;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import com.xy.netdev.sendrecv.entity.device.ModemScmmEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static com.xy.netdev.common.constant.SysConfigConstant.*;
import static com.xy.netdev.common.util.ByteUtils.byteArrayCopy;
import static com.xy.netdev.common.util.ByteUtils.listToBytes;

/**
 * TKuka0.9CA监控设备
 */
@Service
@Slf4j
public class TkukaCaImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> {

    @Autowired
    private TkukaCaPrtcServiceImpl tkukaCaPrtcService;

    /**帧头**/
    private static final String FRAME_HEAD = "7b";
    private static final String FRAME_END = "7d";
    /**帧尾**/

    @Override
    public void callback(FrameRespData frameRespData, IParaPrtclAnalysisService iParaPrtclAnalysisService, IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService, ICtrlInterPrtclAnalysisService ctrlInterPrtclAnalysisService) {
        switch (frameRespData.getOperType()) {
            case OPREATE_QUERY_RESP:  //查询响应
                tkukaCaPrtcService.queryParaResponse(frameRespData);
                break;
            default:
                log.warn("设备{}命令{}未知TKuka0.9CA监控设备响应类型...", frameRespData.getDevNo(), frameRespData.getCmdMark());
                break;
        }
    }

    /**
     * 目前拆包只有设备状态包
     * @param socketEntity
     * @param frameRespData
     * @return
     */
    @Override
    public FrameRespData unpack(SocketEntity socketEntity, FrameRespData frameRespData) {
        byte[] bytes = socketEntity.getBytes();
        int len = bytes.length;
        if (len != 232) {
            log.warn("TKuka0.9CA监控设备响应数据长度错误, 未能正确解析, 数据体长度:{}, 数据体:{}", bytes.length, HexUtil.encodeHexStr(bytes));
            return frameRespData;
        }
        //获取文件头与文件尾（目前此协议只能通过协议帧头尾来区分）
        String headFrame = HexUtil.encodeHexStr(ByteUtils.byteArrayCopy(bytes,0,1));  //帧头
        String endFrame = HexUtil.encodeHexStr(ByteUtils.byteArrayCopy(bytes,len-1,1));  //帧尾
        //判断操作类型赋值
        if (FRAME_HEAD.equals(headFrame) && FRAME_END.equals(endFrame)){
            frameRespData.setCmdMark(FRAME_HEAD);
            frameRespData.setOperType(OPREATE_QUERY_RESP);
            frameRespData.setAccessType(ACCESS_TYPE_INTERF);
        }else{
            log.warn("TKuka0.9CA监控设备响应数据帧头帧尾错误, 未能正确解析, 数据体长度:{}, 数据体:{}", bytes.length, HexUtil.encodeHexStr(bytes));
            return frameRespData;
        }
        //数据体
        byte[] paramBytes = byteArrayCopy(bytes, 4, bytes.length - 7);
        frameRespData.setParamBytes(paramBytes);
        return frameRespData;
    }

    /**
     * 目前打包只有控制包
     * @param frameReqData
     * @return
     */
    @Override
    public byte[] pack(FrameReqData frameReqData) {
        byte[] bytes = frameReqData.getParamBytes();
        if(bytes.length != 114){
            log.warn("TKuka0.9CA监控设备数据帧长度错误, 数据体长度:{}, 数据体:{}，请检查!!", bytes.length, HexUtil.encodeHexStr(bytes));
            return new byte[]{};
        }
        List<byte[]> lists = new ArrayList<>();
        //数据帧总长= 参数正文长度
        //帧头:1
        lists.add(new byte[]{0x7b});
        //标志位
        System.arraycopy(new byte[]{0x05},0,bytes,1,1);
        lists.add(bytes);
        //帧尾
        lists.add(new byte[]{0x7d});
        return ByteUtils.listToBytes(lists);
    }
}
