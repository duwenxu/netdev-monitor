package com.xy.netdev.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.monitor.bo.TransUiData;
import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.monitor.entity.ParaInfo;
import com.xy.netdev.monitor.mapper.InterfaceMapper;
import com.xy.netdev.monitor.service.IInterfaceService;
import com.xy.netdev.monitor.service.IParaInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设备接口 服务实现类
 *
 * @author admin
 * @date 2021-03-05
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class InterfaceServiceImpl extends ServiceImpl<InterfaceMapper, Interface> implements IInterfaceService {

    @Autowired
    private IParaInfoService paraInfoService;

    /**
     * 设备接口已绑定参数列表
     * @param id 设备接口id
     * @return
     */
    @Override
    public List<TransUiData> getlLinkedParams(String id) {
        return formatTransUiData(id,true,false);
    }

    /**
     * 设备接口未绑定参数列表
     * @param id 设备接口id
     * @return
     */
    @Override
    public List<TransUiData> getUnlinkedParams(String id) {
        return formatTransUiData(id,false,false);
    }

    /**
     * 设备接口参数绑定数据转换
     * @param id 设备接口id
     * @param isSelect
     * @return
     */
    private List<TransUiData> formatTransUiData(String id, boolean isBing,boolean isSelect){
        //获取设备接口信息
        Interface anInterface = this.baseMapper.selectById(id);
        //分解设备接口绑定的参数code
        List<String> paraIds = Arrays.asList(anInterface.getItfDataFormat().split(","));
        //获取有效的非子参数的和接口设备类型相同的设备参数列表
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("NDPA_STATUS",SysConfigConstant.STATUS_OK);
        queryWrapper.eq("DEV_TYPE",anInterface.getDevType());
        queryWrapper.ne("NDPA_CMPLEX_LEVEL",SysConfigConstant.PARA_COMPLEX_LEVEL_SUB);
        List<ParaInfo> paraInfos = paraInfoService.list(queryWrapper);
        if(isBing){
            paraInfos = paraInfos.stream().filter(paraInfo -> paraIds.contains(paraInfo.getNdpaId().toString())).collect(Collectors.toList());
        }else{
            paraInfos = paraInfos.stream().filter(paraInfo -> !paraIds.contains(paraInfo.getNdpaId().toString())).collect(Collectors.toList());
        }
        List<TransUiData> dataList = new ArrayList<>();
        //封装前端穿梭框数据
        paraInfos.forEach(paraInfo ->{
            TransUiData data = new TransUiData();
            data.setId(paraInfo.getNdpaId().toString());
            data.setValue(paraInfo.getNdpaName());
            data.setValue2(id);
            data.setIsSelect(isSelect);
            dataList.add(data);
        });
        return dataList;
    }
}
