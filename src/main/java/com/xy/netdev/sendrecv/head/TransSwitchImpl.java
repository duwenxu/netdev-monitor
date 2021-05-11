package com.xy.netdev.sendrecv.head;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.transSwitch.TransSwitchInterPrtcServiceImpl;
import com.xy.netdev.frame.service.transSwitch.TransSwitchPrtcServiceImpl;
import com.xy.netdev.monitor.entity.PrtclFormat;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.xy.netdev.common.constant.SysConfigConstant.*;
import static com.xy.netdev.common.util.ByteUtils.*;
import static com.xy.netdev.common.util.ByteUtils.listToBytes;

/**
 * 1:1 转换开关
 * @Author sunchao
 * @time 2021-05-10
 */
@Service
@Slf4j
public class TransSwitchImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> {

    @Autowired
    private TransSwitchPrtcServiceImpl transSwitchPrtcService;
    @Autowired
    private TransSwitchInterPrtcServiceImpl transSwitchInterPrtcService;

    /**查询/控制响应命令标识*/
    private static final String QUERY_RES = "53";
    private static final String QUERY = "13";
    private static final String CONTROL_RES = "41";

    @Override
    public void callback(FrameRespData frameRespData, IParaPrtclAnalysisService iParaPrtclAnalysisService, IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService, ICtrlInterPrtclAnalysisService ctrlInterPrtclAnalysisService) {
        switch (frameRespData.getOperType()) {
            case OPREATE_QUERY_RESP:  //查询响应
                transSwitchInterPrtcService.queryParaResponse(frameRespData);
                break;
            case OPREATE_CONTROL_RESP:  //控制响应
                transSwitchPrtcService.ctrlParaResponse(frameRespData);
                break;
            default:
                log.warn("设备{}命令{}未知1:1 转换开关响应类型...", frameRespData.getDevNo(), frameRespData.getCmdMark());
                break;
        }
    }

    @Override
    public FrameRespData unpack(SocketEntity socketEntity, FrameRespData frameRespData) {
        byte[] bytes = socketEntity.getBytes();
        if (bytes.length < 7) {
            log.warn("1:1 转换开关响应数据长度错误, 未能正确解析, 数据体长度:{}, 数据体:{}", bytes.length, HexUtil.encodeHexStr(bytes));
            return frameRespData;
        }
        //获取16进制命令字
        String hexRespType = HexUtil.encodeHexStr(ByteUtils.byteArrayCopy(bytes,4,1));
        //判断操作类型赋值
        if (QUERY_RES.equals(hexRespType)){
            frameRespData.setCmdMark(QUERY);
            frameRespData.setOperType(OPREATE_QUERY_RESP);
            frameRespData.setAccessType(ACCESS_TYPE_INTERF);
        }else if (CONTROL_RES.equals(hexRespType)){
            frameRespData.setOperType(OPREATE_CONTROL_RESP);
            //参数关键字
            String hexCmd = HexUtil.encodeHexStr(ByteUtils.byteArrayCopy(bytes,5,1));
            frameRespData.setCmdMark(hexCmd);
            frameRespData.setAccessType(ACCESS_TYPE_PARAM);
        }
        //数据体
        byte[] paramBytes = byteArrayCopy(bytes, 5, bytes.length - 7);
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
        if (frameReqData.getParamBytes() == null) {
            frameReqData.setParamBytes(new byte[]{});
        }
        byte[] bytes = frameReqData.getParamBytes();
        List<byte[]> lists = new ArrayList<>();
        //数据帧总长= 参数正文长度
        int frameLen = bytes.length;
        byte[] frameByte = new byte[frameLen + 7];
        //起始字节:1
        System.arraycopy(new byte[]{0x02}, 0, frameByte, 0, 1);
        //长度:1
        lists.add(ByteUtils.objToBytes(frameLen + 4, 1));
        //类型字节：固定值10H  1
        lists.add(new byte[]{0x10});
        //地址字节：1字节（00-7F）
        lists.add(HexUtil.decodeHex(BaseInfoContainer.getDevInfoByNo(frameReqData.getDevNo()).getDevRemark1Data()));
        //获取操作关键字： 查询关键字/控制关键字
        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterfaceOrPara(frameReqData.getDevType(), frameReqData.getCmdMark());
        //默认为查询
        String keyWord = prtclFormat.getFmtSkey();
        if (OPREATE_CONTROL.equals(frameReqData.getOperType())) {
            //控制
            keyWord = prtclFormat.getFmtCkey();
        }
        //命令类型：1字节
        lists.add(HexUtil.decodeHex(keyWord));
        //参数正文
        lists.add(frameReqData.getParamBytes());
        byte[] byteCheck = listToBytes(lists);
        System.arraycopy(byteCheck, 0, frameByte, 1, frameLen + 4);
        //校验字节 1
        System.arraycopy(new byte[]{addGetBottom256(byteCheck,0,1)}, 0, frameByte, frameLen + 5, 1);
        //结束符 :1
        System.arraycopy(new byte[]{0x0A}, 0, frameByte, frameLen + 6, 1);
        return frameByte;
    }

    /**
     * 校验和  规则：字节累加和模256
     * @param bytes 原始数组
     * @param offset 起始位
     * @param len 长度
     * @return 低位
     */
    public static byte addGetBottom256(byte[] bytes, int offset, int len) {
        byte[] arrayCopy = byteArrayCopy(bytes, offset, len);
        int sum = 0;
        for (byte b : Objects.requireNonNull(arrayCopy)) {
            sum += (b & 0xFF);
        }
        return (byte) Double.valueOf(sum % 256).intValue();
    }
}
