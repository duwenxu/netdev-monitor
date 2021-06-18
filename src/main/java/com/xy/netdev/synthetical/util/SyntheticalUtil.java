package com.xy.netdev.synthetical.util;

import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.monitor.entity.ParaInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


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
     * @param devNo    设备编号
     * @param paraInfo 设备参数信息
     * @return 设备参数OID
     */
    public static String genRptOid(String devNo, ParaInfo paraInfo,ISysParamService sysParamService) {
        StringBuilder oidSbuider = new StringBuilder();
        oidSbuider.append(sysParamService.getParaRemark1(SysConfigConstant.PRIVATE_MIB_PREFIX))
                  .append(".").append(paraInfo.getNdpaRptOid())
                  .append(".").append(sysParamService.getParaRemark1(SysConfigConstant.FIXED_MIB_1))
                  .append(".").append(sysParamService.getParaRemark1(SysConfigConstant.FIXED_MIB_2))
                  .append(".").append(paraInfo.getNdpaNo());
                  //.append(".").append(sysParamService.getParaRemark1(SysConfigConstant.PRIVATE_MIB_REGION))
                  //.append(".").append(sysParamService.getParaRemark1(SysConfigConstant.PRIVATE_MIB_STATION))
                  //.append(".").append(devNo);
        return oidSbuider.toString();
    }
}
