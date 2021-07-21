package com.xy.netdev.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.xy.common.annotation.KeyCode;
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

    @ApiModelProperty(value = "最大值1")
    @TableField(value = "NDPA_VAL_MAX1")
    private String ndpaValMax1;

    @ApiModelProperty(value = "最小值1")
    @TableField(value = "NDPA_VAL_MIN1")
    private String ndpaValMin1;

    @ApiModelProperty(value = "最大值2")
    @TableField(value = "NDPA_VAL_MAX2")
    private String ndpaValMax2;

    @ApiModelProperty(value = "最小值2")
    @TableField(value = "NDPA_VAL_MIN2")
    private String ndpaValMin2;

    @ApiModelProperty(value = "设备XML模型数据格式")
    @TableField(value = "NDPA_VAL_FORMAT")
    private String ndpaValFormat;

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

    @ApiModelProperty(value = "设备状态上报数据映射规则")
    @TableField(value = "NDPA_TRANS_RULE")
    private String ndpaTransRule;

    @ApiModelProperty(value = "枚举类型参数，上报规则")
    @TableField(value = "NDPA_COMB_RULE")
    private String ndpaCombRule;

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
    @Param
    private String ndpaCmplexLevel;

    @ApiModelProperty(value = "缺省值")
    @TableField(value = "NDPA_DEFAULT_VAL")
    private String ndpaDefaultVal;

    @ApiModelProperty(value = "关联类型")
    @Param
    @TableField(value = "NDPA_LINK_TYPE")
    private String ndpaLinkType;

    @ApiModelProperty(value = "关联参数编码")
    @TableField(value = "NDPA_LINK_CODE")
    private String ndpaLinkCode;

    @ApiModelProperty(value = "关联值")
    @TableField(value = "NDPA_LINK_VAL")
    private String ndpaLinkVal;

    @ApiModelProperty(value = "是否重要：0 不重要 1 重要  2隐藏")
    @TableField(value = "NDPA_IS_IMPORTANT")
    private Integer ndpaIsImportant;

    @ApiModelProperty(value = "备注一描述")
    @TableField(value = "NDPA_REMARK1_DESC")
    private String ndpaRemark1Desc;

    @ApiModelProperty(value = "备注一数据")
    @TableField(value = "NDPA_REMARK1_DATA")
    private String ndpaRemark1Data;

    @ApiModelProperty(value = "备注二描述")
    @TableField(value = "NDPA_REMARK2_DESC")
    private String ndpaRemark2Desc;

    @ApiModelProperty(value = "备注二数据")
    @TableField(value = "NDPA_REMARK2_DATA")
    private String ndpaRemark2Data;

    @ApiModelProperty(value = "备注三描述")
    @TableField(value = "NDPA_REMARK3_DESC")
    private String ndpaRemark3Desc;

    @ApiModelProperty(value = "备注三数据")
    @TableField(value = "NDPA_REMARK3_DATA")
    private String ndpaRemark3Data;

    @ApiModelProperty(value = "设备类型编码")
    @TableField(exist = false)
    private String devTypeCode;

    @ApiModelProperty(value = "设备编号")
    @TableField(exist = false)
    private String devNo;

    @ApiModelProperty(value = "上报OID")
    @TableField(value = "NDPA_RPT_OID")
    private String ndpaRptOid;

    @Override
    protected Serializable pkVal() {
        return this.ndpaId;
    }

}
