package com.xy.netdev.frame;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.FIFOCache;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.util.HexUtil;
import com.xy.common.exception.BaseException;
import com.xy.netdev.common.util.BeanFactoryUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.rpt.service.StationControlHandler;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

import static com.xy.netdev.container.BaseInfoContainer.getDevInfo;

/**
 * 设备数据订阅
 * @author cc
 */
@Component
@Slf4j
public class DeviceSocketSubscribe {

    @Autowired
    private StationControlHandler stationControlHandler;

    @Autowired
    private List<AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData>> absSocketHandlerList;

    /**
     * 带时效的先进先出队列
     */
    private FIFOCache<String, AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData>> cache;

//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        init();
//    }
    @PostConstruct
    public void init(){
        //队列
        cache = CacheUtil.newFIFOCache(absSocketHandlerList.size());
    }

    /**
     * 执行响应流程
     * @param socketEntity socket实体
     */
    public void doResponse(SocketEntity socketEntity) throws BaseException{

        BaseInfo devInfo = null;
        List<BaseInfo> devInfos = getDevInfo(socketEntity.getRemoteAddress());
        if(devInfos!=null && devInfos.size()>1){
            for (BaseInfo baseInfo : devInfos) {
                if(baseInfo.getDevPort().equals(String.valueOf(socketEntity.getRemotePort()))){
                    if(StringUtils.isNotEmpty(baseInfo.getDevLocalPort())){
                        if(baseInfo.getDevLocalPort().equals(String.valueOf(socketEntity.getLocalPort()))){
                            devInfo = baseInfo;
                        }
                    }else{
                        devInfo = baseInfo;
                    }
                }
            }
        }else {
            devInfo = devInfos.get(0);
        }
        //站控响应
        if (devInfo.getIsRptIp()!= null && Integer.parseInt(devInfo.getIsRptIp()) == 0){
            log.warn("收到站控数据, 远端地址:{}:{},数据体:{}"
                    , socketEntity.getRemoteAddress()
                    , socketEntity.getRemotePort()
                    , HexUtil.encodeHexStr(socketEntity.getBytes()).toUpperCase());
            stationControlHandler.stationControlReceive(socketEntity);
            return;
        }

        //执行设备数据响应
        Optional<AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData>> socketHandler
                = getHandler(socketEntity.getRemoteAddress());
        BaseInfo finalDevInfo = devInfo;
        socketHandler.ifPresent(handler -> {
            log.debug("收到设备数据, 远端地址:{}:{},数据体:{}"
                    , socketEntity.getRemoteAddress()
                    , socketEntity.getRemotePort()
                    , HexUtil.encodeHexStr(socketEntity.getBytes()).toUpperCase());
            handler.socketResponse(socketEntity, finalDevInfo);
        });
    }

    /**
     * 根据key获取对应实体
     * @param ip ip
     * @return 目标实体
     */

    private Optional<AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData>> getHandler(String ip){
        AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> socketHandler = cache.get(ip);
        if (socketHandler != null){
            return Optional.of(socketHandler);
        }
        //设备信息
        BaseInfo devInfo = getDevInfo(ip).get(0);
        if (devInfo == null){
            log.warn("响应处理未找到指定设备信息, 执行方法getDevInfo(ip), 设备ip:{}", ip);
            return Optional.empty();
        }
        //设备网络协议
        String classByDevType = BaseInfoContainer.getClassByDevType(devInfo.getDevType());
        AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> handler = BeanFactoryUtil.getBean(classByDevType);
        cache.put(ip, handler, DateUnit.MINUTE.getMillis());
        return Optional.of(handler);
    }

}
