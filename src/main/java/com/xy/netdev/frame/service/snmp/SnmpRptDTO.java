package com.xy.netdev.frame.service.snmp;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SNMP---综合网管上报DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SnmpRptDTO {
    @ApiModelProperty(value = "参数ID")
    private Integer paraId;

    @ApiModelProperty(value = "参数编号")
    private String paraNo;

    @ApiModelProperty(value = "参数编码")
    private String paraCode;

    @ApiModelProperty(value = "上报OID")
    private String rptOidSign;

    @ApiModelProperty(value = "参数名称")
    private String paraName;

    @ApiModelProperty(value = "参数值")
    private String paraVal;

    /**
     * 0023001 byte  0023002 int  0023003 unit  0023004 str 0023005 buf  0023006 ipAddress  0023007 ipMask
     */
    @ApiModelProperty(value = "参数数据类型")
    private String paraDatatype;

    @ApiModelProperty(value = "参数单位")
    private String paraUnit;

}
