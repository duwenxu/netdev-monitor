package com.xy.netdev.frame.service.czp;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.constant.MonitorConstants;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReciveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import static com.xy.netdev.common.constant.SysConfigConstant.*;
import static com.xy.netdev.common.util.ByteUtils.byteToNumber;
import static com.xy.netdev.frame.service.gf.GfPrtcServiceImpl.isFloat;
import static com.xy.netdev.frame.service.gf.GfPrtcServiceImpl.isUnsigned;

/**
 * C中频切换矩阵
 *
 * @author sunchao
 * @create 2021-04-07 09:30
 */
@Service
@Slf4j
public class CzpInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private ISysParamService sysParamService;
    @Autowired
    private IDataReciveService dataReciveService;

    /**
     * 查询设备参数
     * @param  reqInfo   请求参数信息
     */
    @Override
    public void queryPara(FrameReqData reqInfo) {
        log.debug("C中频切换矩阵参数查询执行,接收到原始数据：["+reqInfo.getSendOriginalData()+"]");
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    /**
     * 查询设备参数响应
     * @param  respData   数据传输对象
     * @return
     */
    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        log.info("C中频切换矩阵参数查询响应执行,接收到原始数据：["+respData.getReciveOriginalData()+"]");
        byte[] bytes = respData.getParamBytes();
        String bytesData = HexUtil.encodeHexStr(respData.getParamBytes());
        //全查询：按容器中的参数顺序解析
        String devType = respData.getDevType();
        List<FrameParaInfo> frameParaInfos = BaseInfoContainer.getInterLinkParaList(devType,respData.getCmdMark());
        List<FrameParaData> frameParaDataList = new ArrayList<>();
        for (FrameParaInfo frameParaInfo : frameParaInfos){
            //参数下标--->参数下标+参数字节长度+关键字（2）
            byte[] paraValBytes = ByteUtils.byteArrayCopy(bytes,frameParaInfo.getParaStartPoint(),Integer.valueOf(frameParaInfo.getParaByteLen()));
            String data = HexUtil.encodeHexStr(paraValBytes);
            String paraCmk = data.substring(0, 2);
            StringBuffer paraValueStr = new StringBuffer();
            paraValueStr.append(data.substring(2));
            if(PARA_COMPLEX_LEVEL_COMPOSE.equals(frameParaInfo.getCmplexLevel())){
                //当为复杂参数，则按照数据库配置的样式进行处理
                paraValueStr.insert(2,"_");
            }
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
                frameParaData.setParaVal(paraValueStr.toString());
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
        //接口查询响应结果接收
        dataReciveService.interfaceQueryRecive(respData);
        return respData;
    }
}