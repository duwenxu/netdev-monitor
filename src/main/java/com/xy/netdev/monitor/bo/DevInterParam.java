package com.xy.netdev.monitor.bo;

import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.monitor.entity.PrtclFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * <p>
 * 设备接口参数实体类
 * </p>
 *
 * @author sunchao
 * @since 2021-03-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "设备接口参数实体类", description = "设备接口参数实体类")
public class DevInterParam {

    @ApiModelProperty(value = "id(设备类型+命令标识)")
    private String id;

    @ApiModelProperty(value = "设备接口")
    private Interface devInterface;

    @ApiModelProperty(value = "解析协议")
    private PrtclFormat interfacePrtcl;

    @ApiModelProperty(value = "帧参数列表")
    private List<FrameParaInfo> devParamList;

    @ApiModelProperty(value = "子接口列表")
    private List<DevInterParam> subItfList;

    public void addFramePara(FrameParaInfo frameParaInfo){
        devParamList.add(frameParaInfo);
    }
}
