package com.xy.netdev.monitor.service.impl;

import cn.hutool.core.util.HexUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xy.common.exception.BaseException;
import com.xy.common.util.DateUtils;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.common.util.ParaHandlerUtil;
import com.xy.netdev.common.util.XmlUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.monitor.bo.ParaSpinnerInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.ParaInfo;
import com.xy.netdev.monitor.mapper.BaseInfoMapper;
import com.xy.netdev.monitor.service.IBaseInfoService;
import com.xy.netdev.monitor.service.IParaInfoService;
import com.xy.netdev.rpt.service.IDevStatusReportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
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
    @Autowired
    private IDevStatusReportService devStatusReportService;

    @Override
    public Map<String, Object> baseInfoMenuMap() {
        QueryWrapper<BaseInfo> wrapper = new QueryWrapper<>();
        //所有可用的设备
        wrapper.eq("DEV_STATUS", DEV_STATUS_NEW);
        List<BaseInfo> baseInfos = this.baseMapper.selectList(wrapper);
        //顶级菜单设备信息
        List<BaseInfo> topMenu = baseInfos.stream().filter(base ->
                StringUtils.isEmpty(base.getDevParentNo()) && DEV_STATUS_NEW.equals(base.getDevStatus()) && !DEV_NETWORK_GROUP.equals(base.getDevDeployType())
        ).collect(Collectors.toList());
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
            List<BaseInfo> subList = baseInfos.stream().filter(base -> menu.getDevNo().equals(base.getDevParentNo()) && DEV_STATUS_NEW.equals(base.getDevStatus())).collect(Collectors.toList());
            LinkedHashMap<String, Object> subMap = new LinkedHashMap<>();
            //将子设备列表转换为Map
           subList.sort(Comparator.comparing(BaseInfo::getDevMenuSeq,Comparator.nullsLast(Integer::compareTo)));
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
     *
     * @return
     */
    @Override
    public Map<String, Object> downDevFile(String devNo) {
        Map<String, Object> maps = new HashMap<>();
        try {
            BaseInfo baseInfo = BaseInfoContainer.getDevInfoByNo(devNo);
            String subType = ByteUtils.make0HexStr(Optional.ofNullable(ParaHandlerUtil.generateEmptyStr(sysParamService.getParaRemark1(baseInfo.getDevSubType()))).orElse("01"));
            //maps.put("fileName", "P[" + ByteUtils.make0HexStr(sysParamService.getParaRemark1(baseInfo.getDevType())) + subType + "]_" + ByteUtils.make0HexStr(sysParamService.getParaRemark1(baseInfo.getDevType())));
            maps.put("fileName", "P[" + ByteUtils.make0HexStr("39") + ByteUtils.make0HexStr(sysParamService.getParaRemark1(baseInfo.getDevType())) + "]_" + DateUtils.getDateYMDHMS());
            maps.put("fileContext", generateDevModelFileMap(baseInfo).getBytes("gb2312"));
        } catch (UnsupportedEncodingException e) {
            log.error("生成设备文件发生异常！");
        }
        return maps;
    }

    /**
     * 生成设备模型定义文件
     * map中key值增加-号，表示添加节点属性，key值为""字符串，则是直接赋值给当前节点
     *
     * @return
     */
    private String generateDevModelFileMap(BaseInfo baseInfo) {
        Map<String, Object> map = new HashMap<>();
        String devType = baseInfo.getDevType();
        //获取有效且对外开放的参数列表
        List<ParaInfo> paraInfos = paraInfoService.list().stream().filter(paraInfo -> STATUS_OK.equals(paraInfo.getNdpaStatus()) && IS_DEFAULT_TRUE.equals(paraInfo.getNdpaOutterStatus()) && devType.equals(paraInfo.getDevType())).collect(Collectors.toList());
        /***********************增加dev节点********************************/
        Map<String, Object> devMap = new LinkedHashMap<>();
        //给dev节点增加属性值
        devMap.put("-Types", ByteUtils.make0HexStr("39"));
        devMap.put("-ver", ParaHandlerUtil.generateEmptyStr(baseInfo.getDevVer()));
        String subType = ByteUtils.make0HexStr(sysParamService.getParaRemark1(baseInfo.getDevType()));
        devMap.put("-subtype", subType);
        devMap.put("name", ParaHandlerUtil.generateEmptyStr(baseInfo.getDevName()));
        devMap.put("cnName", ParaHandlerUtil.generateEmptyStr(baseInfo.getDevName()));
        devMap.put("corp", ParaHandlerUtil.generateEmptyStr(sysParamService.getParaName(baseInfo.getDevCorp())));
        List paraList = new ArrayList();
        //获取指定设备的参数并过滤生成可提供给54所的参数用来生成文件
        List<ParaInfo> parasTemp = paraInfos.stream().filter(paraInfo -> devType.equals(paraInfo.getDevType()) && paraInfo.getNdpaOutterStatus().equals(IS_DEFAULT_TRUE)).collect(Collectors.toList());
        for (ParaInfo parainfo : parasTemp) {
            String paraType = parainfo.getNdpaCmplexLevel();
            if(paraType.equals(PARA_COMPLEX_LEVEL_COMPOSE) || paraType.equals(PARA_COMPLEX_LEVEL_COMPLEX)){
                continue;
            }
            Map<String, Object> paraMap = new LinkedHashMap<>();
            //给param节点增加属性值
            paraMap.put("-no", ParaHandlerUtil.generateEmptyStr(parainfo.getNdpaNo()));
            paraMap.put("-name", ParaHandlerUtil.generateEmptyStr(ParaHandlerUtil.generateEmptyStr(parainfo.getNdpaName())));
            //特殊处理：当权限为null(0022004)时全设置为可读
            String access = sysParamService.getParaRemark1(parainfo.getNdpaAccessRight());
            if(!paraType.equals(PARA_COMPLEX_LEVEL_SIMPLE)){
                access = "read";
                paraMap.put("-access", "read");
            }else{
                if(access.equals("write")){
                    access = "full";
                }
                paraMap.put("-access", StringUtils.isNotBlank(access) && !access.equals("null") && !access.equals("cmd") ? access : "read");
            }
            paraMap.put("-unit", ParaHandlerUtil.generateEmptyStr(parainfo.getNdpaUnit()));
            /***********************增加showModel节点****************************/
            Map<String, Object> showMap = new LinkedHashMap<>();
            String modeType =  ParaHandlerUtil.generateEmptyStr(sysParamService.getParaRemark1(parainfo.getNdpaShowMode()));
            showMap.put("-name",modeType);
            /***********************增加type节点********************************/
            //给type节点增加属性值:当类型为float、double、str时全部转为str
            String typeCode = parainfo.getNdpaDatatype().equals(PARA_DATA_TYPE_INT) || parainfo.getNdpaDatatype().equals(PARA_DATA_TYPE_UINT) || parainfo.getNdpaDatatype().equals(PARA_DATA_TYPE_FLOAT) || parainfo.getNdpaDatatype().equals(PARA_DATA_TYPE_DOUBLE) || parainfo.getNdpaDatatype().equals(PARA_DATA_TYPE_STR) ? PARA_DATA_TYPE_STR : parainfo.getNdpaDatatype();
            Map<String, Object> typeMap = new LinkedHashMap<>();
            typeMap.put("-name",  modeType.equalsIgnoreCase("comb") ? "byte" : ParaHandlerUtil.generateEmptyStr(sysParamService.getParaName(typeCode)));
            if(!modeType.equals("comb") && typeCode.equals(PARA_DATA_TYPE_STR)){
                if(access.equals("full") && paraType.equals(PARA_COMPLEX_LEVEL_SIMPLE)){
                    String len = parainfo.getNdpaStrLen();
                    if(sysParamService.getParaName(typeCode).equals("str") && StringUtils.isEmpty(len)){
                        throw new BaseException("参数编号为"+parainfo.getNdpaNo()+"的参数可写，长度不能为空！");
                    }else{
                        typeMap.put("-len", len);
                    }
                }else{
                    //当数据类型为字符串指定字符串的len
                    if ( PARA_DATA_TYPE_STR.equals(typeCode)) {
                        typeMap.put("-len", Optional.ofNullable(ParaHandlerUtil.generateEmptyStr(parainfo.getNdpaStrLen())).orElse(sysParamService.getParaRemark1(DATA_TYPE_LEN)));
                    }
                }
            }
            paraMap.put("type", typeMap);
            if (PARA_SHOW_MODEL.equals(parainfo.getNdpaShowMode())) {
                List modelList = new ArrayList();
                if (StringUtils.isNotEmpty(parainfo.getNdpaCombRule())) {
                    /***********************增加type节点********************************/
                    typeMap.put("-name", ParaHandlerUtil.generateEmptyStr(sysParamService.getParaName(PARA_DATA_TYPE_BYTE)));
                    //当数据类型为字符串指定字符串的len
                    paraMap.put("type", typeMap);
                    //当字段类型为无且对外转换字段不为空时
                    Map<String, String> mapIn = Optional.ofNullable(JSONObject.parseObject(parainfo.getNdpaCombRule(), Map.class)).orElse(new HashMap());
                    mapIn.forEach((key, value) -> {
                        Map<String, Object> modelMap = new LinkedHashMap<>();
                        //给type节点增加属性值
                        //modelMap.put("-index", ByteUtils.make0HexStr(ParaHandlerUtil.generateEmptyStr(value)));
                        modelMap.put("-index", HexUtil.encodeHexStr(ParaHandlerUtil.generateEmptyStr(value).getBytes(Charset.forName("GB2312")))+ "H");
                        //给标签设置值
                        modelMap.put("", key);
                        modelList.add(modelMap);
                    });
                } else {
                    JSONArray.parseArray(parainfo.getNdpaSelectData(), ParaSpinnerInfo.class).forEach(paraSpinnerInfo -> {
                        Map<String, Object> modelMap = new LinkedHashMap<>();
                        //给type节点增加属性值
                        if(parainfo.getNdpaDatatype().equals(PARA_DATA_TYPE_BYTE)){
                            modelMap.put("-index", ParaHandlerUtil.generateEmptyStr(paraSpinnerInfo.getCode()) + "H");
                        }else{
                            modelMap.put("-index", HexUtil.encodeHexStr(ParaHandlerUtil.generateEmptyStr(paraSpinnerInfo.getCode()).getBytes(Charset.forName("GB2312")))+ "H");
                        }
                        //给标签设置值
                        modelMap.put("", paraSpinnerInfo.getName());
                        modelList.add(modelMap);
                    });
                }
                showMap.put("option", modelList);
                paraMap.put("showMode", showMap);
            } else {
                paraMap.put("showMode", showMap);
                /***********************增加range节点********************************/
                if (!StringUtils.isBlank(parainfo.getNdpaValMax1()) && !StringUtils.isBlank(parainfo.getNdpaValMin1())) {
                    Map<String, Object> rangeMap = new LinkedHashMap<>();
                    String name = sysParamService.getParaRemark2(parainfo.getNdpaDatatype());
                    if (StringUtils.isBlank(name)) {
                        throw new BaseException("参数["+parainfo.getNdpaName()+"]数据类型配置有误!");
                    } else {
                        rangeMap.put("-name", name);
                        rangeMap.put("-down", ParaHandlerUtil.generateEmptyStr(parainfo.getNdpaValMin1()));
                        rangeMap.put("-up", ParaHandlerUtil.generateEmptyStr(parainfo.getNdpaValMax1()));
                        rangeMap.put("-step", ParaHandlerUtil.generateEmptyStr(parainfo.getNdpaValStep()));
                        rangeMap.put("-format", ParaHandlerUtil.generateEmptyStr(parainfo.getNdpaValFormat()));
                        List<Map<String, Object>> ranges = new ArrayList<>();
                        ranges.add(rangeMap);
                        paraMap.put("range", new LinkedHashMap() {{put("IRange", ranges);}});
                    }
                }
                /***********************增加range节点********************************/
                if (!StringUtils.isBlank(parainfo.getNdpaValMax2()) && !StringUtils.isBlank(parainfo.getNdpaValMin2())) {
                    Map<String, Object> rangeMap = new LinkedHashMap<>();
                    String name = sysParamService.getParaRemark2(parainfo.getNdpaDatatype());
                    if (StringUtils.isBlank(name)) {
                        throw new BaseException("参数["+parainfo.getNdpaName()+"]数据类型配置有误!");
                    } else {
                        rangeMap.put("-name", name);
                        rangeMap.put("-down", ParaHandlerUtil.generateEmptyStr(parainfo.getNdpaValMin2()));
                        rangeMap.put("-up", ParaHandlerUtil.generateEmptyStr(parainfo.getNdpaValMax2()));
                        rangeMap.put("-step", ParaHandlerUtil.generateEmptyStr(parainfo.getNdpaValStep()));
                        rangeMap.put("-format", ParaHandlerUtil.generateEmptyStr(parainfo.getNdpaValFormat()));
                        if(paraMap.containsKey("range")){
                            Map<String,Object> rangeM = (Map<String,Object>)paraMap.get("range");
                            List<Map<String, Object>> ranges = (List<Map<String, Object>>)rangeM.get("IRange");
                            ranges.add(rangeMap);
                        }else{
                            List<Map<String, Object>> ranges = new ArrayList<>();
                            ranges.add(rangeMap);
                            paraMap.put("range", new LinkedHashMap() {{put("IRange", ranges);}});
                        }
                    }
                }
            }
            paraList.add(paraMap);
        }
        devMap.put("ps", new LinkedHashMap() {{
            put("param", paraList);
        }});
        map.put("dev", devMap);
        return XmlUtil.convertToXml(map, "gb2312");
    }

    @Override
    public boolean changeUseStatus(String devNo) {
        boolean isOk = false;
        String masterOrSlaveStatus;
        //修改使用状态
        if (StringUtils.isNotBlank(devNo)) {
            BaseInfo targetDev = this.getById(devNo);
            //当前设备相同父编号的主备设备list
            List<BaseInfo> subList = subListByDevNo(targetDev.getDevParentNo());
            if (!subList.isEmpty()) {
                for (BaseInfo base : subList) {
                    if (DEV_USE_STATUS_INUSE.equals(base.getDevUseStatus())) {
                        base.setDevUseStatus(DEV_USE_STATUS_NOTUSE);
                        this.updateById(base);
                    }
                }
            }
            //上报当前设备 主备状态
            if (DEV_DEPLOY_MASTER.equals(targetDev.getDevDeployType())) {
                masterOrSlaveStatus = RPT_DEV_STATUS_MASTERORSLAVE_MASTER;
            } else {
                masterOrSlaveStatus = RPT_DEV_STATUS_MASTERORSLAVE_SLAVE;
            }
            targetDev.setDevUseStatus(DEV_USE_STATUS_INUSE);
            isOk = this.updateById(targetDev);
            devStatusReportService.rptMasterOrSlave(devNo, masterOrSlaveStatus);
        }
        return isOk;
    }

    /**
     * 设备信息修改时发送 是否启用主备
     *
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
        List<BaseInfo> subMasterSlaveList = subListByDevNo(parentNo);
        if (subMasterSlaveList.size() > 1) {
            devStatusReportService.rptUseStandby(devNo, RPT_DEV_STATUS_USESTANDBY_YES);
        } else {
            devStatusReportService.rptUseStandby(devNo, RPT_DEV_STATUS_USESTANDBY_NO);
        }
    }

    /**
     * 通过设备编号查询同属一个父设备的子设备列表(从数据库获取而非缓存)
     *
     * @return 子设备列表
     */
    private List<BaseInfo> subListByDevNo(String parentNo) {
        LambdaQueryWrapper<BaseInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseInfo::getDevParentNo, parentNo);
        return this.list(wrapper).stream()
                .filter(base -> DEV_DEPLOY_MASTER.equals(base.getDevDeployType()) || DEV_DEPLOY_SLAVE.equals(base.getDevDeployType())).collect(Collectors.toList());
    }
}
