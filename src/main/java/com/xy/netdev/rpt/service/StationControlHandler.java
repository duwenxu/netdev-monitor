package com.xy.netdev.rpt.service;

import cn.hutool.core.util.StrUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.PrtclFormat;
import com.xy.netdev.rpt.bo.RptBodyDev;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

import static com.xy.netdev.common.util.ByteUtils.byteArrayCopy;
import static com.xy.netdev.common.util.ByteUtils.bytesToNum;

/**
 * 站控支持
 * @author cc
 */
public abstract class StationControlHandler implements IDownRptPrtclAnalysisService{

    @Setter
    @Getter
   public static class StationControlHeadEntity {

        private BaseInfo baseInfo;

        private String cmdMarkHexStr;

        private Integer length;

        private byte[] paramData;

        private String className;

        /**
         * 数据头解析
         * @param socketEntity socket数据信息
         * @param devInfo 设备信息
         * @return 站控对象
         */
        public static Optional<StationControlHeadEntity> unpackHead(SocketEntity socketEntity, BaseInfo devInfo){
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
    }

    /**
     * 数据解析数据体
     * @param stationControlHeadEntity 站控对象
     * @return 中心数据格式
     */
    abstract List<RptBodyDev> unpackBody(StationControlHeadEntity stationControlHeadEntity);

    /**
     * 收站控socket数据
     * @param stationControlHeadEntity 站控对象
     */
    public void receiverSocket(StationControlHeadEntity stationControlHeadEntity){
        //数据解析
        List<RptBodyDev> rptBodyDevs = unpackBody(stationControlHeadEntity);
        //数据发送中心
        answer(rptBodyDevs);
    }

}
