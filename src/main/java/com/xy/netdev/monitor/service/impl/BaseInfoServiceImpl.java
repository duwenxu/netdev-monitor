package com.xy.netdev.monitor.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.mapper.BaseInfoMapper;
import com.xy.netdev.monitor.service.IBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 设备信息 服务实现类
 *
 * @author admin
 * @date 2021-03-05
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class BaseInfoServiceImpl extends ServiceImpl<BaseInfoMapper, BaseInfo> implements IBaseInfoService {

    @Override
    public Map<String, Object> baseInfoMenuMap() {
        QueryWrapper<BaseInfo> wrapper = new QueryWrapper<>();
        //所有可用的设备
        wrapper.eq("DEV_STATUS", SysConfigConstant.DEV_STATUS_NEW);
        List<BaseInfo> baseInfos = this.baseMapper.selectList(wrapper);
        //顶级菜单设备信息
        List<BaseInfo> topMenu = baseInfos.stream().filter(base -> StringUtils.isEmpty(base.getDevParentNo())).collect(Collectors.toList());
        LinkedHashMap<String, Object> topMap = new LinkedHashMap<>();
        //递归拼接
        assembleOneMenu(baseInfos, topMenu, topMap);
        return topMap;
    }

    /**
     * 拼接单层设备菜单
     *
     * @param baseInfos 设备信息全集列表
     * @param topMenu   顶级设备
     * @param topMap    最外层的Map结构
     * @return 当前层的菜单结构
     */
    private LinkedHashMap<String, Object> assembleOneMenu(List<BaseInfo> baseInfos, List<BaseInfo> topMenu, LinkedHashMap<String, Object> topMap) {
        topMenu.stream().forEach(menu -> {
            String currentDevName = menu.getDevName();
            //当前设备Map
            LinkedHashMap<String, Object> currentBaseMap = JSONObject.parseObject(JSONObject.toJSONString(menu), LinkedHashMap.class);
            //获取子设备
            List<BaseInfo> subList = baseInfos.stream().filter(base -> menu.getDevNo().equals(base.getDevParentNo())).collect(Collectors.toList());
            LinkedHashMap<String, Object> subMap = new LinkedHashMap<>();
            //将子设备列表转换为Map
            subList.forEach(targetInfo -> {
                LinkedHashMap map = JSONObject.parseObject(JSONObject.toJSONString(targetInfo), LinkedHashMap.class);
                subMap.put(targetInfo.getDevName(), map);
            });
            //添加子设备Map列表
            if (subMap.size() > 0) {
                currentBaseMap.put("subMap", subMap);
            }
            LinkedHashMap<String, Object> dfsSubMap = assembleOneMenu(baseInfos, subList, currentBaseMap);
            //存在subMap则插入到subMap中
            if (subList.size() != 0 && topMap.keySet().contains("subMap")) {
                ((LinkedHashMap) topMap.get("subMap")).put(currentDevName, dfsSubMap);
            }
            //顶级设备直接加入
            if (StringUtils.isEmpty(menu.getDevParentNo())) {
                topMap.put(currentDevName, dfsSubMap);
            }
        });
        return topMap;
    }
}
