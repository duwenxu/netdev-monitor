package com.xy.netdev.admin.mapper;

import com.xy.netdev.admin.entity.SysParam;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 参数信息 Mapper 接口
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-17
 */
public interface SysParamMapper extends BaseMapper<SysParam> {

    /**
     * 通过父id查询出参数表
     * @param parentId
     * @return
     */
    public List<SysParam> queryParamsByParentId(@Param("paraParentId") String parentId);

    /**
     * 通过参数查询出参数表
     * @param paraName
     * @return
     */
    public List<SysParam> queryParamByName(@Param("paraName") String paraName);

}
