package com.xy.netdev.frame.service.snmp;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SNMP响应接受结构体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SnmpResDTO {

    @ApiModelProperty(value = "命令标识符")
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
     * 参数表中 0020
     */
    @ApiModelProperty(value = "设备类型")
    private String devType;

    @ApiModelProperty(value = "设备编号")
    private String devNo;
    /**
     * 参数表中  0025001 参数  0025002 接口
     */
    @ApiModelProperty(value = "访问类型")
    private String accessType;

    /**
     * 参数表中  0026002 查询响应  0026004 控制响应
     */
    @ApiModelProperty(value = "操作类型")
    private String operType;

    /**
     * 若协议中未定义响应码 则 0为成功  1为失败,协议中定义响应码  则协议格式中需要配置 查询响应类型/控制响应类型
     */
    @ApiModelProperty(value = "响应码")
    private String respCode;
}
