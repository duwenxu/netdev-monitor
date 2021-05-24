package com.xy.netdev.frame.service.comtech;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.HexUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.rpt.enums.ComtechSpeComEnum;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReciveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.xy.netdev.common.constant.SysConfigConstant.PARA_COMPLEX_LEVEL_COMPOSE;

/**
 * Comtech功率放大器 单参数 查询控制
 *
 * @author duwenxu
 * @create 2021-05-20 15:12
 */
@Service
@Slf4j
public class ComtechPrtclAnalyseService implements IParaPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private IDataReciveService dataReciveService;

    @Override
    public void queryPara(FrameReqData reqInfo) {
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        byte[] bytes = respData.getParamBytes();
        log.info("接收到Comtech响应帧：[{}]", HexUtil.encodeHexStr(bytes));
        String devType = respData.getDevType();
        String cmdMark = respData.getCmdMark();

        FrameParaInfo paraInfo = BaseInfoContainer.getParaInfoByCmd(devType, cmdMark);
        FrameParaData paraData = new FrameParaData();
        BeanUtil.copyProperties(paraInfo, paraData, true);
        BeanUtil.copyProperties(respData, paraData, true);

        //TODO 目前先关注转换成协议上显示的 String内容后的解析  例如：2 A X CBBBBBBPDS 3 C
        String content = new String(bytes);
        int paraByteLen = 0;
        if (!StringUtils.isBlank(paraInfo.getParaByteLen())){
            paraByteLen = Integer.parseInt(paraInfo.getParaByteLen());
        }
        CopyOnWriteArrayList<FrameParaData> paraList = new CopyOnWriteArrayList<>();
        try {
            //处理特殊参数
            if (content.startsWith(ComtechSpeComEnum.PBM.getReqCommand())||content.startsWith(ComtechSpeComEnum.PBW.getReqCommand())){
                String paraVal = content.substring(3, 3 + paraByteLen);
                paraData.setParaVal(paraVal);
            //故障状态 字符串循环
            }else if (paraInfo.getCmdMark().equals("4")){

            //处理复杂参数
            }else if (PARA_COMPLEX_LEVEL_COMPOSE.equals(paraInfo.getCmplexLevel())){
                List<FrameParaInfo> subParaList = paraInfo.getSubParaList();


            }else {
                String paraVal = content.substring(1, 1 + paraByteLen);
                paraData.setParaVal(paraVal);
            }
        } catch (Exception e) {
            log.error("Comtech数据解析异常：设备类型：{}---参数编号：{}---参数标识字：{}",devType,paraInfo.getParaNo(),paraInfo.getCmdMark());
        }
        paraList.add(paraData);
        respData.setFrameParaList(paraList);
        //响应结果向下流转
        dataReciveService.paraQueryRecive(respData);
        return respData;
    }

    @Override
    public void ctrlPara(FrameReqData reqInfo) {

    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        return null;
    }
}
