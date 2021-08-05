package com.xy.netdev.frame.service.kabuc100;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.xy.common.exception.BaseException;
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
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.xy.netdev.common.constant.SysConfigConstant.PARA_COMPLEX_LEVEL_COMPOSE;

/**
 * Ka100WBUC功放
 *
 * @author zb
 * @date 2021-06-20
 */
@Component
@Slf4j
public class Ka100BucInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {

    @Autowired
    SocketMutualService socketMutualService;
    @Autowired
    IDataReciveService dataReciveService;

    /**
     * 查询设备接口
     * @param  reqInfo    请求参数信息
     */
    @Override
    public void queryPara(FrameReqData reqInfo) {
        StringBuilder sb = new StringBuilder();
        // 物理地址固定前缀
        String localAddr = "001";
        // 拼接接口查询命令
        sb.append(Ka100BucPrtcServiceImpl.SEND_START_MARK).append(localAddr).append("/")
                .append(reqInfo.getCmdMark())
                .append("_?")
                .append(StrUtil.CRLF);
        String command = sb.toString();
        // 设置协议解析与收发层交互的数据体
        reqInfo.setParamBytes(command.getBytes());
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    /**
     * 查询设备接口响应
     * @param  respData   协议解析响应数据
     * @return
     */
    public FrameRespData queryParaResponse(FrameRespData respData) {
        String respStr = new String(respData.getParamBytes());
        // 获取协议固定标识符号位置
        int startIdx = respStr.indexOf("/");
        // 获取协议数据结束位置
        int endIdx = respStr.indexOf(StrUtil.LF);
        if(endIdx==-1){
            endIdx = respStr.length();
        }
        String[] params;
        try{
            // 截取接口响应参数数据
            String str = respStr.substring(startIdx + 1,endIdx);
            // 通过数据体中固定分隔符将数据截取为若干命令响应数据
            params = str.split(",");
        }catch (Exception e){
            log.error("接口响应数据异常！源数据：{}",respStr);
            throw new BaseException("接口响应数据异常！");
        }
        List<FrameParaData> frameParaList = new ArrayList<>();
        // 处理单条命令及响应数据
        for (String param : params) {
            // 根据固定标识截取命令及命令参数
            String cmdMark = param.split("_")[0];
            String value = "";
            String[] values = param.split("_");
            if(values.length>1){
                value = values[1];
            }
            // 协议中功放电源没有命令标识   需手动赋值
            if (cmdMark.equals("MUTE") || cmdMark.equals("UNMUTE")){
                value = cmdMark;
                cmdMark = "MU";
            }
            FrameParaData paraData= new FrameParaData();
            // 获取设备类型及命令标识
            FrameParaInfo frameParaDetail = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(),cmdMark);
            // 复制对象属性
            BeanUtil.copyProperties(frameParaDetail, paraData, true);
            // 命令"FA"为组合参数  需特殊处理
            if(cmdMark.equals("FA") && value.length()>1){
                // 判断是否为组合参数参数
                if(PARA_COMPLEX_LEVEL_COMPOSE.equals(frameParaDetail.getCmplexLevel())){
                    // 获取子参数列表
                    List<FrameParaInfo> subList  = frameParaDetail.getSubParaList();
                    // 为子参数排序
                    subList.sort(Comparator.comparing(frameParaInfo1 -> Integer.valueOf(frameParaInfo1.getParaNo())));
                    // 为子参数赋值
                    for (int i = 0; i < subList.size(); i++) {
                        FrameParaData frameParaData = genFramepara(subList.get(i),value.charAt(i) + "",respData);
                        frameParaList.add(frameParaData);
                    }
                }
            }
            // 无需特殊处理的命令直接填值
            paraData.setParaVal(value);
            frameParaList.add(paraData);
        }
        respData.setFrameParaList(frameParaList);
        dataReciveService.interfaceQueryRecive(respData);
        return respData;
    }

    /**
     * 构建FrameParaData
     * @return
     */
    private  FrameParaData genFramepara(FrameParaInfo currentpara,String paraValueStr,FrameRespData respData){
        FrameParaData frameParaData = FrameParaData.builder()
                .devType(currentpara.getDevType())
                .paraNo(currentpara.getParaNo())
                .devNo(respData.getDevNo())
                .build();
        frameParaData.setParaVal(paraValueStr);
        return  frameParaData;
    }
}
