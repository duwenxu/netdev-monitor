package com.xy.netdev.monitor.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 设备接口
 *
 * @author admin
 * @date 2021-03-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@TableName("NTDV_INTERFACE")
@ApiModel(value="Interface对象", description="设备接口")
public class Interface extends Model<Interface> {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "接口ID")
    @TableId(value = "ITF_ID", type = IdType.AUTO)
    private Integer itfId;

    @ApiModelProperty(value = "设备类型")
    @TableField(value = "DEV_TYPE")
    private String devType;

    @ApiModelProperty(value = "格式ID")
    @TableField(value = "FMT_ID")
    private Integer fmtId;

    @ApiModelProperty(value = "接口编码")
    @TableField(value = "ITF_CODE")
    private String itfCode;

    @ApiModelProperty(value = "接口名称")
    @TableField(value = "ITF_NAME")
    private String itfName;

    @ApiModelProperty(value = "接口类型")
    @TableField(value = "ITF_TYPE")
    private String itfType;

    @ApiModelProperty(value = "接口状态")
    @TableField(value = "ITF_STATUS")
    private String itfStatus;

    @ApiModelProperty(value = "此处用逗号分隔,填写参数ID,如果是查询表明 响应的字段,如果是控制 表明 控制的字段必须按照顺序填写")
    @TableField(value = "ITF_DATA_FORMAT")
    private String itfDataFormat;


    @Override
    protected Serializable pkVal() {
        return this.itfId;
    }

}
