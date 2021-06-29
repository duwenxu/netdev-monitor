package com.xy.netdev.synthetical.util;

import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.synthetical.agent.XySnmpColumn;
import com.xy.netdev.synthetical.agent.XySnmpTable;
import org.apache.commons.lang.StringUtils;
import org.snmp4j.agent.mo.DefaultMOTableRow;
import org.snmp4j.agent.mo.MOTableIndex;
import org.snmp4j.agent.mo.MOTableSubIndex;
import org.snmp4j.smi.*;

import java.util.ArrayList;
import java.util.List;

import static com.xy.netdev.container.DevParaInfoContainer.SNMP_RPT_SUFFIX;


/**
 * <p>
 * 综合网管上报工具类
 * </p>
 *
 * @author tangxl
 * @since 2021-06-16
 */

public class SyntheticalUtil {
    /**
     * 生成上报OID
     * @param rptOid 上报OID
     * @param paraCode 参数编码
     * @param sysParamService 参数服务类
     * @return OID
     */
    public static String genRptOid(String rptOid,String paraCode, ISysParamService sysParamService) {
        StringBuilder oidSbuider = new StringBuilder();
        oidSbuider.append(sysParamService.getParaRemark1(SysConfigConstant.PRIVATE_MIB_PREFIX))
                  .append(".").append(rptOid)
                  .append(".").append(sysParamService.getParaRemark1(SysConfigConstant.FIXED_MIB_1))
                  .append(".").append(sysParamService.getParaRemark1(SysConfigConstant.FIXED_MIB_2))
                  .append(".").append(paraCode)
                  .append(SNMP_RPT_SUFFIX);
                  //.append(".").append(sysParamService.getParaRemark1(SysConfigConstant.PRIVATE_MIB_REGION))
                  //.append(".").append(sysParamService.getParaRemark1(SysConfigConstant.PRIVATE_MIB_STATION))
                  //.append(".").append(devNo);
        return oidSbuider.toString();
    }


    /**
     * 生成OID 数据体
     * @param ndpaDatatype    数据类型
     * @param dataV           数据值
     * @return 设备参数OID
     */
    public static Variable genSnmpVariable(String ndpaDatatype,String dataV) {
        if(ndpaDatatype.equals(SysConfigConstant.PARA_DATA_TYPE_BYTE)){
            if(StringUtils.isEmpty(dataV)){
                return  new Integer32(0);
            }else{
                return  new Integer32(Integer.parseInt(dataV));
            }
        }else if(ndpaDatatype.equals(SysConfigConstant.PARA_DATA_TYPE_INT)){
            if(StringUtils.isEmpty(dataV)){
                return  new Integer32(0);
            }else{
                return  new Integer32(Integer.parseInt(dataV));
            }
        }else if(ndpaDatatype.equals(SysConfigConstant.PARA_DATA_TYPE_UINT)){
            if(StringUtils.isEmpty(dataV)){
                return  new Integer32(0);
            }else{
                return  new Integer32(Integer.parseInt(dataV));
            }
        }else if(ndpaDatatype.equals(SysConfigConstant.PARA_DATA_TYPE_IPADDRESS)){
            return new IpAddress(dataV);
        }else {
            return new OctetString(dataV);
        }
    }
    /**
     * 生成指令设备的SNMP列对象
     * @param devOidStr                 设备参数OID
     * @param devNo                     设备编号
     * @param devTypeParaList           设备参数列表
     * @return SNMP列对象
     */
    public static XySnmpTable genXySnmpTable(String devOidStr,String devNo,List<FrameParaInfo> devTypeParaList){
        OID devOid = new OID(devOidStr);
        List<XySnmpColumn> devParaColumnList = new ArrayList<>();
        devParaColumnList.add(genXySnmpColumn(SysConfigConstant.PARA_DATA_TYPE_INT,"1",devNo));
        devParaColumnList.add(genXySnmpColumn(SysConfigConstant.PARA_DATA_TYPE_INT,"2",devNo));
        devParaColumnList.add(genXySnmpColumn(SysConfigConstant.PARA_DATA_TYPE_INT,"3",devNo));
        devParaColumnList.add(genXySnmpColumn(SysConfigConstant.PARA_DATA_TYPE_INT,"4",devNo));
        for(FrameParaInfo paraInfo:devTypeParaList){
            if(paraInfo.getNdpaOutterStatus().equals(SysConfigConstant.IS_DEFAULT_TRUE)&&!StringUtils.isEmpty(paraInfo.getNdpaRptOid())) {
                devParaColumnList.add(genXySnmpColumn(paraInfo.getDataType(), paraInfo.getParaNo(), devNo));
            }
        }
        MOTableSubIndex[] subIndexArray = new MOTableSubIndex[] {
                new MOTableSubIndex(SMIConstants.SYNTAX_OCTET_STRING) };
        MOTableIndex moTableIndex = new MOTableIndex(subIndexArray);
        XySnmpColumn[] snmpColumnArray = new XySnmpColumn[devParaColumnList.size()];
        Variable[] values = new Variable[devParaColumnList.size()];
        for(int i=0;i<devParaColumnList.size();i++){
            snmpColumnArray[i] =devParaColumnList.get(i);
            values[i] = new OctetString("i.0");
        }
        XySnmpTable  snmpTable = new XySnmpTable(devOid,moTableIndex, snmpColumnArray);
        snmpTable.setDevNo(devNo);
        snmpTable.addRow(new DefaultMOTableRow(new OID("0"),values));
        return snmpTable;
    }
    /**
     * 生成指令设备的SNMP列对象
     * @param ndpaDatatype    数据类型
     * @param paraNo          参数编号
     * @param devNo           设备编号
     * @return SNMP列对象
     */
    public static XySnmpColumn genXySnmpColumn(String ndpaDatatype,String paraNo,String devNo){
        XySnmpColumn  snmpColumn = new XySnmpColumn(Integer.parseInt(paraNo),genSyntax(ndpaDatatype));
        snmpColumn.setDevNo(devNo);
        snmpColumn.setParaNo(paraNo);
        return snmpColumn;
    }

    /**
     * 生成Syntax
     * @param ndpaDatatype    数据类型
     * @return a SMI syntax ID as defined by {@link SMIConstants}
     */
    public static int genSyntax(String ndpaDatatype){
        if(ndpaDatatype.equals(SysConfigConstant.PARA_DATA_TYPE_BYTE)){
            return SMIConstants.SYNTAX_INTEGER;
        }else if(ndpaDatatype.equals(SysConfigConstant.PARA_DATA_TYPE_INT)){
            return SMIConstants.SYNTAX_INTEGER32;
        }else if(ndpaDatatype.equals(SysConfigConstant.PARA_DATA_TYPE_UINT)){
            return SMIConstants.SYNTAX_UNSIGNED_INTEGER32;
        }else if(ndpaDatatype.equals(SysConfigConstant.PARA_DATA_TYPE_IPADDRESS)){
            return SMIConstants.SYNTAX_IPADDRESS;
        }else {
            return SMIConstants.SYNTAX_OCTET_STRING;
        }
    }

}
