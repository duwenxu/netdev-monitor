package com.xy.netdev.monitor.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xy.common.util.DateUtils;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.ParaHandlerUtil;
import com.xy.netdev.common.util.XmlUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.monitor.bo.ParaSpinnerInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.ParaInfo;
import com.xy.netdev.monitor.mapper.BaseInfoMapper;
import com.xy.netdev.monitor.service.IBaseInfoService;
import com.xy.netdev.monitor.service.IParaInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.xy.netdev.common.constant.SysConfigConstant.*;

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
    @Autowired
    private IParaInfoService paraInfoService;

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
        //获取有效且对外开放的参数列表
        List<ParaInfo> paraInfos = paraInfoService.list().stream().filter(paraInfo -> SysConfigConstant.STATUS_OK.equals(paraInfo.getNdpaStatus()) && SysConfigConstant.IS_DEFAULT_TRUE.equals(paraInfo.getNdpaOutterStatus())).collect(Collectors.toList());
        /***********************增加dev节点********************************/
        BaseInfoContainer.getDevInfos().forEach(baseInfo -> {
            Map<String,Object> devMap = new LinkedHashMap<>();
            //给dev节点增加属性值
            devMap.put("-type",sysParamService.getParaRemark1(baseInfo.getDevType()));
            devMap.put("-name",ParaHandlerUtil.generateEmptyStr(baseInfo.getDevName()));
            devMap.put("-ver",ParaHandlerUtil.generateEmptyStr(baseInfo.getDevVer()));
            devMap.put("-corp",sysParamService.getParaRemark1(baseInfo.getDevCorp()));
            devMap.put("-cname",ParaHandlerUtil.generateEmptyStr(baseInfo.getDevName()));
            List paraList = new ArrayList();
            //获取指定设备的参数并过滤生成可提供给54所的参数用来生成文件
            String devType = BaseInfoContainer.getDevInfoByNo(baseInfo.getDevNo()).getDevType();
            paraInfos.stream().filter(paraInfo -> devType.equals(paraInfo.getDevType())).forEach(parainfo->{
                Map<String,Object> paraMap = new LinkedHashMap<>();
                //给param节点增加属性值
                paraMap.put("-no",parainfo.getNdpaNo());
                paraMap.put("-name",ParaHandlerUtil.generateEmptyStr(parainfo.getNdpaName()));
                paraMap.put("-access",ParaHandlerUtil.generateEmptyStr(sysParamService.getParaRemark1(parainfo.getNdpaAccessRight())));
                paraMap.put("-unit", ParaHandlerUtil.generateEmptyStr(parainfo.getNdpaUnit()));
                /***********************增加type节点********************************/
                Map<String,Object> typeMap = new LinkedHashMap<>();
                //给type节点增加属性值
                typeMap.put("-name",sysParamService.getParaName(parainfo.getNdpaDatatype()));
                //当数据类型为字符串指定字符串的len
                if("0023004".equals(parainfo.getNdpaDatatype())){
                    typeMap.put("-len",parainfo.getNdpaStrLen());
                }
                paraMap.put("type",typeMap);
                /***********************增加showModel节点****************************/
                Map<String,Object> showMap = new LinkedHashMap<>();
                showMap.put("-name", ParaHandlerUtil.generateEmptyStr(sysParamService.getParaRemark1(parainfo.getNdpaShowMode())));
                if ("0024002".equals(parainfo.getNdpaShowMode())) {
                    List modelList = new ArrayList();
                    JSONArray.parseArray(parainfo.getNdpaSelectData(), ParaSpinnerInfo.class).forEach(paraSpinnerInfo -> {
                        Map<String, Object> modelMap = new LinkedHashMap<>();
                        //给type节点增加属性值
                        modelMap.put("-index", ParaHandlerUtil.generateEmptyStr(paraSpinnerInfo.getCode()));
                        modelMap.put("", paraSpinnerInfo.getName());
                        modelList.add(modelMap);
                    });
                    showMap.put("option", modelList);
                }
                paraMap.put("showMode", showMap);
                /***********************增加range节点********************************/
                String iRange = sysParamService.getParaRemark2(parainfo.getNdpaDatatype());
                if(!StringUtils.isBlank(iRange)){
                    Map<String,Object> rangeMap = new LinkedHashMap<>();
                    rangeMap.put("-name",iRange);
                    rangeMap.put("-down",ParaHandlerUtil.generateEmptyStr(parainfo.getNdpaValMax()));
                    rangeMap.put("-up",ParaHandlerUtil.generateEmptyStr(parainfo.getNdpaValMin()));
                    rangeMap.put("-step",parainfo.getNdpaValStep());
                    paraMap.put("range",new LinkedHashMap(){{put("IRange",rangeMap);}});
                }
                paraList.add(paraMap);
            });
            devMap.put("param",paraList);
            list.add(devMap);
        });
        map.put("devList",new LinkedHashMap(){{put("dev",list);}});
        return XmlUtil.convertToXml(map);
    }

    @Override
    public boolean changeUseStatus(String devNo) {
        boolean isOk=false;
        String masterOrSlaveStatus;
        //修改使用状态
        if (StringUtils.isNotBlank(devNo)) {
            BaseInfo targetDev = this.getById(devNo);
            //当前设备相同父编号的主备设备list
            List<BaseInfo> subList = BaseInfoContainer.getDevsFatByDevNo(devNo);
            if (!subList.isEmpty()) {
                for (BaseInfo base : subList) {
                    if (DEV_USE_STATUS_INUSE.equals(base.getDevUseStatus())) {
                        base.setDevUseStatus(DEV_USE_STATUS_NOTUSE);
                        this.updateById(base);
                    }
                }
            }

            if (DEV_DEPLOY_MASTER.equals(targetDev.getDevDeployType())){
                masterOrSlaveStatus = RPT_DEV_STATUS_MASTERORSLAVE_MASTER;
            }else {
                masterOrSlaveStatus = RPT_DEV_STATUS_MASTERORSLAVE_SLAVE;
            }

            targetDev.setDevUseStatus(DEV_USE_STATUS_INUSE);
            isOk = this.updateById(targetDev);
        }
        return isOk;
    }

    /**
     * 设备信息修改时发送 是否启用主备
     * @param devNo 设备编号
     */
    @Override
    public void devStatusUpdate(String devNo, String devParentNo) {
        String parentNo;
        //删除时传递事先拿到的parentNo
        if (StringUtils.isEmpty(devNo)) {
            parentNo = devParentNo;
        } else {
            BaseInfo targetDev = this.getById(devNo);
            parentNo = targetDev.getDevParentNo();
        }
        LambdaQueryWrapper<BaseInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseInfo::getDevParentNo, parentNo);
        List<BaseInfo> subMasterSlaveList = this.list(wrapper).stream()
                .filter(base -> DEV_DEPLOY_MASTER.equals(base.getDevDeployType()) || DEV_DEPLOY_SLAVE.equals(base.getDevDeployType())).collect(Collectors.toList());
        if (subMasterSlaveList.size() > 1) {
            //todo 发送是否启用主备通知
        }
    }
}
