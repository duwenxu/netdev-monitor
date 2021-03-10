package com.xy.netdev.frame.bo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * 协议解析响应数据
 * </p>
 *
 * @author tangxl
 * @since 2021-03-10
 */
@Data
public class FrameRespData {

    @ApiModelProperty(value = "命令标识符")
    private String cmdMark;
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
     * 响应标注 成功  失败 等信息时 填写
     */
    @ApiModelProperty(value = "响应码")
    private String respCode;

    @ApiModelProperty(value = "帧参数列表")
    private List<FrameParaData> frameParaList;

}
