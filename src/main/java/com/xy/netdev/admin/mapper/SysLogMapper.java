package com.xy.netdev.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xy.netdev.admin.entity.SysLog;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 系统日志表 Mapper 接口
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-17
 */
public interface SysLogMapper extends BaseMapper<SysLog> {

    /**
     * @功能：清空所有日志记录
     */
    public void removeAll();

    /**
     * 获取系统总访问次数
     *
     * @return Long
     */
    Long findTotalVisitCount();

    /**
     * 获取系统今日访问次数
     *
     * @return Long
     */
    Long findTodayVisitCount(@Param("dayStart") Date dayStart, @Param("dayEnd") Date dayEnd);

    /**
     * 获取系统今日访问 IP数
     *
     * @return Long
     */
    Long findTodayIp(@Param("dayStart") Date dayStart, @Param("dayEnd") Date dayEnd);

    /**
     *   首页：根据时间统计访问数量/ip数量
     * @param dayStart
     * @param dayEnd
     * @return
     */
    List<Map<String,Object>> findVisitCount(@Param("dayStart") Date dayStart, @Param("dayEnd") Date dayEnd);
}
