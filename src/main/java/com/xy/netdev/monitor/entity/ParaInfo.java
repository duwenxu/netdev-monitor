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
 * 设备参数
 *
 * @author admin
 * @date 2021-03-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@TableName("NTDV_PARA_INFO")
@ApiModel(value="ParaInfo对象", description="设备参数")
public class ParaInfo extends Model<ParaInfo> {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "参数ID")
    @TableId(value = "NDPA_ID", type = IdType.AUTO)
    private Integer ndpaId;

    @ApiModelProperty(value = "格式ID")
    @TableField(value = "FMT_ID")
    private Integer fmtId;

    @ApiModelProperty(value = "参数编号")
    @TableField(value = "NDPA_NO")
    private String ndpaNo;

    @ApiModelProperty(value = "参数编码")
    @TableField(value = "NDPA_CODE")
    private String ndpaCode;

    @ApiModelProperty(value = "参数名称")
    @TableField(value = "NDPA_NAME")
    private String ndpaName;

    @ApiModelProperty(value = "设备类型")
    @Param
    @TableField(value = "DEV_TYPE")
    private String devType;

    @ApiModelProperty(value = "访问权限")
    @Param
    @TableField(value = "NDPA_ACCESS_RIGHT")
    private String ndpaAccessRight;

    @ApiModelProperty(value = "参数单位")
    @TableField(value = "NDPA_UNIT")
    private String ndpaUnit;

    @ApiModelProperty(value = "参数数据类型")
    @Param
    @TableField(value = "NDPA_DATATYPE")
    private String ndpaDatatype;

    @ApiModelProperty(value = "参数长度")
    @TableField(value = "NDPA_STR_LEN")
    private String ndpaStrLen;

    @ApiModelProperty(value = "显示模式")
    @TableField(value = "NDPA_SHOW_MODE")
    @Param
    private String ndpaShowMode;

    @ApiModelProperty(value = "最大值")
    @TableField(value = "NDPA_VAL_MAX")
    private String ndpaValMax;

    @ApiModelProperty(value = "最小值")
    @TableField(value = "NDPA_VAL_MIN")
    private String ndpaValMin;

    @ApiModelProperty(value = "步进")
    @TableField(value = "NDPA_VAL_STEP")
    private String ndpaValStep;

    @ApiModelProperty(value = "下拉值域")
    @TableField(value = "NDPA_SELECT_DATA")
    private String ndpaSelectData;

    @ApiModelProperty(value = "字节长度")
    @TableField(value = "NDPA_BYTE_LEN")
    private String ndpaByteLen;

    @ApiModelProperty(value = "命令标识")
    @TableField(value = "NDPA_CMD_MARK")
    private String ndpaCmdMark;

    @ApiModelProperty(value = "参数状态")
    @Param
    @TableField(value = "NDPA_STATUS")
    private String ndpaStatus;

    @ApiModelProperty(value = "是否该字段提供给54所访问")
    @Param
    @TableField(value = "NDPA_OUTTER_STATUS")
    private String ndpaOutterStatus;

    @ApiModelProperty(value = "提供给54所时 数据映射规则")
    @TableField(value = "NDPA_TRANS_RULE")
    private String ndpaTransRule;

    @ApiModelProperty(value = "字段类型")
    @Param
    @TableField(value = "NDPA_ALERT_PARA")
    private String ndpaAlertPara;

    @ApiModelProperty(value = "报警级别")
    @Param
    @TableField(value = "NDPA_ALERT_LEVEL")
    private String ndpaAlertLevel;

    @ApiModelProperty(value = "拼装样式")
    @TableField(value = "NDPA_SPELL_FMT")
    private String ndpaSpellFmt;

    @ApiModelProperty(value = "显示样式")
    @TableField(value = "NDPA_VIEW_FMT")
    private String ndpaViewFmt;

    @ApiModelProperty(value = "上级参数编号")
    @TableField(value = "NDPA_PARENT_NO")
    private String ndpaParentNo;

    @ApiModelProperty(value = "复杂级别")
    @TableField(value = "NDPA_CMPLEX_LEVEL")
    private String ndpaCmplexLevel;

    @ApiModelProperty(value = "缺省值")
    @TableField(value = "NDPA_DEFAULT_VAL")
    private String ndpaDefaultVal;

    @ApiModelProperty(value = "设备类型编码")
    @TableField(exist = false)
    private String devTypeCode;

    @Override
    protected Serializable pkVal() {
        return this.ndpaId;
    }

}
