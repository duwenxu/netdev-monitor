package com.xy.netdev.frame.bo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@NoArgsConstructor
@AllArgsConstructor
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
     * 若协议中未定义响应码 则 0为成功  1为失败,协议中定义响应码  则协议格式中需要配置 查询响应类型/控制响应类型
     */
    @ApiModelProperty(value = "响应码")
    private String respCode;

    @ApiModelProperty(value = "协议解析与收发层交互的数据体")
    private byte[] paramBytes;

    @ApiModelProperty(value = "接收到的原始数据")
    private String reciveOriginalData;

    @ApiModelProperty(value = "帧参数列表")
    private List<FrameParaData> frameParaList;

}
