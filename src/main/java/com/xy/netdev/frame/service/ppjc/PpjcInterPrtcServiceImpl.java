package com.xy.netdev.frame.service.ppjc;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReceiveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.xy.netdev.common.util.ByteUtils.byteToNumber;

/**
 * 频谱监测设备
 *
 * @author sunchao
 * @create 2021-04-08 16:10
 */
@Service
@Slf4j
public class PpjcInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private ISysParamService sysParamService;
    @Autowired
    private IDataReceiveService dataReciveService;
    //频谱监测设备协议分隔符
    private static final String separator = "2c";

    /**
     * 查询设备参数
     * @param  reqInfo   请求参数信息
     */
    @Override
    public void queryPara(FrameReqData reqInfo) {
        log.debug("频谱监测设备参数查询执行！");
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    /**
     * 查询设备参数响应   查询协议解析数据
     * @param  respData   数据传输对象
     * @return
     */
    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
//        拿到协议层数据交互体，按照频谱监测设备协议分隔符“2c”切割后，将string数组转化为16进制字符串赋值给string数组bytesDate   去数据交互体中的“2c”
        String[] bytesData = HexUtil.encodeHexStr(respData.getParamBytes()).split(separator);
        //全查询：按容器中的参数顺序解析
        String devType = respData.getDevType();//获取设备类型
//        将设备类型和命令标识存储进协议响应数据列表frameParaInfos中
        List<FrameParaInfo> frameParaInfos = BaseInfoContainer.getInterLinkParaList(devType,respData.getCmdMark());
        List<FrameParaData> frameParaDataList = new ArrayList<>();

        for (FrameParaInfo frameParaInfo : frameParaInfos){
            //参数下标--->参数下标+参数字节长度+关键字（2）
            String data = bytesData[frameParaInfo.getParaSeq()-1];//getParaSeq获取参数序号
            String paraCmk = data.substring(0, 2);//paraCmk获取字符串前两位
            String paraValueStr = data.substring(2);//paraValueStr获取字符串两位后的所有
            byte[] paraValBytes = HexUtil.decodeHex(paraValueStr);//将16进制字符串转化为字节数组
            FrameParaInfo currentPara = BaseInfoContainer.getParaInfoByCmd(devType, paraCmk);//根据设备类型和命令标识获取参数信息
            if (StringUtils.isEmpty(currentPara.getParaNo())){ continue;}//如果拿到的参数编号为空  进入下一次循环
            FrameParaData frameParaData = FrameParaData.builder()// 为数据帧参数数据赋值
                    .devType(devType)
                    .devNo(respData.getDevNo())
                    .paraNo(currentPara.getParaNo())
                    .paraOrigByte(paraValBytes)
                    .build();
            frameParaData.setParaVal(paraValueStr);
            frameParaDataList.add(frameParaData);
        }
        respData.setFrameParaList(frameParaDataList);//设置帧参数列表
        //接口查询响应结果接收
        dataReciveService.interfaceQueryRecive(respData);
        return respData;//返回处理过的帧参数
    }
}