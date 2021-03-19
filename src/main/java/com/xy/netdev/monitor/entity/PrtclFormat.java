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
 * 协议格式
 *
 * @author admin
 * @date 2021-03-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@TableName("NTDV_PRTCL_FORMAT")
@ApiModel(value="PrtclFormat对象", description="协议格式")
public class PrtclFormat extends Model<PrtclFormat> {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "格式ID")
    @TableId(value = "FMT_ID", type = IdType.AUTO)
    private Integer fmtId;

    @ApiModelProperty(value = "协议名称")
    @TableField(value = "FMT_NAME")
    private String fmtName;

    @ApiModelProperty(value = "设备类型")
    @TableField(value = "DEV_TYPE")
    @Param
    private String devType;

    @ApiModelProperty(value = "查询关键字")
    @TableField(value = "FMT_SKEY")
    private String fmtSkey;

    @ApiModelProperty(value = "控制关键字")
    @TableField(value = "FMT_CKEY")
    private String fmtCkey;

    @ApiModelProperty(value = "控制响应关键字")
    @TableField(value = "FMT_CCKEY")
    private String fmtCckey;

    @ApiModelProperty(value = "查询响应关键字")
    @TableField(value = "FMT_SCKEY")
    private String fmtSckey;

    @ApiModelProperty(value = "查询响应条数")
    @TableField(value = "FMT_SC_NUM")
    private Integer fmtScNum;

    @ApiModelProperty(value = "控制响应条数")
    @TableField(value = "FMT_CC_NUM")
    private Integer fmtCcNum;

    @ApiModelProperty(value = "格式处理类")
    @TableField(value = "FMT_HANDLER_CLASS")
    private String fmtHandlerClass;

    @ApiModelProperty(value = "查询响应类型")
    @TableField(value = "FMT_SC_TYPE")
    private String fmtScType;

    @ApiModelProperty(value = "控制响应类型")
    @TableField(value = "FMT_CC_TYPE")
    @Param
    private String fmtCcType;

    @ApiModelProperty(value = "归属(0：参数；1：接口)")
    @TableField(exist = false)
    private Integer isPrtclParam;

    @Override
    protected Serializable pkVal() {
        return this.fmtId;
    }

}
