package com.xy.netdev.monitor.vo;

import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.monitor.entity.ParaInfo;
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

    @ApiModelProperty(value = "id(接口编码+设备类型)")
    private String id;

    @ApiModelProperty(value = "设备接口")
    private Interface devInterface;

    @ApiModelProperty(value = "接口协议")
    private PrtclFormat interfacePrtcl;

    @ApiModelProperty(value = "参数列表")
    private List<ParaInfo> devParamList;
}
