package com.xy.netdev.transit.schedule;

import cn.hutool.core.bean.BeanUtil;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.SnmpUtil;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.service.snmp.SnmpReqDTO;
import com.xy.netdev.frame.service.snmp.SnmpResDTO;
import com.xy.netdev.transit.ISnmpDataReceiveService;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

/**
 * 定时查询上报状态任务类
 *
 * @author duwenxu
 * @create 2021-03-10 18:07
 */
@Slf4j
public class SnmpScheduleQueryTask implements Runnable {

    private final ISnmpDataReceiveService snmpDataReceiveService;
    private final List<SnmpReqDTO> snmpReqDataList;
    private final String ip;
    private final Long interval;
    private final Long commonInterval;
    private static final String COMMUNITY = "public";

    public SnmpScheduleQueryTask(List<SnmpReqDTO> snmpReqDataList, Long interval, Long commonInterval, ISnmpDataReceiveService snmpDataReceiveService,String baseIp) {
        this.snmpReqDataList = snmpReqDataList;
        this.ip = baseIp;
        this.interval = interval;
        this.commonInterval = commonInterval;
        this.snmpDataReceiveService = snmpDataReceiveService;
    }

    @Override
    public void run() {
        while (true) {
            try {
                //单个设备单次整体查询完之后的间隔
                Thread.sleep(commonInterval);
            } catch (Exception e) {
                log.error("线程+{}+休眠发生异常！", Thread.currentThread().getName());
            }
            snmpReqDataList.forEach(data -> {
                String accessType = data.getAccessType();
                //分别调用接口或参数查询方法
                try {
                    if (SysConfigConstant.ACCESS_TYPE_PARAM.equals(accessType)) {
                        SnmpResDTO snmpResDTO = doExecSnmpQuery(data,ip);
                        log.debug("baseInfo query:设备编号：{}--参数编号：{}--参数指令:{}", data.getDevNo(), data.getParaNo(), data.getCmdMark());
                        snmpDataReceiveService.paraQueryRecive(snmpResDTO);
                    } else if (SysConfigConstant.ACCESS_TYPE_INTERF.equals(accessType)) {
                        SnmpResDTO snmpResDTO = doExecSnmpQuery(data,ip);
                        log.debug("baseInfo query:设备编号：{}-接口指令:{}", data.getDevNo(), data.getCmdMark());
                        snmpDataReceiveService.paraQueryRecive(snmpResDTO);
                    }
                } catch (Exception e) {
                    log.error("定时查询线程执行异常.当前查询设备编号：{}，当前查询类型：{}", data.getDevNo(), accessType,e);
                }
                //根据不同设备指定间隔查询
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    log.error("线程+{}+休眠发生异常！", Thread.currentThread().getName());
                }
            });
        }
    }

    /**
     * 真正请求SNMP数据
     * @param snmpReqDTO SNMP查询请求结构体
     * @return SNMP响应结构体
     */
    private SnmpResDTO doExecSnmpQuery(SnmpReqDTO snmpReqDTO,String baseIp) {
        String reqOid = snmpReqDTO.getOid();

        Map<String, Variable> snmpMap = SnmpUtil.snmpGet(baseIp, COMMUNITY, reqOid);
        Variable variable = snmpMap.get(reqOid);
        Assert.isTrue(variable !=null,"SNMP请求失败不存在当前oid:["+reqOid+"]的结果");

        SnmpResDTO snmpResDTO = new SnmpResDTO();
        BeanUtil.copyProperties(snmpReqDTO, snmpResDTO, true);
        snmpResDTO.setParaVal(variable.toString());
        return snmpResDTO;
    }


}
