package com.xy.netdev.frame.bo;

import lombok.*;

import java.util.List;

/**
 * 参数处理扩展配置实体
 *
 * @author duwenxu
 * @create 2021-04-06 10:19
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ExtParamConf {

    /**
     * start : 1
     * point : 2
     * ext : [2]
     */

    private Integer start;
    private Integer point;
    private List<Object> ext;
}
