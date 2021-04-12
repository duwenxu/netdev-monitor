package com.xy.netdev.frame.service.modem;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.HexUtil;
import com.alibaba.fastjson.JSON;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.util.BeanFactoryUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.ExtParamConf;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.ParamCodec;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.frame.service.codec.DirectParamCodec;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReciveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 650调制解调器接口查询实现
 *
 * @author duwenxu
 * @create 2021-04-09 9:36
 */
@Service
@Slf4j
public class ModemInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    ISysParamService sysParamService;
    @Autowired
    private IDataReciveService dataReciveService;
    /**查询应答帧 分隔符*/
    private static final String SPLIT = "5f";

    @Override
    public void queryPara(FrameReqData reqInfo) {
        //暂时是单个参数查询 cmdMark为单个参数的命令标识
        byte[] bytes = HexUtil.decodeHex(reqInfo.getCmdMark());
        reqInfo.setParamBytes(bytes);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

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
            //转换为当前字节
            byte[] paraValBytes = HexUtil.decodeHex(paraValueStr);
            FrameParaInfo currentPara = BaseInfoContainer.getParaInfoByCmd(devType, paraCmk);
            if (StringUtils.isEmpty(currentPara.getParaNo())){ continue;}
            FrameParaData paraInfo = new FrameParaData();
            BeanUtil.copyProperties(currentPara, paraInfo, true);
            BeanUtil.copyProperties(respData, paraInfo, true);

            //获取参数解析配置信息
            String confClass = currentPara.getNdpaRemark2Data();
            String confParams = currentPara.getNdpaRemark3Data();
            //默认直接转换
            ParamCodec codec = new DirectParamCodec();
            ExtParamConf paramConf = new ExtParamConf();
            Object[] params = new Object[0];
            if (!StringUtils.isBlank(confParams)) {
                paramConf = JSON.parseObject(confParams, ExtParamConf.class);
            }
            //按配置的解析方式解析
            if (!StringUtils.isBlank(confClass)) {
                codec = BeanFactoryUtil.getBean(confClass);
            }
            //构造参数
            if (paramConf.getPoint() != null && paramConf.getStart() != null) {
                params = new Integer[]{paramConf.getStart(), paramConf.getPoint()};
            } else if (paramConf.getExt() != null){
                params =paramConf.getExt().toArray();
            }
            String value = null;
            try {
                value = codec.decode(paraValBytes, params);
            } catch (Exception e) {
                log.error("参数解析异常：{}",paraInfo);
            }
            paraInfo.setParaVal(value);
            frameParaDataList.add(paraInfo);
        }
        respData.setFrameParaList(frameParaDataList);
        //参数查询响应结果接收
        dataReciveService.interfaceQueryRecive(respData);
        return respData;
    }
}
