package com.xy.netdev.monitor.bo;


import com.xy.netdev.monitor.entity.Interface;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

/**
 * 设备接口
 *
 * @author admin
 * @date 2021-03-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ApiModel(value="InterfaceViewInfo对象", description="设备接口显示对象")
public class InterfaceViewInfo extends Interface {

    private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "设备编号")
    private String devNo;


    @ApiModelProperty(value = "接口参数列表")
    private List<ParaViewInfo> subParaList = new ArrayList<>();


    @ApiModelProperty(value = "接口列表")
    private List<InterfaceViewInfo> subInterList = new ArrayList<>();
}
