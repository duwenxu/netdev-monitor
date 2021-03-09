package com.xy.netdev.frame.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.ParaInfo;
<<<<<<< HEAD
import com.xy.netdev.monitor.service.IBaseInfoService;
import com.xy.netdev.monitor.service.IInterfaceService;
import com.xy.netdev.monitor.service.IParaInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import com.xy.netdev.monitor.bo.DevInterParam;


import java.util.List;

/**
 * 39所Ku&L下变频器接口协议解析
 *
 * @author admin
 * @date 2021-03-05
 */
public class FreqConverterInterPrtcParser implements IQueryInterPrtclAnalysisService {


    @Autowired
    IInterfaceService interfaceService;

    @Override
    public void queryPara(BaseInfo devInfo, Interface interInfo) {
        devInfo = BaseInfoContainer.getDevInfo(devInfo.getDevIpAddr());
        interInfo = getInterfaceDetail(devInfo,interInfo);
        StringBuilder sb = new StringBuilder();
        sb.append(FreqConverterPrtcParser.SEND_START_MARK).append(devInfo.getDevIpAddr()).append("/")
                .append(interInfo.getItfCode()).append("_").append(FreqConverterPrtcParser.SEND_END_MARK);
        String command = sb.toString();
    }

    @Override
    public List<ParaInfo> queryParaResponse(BaseInfo devInfo, List<ParaInfo> paraList) {
        /*devInfo = BaseInfoContainer.getDevInfo(devInfo.getDevIpAddr());

        interInfo = getInterfaceDetail(devInfo,interInfo);
        StringBuilder sb = new StringBuilder();
        sb.append(FreqConverterPrtcParser.SEND_START_MARK).append(devInfo.getDevIpAddr()).append("/")
                .append(interInfo.getItfCode()).append("_").append(FreqConverterPrtcParser.SEND_END_MARK);
        String command = sb.toString();*/
        return null;
    }


    /**
     * 获取接口详细信息
     * @param devInfo
     * @param interInfo
     * @return
     */
    private Interface getInterfaceDetail(BaseInfo devInfo, Interface interInfo){
        String devType = devInfo.getDevType();
        Integer itfId = interInfo.getItfId();
        QueryWrapper<Interface> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Interface::getDevType,devType);
        queryWrapper.lambda().eq(Interface::getItfId,itfId);
        return interfaceService.getOne(queryWrapper);
    }
}
