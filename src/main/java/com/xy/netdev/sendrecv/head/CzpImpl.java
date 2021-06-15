package com.xy.netdev.sendrecv.head;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.czp.CzpInterPrtcServiceImpl;
import com.xy.netdev.frame.service.czp.CzpPrtcServiceImpl;
import com.xy.netdev.monitor.entity.PrtclFormat;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import static com.xy.netdev.common.constant.SysConfigConstant.*;
import static com.xy.netdev.common.util.ByteUtils.*;

/**
 * C中频切换矩阵
 */
@Service
@Slf4j
public class CzpImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> {

    @Autowired
    private CzpPrtcServiceImpl czpPrtcService;
    @Autowired
    private CzpInterPrtcServiceImpl czpInterPrtcService;
    /**查询/控制响应命令标识*/
    private static final String QUERY_RES = "83";
    private static final String CONTROL_RES = "81";
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
                czpInterPrtcService.queryParaResponse(frameRespData);
                break;
            case OPREATE_CONTROL_RESP:  //控制响应
                czpPrtcService.ctrlParaResponse(frameRespData);
                break;
            default:
                log.warn("设备{}命令{}未知C中频切换矩阵响应类型...", frameRespData.getDevNo(), frameRespData.getCmdMark());
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
        if (bytes.length < 10) {
            log.warn("C中频切换矩阵响应数据长度错误, 未能正确解析, 数据体长度:{}, 数据体:{}", bytes.length, HexUtil.encodeHexStr(bytes));
            return frameRespData;
        }
        //获取16进制命令字
        String hexRespType = HexUtil.encodeHexStr(ByteUtils.byteArrayCopy(bytes,7,1));
        //判断操作类型赋值
        if (QUERY_RES.equals(hexRespType)){
            frameRespData.setCmdMark(QUERY_CMD);
            frameRespData.setOperType(OPREATE_QUERY_RESP);
        }else if (CONTROL_RES.equals(hexRespType)){
            frameRespData.setOperType(OPREATE_CONTROL_RESP);
            //参数关键字
            Byte cmd = bytesToNum(bytes, 8, 1, ByteBuf::readByte);
            String hexCmd = numToHexStr(Long.valueOf(cmd));
            frameRespData.setCmdMark(hexCmd);
            frameRespData.setAccessType(ACCESS_TYPE_PARAM);
        }
        //数据体
        byte[] paramBytes = byteArrayCopy(bytes, 8, bytes.length - 10);
        frameRespData.setParamBytes(paramBytes);
        return frameRespData;
    }

    /**
     * 打包
     *
     * @param frameReqData
     * @return
     */
    @Override
    public byte[] pack(FrameReqData frameReqData) {
        if(frameReqData.getParamBytes() == null){
            frameReqData.setParamBytes(new byte[]{});
        }
        byte[] bytes = frameReqData.getParamBytes();
        //c中频切换矩阵数据帧总长= 参数体长度 + 固定字节长度10
        int frameLen = bytes.length + 10;
        //获取操作关键字： 查询关键字/控制关键字
        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterfaceOrPara(frameReqData.getDevType(), frameReqData.getCmdMark());
        //默认为查询
        String keyWord = prtclFormat.getFmtSkey();
        if (OPREATE_CONTROL.equals(frameReqData.getOperType())) {
            //控制
            keyWord = prtclFormat.getFmtCkey();
        }
        byte[] frameByte = new byte[frameLen];
        //起始字节:1
        System.arraycopy(new byte[]{0x7E}, 0, frameByte, 0, 1);
        //设备类型:1
        System.arraycopy(new byte[]{0x29}, 0, frameByte, 1, 1);
        //设备型号:1
        /*String devSubType = BaseInfoContainer.getDevInfoByNo(frameReqData.getDevNo()).getDevSubType();*/
        System.arraycopy(new byte[]{0x16}, 0, frameByte, 2, 1);
        List<byte[]> lists = new ArrayList<>();
        //设备地址:2
        lists.add(HexUtil.decodeHex(BaseInfoContainer.getDevInfoByNo(frameReqData.getDevNo()).getDevRemark1Data()));
        //长度:2   命令字+参数体
        lists.add(ByteUtils.objToBytes(frameLen-9, 2));
        //命令字 :1
        lists.add(new byte[]{(byte) Integer.parseInt(keyWord, 16)});
        //参数体 :n
        lists.add(frameReqData.getParamBytes());
        byte[] byteCheck = listToBytes(lists);
        System.arraycopy(byteCheck, 0, frameByte, 3, frameLen-5);
        //校验字 :1
        byte check = ByteUtils.addGetBottom(byteCheck,0,byteCheck.length);
        System.arraycopy(new byte[]{check}, 0, frameByte, frameLen-2, 1);
        //结束符 :1
        System.arraycopy(new byte[]{0x7E}, 0, frameByte, frameLen-1, 1);
        return frameByte;
    }
}
