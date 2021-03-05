package com.xy.netdev.frame.bo;



import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 数据体中数据参数
 * </p>
 *
 * @author tangxl
 * @since 2021-03-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "DataBodyPara对象", description = "数据体中参数")
public class DataBodyPara {
    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "参数ID")
    private String paraId;

    @ApiModelProperty(value = "参数编号")
    private String paraNo;

    @ApiModelProperty(value = "参数序号")
    private Integer paraSeq;

    @ApiModelProperty(value = "字节长度")
    private Integer byteLen;

    @ApiModelProperty(value = "参数值")
    private String paraVal;

}
