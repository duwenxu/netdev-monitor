package com.xy.netdev.frame.protocol.xml;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Xml协议描述格式
 *
 * @author cc
 */
@Setter
@Getter
public class ProtocolXmlEntity {
    @ApiModelProperty("设备标识")
    private String deviceMark;

    @ApiModelProperty("参数类型")
    private List<ParamBodyEntity> bodyEntities;

    @Setter
    @Getter
    public static class ParamBodyEntity{
        @ApiModelProperty("参数标识")
        private String paramMark;

        @ApiModelProperty("数组体")
        List<ParamEntity> paramEntities;
    }

    @Getter
    @Setter
    public static class ParamEntity{
        @ApiModelProperty("名称")
        private String name;

        @ApiModelProperty("编号")
        private Integer no;

        @ApiModelProperty("起始位")
        private Integer offset;

        @ApiModelProperty("长度")
        private Integer length;

        @ApiModelProperty("值")
        private String value;

        @ApiModelProperty("字节序")
        private Integer order;

        @ApiModelProperty("是否数据体")
        private Boolean isDataBody;
    }



}
