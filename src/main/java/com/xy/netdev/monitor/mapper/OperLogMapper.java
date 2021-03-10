package com.xy.netdev.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xy.netdev.monitor.entity.OperLog;
import org.apache.ibatis.annotations.Mapper;
/**
 * 操作日志信息 Mapper 接口
 * @author admin
 * @date 2021-03-05
 */
@Mapper
public interface OperLogMapper extends BaseMapper<OperLog> {

}
