package com.xy.netdev.frame.bo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * <p>
 * 协议解析请求数据
 * </p>
 *
 * @author tangxl
 * @since 2021-03-10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FrameReqData {

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
     * 参数表中  0026001 查询  0026003 控制
     */
    @ApiModelProperty(value = "操作类型")
    private String operType;

    /**
     * 0 成功  1 失败
     */
    @ApiModelProperty(value = "发送是否成功")
    private String isOk;

    @ApiModelProperty(value = "协议解析与收发层交互的数据体")
    private byte[] paramBytes;

    @ApiModelProperty(value = "发送的原始数据")
    private String sendOrignData;

    @ApiModelProperty(value = "帧参数列表")
    private List<FrameParaData> frameParaList;
}
