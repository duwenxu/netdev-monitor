package com.xy.netdev.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.xy.common.annotation.Param;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 设备信息
 *
 * @author admin
 * @date 2021-03-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@TableName("NTDV_BASE_INFO")
@ApiModel(value="BaseInfo对象", description="设备信息")
public class BaseInfo extends Model<BaseInfo> {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "设备编号")
    @TableId(value = "DEV_NO", type = IdType.ID_WORKER)
    private String devNo;

    @ApiModelProperty(value = "设备类型")
    @TableField(value = "DEV_TYPE")
    @Param
    private String devType;

    @ApiModelProperty(value = "设备名称")
    @TableField(value = "DEV_NAME")
    private String devName;

    @ApiModelProperty(value = "设备状态")
    @Param
    @TableField(value = "DEV_STATUS")
    private String devStatus;

    @ApiModelProperty(value = "设备所属公司")
    @TableField(value = "DEV_CORP")
    @Param
    private String devCorp;

    @ApiModelProperty(value = "设备版本")
    @TableField(value = "DEV_VER")
    private String devVer;

    @ApiModelProperty(value = "设备IP地址")
    @TableField(value = "DEV_IP_ADDR")
    private String devIpAddr;

    @ApiModelProperty(value = "设备内部地址")
    @TableField(value = "DEV_LOCAL_ADDR")
    private String devLocalAddr;

    @ApiModelProperty(value = "设备端口")
    @TableField(value = "DEV_PORT")
    private String devPort;

    @ApiModelProperty(value = "本机端口")
    @TableField(value = "DEV_LOCAL_PORT")
    private String devLocalPort;

    @ApiModelProperty(value = "上级设备编号")
    @TableField(value = "DEV_PARENT_NO")
    private String devParentNo;

    @ApiModelProperty(value = "设备访间隔时间(毫秒)")
    @TableField(value = "DEV_INTERVAL_TIME")
    private Integer devIntervalTime;

    @ApiModelProperty(value = "设备网络协议")
    @TableField(value = "DEV_NET_PTCL")
    @Param
    private String devNetPtcl;

    @ApiModelProperty(value = "设备部署类型")
    @TableField(value = "DEV_DEPLOY_TYPE")
    @Param
    private String devDeployType;

    /**
     * 0032001 设备使用状态-在用  0032002 设备使用状态-不在用
     */
    @ApiModelProperty(value = "设备使用状态")
    @TableField(value = "DEV_USE_STATUS")
    @Param
    private String devUseStatus;

    /**
     * 设备型号
     */
    @ApiModelProperty(value = "设备型号")
    @TableField(value = "DEV_SUB_TYPE")
    @Param
    private String devSubType;

    /**
     * 设备是否连接远程服务
     * 0003001 是  0003002 否
     */
    @ApiModelProperty(value = "是否连接远程服务")
    @TableField(value = "DEV_IS_LINK")
    @Param
    private String devIsLink;

    @ApiModelProperty(value = "备注一描述")
    @TableField(value = "DEV_REMARK1_DESC")
    private String devRemark1Desc;

    @ApiModelProperty(value = "备注一数据")
    @TableField(value = "DEV_REMARK1_DATA")
    private String devRemark1Data;

    @ApiModelProperty(value = "备注二描述")
    @TableField(value = "DEV_REMARK2_DESC")
    private String devRemark2Desc;

    @ApiModelProperty(value = "备注二数据")
    @TableField(value = "DEV_REMARK2_DATA")
    private String devRemark2Data;

    @ApiModelProperty(value = "备注三描述")
    @TableField(value = "DEV_REMARK3_DESC")
    private String devRemark3Desc;

    @ApiModelProperty(value = "备注三数据")
    @TableField(value = "DEV_REMARK3_DATA")
    private String devRemark3Data;

    /**
     * 0 代表  上报54所IP
     */
    @ApiModelProperty(value = "是否上报IP")
    @TableField(exist = false)
    private String isRptIp;

    @Override
    protected Serializable pkVal() {
        return this.devNo;
    }

}
