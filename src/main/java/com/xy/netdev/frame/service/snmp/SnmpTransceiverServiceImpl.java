package com.xy.netdev.frame.service.snmp;

import cn.hutool.core.bean.BeanUtil;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.util.SnmpUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.transit.IDataSendService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.snmp4j.smi.Variable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.xy.netdev.common.constant.SysConfigConstant.PARA_COMPLEX_LEVEL_COMPOSE;

/**
 * SNMP协议接口查询实现
 */
@Service
@Slf4j
public class SnmpTransceiverServiceImpl implements SnmpTransceiverService {
    private static final String COMMUNITY = "public";
    @Autowired
    private ISysParamService sysParamService;
    @Autowired
    private IDataSendService dataSendService;

    @Override
    public SnmpResDTO queryParam(SnmpReqDTO snmpReqDTO,String baseIp) {
        FrameParaData paraData = snmpReqDTO.getFrameParaList().get(0);
        String reqOid = paraData.getOid();

        Map<String, Variable> snmpMap = SnmpUtil.snmpGet(baseIp, COMMUNITY, reqOid);
        Variable variable = snmpMap.get(reqOid);

        SnmpResDTO snmpResDTO = new SnmpResDTO();
        BeanUtil.copyProperties(snmpReqDTO, snmpResDTO, true);
        if (variable!=null){
            paraData.setParaVal(variable.toString());
        }
        snmpResDTO.getFrameParaList().add(paraData);
        return snmpResDTO;
    }

    @Override
    public SnmpResDTO queryParamList(SnmpReqDTO snmpReqDTO, String baseIp) {
        List<FrameParaInfo> interLinkParaList = BaseInfoContainer.getInterLinkParaList(snmpReqDTO.getDevType(), snmpReqDTO.getCmdMark());
        ConcurrentHashMap<String, FrameParaData> queryMap = new ConcurrentHashMap<>();
        //处理得到oidList
        for (FrameParaInfo paraInfo : interLinkParaList) {
            if (PARA_COMPLEX_LEVEL_COMPOSE.equals(paraInfo.getCmplexLevel())){
                List<FrameParaInfo> subParaList = paraInfo.getSubParaList();
                for (FrameParaInfo frameParaInfo : subParaList) {
                    addToQueryMap(snmpReqDTO, queryMap, frameParaInfo);
                }
            }else {
                addToQueryMap(snmpReqDTO, queryMap, paraInfo);
            }
        }
        List<String> oidList = new ArrayList<>(queryMap.keySet());
        //查询赋值
        Map<String, Variable> resMap = SnmpUtil.snmpGetList(baseIp, COMMUNITY, oidList);
        for (Map.Entry<String, FrameParaData> resEntry : queryMap.entrySet()) {
            Variable variable = resMap.get(resEntry.getKey());
            if (variable!=null){
                resEntry.getValue().setParaVal(variable.toString());
            }
        }
        //添加父参数 参数值 用于判断更改
        for (FrameParaInfo paraInfo : interLinkParaList) {
            if (PARA_COMPLEX_LEVEL_COMPOSE.equals(paraInfo.getCmplexLevel())){
                addToQueryMap(snmpReqDTO, queryMap, paraInfo);
                StringBuilder parentParaBuf = new StringBuilder();
                for (FrameParaData info : queryMap.values()) {
                    if (!StringUtils.isBlank(info.getParaVal())){
                        parentParaBuf.append(info.getParaVal());
                    }
                }
                queryMap.get(paraInfo.getCmdMark()).setParaVal(parentParaBuf.toString());
            }
        }

        SnmpResDTO snmpResDTO = new SnmpResDTO();
        BeanUtil.copyProperties(snmpReqDTO,snmpResDTO,true);
        List<FrameParaData> resParaList = new ArrayList<>(queryMap.values());
        snmpResDTO.setFrameParaList(resParaList);
        return snmpResDTO;
    }

    @Override
    public void paramCtrl(SnmpReqDTO snmpReqDTO, String baseIp) {
        List<FrameParaData> frameParaList = snmpReqDTO.getFrameParaList();
        for (FrameParaData paraData : frameParaList) {
            String oid = paraData.getOid();
            String paraVal = paraData.getParaVal();
            String respCode = "0";
            Assert.isTrue(!StringUtils.isBlank(oid)&&!StringUtils.isBlank(paraVal),"SNMP参数控制:oid和paraVal不能为空,oid:"+oid+",paraVal:"+paraVal);
            try {
                respCode = SnmpUtil.setPDU(baseIp, COMMUNITY, oid, paraVal);
            } catch (Exception e) {
                log.error("SNMP参数设置异常：IP:[{}],设备编号：[{}],参数编号：[{}],参数OID：[{}],参数值：[{}]",baseIp,paraData.getDevNo(),paraData.getParaNo(),oid,paraVal);
            }
            FrameReqData frameReqData = convertFrameReqDto(snmpReqDTO,respCode);
            //回调
            dataSendService.notifyNetworkResult(frameReqData);
        }

    }

    private FrameReqData convertFrameReqDto(SnmpReqDTO snmpReqDTO,String respCode) {
        FrameReqData frameReqData = new FrameReqData();
        BeanUtil.copyProperties(snmpReqDTO,frameReqData,true);
        frameReqData.setIsOk(respCode);
        return frameReqData;
    }

    /**
     * 组装查询map
     * @param snmpReqDTO snmp查询结构体
     * @param queryMap 查询map
     * @param frameParaInfo 需要添加的参数体
     */
    private void addToQueryMap(SnmpReqDTO snmpReqDTO, ConcurrentHashMap<String, FrameParaData> queryMap, FrameParaInfo frameParaInfo) {
        Assert.isTrue(frameParaInfo.getParaByteLen().length()!=0,"设备类型"+ frameParaInfo.getDevType()+" 参数编号"+ frameParaInfo.getParaNo()+"的参数字节长度配置错误");
        String oid = oidSplic(frameParaInfo.getCmdMark(), snmpReqDTO.getDevType());
        FrameParaData snmpPara = FrameParaData.builder()
                .len(Integer.parseInt(frameParaInfo.getParaByteLen()))
                .devType(frameParaInfo.getDevType())
                .devNo(snmpReqDTO.getDevNo())
                .paraNo(frameParaInfo.getParaNo())
                .paraCmk(frameParaInfo.getCmdMark())
                .oid(oid)
                .build();
        queryMap.put(oid,snmpPara);
    }

    /**
     * 拼接OID
     * @param cmdMark
     * @param devType
     * @return
     */
    public synchronized String oidSplic(String cmdMark,String devType) {
        FrameParaInfo paraInfo = BaseInfoContainer.getParaInfoByCmd(devType, cmdMark);
        String oidPrefixCode = paraInfo.getNdpaRemark1Data();
        //复杂参数不查询，直接返回
        if (PARA_COMPLEX_LEVEL_COMPOSE.equals(paraInfo.getCmplexLevel())){
            return cmdMark;
        }
        if (StringUtils.isBlank(oidPrefixCode)){
            log.error("参数编号：[{}]---参数标识：[{}]的参数oid前缀编号为空",paraInfo.getParaNo(),cmdMark);
        }
        String oidPrefix = sysParamService.getParaRemark1(oidPrefixCode);
        return oidPrefix+ "." + cmdMark;
    }
}
