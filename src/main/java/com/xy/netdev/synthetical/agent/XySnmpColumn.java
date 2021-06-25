package com.xy.netdev.synthetical.agent;

import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.container.DevStatusContainer;
import com.xy.netdev.monitor.bo.DevStatusInfo;
import com.xy.netdev.monitor.bo.ParaViewInfo;
import com.xy.netdev.synthetical.util.SyntheticalUtil;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOMutableColumn;
import org.snmp4j.agent.mo.MOTableRow;
import org.snmp4j.agent.request.SubRequest;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.Variable;

/**
 * <p>
 * snmp table 把各个设备参数封装成表的字段
 * </p>
 *
 * @author tangxl
 * @since 2021-06-21
 */
public class XySnmpColumn extends MOMutableColumn {

    private String paraNo;

    private String devNo;

    public String getParaNo() {
        return paraNo;
    }

    public void setParaNo(String paraNo) {
        this.paraNo = paraNo;
    }

    public String getDevNo() {
        return devNo;
    }

    public void setDevNo(String devNo) {
        this.devNo = devNo;
    }

    public XySnmpColumn(int columnID, int syntax) {
        super(columnID, syntax,MOAccessImpl.ACCESS_READ_ONLY);
    }

    public Variable getValue(MOTableRow row, int column, SubRequest subRequest) {
        if(column>4){
            ParaViewInfo paraInfo = DevParaInfoContainer.getDevParaView(devNo,paraNo);
            if(paraInfo==null){
                return new Null();
            }
            return SyntheticalUtil.genSnmpVariable(paraInfo.getParaDatatype(),paraInfo.getParaVal());
        }
        ISysParamService sysParamService = BaseInfoContainer.getSysParamService();
        if(column ==1){
            return new Integer32(Integer.parseInt(sysParamService.getParaRemark1(SysConfigConstant.PRIVATE_MIB_REGION)));
        }
        if(column ==2){
            return new Integer32(Integer.parseInt(sysParamService.getParaRemark1(SysConfigConstant.PRIVATE_MIB_STATION)));
        }
        if(column ==3){
            return new Integer32(Integer.parseInt(devNo));
        }
        if(column ==4){
            DevStatusInfo devStatusInfo = DevStatusContainer.getDevStatusInfo(devNo);
            if(devStatusInfo.getIsInterrupt().equals("0")){
                return new Integer32(1);
            }
            return new Integer32(0);
        }
        return new Null();
    }
}
