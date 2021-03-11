package com.xy.netdev.rpt.bo;

import com.xy.netdev.frame.bo.FrameReqData;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 定时查询数据封装类
 *
 * @author duwenxu
 * @create 2021-03-10 16:51
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduleReqBody {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "处理类")
    private Object handlerClass;

    @ApiModelProperty(value = "查询信息body")
    private FrameReqData frameReqData;
}
