package com.xy.netdev.monitor.service.impl;

import cn.hutool.Hutool;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xy.netdev.admin.entity.SysParam;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.util.BeanCopierUtil;
import com.xy.netdev.container.BaseContainerLoader;
import com.xy.netdev.monitor.bo.TransUiData;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.TruckInfo;
import com.xy.netdev.monitor.entity.ParaInfo;
import com.xy.netdev.monitor.mapper.TruckInfoMapper;
import com.xy.netdev.monitor.service.IBaseInfoService;
import com.xy.netdev.monitor.service.IParaInfoService;
import com.xy.netdev.monitor.service.ITruckInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static com.xy.netdev.common.constant.SysConfigConstant.*;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class TruckInfoServiceImpl extends ServiceImpl<TruckInfoMapper, TruckInfo> implements ITruckInfoService {

    @Autowired
    private IBaseInfoService devInfoService;
    @Autowired
    private BaseContainerLoader baseContainerLoader;
    @Autowired
    private ISysParamService sysParamService;
    /**设备类型参数组父ID**/
    private static final String DEV_TYPE_GROUP_ID = "0020";

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
        TruckInfo anTruckInfo = this.baseMapper.selectById(id);
        //分解设备接口绑定的参数code
        List<String> devTypes = new ArrayList<>();
        if(!StringUtils.isBlank(anTruckInfo.getTruckDevs())){
            devTypes = Arrays.asList(anTruckInfo.getTruckDevs().split(","));
        }
        List<SysParam> devTypeList = new ArrayList<>();
        List<SysParam> types = sysParamService.queryParamsByParentId(DEV_TYPE_GROUP_ID);
        devTypeList.addAll(types);
        Map<String, SysParam> devTypesMap = devTypeList.stream().collect(Collectors.toMap(SysParam::getParaCode, SysParam -> SysParam));
        List<String> finalDevTypes = devTypes;
        if(isBing){
            //置空重新添加数据
            devTypeList.clear();
            for (String devType : finalDevTypes){
                devTypeList.add(devTypesMap.get(devType));
            }
        }else{
            devTypeList = devTypeList.stream().filter(sysParam -> !finalDevTypes.contains(sysParam.getParaCode())).collect(Collectors.toList());
        }
        List<TransUiData> dataList = new ArrayList<>();
        //封装前端穿梭框数据
        devTypeList.stream().filter(Objects::nonNull).forEach(sysParam ->{
            TransUiData data = new TransUiData();
            data.setId(sysParam.getParaCode());
            data.setValue(sysParam.getParaName());
            data.setValue2(id);
            data.setIsSelect(isSelect);
            dataList.add(data);
        });
        return dataList;
    }


}
