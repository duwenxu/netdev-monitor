package com.xy.netdev.SpacePreset.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xy.netdev.SpacePreset.entity.NtdvSpacePreset;
import com.xy.netdev.SpacePreset.mapper.NtdvSpacePresetMapper;
import com.xy.netdev.SpacePreset.service.INtdvSpacePresetService;
import com.xy.netdev.admin.entity.SysParam;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.transit.IDevCmdSendService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.xy.netdev.common.constant.SysConfigConstant.*;

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
        //通过信标频率设置本振值：每个本振对应一个信标频率
        SysParam  sysParamA= sysParamService.getById(LOCAL_VIBRATE_A);
        SysParam  sysParamB= sysParamService.getById(LOCAL_VIBRATE_B);
        SysParam  sysParamC= sysParamService.getById(LOCAL_VIBRATE_C);
        SysParam  sysParamD= sysParamService.getById(LOCAL_VIBRATE_D);
        String localVibr = sysParamService.getParaName(spacePreset.getSpLocalOscillator());
        if(Double.valueOf(sysParamA.getRemark2()) < Double.valueOf(value) && Double.valueOf(value)< Double.valueOf(sysParamA.getRemark3())){
            //如在A本振范围内则设置本振为A
            localVibr = sysParamService.getParaName(LOCAL_VIBRATE_A);
        }else if(Double.valueOf(sysParamB.getRemark2()) < Double.valueOf(value) && Double.valueOf(value)< Double.valueOf(sysParamB.getRemark3())){
            //如在A本振范围内则设置本振为B
            localVibr = sysParamService.getParaName(LOCAL_VIBRATE_B);
        }else if(Double.valueOf(sysParamC.getRemark2()) < Double.valueOf(value) && Double.valueOf(value)< Double.valueOf(sysParamC.getRemark3())){
            //如在A本振范围内则设置本振为C
            localVibr = sysParamService.getParaName(LOCAL_VIBRATE_C);
        }else if(Double.valueOf(sysParamD.getRemark2()) < Double.valueOf(value) && Double.valueOf(value)< Double.valueOf(sysParamD.getRemark3())){
            //如在A本振范围内则设置本振为D
            localVibr = sysParamService.getParaName(LOCAL_VIBRATE_D);
        }
        //1.执行设置本振及频率
        if(StringUtils.isNotEmpty(spacePreset.getSpLocalOscillator())){
            value = "{Z}["+localVibr+"]{F}["+value+"]";
        }
        devCmdSendService.paraCtrSend(spacePreset.getDevNo(),"cmdsj", value);
        //2.执行一键对星
        value = "["+spacePreset.getSpLongitude()+"]{M}["+sysParamService.getParaRemark1(spacePreset.getSpPolarization())+"]";
        devCmdSendService.paraCtrSend(spacePreset.getDevNo(),"cmdso", value);
    }
}
