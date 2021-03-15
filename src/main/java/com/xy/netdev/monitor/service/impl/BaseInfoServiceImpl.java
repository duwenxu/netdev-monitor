package com.xy.netdev.monitor.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xy.common.util.DateUtils;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.ParaHandlerUtil;
import com.xy.netdev.common.util.XmlUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.mapper.BaseInfoMapper;
import com.xy.netdev.monitor.service.IBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
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
    @Autowired
    private ISysParamService sysParamService;

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

    /**
     * 下载设备模型文件
     * @return
     */
    @Override
    public Map<String, Object> downDevFile() {
        Map<String,Object> maps = new HashMap<>();
        maps.put("fileName","DevModelFile_"+ DateUtils.getDateYMDHMS());
        maps.put("fileContext",generateDevModelFileMap().getBytes());
        return maps;
    }

    /**
     * 生成设备模型定义文件
     * map中key值增加-号，表示添加节点属性，key值为""字符串，则是直接赋值给当前节点
     * @return
     */
    private String generateDevModelFileMap(){
        List list = new ArrayList();
        Map<String,Object> map = new HashMap<>();
        Map<String,Object> fileMap = new HashMap<>();
        BaseInfoContainer.getDevInfos().forEach(baseInfo -> {
            Map<String,Object> devMap = new LinkedHashMap<>();
            //给dev节点增加属性值
            devMap.put("-type",sysParamService.getParaRemark1(baseInfo.getDevType()));
            devMap.put("-name",ParaHandlerUtil.generateEmptyStr(baseInfo.getDevName()));
            devMap.put("-ver",ParaHandlerUtil.generateEmptyStr(baseInfo.getDevVer()));
            devMap.put("-corp",sysParamService.getParaRemark1(baseInfo.getDevCorp()));
            devMap.put("-cname",ParaHandlerUtil.generateEmptyStr(baseInfo.getDevName()));
            List paraList = new ArrayList();
            DevParaInfoContainer.getDevParaViewList(baseInfo.getDevNo()).forEach(parainfo->{
                Map<String,Object> paraMap = new LinkedHashMap<>();
                //给param节点增加属性值
                paraMap.put("-no",parainfo.getParaNo());
                paraMap.put("-name",ParaHandlerUtil.generateEmptyStr(parainfo.getParaName()));
                paraMap.put("-access",ParaHandlerUtil.generateEmptyStr(sysParamService.getParaRemark1(parainfo.getAccessRight())));
                paraMap.put("-unit", ParaHandlerUtil.generateEmptyStr(parainfo.getParaUnit()));
                Map<String,Object> typeMap = new LinkedHashMap<>();
                //给type节点增加属性值
                typeMap.put("-name",sysParamService.getParaName(parainfo.getParaDatatype()));
                //当数据类型为字符串指定字符串的len
                if("0023004".equals(parainfo.getParaDatatype())){
                    typeMap.put("-len",parainfo.getParaStrLen());
                }
                paraMap.put("type",typeMap);
                Map<String,Object> showMap = new LinkedHashMap<>();
                showMap.put("-name",ParaHandlerUtil.generateEmptyStr(sysParamService.getParaRemark1(parainfo.getParahowMode())));
                if("0024002".equals(parainfo.getParahowMode())){
                    List  modelList = new ArrayList();
                    parainfo.getSpinnerInfoList().forEach(paraSpinnerInfo->{
                        Map<String,Object> modelMap = new LinkedHashMap<>();
                        //给type节点增加属性值
                        modelMap.put("-index",ParaHandlerUtil.generateEmptyStr(paraSpinnerInfo.getCode()));
                        modelMap.put("",paraSpinnerInfo.getName());
                        modelList.add(modelMap);
                    });
                    showMap.put("option",modelList);
                }
                paraMap.put("showMode",showMap);
                paraMap.put("range","");
                paraList.add(paraMap);
            });
            devMap.put("param",paraList);
            list.add(devMap);
        });
        fileMap.put("dev",list);
        map.put("devList",fileMap);
        return XmlUtil.convertToXml(map);
    }
}
