package com.xy.netdev.frame.service.kabuc100;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.service.IBaseInfoService;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReciveService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.xy.netdev.common.constant.SysConfigConstant.PARA_COMPLEX_LEVEL_COMPOSE;


/**
 * Ka100WBUC功放
 *
 * @author luo
 * @date 2021-03-05
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
    public final static String SET_ADDR_CMD = "ADDR";


    @Autowired
    SocketMutualService socketMutualService;
    @Autowired
    IDataReciveService dataReciveService;
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
        String localAddr = "001";
        sb.append(SEND_START_MARK).append(localAddr).append("/")
                .append(reqInfo.getCmdMark()).append("_?").append(StrUtil.CRLF);
        String command = sb.toString();
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
        String respStr = new String(respData.getParamBytes());
        respStr = respStr.split(RESP_START_MARK)[1];
        int stIndex = respStr.indexOf("_");

        String cmdMk = respStr.substring(respStr.indexOf("/") + 1, stIndex);
        int edIndex = respStr.indexOf(StrUtil.LF);
//        String val = respStr.substring(stIndex+1,respStr.length() - 2);
        String val = respStr.substring(stIndex + 1, edIndex);
        List<FrameParaData> frameParas = new ArrayList<>();
        FrameParaData paraInfo = new FrameParaData();
        FrameParaInfo frameParaDetail = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(), cmdMk);
        BeanUtil.copyProperties(frameParaDetail, paraInfo, true);

        if (cmdMk.equals("VOLT")) {
            String[] volt = val.split(",");

            if (PARA_COMPLEX_LEVEL_COMPOSE.equals(frameParaDetail.getCmplexLevel())) {
                List<FrameParaInfo> subList = frameParaDetail.getSubParaList();
                subList.sort(Comparator.comparing(frameParaInfo1 -> Integer.valueOf(frameParaInfo1.getParaNo())));
                for (int i = 0; i < subList.size(); i++) {
                    FrameParaData frameParaData = genFramepara(subList.get(i), volt[i], respData);
                    frameParas.add(frameParaData);
                }

            }
        }
        if (cmdMk.equals("CURR")) {
            String[] curr = val.split(",");
            if (PARA_COMPLEX_LEVEL_COMPOSE.equals(frameParaDetail.getCmplexLevel())) {
                List<FrameParaInfo> subList = frameParaDetail.getSubParaList();
                subList.sort(Comparator.comparing(frameParaInfo1 -> Integer.valueOf(frameParaInfo1.getParaNo())));
                for (int i = 0; i < subList.size(); i++) {
                    FrameParaData frameParaData = genFramepara(subList.get(i), curr[i], respData);
                    frameParas.add(frameParaData);
                }

            }
        }
        if (cmdMk.equals("WHAT")) {
            String[] what = val.split("_");
            if (PARA_COMPLEX_LEVEL_COMPOSE.equals(frameParaDetail.getCmplexLevel())) {
                List<FrameParaInfo> subList = frameParaDetail.getSubParaList();
                subList.sort(Comparator.comparing(frameParaInfo1 -> Integer.valueOf(frameParaInfo1.getParaNo())));
                for (int i = 0; i < subList.size(); i++) {
                    FrameParaData frameParaData = genFramepara(subList.get(i), what[i], respData);
                    frameParas.add(frameParaData);
                }

            }
        }
        if (cmdMk.equals("FREQ")) {
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
        }


        FrameParaInfo frameParaInfo = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(), respData.getCmdMark());
        FrameParaData frameParaData = genFramepara(frameParaInfo, val, respData);
        //BeanUtil.copyProperties(frameParaInfo, frameParaData, true);
        //frameParaData.setParaVal(val);
        frameParas.add(frameParaData);
        respData.setFrameParaList(frameParas);
        dataReciveService.paraQueryRecive(respData);
        return respData;
    }

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
     * 设置设备参数
     *
     * @param reqInfo 请求参数信息
     */
    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        StringBuilder sb = new StringBuilder();
        String localAddr = "001";
        String cmdMK = reqInfo.getCmdMark();
        switch (cmdMK) {
            case "FREQ":
                StringBuilder freqStr = new StringBuilder(reqInfo.getFrameParaList().get(0).getParaVal());
                String[] freq = freqStr.toString().split("\\.");
                if (freq.length == 1) {
                    freqStr = new StringBuilder("0" + freqStr + ".0000");
                } else {
                    freqStr = new StringBuilder("0" + freqStr);
                    for (int i = 0; i < 4 - freq[1].length(); i++) {
                        freqStr.append("0");
                    }
                }
                sb.append(SEND_START_MARK).append(localAddr).append("/").append(reqInfo.getCmdMark())
                        .append("_").append(freqStr).append(StrUtil.CRLF);
                break;
            case "ECLR":
                sb.append(SEND_START_MARK).append(localAddr).append("/").append(reqInfo.getCmdMark()).append(StrUtil.CRLF);
                break;
            case "AT":
                String atStr = reqInfo.getFrameParaList().get(0).getParaVal();
                String[] at = atStr.split("\\.");
                if (at.length == 1) {
                    atStr = atStr + ".0";
                }
                if (at[0].length() == 1) {
                    atStr = "0" + atStr;
                }
                sb.append(SEND_START_MARK).append(localAddr).append("/").append(reqInfo.getCmdMark())
                        .append("_").append(atStr).append(StrUtil.CRLF);
                break;
            default:
                sb.append(SEND_START_MARK).append(localAddr).append("/").append(reqInfo.getCmdMark())
                        .append("_").append(reqInfo.getFrameParaList().get(0).getParaVal()).append(StrUtil.CRLF);
        }
        String command = sb.toString();
        reqInfo.setParamBytes(command.getBytes());
        String cmdMark = reqInfo.getCmdMark();
        if (cmdMark.equals(SET_ADDR_CMD)) {
            setDevLocalAddr(reqInfo);
        }
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    /**
     * 设置设备参数响应
     *
     * @param respData 数据传输对象
     * @return
     */
    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        String respStr = new String(respData.getParamBytes());
        respStr = respStr.split(RESP_START_MARK)[1];
        int stIndex = respStr.indexOf("_");
//        String cmdMk = respStr.substring(0,stIndex);
        int edIndex = respStr.indexOf(StrUtil.LF);
        String val = respStr.substring(stIndex + 1, edIndex);
//        String val = respStr.substring(stIndex+1,respStr.length() - 2);


        List<FrameParaData> frameParas = new ArrayList<>();
        FrameParaInfo frameParaInfo = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(), respData.getCmdMark());
        FrameParaData frameParaData = new FrameParaData();
        BeanUtil.copyProperties(frameParaInfo, frameParaData, true);
        frameParaData.setParaVal(val);
        frameParas.add(frameParaData);
        respData.setFrameParaList(frameParas);
        dataReciveService.paraQueryRecive(respData);
        return respData;
    }


    /**
     * 设置设备物理地址
     *
     * @param reqInfo
     */
    private void setDevLocalAddr(FrameReqData reqInfo) {
        String devNo = reqInfo.getDevNo();
        BaseInfo baseInfo = new BaseInfo();
        baseInfo.setDevNo(devNo);
        baseInfo.setDevLocalAddr(reqInfo.getFrameParaList().get(0).getParaVal());
        baseInfoService.updateById(baseInfo);
        BaseInfoContainer.updateBaseInfo(devNo);
    }


}
