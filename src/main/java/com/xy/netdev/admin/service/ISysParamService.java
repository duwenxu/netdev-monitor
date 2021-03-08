package com.xy.netdev.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xy.netdev.admin.entity.SysParam;

import java.util.List;

/**
 * <p>
 * 参数信息 服务类
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-17
 */
public interface ISysParamService extends IService<SysParam> {

    /**
     * 通过父id查询出参数表
     * @param parentId
     * @return
     */
    public List<SysParam> queryParamsByParentId(String parentId);

    /***
     * 按参数名称查询，获取LIST
     * @param parentName
     * @return
     */
    public List<SysParam> getParaByName(String parentName);


    /**
     * 通过参数编码查询参数名称
     * @param paraCode
     * @return
     */
    public String getParaName(String paraCode);

    /**
     * 通过参数编码查询备注一
     * @param paraCode
     * @return
     */
    public  String getParaRemark1(String paraCode);

    /**
     * 通过参数编码查询备注二
     * @param paraCode
     * @return
     */
    public String getParaRemark2(String paraCode);

    /**
     * 通过参数编码查询备注三
     * @param paraCode
     * @return
     */
    public String getParaRemark3(String paraCode);

    /**
     * 清楚字典缓存
     * @param paraCode
     */
    public void deleteCacheComboxData(String paraCode);
}
