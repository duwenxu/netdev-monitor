package com.xy.netdev.frame.bo;



import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 数据体
 * </p>
 *
 * @author tangxl
 * @since 2021-03-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "DataBody对象", description = "协议中数据体")
public class DataBody {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "字节长度")
    private Integer byteLen;

    @ApiModelProperty(value = "节点编码")
    private String nodeCode;

    @ApiModelProperty(value = "节点ID")
    private String nodeId;

    @ApiModelProperty(value = "节点播放内容")
    private String nodeSpeakCxt;

    @ApiModelProperty(value = "对应弧段编号(随着弧段执行发生变化)")
    private String arcId;

    @ApiModelProperty(value = "圈号(随着弧段执行发生变化)")
    private String arcNum;
}
