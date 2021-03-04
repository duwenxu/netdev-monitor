package com.xy.netdev.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xy.common.exception.BaseException;
import com.xy.netdev.admin.entity.SysParam;
import com.xy.netdev.admin.mapper.SysParamMapper;
import com.xy.netdev.admin.service.ISysParamService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * package com.xy.netdev.base.Impl.impl;
 *
 * public class TkplOrderSetInfoServiceImpl {
 * }
 * <p>
 * 参数信息 服务实现类
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-17
 */
@Service
public class SysParamServiceImpl extends ServiceImpl<SysParamMapper, SysParam> implements ISysParamService {

    @Resource
    private SysParamMapper paramMapper;
    private static Map<String, ParaCombox> paraMap = new HashMap<String, ParaCombox>();
    private static final int DEFAULT = 0;

    /**
     * 通过父id查询出参数表
     *
     * @param parentId
     * @return
     */
    @Override
    public List<SysParam> queryParamsByParentId(String parentId) {
        ParaCombox combox = getParaObjByParentId(parentId);
        List<SysParam> result = combox.getComboxList();
        return result;
    }

    /**
     * 通过参数编码查询参数名称
     *
     * @param paraCode
     * @return
     */
    @Override
    public String getParaName(String paraCode) {
        if (paraCode == null || paraCode.length()<4) {
            return paraCode;
        }
        ParaCombox combox = getParaObjByParentId(paraCode.substring(0, 4));
        Map<String, SysParam> subParaMap = combox.getComboxMap();
        if(null!=subParaMap && subParaMap.containsKey(paraCode) && null!=subParaMap.get(paraCode)){
            return subParaMap.get(paraCode).getParaName();
        }else {
            return "";
        }
    }




    /**
     * 通过参数编码查询备注一
     *
     * @param paraCode
     * @return
     */
    @Override
    public String getParaRemark1(String paraCode) {
        if (paraCode == null || paraCode.trim().equals("")) {
            return "";
        }
        String remark1 = "";
        if(paraCode.length() > 4){
            ParaCombox combox = getParaObjByParentId(paraCode.substring(0, 4));
            Map<String, SysParam> subParaMap = combox.getComboxMap();
            remark1 = subParaMap.get(paraCode).getRemark1();
        }else{
            SysParam sysParam = getParaObjByCode(paraCode);
            remark1 = sysParam.getRemark1();
        }
        return remark1;
    }

    /**
     * 通过参数编码查询备注二
     *
     * @param paraCode
     * @return
     */
    @Override
    public String getParaRemark2(String paraCode) {
        if (paraCode == null || paraCode.trim().equals("")) {
            return "";
        }
        ParaCombox combox = getParaObjByParentId(paraCode.substring(0, 4));
        Map<String, SysParam> subParaMap = combox.getComboxMap();
        return subParaMap.get(paraCode).getRemark2();
    }

    /**
     * 通过参数编码查询备注三
     *
     * @param paraCode
     * @return
     */
    @Override
    public String getParaRemark3(String paraCode) {
        if (paraCode == null || paraCode.trim().equals("")) {
            return "";
        }
        ParaCombox combox = getParaObjByParentId(paraCode.substring(0, 4));
        Map<String, SysParam> subParaMap = combox.getComboxMap();
        return subParaMap.get(paraCode).getRemark3();
    }

    /***
     * 按参数名称查询，获取LIST
     * @param parentName
     * @return
     */
    @Override
    public List<SysParam> getParaByName(String parentName) {
        ParaCombox combox = getParaObjByName(parentName);
        List<SysParam> result = combox.getComboxList();
        return result;
    }

    /**
     * 获取
     * @param paraCode
     * @return
     */
    public SysParam getParaObjByCode(String paraCode) {
        if (paraCode == null || paraCode.length()<4) {
            throw new BaseException("非法参数CODE!");
        }
        QueryWrapper<SysParam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("PARA_CODE",paraCode);
        SysParam sysParam = getOne(queryWrapper);
        return sysParam;
    }

    /***
     * 按父代码查询子代码
     * @param parentId
     * @return
     */
    private synchronized ParaCombox getParaObjByParentId(String parentId) {
        ParaCombox combox = paraMap.get(parentId);
        if (combox == null) {
            combox = new ParaCombox();
            List<SysParam> params = paramMapper.queryParamsByParentId(parentId);
            combox.setComboxList(params);
            Map<String, SysParam> subParaMap = new HashMap<String, SysParam>();
            for (int i = 0; i < params.size(); i++) {
                subParaMap.put(params.get(i).getParaCode(), params.get(i));
            }
            combox.setComboxMap(subParaMap);
            if (subParaMap.size() > 0) {
                paraMap.put(parentId, combox);
            }
            return combox;
        } else {
            return combox;
        }
    }






    /***
     * 按名称模糊查询
     * @param paraName
     * @return
     */
    private synchronized ParaCombox getParaObjByName(String paraName) {
        ParaCombox combox = paraMap.get(paraName);
        if (combox == null) {
            combox = new ParaCombox();
            Map<String, Object> para = new HashMap<String, Object>();
            para.put("paraName", paraName);
            List<SysParam> result = new ArrayList<SysParam>();
            try {
                result = this.paramMapper.queryParamByName(paraName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            combox.setComboxList(result);
            Map<String, SysParam> subParaMap = new HashMap<String, SysParam>();
            for (int i = 0; i < result.size(); i++) {
                subParaMap.put(result.get(i).getParaCode(), result.get(i));
            }
            combox.setComboxMap(subParaMap);
            paraMap.put(paraName, combox);
            return combox;
        } else {
            return combox;
        }
    }

    @Override
    public void deleteCacheComboxData(String paraCode) {
        paraMap.remove(paraCode.substring(0, 4));
    }

    private class ParaCombox {
        List<SysParam> comboxList;
        Map<String, SysParam> comboxMap;
        int defalutstatus = DEFAULT;

        public List<SysParam> getComboxList() {
            return comboxList;
        }

        public void setComboxList(List<SysParam> comboxList) {
            this.comboxList = comboxList;
        }

        public Map<String, SysParam> getComboxMap() {
            return comboxMap;
        }

        public void setComboxMap(Map<String, SysParam> comboxMap) {
            this.comboxMap = comboxMap;
        }

    }
}
