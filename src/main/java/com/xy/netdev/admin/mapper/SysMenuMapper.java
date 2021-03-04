package com.xy.netdev.admin.mapper;

import com.xy.netdev.admin.entity.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 菜单信息表 Mapper 接口
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-17
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {


    public List<SysMenu> queryMenuByRole(Map<String,Object> queryMap);

}
