package com.xy.netdev.sendrecv.head;


import cn.hutool.core.util.HexUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.lpdSwitch.LpdSwitchInterPrtcServiceImpl;
import com.xy.netdev.frame.service.lpdSwitch.LpdSwitchPrtcServiceImpl;
import com.xy.netdev.monitor.entity.PrtclFormat;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import static com.xy.netdev.common.constant.SysConfigConstant.*;
import static com.xy.netdev.common.util.ByteUtils.*;
import static com.xy.netdev.common.util.ByteUtils.byteArrayCopy;

/**
 * L频段4x4开关矩阵
 *
 * @author sunchao
 * @create 2021-04-28 15:30
 */
@Service
@Slf4j
public class LpdSwitch extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> {

    @Autowired
    private LpdSwitchInterPrtcServiceImpl lpdSwitchInterPrtcService;
    @Autowired
    private LpdSwitchPrtcServiceImpl lpdSwitchPrtcService;

    /**查询/控制响应命令标识*/
    private static final String QUERY_RES = "3031";
    private static final String CONTROL_RES = "3032";

    @Override
    public void callback(FrameRespData frameRespData, IParaPrtclAnalysisService iParaPrtclAnalysisService, IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService, ICtrlInterPrtclAnalysisService ctrlInterPrtclAnalysisService) {
        switch (frameRespData.getOperType()) {
            case OPREATE_QUERY_RESP:  //查询响应
                lpdSwitchInterPrtcService.queryParaResponse(frameRespData);
                break;
            case OPREATE_CONTROL_RESP:  //控制响应
                lpdSwitchPrtcService.ctrlParaResponse(frameRespData);
                break;
            default:
                log.warn("设备{}命令{}未知L频段4x4开关矩阵响应类型...", frameRespData.getDevNo(), frameRespData.getCmdMark());
                break;
        }
    }

    @Override
    public FrameRespData unpack(SocketEntity socketEntity, FrameRespData frameRespData) {
        byte[] bytes = socketEntity.getBytes();
        if (bytes.length < 10) {
            log.warn("L频段4x4开关矩阵响应数据长度错误, 未能正确解析, 数据体长度:{}, 数据体:{}", bytes.length, HexUtil.encodeHexStr(bytes));
            return frameRespData;
        }
        //获取16进制命令字
        String hexRespType = HexUtil.encodeHexStr(ByteUtils.byteArrayCopy(bytes,3,2));
        //判断操作类型赋值
        if (QUERY_RES.equals(hexRespType)){
            frameRespData.setCmdMark(QUERY_RES);
            frameRespData.setOperType(OPREATE_QUERY_RESP);
        }else if (CONTROL_RES.equals(hexRespType)){
            frameRespData.setCmdMark(CONTROL_RES);
            frameRespData.setOperType(OPREATE_CONTROL_RESP);
        }
        //数据体
        byte[] paramBytes = byteArrayCopy(bytes, 7, bytes.length - 11);
        frameRespData.setParamBytes(paramBytes);
        return frameRespData;
    }

    @Override
    public byte[] pack(FrameReqData frameReqData) {
        if(frameReqData.getParamBytes() == null){
            frameReqData.setParamBytes(new byte[]{});
        }
        byte[] bytes = frameReqData.getParamBytes();
        //数据帧总长= 参数正文长度
        int frameLen = bytes.length;
        byte[] frameByte = new byte[frameLen+11];
        //起始字节:1
        System.arraycopy(new byte[]{0x3A}, 0, frameByte, 0, 1);
        List<byte[]> lists = new ArrayList<>();
        /**************(后续修改：确认后可设置到设备信息的备注1中)**************/
        //设备地址:2字节(暂时写定，后续确认再改)
        //地址字节：1字节（00-7F）
        lists.add(HexUtil.decodeHex(BaseInfoContainer.getDevInfoByNo(frameReqData.getDevNo()).getDevRemark1Data()));
        //获取操作关键字： 查询关键字/控制关键字
        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterfaceOrPara(frameReqData.getDevType(), frameReqData.getCmdMark());
        //默认为查询
        String keyWord = prtclFormat.getFmtSkey();
        //长度默认为3030
        byte[] lenByte = new byte[]{0x30,0x30};
        if (OPREATE_CONTROL.equals(frameReqData.getOperType())) {
            //控制
            keyWord = prtclFormat.getFmtCkey();
            lenByte = ByteUtils.objToBytes(frameLen, 2);
        }
        //命令类型：2字节
        lists.add(HexUtil.decodeHex(keyWord));
        //长度:2(参数体)
        lists.add(lenByte);
        //参数正文：2字节
        lists.add(frameReqData.getParamBytes());
        byte[] byteCheck = listToBytes(lists);
        System.arraycopy(byteCheck, 0, frameByte, 1, frameLen+6);
        /**************(后续修改)**************/
        //校验字：LRC校验
        String  str = getLRC(byteCheck);
        System.arraycopy(new byte[]{0x00,0x02}, 0, frameByte, frameLen+7, 2);
        //结束符 :1
        System.arraycopy(new byte[]{0x0D,0x0A}, 0, frameByte, frameLen+9, 2);
        return frameByte;
    }

    /*
     * 输入byte[] data , 返回LRC校验byte
     */
    private String getLRC(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int tmp = 0;
        for (int i = 0; i < data.length; i++) {
            tmp = tmp + (byte) data[i];
        }
        tmp = ~tmp;
        tmp = (tmp & (0xff));
        tmp += 1;
        String hexStr = HexUtil.encodeHexStr(new byte[]{(byte) tmp});
        for(int len = 0;len<hexStr.length();len++){
            sb.append(convertHexToString(hexStr.substring(len,len+1)));
        }
        return sb.toString();
    }

    /**
     * 将16进制转换为ASCII
     * @param hex
     * @return
     */
    public String convertHexToString(String hex){
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        for( int i=0; i<hex.length()-1; i+=2 ){
            String output = hex.substring(i, (i + 2));
            int decimal = Integer.parseInt(output, 16);
            sb.append((char)decimal);
            temp.append(decimal);
        }

        return sb.toString();
    }

}
