package com.xy.netdev.rpt.service.impl;

import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.container.DevStatusContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.monitor.bo.DevStatusInfo;
import com.xy.netdev.monitor.bo.ParaViewInfo;
import com.xy.netdev.rpt.bo.RptBodyDev;
import com.xy.netdev.rpt.bo.RptHeadDev;
import com.xy.netdev.rpt.service.IDownRptPrtclAnalysisService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 站控 参数查询/设置 实现
 *
 * @author duwenxu
 * @create 2021-03-15 11:19
 */
@Service
public class IDownRptPrtclAnalysisServiceImpl implements IDownRptPrtclAnalysisService {
    /**
     * 标识：查询所有参数
     */
    private static final String ALL_PARAS_QUERY = "0";

    @Override
    public RptHeadDev queryNewCache(RptHeadDev rptHeadDev) {
        return null;
    }

    @Override
    public RptHeadDev doAction(RptHeadDev headDev) {
        //站控命令标识
        String cmdMarkHexStr = headDev.getCmdMarkHexStr();
        RptHeadDev resBody = new RptHeadDev();
        switch (cmdMarkHexStr) {
            case "0003":
                resBody = doParaQueryAction(headDev);
                break;
            case "0005":
//                resBody = doParaSetAction(headDev);
                break;
            default:
                break;
        }
        return resBody;
    }

    /**
     * 站控 参数查询
     *
     * @param headDev 参数查询结构体
     * @return 参数查询响应结果
     */
    private RptHeadDev doParaQueryAction(RptHeadDev headDev) {
        List<RptBodyDev> rptBodyDev = (List<RptBodyDev>) headDev.getParam();
        rptBodyDev.forEach(rptBody -> {
            String devNo = rptBody.getDevNo();
            //获取指定设备当前可读的参数列表
            List<ParaViewInfo> devParaViewList = DevParaInfoContainer.getDevParaViewList(devNo).stream()
                    .filter(paraView -> !SysConfigConstant.ONLY_WRITE.equals(paraView.getAccessRight())).collect(Collectors.toList());
            //当前设备的查询响应参数列表
            List<FrameParaData> resFrameParaList = new ArrayList<>();
            for (FrameParaData para : rptBody.getDevParaList()) {
                String paraNo = para.getParaNo();
                //参数编号为0,查询所有
                if (ALL_PARAS_QUERY.equals(paraNo)) {
                    devParaViewList.forEach(paraView -> {
                        FrameParaData frameParaData = frameParaDataWrapper(paraView);
                        resFrameParaList.add(frameParaData);
                    });
                    break;
                }
                //获取单个参数信息
                devParaViewList.stream()
                        .filter(paraView -> paraNo.equals(paraView.getParaNo())).findFirst()
                        .ifPresent(paraView -> {
                            FrameParaData frameParaData = frameParaDataWrapper(paraView);
                            resFrameParaList.add(frameParaData);
                        });
            }
            rptBody.setDevParaList(resFrameParaList);
        });
        headDev.setParam(rptBodyDev);
        return headDev;
    }


    private FrameParaData frameParaDataWrapper(ParaViewInfo paraView) {
        return FrameParaData.builder()
                .paraNo(paraView.getParaNo())
                .paraVal(paraView.getParaVal())
                .devType(paraView.getDevType())
                .devNo(paraView.getDevNo())
                .len(Integer.parseInt(paraView.getParaStrLen()))
                .build();
    }

}
