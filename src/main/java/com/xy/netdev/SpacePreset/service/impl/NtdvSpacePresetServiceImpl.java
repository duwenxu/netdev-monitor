package com.xy.netdev.SpacePreset.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xy.netdev.SpacePreset.entity.NtdvSpacePreset;
import com.xy.netdev.SpacePreset.mapper.NtdvSpacePresetMapper;
import com.xy.netdev.SpacePreset.service.INtdvSpacePresetService;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.transit.IDevCmdSendService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 值班安排 服务实现类
 * </p>
 *
 * @author zb
 * @since 2021-06-09
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class NtdvSpacePresetServiceImpl extends ServiceImpl<NtdvSpacePresetMapper, NtdvSpacePreset> implements INtdvSpacePresetService {

    @Autowired
    private IDevCmdSendService devCmdSendService;
    @Autowired
    private ISysParamService sysParamService;

    /**
     * 预置卫星执行一键对星功能
     * @param spacePreset  预置卫星信息
     */
    @Override
    public void keyStarByPolar(NtdvSpacePreset spacePreset) {
        String value = spacePreset.getSpBeaconFrequency();
        //1.执行设置本振及频率
        if(StringUtils.isNotEmpty(spacePreset.getSpLocalOscillator())){
            value = "{Z}["+sysParamService.getParaName(spacePreset.getSpLocalOscillator())+"]{F}["+value+"]";
        }
        devCmdSendService.paraCtrSend(spacePreset.getDevNo(),"cmdsj", value);
        //2.执行一键对星
        value = "["+spacePreset.getSpLongitude()+"]{M}["+sysParamService.getParaRemark1(spacePreset.getSpPolarization())+"]";
        devCmdSendService.paraCtrSend(spacePreset.getDevNo(),"cmdso", value);
    }
}
