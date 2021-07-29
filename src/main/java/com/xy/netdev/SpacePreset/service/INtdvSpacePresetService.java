package com.xy.netdev.SpacePreset.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xy.netdev.SpacePreset.entity.NtdvSpacePreset;

/**
 * <p>
 * 卫星预置信息 服务类
 * </p>
 *
 * @author zb
 * @since 2021-06-09
 */
public interface INtdvSpacePresetService extends IService<NtdvSpacePreset> {

    /**
     * 预置卫星执行一键对星功能
     * @param spacePreset  预置卫星信息
     */
    void keyStarByPolar(NtdvSpacePreset spacePreset);
}
