package com.xy.netdev.frame.service.czp;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.constant.MonitorConstants;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReciveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.xy.netdev.common.util.ByteUtils.byteToNumber;
import static com.xy.netdev.frame.service.gf.GfPrtcServiceImpl.isFloat;
import static com.xy.netdev.frame.service.gf.GfPrtcServiceImpl.isUnsigned;

/**
 * C中频切换矩阵
 *
 * @author sunchao
 * @date 2021-04-01
 */
@Slf4j
@Component
public class CzpPrtcServiceImpl  implements IParaPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private IDataReciveService dataReciveService;
    @Autowired
    private ISysParamService sysParamService;
    /**查询应答帧 分隔符*/
    private static final String SPLIT = "5f";

    /**
     * 查询设备参数
     * @param  reqInfo   请求参数信息
     */
    @Override
    public void queryPara(FrameReqData reqInfo) {
        log.info("C中频切换矩阵参数查询执行！");
        if(reqInfo.getFrameParaList() == null && reqInfo.getFrameParaList().isEmpty()){
            log.info("C中频切换矩阵无参数，全查询取消！");
            return ;
        }
        //此处需确认关键字是否为参数序号，是否需要设置参数的字节长度
        List<byte[]> bytes = reqInfo.getFrameParaList().stream()
                .filter(frameParaData -> StringUtils.isNotBlank(frameParaData.getParaNo()))
                .map(frameParaData->ByteUtils.objToBytes(frameParaData.getDevNo(),frameParaData.getLen()))
                .collect(Collectors.toList());
        reqInfo.setParamBytes(ByteUtils.listToBytes(bytes));
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    /**
     * 查询设备参数响应
     * @param  respData   数据传输对象
     * @return
     */
    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        String bytesData = HexUtil.encodeHexStr(respData.getParamBytes());
        String[] dataList = bytesData.toLowerCase().split(SPLIT.toLowerCase());
        String devType = respData.getDevType();
        //拆分后根据关键字获取参数
        List<FrameParaData> frameParaDataList = new ArrayList<>();
        for (String data : dataList) {
            String paraCmk = data.substring(0, 2);
            String paraValueStr = data.substring(2);
            byte[] paraValBytes = HexUtil.decodeHex(paraValueStr);
            FrameParaInfo currentPara = BaseInfoContainer.getParaInfoByCmd(devType, paraCmk);
            if (StringUtils.isEmpty(currentPara.getParaNo())){ continue;}
            FrameParaData frameParaData = FrameParaData.builder()
                    .devType(devType)
                    .devNo(respData.getDevNo())
                    .paraNo(currentPara.getParaNo())
                    .build();
            //根据是否为String类型采取不同的处理方式
            boolean isStr = MonitorConstants.STRING_CODE.equals(currentPara.getDataType());
            if (isStr){
                frameParaData.setParaVal(paraValueStr);
            }else {
                //单个参数值转换
                frameParaData.setParaVal(byteToNumber(paraValBytes, 0,
                        Integer.parseInt(currentPara.getParaByteLen())
                        ,isUnsigned(sysParamService, currentPara.getDataType())
                        ,isFloat(sysParamService, currentPara.getDataType())
                ).toString());
            }
            frameParaDataList.add(frameParaData);
        }
        respData.setFrameParaList(frameParaDataList);
        //参数查询响应结果接收
        dataReciveService.paraQueryRecive(respData);
        return respData;
    }

    /**
     * 设置设备参数
     * @param  reqInfo   请求参数信息
     */
    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        log.info("C中频切换矩阵查询设置设备参数执行！");
        if(reqInfo.getFrameParaList() == null && reqInfo.getFrameParaList().isEmpty()){
            log.info("C中频切换矩阵无参数，设置设备参数取消！");
            return ;
        }
        List<byte[]> list = new ArrayList<>();
        reqInfo.getFrameParaList().forEach(frameParaData->{
            FrameParaInfo paraInfoByNo = BaseInfoContainer.getParaInfoByNo(frameParaData.getDevType(), frameParaData.getParaNo());
            String dataBody = paraInfoByNo.getCmdMark() + frameParaData.getParaVal();
            list.add(HexUtil.decodeHex(dataBody));
        });
        reqInfo.setParamBytes(ByteUtils.listToBytes(list));
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    /**
     * 设置设备参数响应
     * @param  respData   数据传输对象
     * @return
     */
    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        log.info("C中频切换矩阵设置设备参数响应执行！");
        String data = HexUtil.encodeHexStr(respData.getParamBytes());
        String controlSuccessCode = sysParamService.getParaRemark1(SysConfigConstant.CONTROL_SUCCESS);
        String controlFailCode = sysParamService.getParaRemark1(SysConfigConstant.CONTROL_FAIL);
        if (data.contains(controlSuccessCode)) {
            respData.setRespCode(controlSuccessCode);
        } else if (data.contains(controlFailCode)) {
            respData.setRespCode(controlFailCode);
        } else {
            throw new IllegalStateException("调制解调器控制响应异常，数据字节：" + data);
        }
        return respData;
    }
}
