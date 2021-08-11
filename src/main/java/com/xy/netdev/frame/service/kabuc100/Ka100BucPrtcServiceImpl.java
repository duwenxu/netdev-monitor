package com.xy.netdev.frame.service.kabuc100;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.service.IBaseInfoService;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReceiveService;
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
public class Ka100BucPrtcServiceImpl implements IParaPrtclAnalysisService {


    /**
     * 用户命令起始标记
     */
    public final static String SEND_START_MARK = "<";
    /**
     * 设备响应开始标记
     */
    public final static String RESP_START_MARK = ">";
    /**
     * 设备物理地址设置
     */

    @Autowired
    SocketMutualService socketMutualService;
    @Autowired
    IDataReceiveService dataReciveService;
    @Autowired
    IBaseInfoService baseInfoService;

    /**
     * 查询设备参数
     *
     * @param reqInfo 请求参数信息
     */
    @Override
    public void queryPara(FrameReqData reqInfo) {
        StringBuilder sb = new StringBuilder();
        // 物理地址固定前缀
        String localAddr = "001";
        // 拼接参数查询命令
        sb.append(SEND_START_MARK).append(localAddr).append("/")
                .append(reqInfo.getCmdMark())
                .append("_?")
                .append(StrUtil.CRLF);
        String command = sb.toString();
        // 设置协议解析与收发层交互的数据体
        reqInfo.setParamBytes(command.getBytes());
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    /**
     * 查询设备参数响应
     *
     * @param respData 数据传输对象
     * @return
     */
    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        // 获取协议解析与收发层交互的数据体
        String respStr = new String(respData.getParamBytes());
        respStr = respStr.split(RESP_START_MARK)[1];
        // 按照协议固定字符获取命令内容开始下标
        int startIdx = respStr.indexOf("_");
        // 去除响应格式中的固定前缀  获取命令标识
        String cmdMk = respStr.substring(respStr.indexOf("/") + 1, startIdx);
        // 按照协议固格式定结束标识获取内容结束下标
        int endIdx = respStr.indexOf(StrUtil.LF);
        // 截取有效数据内容
        String val = respStr.substring(startIdx + 1, endIdx);
        List<FrameParaData> frameParas = new ArrayList<>();
        FrameParaData paraInfo = new FrameParaData();
        // 获取设备类型及命令标识
        FrameParaInfo frameParaDetail = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(), cmdMk);
        // 复制对象属性
        BeanUtil.copyProperties(frameParaDetail, paraInfo, true);
        // 命令"VOLT(电压)"、"CURR(电流)"、"WHAT(设备信息)"为组合参数  需特殊处理
        switch (cmdMk){
            case "CURR":
            case "VOLT":
                String[] cvpara = val.split(",");
                // 判断是否为组合参数参数
                if (PARA_COMPLEX_LEVEL_COMPOSE.equals(frameParaDetail.getCmplexLevel())) {
                    // 调用子参数赋值方法为子参数赋值
                    assignment(frameParaDetail,cvpara,respData,frameParas);
                }
                break;
            case "WHAT":
                String[] what = val.split("_");
                if (PARA_COMPLEX_LEVEL_COMPOSE.equals(frameParaDetail.getCmplexLevel())) {
                    // 调用子参数赋值方法为子参数赋值
                    assignment(frameParaDetail,what,respData,frameParas);
                }
                break;
            case "FREQ":
                // 去除输入频率查询结果前后的补0
                String[] freq = val.split("\\.");
                if (freq.length == 1) {
                    val = freq[0].substring(1);
                } else {
                    for (int i = freq[1].length() - 1; i >= 0; i--) {
                        if (freq[1].charAt(i) == '0') {
                            freq[1] = freq[1].substring(0, freq[1].length() - 1);
                        }
                    }
                    if (freq[1].length() == 0) {
                        val = freq[0].substring(1);
                    } else {
                        val = freq[0].substring(1) + "." + freq[1];
                    }
                }
                break;
            default:
        }
        // 获取设备类型及命令标识
        FrameParaInfo frameParaInfo = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(), respData.getCmdMark());
        FrameParaData frameParaData = genFramepara(frameParaInfo, val, respData);
        frameParas.add(frameParaData);
        respData.setFrameParaList(frameParas);
        dataReciveService.paraQueryRecive(respData);
        return respData;
    }

    /**
     * 设置设备参数
     *
     * @param reqInfo 请求参数信息
     */
    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        StringBuilder sb = new StringBuilder();
        // 物理地址固定前缀
        String localAddr = "001";
        // 获取命令标识
        String cmdMK = reqInfo.getCmdMark();
        switch (cmdMK) {
            // 输入频率设置 按照协议需求，在参数前后补0
            case "FREQ":
                StringBuilder freqStr = new StringBuilder(reqInfo.getFrameParaList().get(0).getParaVal());
                String[] freq = freqStr.toString().split("\\.");
                // 判断是否为小数
                if (freq.length == 1) {
                    freqStr = new StringBuilder("0" + freqStr + ".0000");
                } else {
                    freqStr = new StringBuilder("0" + freqStr);
                    for (int i = 0; i < 4 - freq[1].length(); i++) {
                        freqStr.append("0");
                    }
                }
                // 拼接设置命令
                sb.append(SEND_START_MARK).append(localAddr).append("/").append(reqInfo.getCmdMark())
                        .append("_").append(freqStr).append(StrUtil.CRLF);
                break;
            // 报警标志清除命令中不需要传参数值 需特殊处理
            case "ECLR":
                sb.append(SEND_START_MARK).append(localAddr).append("/").append(reqInfo.getCmdMark()).append(StrUtil.CRLF);
                break;
            // 衰减设置 小于10位和整数时需要补0
            case "AT":
                String atStr = reqInfo.getFrameParaList().get(0).getParaVal();
                String[] at = atStr.split("\\.");
                // 判断是否为小数
                if (at.length == 1) {
                    atStr = atStr + ".0";
                }
                if (at[0].length() == 1) {
                    atStr = "0" + atStr;
                }
                // 拼接设置命令
                sb.append(SEND_START_MARK).append(localAddr).append("/").append(reqInfo.getCmdMark())
                        .append("_").append(atStr).append(StrUtil.CRLF);
                break;
            default:
                // 常规命令拼接
                sb.append(SEND_START_MARK).append(localAddr).append("/").append(reqInfo.getCmdMark())
                        .append("_").append(reqInfo.getFrameParaList().get(0).getParaVal()).append(StrUtil.CRLF);
        }
        String command = sb.toString();
        reqInfo.setParamBytes(command.getBytes());
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    /**
     * 设置设备参数响应  设置与查询无区别标识 设置响应与查询响应共用一个方法
     * @param respData 数据传输对象
     * @return
     */
    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        return null;
    }

    /**
     * 构建FrameParaData
     * @param currentpara 帧参数对象
     * @param paraValueStr 响应数据参数部分
     * @param respData 响应数据对象
     * @return
     */
    private FrameParaData genFramepara(FrameParaInfo currentpara, String paraValueStr, FrameRespData respData) {
        FrameParaData frameParaData = FrameParaData.builder()
                .devType(currentpara.getDevType())
                .paraNo(currentpara.getParaNo())
                .devNo(respData.getDevNo())
                .build();
        frameParaData.setParaVal(paraValueStr);
        return frameParaData;
    }

    /**
     * 子参数赋值方法
     * @param frameParaDetail 帧参数对象
     * @param paras 处理后的参数数组
     * @param frameRespData 响应数据对象
     * @param list 数据帧参数对象列表
     * @return
     */
    private void assignment(FrameParaInfo frameParaDetail,String[] paras,FrameRespData frameRespData,List<FrameParaData> list){
        // 获取子参数列表
        List<FrameParaInfo> subList = frameParaDetail.getSubParaList();
        // 为子参数排序
        subList.sort(Comparator.comparing(frameParaInfo1 -> Integer.valueOf(frameParaInfo1.getParaNo())));
        // 为子参数赋值
        for (int i = 0; i < subList.size(); i++) {
            FrameParaData frameParaData = genFramepara(subList.get(i), paras[i],frameRespData);
            list.add(frameParaData);
        }
    }

}
