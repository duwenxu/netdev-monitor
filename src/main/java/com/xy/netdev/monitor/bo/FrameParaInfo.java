package com.xy.netdev.monitor.bo;


import com.xy.netdev.monitor.entity.PrtclFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;


/**
 * <p>
 * 帧参数
 * </p>
 *
 * @author tangxl
 * @since 2021-03-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "FrameParaInfo", description = "帧参数")
public class FrameParaInfo {

    @ApiModelProperty(value = "参数ID")
    private Integer paraId;

    @ApiModelProperty(value = "参数编号")
    private String paraNo;

    @ApiModelProperty(value = "参数编码")
    private String paraCode;

    @ApiModelProperty(value = "参数名称")
    private String paraName;

    @ApiModelProperty(value = "参数值")
    private String paraVal;

    @ApiModelProperty(value = "参数序号")
    private Integer paraSeq;

    @ApiModelProperty(value = "命令标识符")
    private String cmdMark;

    @ApiModelProperty(value = "字节长度")
    private String paraByteLen;
    /**
     * 按照序号 累加 字节长度
     */
    @ApiModelProperty(value = "参数下标")
    private Integer paraStartPoint;
    /**
     * 参数表中 0020
     */
    @ApiModelProperty(value = "设备类型")
    private String devType;

    @ApiModelProperty(value = "访问权限")
    private String ndpaAccessRight;

    /**
     * 参数表中 0020 中编码
     */
    @ApiModelProperty(value = "设备类型编码")
    private String devTypeCode;

    /*@ApiModelProperty(value = "设备编号")
    private String devNo;*/

    @ApiModelProperty(value = "告警级别")
    private String alertLevel;

    @ApiModelProperty(value = "解析协议")
    private PrtclFormat interfacePrtcl;

    @ApiModelProperty(value = "数据内外转换值域")
    private String transRule;

    @ApiModelProperty(value = "字段类型")
    private String alertPara;

    @ApiModelProperty(value = "数值类型")
    private String dataType;

    @ApiModelProperty(value = "备注一描述")
    private String ndpaRemark1Desc;

    @ApiModelProperty(value = "备注一数据")
    private String ndpaRemark1Data;

    @ApiModelProperty(value = "备注二描述")
    private String ndpaRemark2Desc;

    @ApiModelProperty(value = "备注二数据")
    private String ndpaRemark2Data;

    /**
     * 下拉框显示数据,当显示模式 是0024002 需要赋值下拉框列表
     */
    @ApiModelProperty(value = "下拉值域")
    private Map<String,Object> selectMap;

    @ApiModelProperty(value = "子参数列表")
    private List<FrameParaInfo> subParaList;

    /**
     * 增加参数的子参数
     * @param frameParaInfo
     */
    public void addSubPara(FrameParaInfo frameParaInfo){
        subParaList.add(frameParaInfo);
    }
}
