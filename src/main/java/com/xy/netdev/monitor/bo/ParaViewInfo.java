package com.xy.netdev.monitor.bo;


import com.baomidou.mybatisplus.annotation.TableField;
import com.xy.common.annotation.Param;
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
    private String paraNo;

    @ApiModelProperty(value = "参数编码")
    private String paraCode;

    @ApiModelProperty(value = "参数名称")
    private String paraName;

    @ApiModelProperty(value = "参数命令标识符")
    private String paraCmdMark;

    @ApiModelProperty(value = "参数值")
    private String paraVal;

    @ApiModelProperty(value = "参数原始字节数组")
    private byte[] paraOrigByte;

    /**
     * 0022001 只写  0022002 只读  0022003读写  0022004无权限  0022005命令
     */
    @ApiModelProperty(value = "访问权限")
    private String accessRight;

    @ApiModelProperty(value = "参数单位")
    private String paraUnit;
    /**
     * 0023001 byte  0023002 int  0023003 unit  0023004 str 0023005 buf  0023006 ipAddress  0023007 ipMask
     */
    @ApiModelProperty(value = "参数数据类型")
    private String paraDatatype;

    /**
     * 0 整数   1 字符串  2浮点数
     */
    @ApiModelProperty(value = "参数简易数据类型")
    private String paraSimpleDatatype;

    /**
     * 参数数据类型是 0023004 时,设置时需判断长度
     */
    @ApiModelProperty(value = "参数长度")
    private String paraStrLen;

    @ApiModelProperty(value = "是否该字段提供给54所访问")
    private String ndpaOutterStatus;

    @ApiModelProperty(value = "字节长度")
    private String paraByteLen;

    /**
     * 0024001 文本框  0024002 下拉框
     */
    @ApiModelProperty(value = "显示模式")
    private String parahowMode;

    /**
     * 参数数据类型是 0023001,0023002,0023003 时,并数据合法时,设置时需判断最大值
     */
    @ApiModelProperty(value = "最大值")
    private String paraValMax1;

    /**
     * 参数数据类型是 0023001,0023002,0023003 时,设置时需判断最小值
     */
    @ApiModelProperty(value = "最小值")
    private String paraValMin1;

    /**
     * 参数数据类型是 0023001,0023002,0023003 时,并数据合法时,设置时需判断最大值
     */
    @ApiModelProperty(value = "最大值")
    private String paraValMax2;

    /**
     * 参数数据类型是 0023001,0023002,0023003 时,设置时需判断最小值
     */
    @ApiModelProperty(value = "最小值")
    private String paraValMin2;

    /**
     * 参数数据类型是 0023001,0023002,0023003 时,并数据合法时,设置时需添加加减按钮步进
     */
    @ApiModelProperty(value = "步进")
    private String paraValStep;

    /**
     * 当复杂级别是0019002-复杂参数 0019003-组合参数时,需要对参数值进行解析,按照显示样式展示,
     * 参数修改提交时按照拼装样式进行拼装提交
     * 参数 []
     * 分割符{}
     *
     * 例一:xxx_yyy
     * 数据拼装样式 [A]{-}[B]
     * 数据显示样式 A[A]-B[B]
     * 例二:Xxxx.xxYxxx.xxZxxx.xx
     * 数据拼装样式 {X}[A]{Y}[B]{Z}[C]
     * 数据显示样式 X[A]-Y[B]-Z[C]
     */
    @ApiModelProperty(value = "拼装样式")
    private String paraSpellFmt;

    @ApiModelProperty(value = "显示样式")
    private String paraViewFmt;
    /**
     * 0019001-简单参数 0019002-复杂参数 0019003-组合参数 0019004-子参数
     */
    @ApiModelProperty(value = "复杂级别")
    private String paraCmplexLevel;

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
     * 仅复杂级别为0019004-子参数时有效
     * 0018001-独立参数 0018002-值关联  0018003-数据组合
     * 0018003 需要再页面显示父参数  框 子参数
     */
    @ApiModelProperty(value = "子参数关联类型")
    private String subParaLinkType;

    /**
     * 仅复杂级别为0019004-子参数   子参数关联类型 为0018002-值关联 时有效
     * 该条参数关联 同一父参数的子参数的编码
     */
    @ApiModelProperty(value = "子参数关联参数编码")
    private String subParaLinkCode;

    /**
     * 仅复杂级别为0019004-子参数   子参数关联类型 为0018002-值关联 时有效
     * 该条参数关联 同一父参数的子参数的值
     */
    @ApiModelProperty(value = "子参数关联值")
    private String subParaLinkVal;
    /**
     *  下拉框显示数据,当显示模式 是0024002 需要赋值下拉框列表
     */
    @ApiModelProperty(value = "下拉框显示数据")
    private List<ParaSpinnerInfo> spinnerInfoList = new ArrayList<>();

    /**
     * 当复杂级别是0019003-组合参数时,复杂参数的子参数列表
     */
    @ApiModelProperty(value = "子参数列表")
    private List<ParaViewInfo> subParaList = new ArrayList<>();

    /**
     * 是否在拓扑图显示
     */
    @ApiModelProperty(value = "是否在拓扑图显示")
    private Boolean ndpaIsTopology ;

    /**
     * 是否返回前端显示
     */
    @ApiModelProperty(value = "是否返回前端显示")
    private Boolean isShow = true;

    /**
     * 添加子参数
     */
    public void  addSubPara(ParaViewInfo subParaViewInfo){
        subParaList.add(subParaViewInfo);
    }

}
