package com.xy.netdev.rpt.bo;




import com.xy.netdev.monitor.entity.ParaInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.util.List;

/**
 * <p>
 * 上报数据参数
 * </p>
 *
 * @author tangxl
 * @since 2021-03-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "RptBodyDev对象", description = "上报中设备")
public class RptBodyDev {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "设备类型编码")
    private String devTypeCode;

    @ApiModelProperty(value = "设备编号")
    private String devNo;

    @ApiModelProperty(value = "设备参数数量")
    private String devParaTotal;

    @ApiModelProperty(value = "设备参数列表")
    private List<ParaInfo> devParaList;



}
