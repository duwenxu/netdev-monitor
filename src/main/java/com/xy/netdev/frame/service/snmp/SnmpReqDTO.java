package com.xy.netdev.frame.service.snmp;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SNMP查询发送结构体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SnmpReqDTO {

    /**
     * 参数表中 0020
     */
    @ApiModelProperty(value = "设备类型")
    private String devType;

    @ApiModelProperty(value = "设备编号")
    private String devNo;

    @ApiModelProperty(value = "参数标识符")
    private String cmdMark;

    @ApiModelProperty(value = "参数OID")
    private String oid;

    @ApiModelProperty(value = "参数编号")
    private String paraNo;

    @ApiModelProperty(value = "参数值")
    private String paraVal;


    @ApiModelProperty(value = "参数长度")
    private Integer len;

    /**
     * 参数表中  0025001 参数  0025002 接口
     */
    @ApiModelProperty(value = "访问类型")
    private String accessType;

    /**
     * 参数表中  0026001 查询  0026003 控制
     */
    @ApiModelProperty(value = "操作类型")
    private String operType;

    /**
     * 0 成功  1 失败
     */
    @ApiModelProperty(value = "发送是否成功")
    private String isOk;
}
