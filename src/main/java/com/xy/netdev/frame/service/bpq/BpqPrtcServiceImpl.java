package com.xy.netdev.frame.service.bpq;

import cn.hutool.core.bean.BeanUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.service.IBaseInfoService;
import com.xy.netdev.transit.IDataReciveService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


/**
 * 39所Ku&L下变频器参数协议解析
 *
 * @author luo
 * @date 2021-03-05
 */
@Component
public class BpqPrtcServiceImpl implements IParaPrtclAnalysisService {


    /**用户命令起始标记*/
    public final static String SEND_START_MARK = "<";
    /**设备响应开始标记*/
    public final static String RESP_START_MARK = ">";
    /**设备物理地址设置*/
    public final static String SET_ADDR_CMD = "SPA";
    /**设备物理广播地址*/
    public final static String BROADCAST_ADDR = "255";


    @Autowired
    SocketMutualService socketMutualService;
    @Autowired
    IDataReciveService dataReciveService;
    @Autowired
    IBaseInfoService baseInfoService;


    /**
     * 查询设备参数
     * @param  reqInfo   请求参数信息
     */
    @Override
    public void queryPara(FrameReqData reqInfo) {
        StringBuilder sb = new StringBuilder();
        String localAddr = getDevLocalAddr(reqInfo);
        sb.append(SEND_START_MARK).append(localAddr).append("/")
                .append(reqInfo.getCmdMark());
        String command = sb.toString();
        reqInfo.setParamBytes(command.getBytes());
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    /**
     * 查询设备参数响应
     * @param  respData   数据传输对象
     * @return
     */
    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        String respStr = new String(respData.getParamBytes());
        int beginIdx = respStr.indexOf("/");
        int endIdx = respStr.indexOf("_");
        String cmdMark = respStr.substring(beginIdx+1,endIdx);
        String value = respStr.substring(endIdx+1,respStr.indexOf("\\r"));
        List<FrameParaData> frameParas = new ArrayList<>();
        FrameParaInfo frameParaInfo = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(),respData.getCmdMark());
        FrameParaData frameParaData = new FrameParaData();
        frameParaData.setParaVal(value);
        BeanUtil.copyProperties(frameParaInfo, frameParaData, true);
        frameParas.add(frameParaData);
        respData.setFrameParaList(frameParas);
        dataReciveService.paraQueryRecive(respData);
        return respData;
    }

    /**
     * 设置设备参数
     * @param  reqInfo   请求参数信息
     */
    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        StringBuilder sb = new StringBuilder();
        String localAddr = getDevLocalAddr(reqInfo);
        sb.append(SEND_START_MARK).append(localAddr).append("/").append(reqInfo.getCmdMark())
                .append("_").append(reqInfo.getFrameParaList().get(0).getParaVal());
        String command = sb.toString();
        reqInfo.setParamBytes(command.getBytes());
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    /**
     * 设置设备参数响应
     * @param  respData   数据传输对象
     * @return
     */
    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        String respStr = new String(respData.getParamBytes());
        int beginIdx = respStr.indexOf("/");
        int endIdx = respStr.indexOf("_");
        String value = respStr.substring(endIdx+1,respStr.indexOf("\\r"));
        String cmdMark = respData.getCmdMark();
        if(cmdMark.equals(SET_ADDR_CMD)) {
            setDevLocalAddr(respData,value);
        }
        FrameParaInfo frameParaInfo = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(),cmdMark);
        List<FrameParaData> frameParaDatas = new ArrayList<>();
        FrameParaData frameParaData = new FrameParaData();
        BeanUtil.copyProperties(frameParaInfo, frameParaData, true);
        frameParaData.setDevNo(respData.getDevNo());
        frameParaData.setParaVal(value);
        frameParaDatas.add(frameParaData);
        respData.setFrameParaList(frameParaDatas);
        respData.setReciveOrignData(respStr);
        dataReciveService.paraCtrRecive(respData);
        return respData;
    }

    /**
     * 获取设备物理地址
     * @param reqInfo
     * @return
     */
    protected String getDevLocalAddr(FrameReqData reqInfo){
        BaseInfo baseInfo = BaseInfoContainer.getDevInfoByNo(reqInfo.getDevNo());
        String localAddr = baseInfo.getDevLocalAddr();
        if(StringUtils.isEmpty(localAddr)){
            if(reqInfo.getCmdMark().equals(SET_ADDR_CMD)){
                localAddr = BROADCAST_ADDR;
            }
        }
        return localAddr;
    }

    /**
     * 设置设备物理地址
     * @param respData
     * @param value
     */
    private void setDevLocalAddr(FrameRespData respData,String value){
        String devNo = respData.getDevNo();
        BaseInfo baseInfo = new BaseInfo();
        baseInfo.setDevNo(devNo);
        baseInfo.setDevLocalAddr(value);
        baseInfoService.updateById(baseInfo);
    }

}
