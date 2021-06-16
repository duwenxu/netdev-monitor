package com.xy.netdev.frame.service.czp;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReciveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import static com.xy.netdev.common.constant.SysConfigConstant.*;

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
    private IDataReciveService dataReciveService;

    /**
     * 查询设备参数
     * @param  reqInfo   请求参数信息
     */
    @Override
    public void queryPara(FrameReqData reqInfo) {
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    /**
     * 查询设备参数响应
     * @param  respData   数据传输对象
     * @return
     */
    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        byte[] bytes = respData.getParamBytes();
        //全查询：按容器中的参数顺序解析
        String devType = respData.getDevType();
        List<FrameParaInfo> frameParaInfos = BaseInfoContainer.getInterLinkParaList(devType,respData.getCmdMark());
        List<FrameParaData> frameParaDataList = new ArrayList<>();
        for (FrameParaInfo frameParaInfo : frameParaInfos){
            //参数下标--->参数下标+参数字节长度+关键字（2）
            byte[] paraValBytes = ByteUtils.byteArrayCopy(bytes,frameParaInfo.getParaStartPoint(),Integer.valueOf(frameParaInfo.getParaByteLen()));
            if(PARA_COMPLEX_LEVEL_COMPOSE.equals(frameParaInfo.getCmplexLevel())){
                //子参数列表
                List<FrameParaInfo> subList = frameParaInfo.getSubParaList();
                for(int i=0;i<subList.size();i++){
                    FrameParaData frameParaData = genFramePara(subList.get(i),ByteUtils.byteArrayCopy(paraValBytes,i,1),respData.getDevNo());
                    frameParaDataList.add(frameParaData);
                }
            }
            frameParaDataList.add(genFramePara(frameParaInfo,paraValBytes,respData.getDevNo()));
        }
        respData.setFrameParaList(frameParaDataList);
        //接口查询响应结果接收
        dataReciveService.interfaceQueryRecive(respData);
        return respData;
    }

    /**
     * 生成FrameParaData
     * @param currentPara
     * @param paraValueByte
     * @return
     */
    private  FrameParaData genFramePara(FrameParaInfo currentPara,byte[] paraValueByte,String devNo){
        FrameParaData frameParaData = FrameParaData.builder()
                .devType(currentPara.getDevType())
                .paraNo(currentPara.getParaNo())
                .paraOrigByte(paraValueByte)
                .devNo(devNo)
                .paraVal(HexUtil.encodeHexStr(paraValueByte))
                .build();
        return frameParaData;
    }
}