package com.xy.netdev.rpt.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.xy.netdev.common.util.BeanFactoryUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.PrtclFormat;
import com.xy.netdev.network.NettyUtil;
import com.xy.netdev.rpt.bo.RptBodyDev;
import com.xy.netdev.rpt.bo.RptHeadDev;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.xy.netdev.common.util.ByteUtils.byteArrayCopy;
import static com.xy.netdev.common.util.ByteUtils.bytesToNum;

/**
 * 站控支持
 * @author cc
 */
@Component
public class StationControlHandler implements IUpRptPrtclAnalysisService{



    @Setter
    @Getter
   public static class StationControlHeadEntity {

        private BaseInfo baseInfo;

        private String cmdMarkHexStr;

        private Integer length;

        private byte[] paramData;

        private String className;

    }

    /**
     * 上报外部协议
     * @param socketEntity 数据体
     * @param devInfo 设备信息
     */
    public void queryParaResponse(SocketEntity socketEntity, BaseInfo devInfo){
        unpackHead(socketEntity, devInfo)
                .ifPresent(this::receiverSocket);
    }

    /**
     * 数据头解析
     * @param socketEntity socket数据信息
     * @param devInfo 设备信息
     * @return 站控对象
     */
    private Optional<StationControlHeadEntity> unpackHead(SocketEntity socketEntity, BaseInfo devInfo){
        byte[] bytes = socketEntity.getBytes();
        int cmdMark = bytesToNum(bytes, 0, 2, ByteBuf::readShort);
        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterfaceOrPara(devInfo.getDevType(), Integer.toHexString(cmdMark));
        if (StrUtil.isBlank(prtclFormat.getFmtHandlerClass())){
            return Optional.empty();
        }
        int len = bytesToNum(bytes, 2, 2, ByteBuf::readShort);
        byte[] paramData = byteArrayCopy(bytes, 8, len - 8);

        StationControlHeadEntity stationControlHeadEntity = new StationControlHeadEntity();
        stationControlHeadEntity.setBaseInfo(devInfo);
        stationControlHeadEntity.setCmdMarkHexStr(Integer.toHexString(cmdMark));
        stationControlHeadEntity.setLength(len);
        stationControlHeadEntity.setParamData(paramData);
        stationControlHeadEntity.setClassName(prtclFormat.getFmtHandlerClass());
        return Optional.of(stationControlHeadEntity);
    }


    /**
     * 收站控socket数据
     * @param stationControlHeadEntity 站控对象
     */
    private void receiverSocket(StationControlHeadEntity stationControlHeadEntity){
        ResponseService responseService = BeanFactoryUtil.getBean(stationControlHeadEntity.getClassName());
        //数据解析
        List<RptBodyDev> rptBodyDevs = responseService.unpackBody(stationControlHeadEntity);
        //数据发送中心
        responseService.answer(rptBodyDevs);
    }


    @Override
    public void queryParaResponse(RptHeadDev headDev) {
        BaseInfo stationInfo = BaseInfoContainer.getDevInfoByNo(headDev.getDevNo());
        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterfaceOrPara(stationInfo.getDevType(), headDev.getCmdMark());
        RequestService requestService = BeanFactoryUtil.getBean(prtclFormat.getFmtHandlerClass());
        byte[] bytes = requestService.pack(headDev.getRptBodyDevs());
        int port = Integer.parseInt(stationInfo.getDevPort());
        NettyUtil.sendMsg(bytes, port, stationInfo.getDevIpAddr(), port, Integer.parseInt(stationInfo.getDevNetPtcl()));
    }
}
