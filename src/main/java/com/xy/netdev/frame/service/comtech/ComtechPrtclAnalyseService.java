package com.xy.netdev.frame.service.comtech;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.xy.common.exception.BaseException;
import com.xy.netdev.admin.entity.SysParam;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.frame.service.modemscmm.ModemScmmInterPrtcServiceImpl;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReceiveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static com.xy.netdev.common.constant.SysConfigConstant.PARA_COMPLEX_LEVEL_COMPOSE;
import static com.xy.netdev.common.util.ByteUtils.byteArrayCopy;

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
    private IDataReceiveService dataReciveService;
    @Autowired
    private ModemScmmInterPrtcServiceImpl modemScmmInterPrtcService;
    @Autowired
    private ISysParamService sysParamService;
    /**
     * 故障历史 字段标识
     */
    private static String FAULT_HISTORY_COMMAND = "4";

    @Override
    public void queryPara(FrameReqData reqInfo) {
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        byte[] bytes = respData.getParamBytes();
        log.info("接收到Comtech查询响应帧：[{}]", HexUtil.encodeHexStr(bytes));
        String devType = respData.getDevType();
        String cmdMark = respData.getCmdMark();
        String devNo = respData.getDevNo();

        FrameParaInfo paraInfo = BaseInfoContainer.getParaInfoByCmd(devType, cmdMark);
        FrameParaData paraData = new FrameParaData();
        BeanUtil.copyProperties(paraInfo, paraData, true);
        BeanUtil.copyProperties(respData, paraData, true);

        //获取协议规定的参数值长度
        int paraByteLen = 0;
        if (!StringUtils.isBlank(paraInfo.getParaByteLen())) {
            paraByteLen = Integer.parseInt(paraInfo.getParaByteLen());
        }
        //校验实际收到的参数值长度是否与协议一致
        int actualLen = bytes.length-cmdMark.length();
        if (paraByteLen!=actualLen){
            log.error("Comtech收到cmd为：[{}]的响应帧,值长度与预期不一致. 参数编号：{}，参数名：{}，参数长度：{}，数据帧：{}",cmdMark,paraInfo.getParaNo()
                    ,paraInfo.getParaName(),paraInfo.getParaByteLen(),HexUtil.encodeHexStr(bytes));
        }

        //此处按命令字长度截取数据体内容
        byte[] dataBytes = byteArrayCopy(bytes, cmdMark.length(), actualLen);
        //根据响应进行内容或拒绝码的解析
        if (respData.getRespCode().equals("1")){
            respData =  rejectCodeHandler(bytes,respData);
        }else {
            CopyOnWriteArrayList<FrameParaData> paraList = new CopyOnWriteArrayList<>();
            try {
                //故障状态 字符串循环
                if (paraInfo.getCmdMark().equals(FAULT_HISTORY_COMMAND)) {

                    //处理复杂参数
                } else if (PARA_COMPLEX_LEVEL_COMPOSE.equals(paraInfo.getCmplexLevel())) {
                    List<FrameParaInfo> subParaList = paraInfo.getSubParaList();
                    for (FrameParaInfo subPara : subParaList) {
                        //多个字节时获取字节配置位置
                        String remark1Data = subPara.getNdpaRemark1Data();
                        int byteIndex = 0;
                        if (StringUtils.isNoneBlank(remark1Data)){
                            byteIndex = Integer.parseInt(remark1Data);
                        }
                        byte[] subByte = ByteUtils.byteArrayCopy(dataBytes,byteIndex,1);
                        String value = modemScmmInterPrtcService.doGetValue(subPara, subByte);
                        FrameParaData subParaData = new FrameParaData();
                        BeanUtil.copyProperties(subPara, subParaData, true);
                        subParaData.setParaVal(value);
                        subParaData.setDevNo(devNo);
                        paraList.add(subParaData);
                    }
                    //添加复杂参数  父参数
                    paraData.setParaVal(StrUtil.str(dataBytes,StandardCharsets.UTF_8));
                    paraList.add(paraData);
                } else {
                    //普通参数
                    String paraVal = StrUtil.str(dataBytes, StandardCharsets.UTF_8);
                    paraData.setParaVal(paraVal);
                    paraList.add(paraData);
                }
            } catch (Exception e) {
                log.error("Comtech数据解析异常：设备类型：{}---参数编号：{}---参数标识字：{}", devType, paraInfo.getParaNo(), paraInfo.getCmdMark());
            }
            respData.setFrameParaList(paraList);
        }
        //响应结果向下流转
        dataReciveService.paraQueryRecive(respData);
        return respData;
    }

    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        List<FrameParaData> paraList = reqInfo.getFrameParaList();
        if (paraList == null || paraList.isEmpty()) {
            return;
        }
        if(reqInfo.getCmdMark().equals(":")){
            reqInfo.setCmdMark(":");
        }
        //控制参数信息拼接
        FrameParaData paraData = paraList.get(0);
        String paraVal = paraData.getParaVal();
        byte[] dataBytes = null;
        if (!StringUtils.isBlank(paraVal)) {
            dataBytes = StrUtil.bytes(paraVal);
        }
        //衰减值，需要衰减值设置参数来设置
        if(reqInfo.getCmdMark().equals("A")){
            reqInfo.setCmdMark("K");
            reqInfo.getFrameParaList().get(0).setParaNo("63");
        }
        reqInfo.setParamBytes(dataBytes);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        byte[] bytes = respData.getParamBytes();
        String cmdMark = respData.getCmdMark();
        log.info("接收到Comtech控制响应帧：[{}]", HexUtil.encodeHexStr(bytes));
        if(bytes.length>1){
            ArrayList<FrameParaData> paraList = new ArrayList<>();
            FrameParaInfo paraInfo = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(), cmdMark);
            FrameParaData paraData = new FrameParaData();
            BeanUtil.copyProperties(paraInfo, paraData, true);
            BeanUtil.copyProperties(respData, paraData, true);
            //普通参数
            String paraVal = StrUtil.str(bytes, StandardCharsets.UTF_8);
            paraData.setParaVal(paraVal.substring(1));
            if(cmdMark.equals("K")){
                paraData.setParaNo("59");
                paraData.setParaCmk("A");
            }
            paraList.add(paraData);
            respData.setFrameParaList(paraList);
        }
        //此处按命令字长度截取数据体内容
        if (respData.getRespCode().equals("1")){
            respData =  rejectCodeHandler(bytes,respData);
        }
        dataReciveService.paraCtrRecive(respData);
        return respData;
    }

    /**
     * 拒绝码解析
     * @param dataBytes 响应字节内容
     * @param respData  响应数据
     * @return 增加了拒绝码信息的响应字节
     */
    public FrameRespData rejectCodeHandler(byte[] dataBytes, FrameRespData respData) {
        String cmdMark = respData.getCmdMark();
        if (dataBytes.length>cmdMark.length()){
            byte[] errorByte = byteArrayCopy(dataBytes, cmdMark.length(), 1);
            String errCode = StrUtil.str(errorByte, StandardCharsets.UTF_8);
            SysParam errParam = getErr(errCode);
            String errMsg = errParam.getParaName();
            log.info("Comtech拒绝响应：命令标识：[{}],响应code:[{}],响应信息：[{}]", cmdMark, errCode, errMsg);
        }
        return respData;
    }

    /**
     * 根据响应错误代码获取 配置的响应参数
     *
     * @param errCode 错误代码
     * @return 响应参数
     */
    public SysParam getErr(String errCode) {
        List<SysParam> errParams = sysParamService.queryParamsByParentId(SysConfigConstant.ERR_PARENT_COMTECH);
        List<SysParam> list = errParams.stream().filter(param -> errCode.equals(param.getRemark1())).collect(Collectors.toList());
        if (!list.isEmpty()) {
            return list.get(0);
        } else {
            throw new BaseException("Comtech功放控制响应解析异常：非法的错误类型CODE:" + errCode);
        }
    }
}
