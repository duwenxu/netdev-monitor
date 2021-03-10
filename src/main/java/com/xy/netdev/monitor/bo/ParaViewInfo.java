package com.xy.netdev.monitor.bo;



import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 设备参数扩展类
 * </p>
 *
 * @author tangxl
 * @since 2021-03-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "ParaViewInfo", description = "设备参数扩展类")
public class ParaViewInfo {
    @ApiModelProperty(value = "参数ID")
    private Integer paraId;

    @ApiModelProperty(value = "参数编号")
    private Integer paraNo;

    @ApiModelProperty(value = "参数值")
    private String paraVal;

    /**
     * 0022001 只写  0022002 只读  0022003读写
     */
    @ApiModelProperty(value = "访问权限")
    private String accessRight;

    @ApiModelProperty(value = "参数单位")
    private String paraUnit;

    @ApiModelProperty(value = "参数数据类型")
    private String paraDatatype;

    @ApiModelProperty(value = "参数长度")
    private String paraStrLen;

    /**
     * 0024001 文本框  0024002 下拉框
     */
    @ApiModelProperty(value = "显示模式")
    private String parahowMode;

    @ApiModelProperty(value = "最大值")
    private String paraValMax;

    @ApiModelProperty(value = "最小值")
    private String paraValMin;

    @ApiModelProperty(value = "步进")
    private String paraValStep;
    /**
     * 参数表中 0020
     */
    @ApiModelProperty(value = "设备类型")
    private String devType;
    /**
     * 参数表中 0020 中编码
     */
    @ApiModelProperty(value = "设备类型编码")
    private String devTypeCode;

    @ApiModelProperty(value = "设备编号")
    private String devNo;
    /**
     *  下拉框显示数据,当显示模式 是0024002 需要赋值下拉框列表
     */
    @ApiModelProperty(value = "下拉框显示数据")
    private List<ParaSpinnerInfo> spinnerInfoList = new ArrayList<>();

}
