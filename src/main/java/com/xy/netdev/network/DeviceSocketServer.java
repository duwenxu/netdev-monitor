package com.xy.netdev.network;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.network.handler.SimpleTcpMessage;
import com.xy.netdev.network.handler.SimpleUdpMessage;
import com.xy.netdev.network.server.NettyTcpClient;
import com.xy.netdev.network.server.NettyUdp;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.xy.netdev.container.BaseInfoContainer.getDevInfos;

/**
 * 设备通讯服务启动类
 * @author cc
 */
@Component
@Slf4j
public class DeviceSocketServer {


    private final int reTrySeconds = 10 * 6;

    @PostConstruct
    public void start() {
        Collection<BaseInfo> devInfos = getDevInfos();
        ThreadUtil.execute(() -> run(devInfos));
    }

    @SneakyThrows
    private void run(Collection<BaseInfo> devInfos)  {
        Map<String, List<BaseInfo>> socketDevMap = devInfos.stream()
                .filter(baseInfo -> StrUtil.isNotBlank(baseInfo.getDevIpAddr()))
                .collect(Collectors.groupingBy(BaseInfo::getDevIpAddr));
        if (socketDevMap.isEmpty()){
            log.warn("设备通讯服务启动失败, 未查询到设备远程地址信息信息, {}秒后重试....", reTrySeconds);
            TimeUnit.SECONDS.sleep(reTrySeconds);
            this.run(devInfos);
        }
    }


    private void udpStart(List<BaseInfo> list){
        ThreadUtil.execute(() -> {
            Set<Integer> setPort = list.stream()
                    .map(baseInfo -> Integer.parseInt(baseInfo.getDevPort()))
                    .collect(Collectors.toSet());
            //创建随机端口保活
            setPort.add(0);
            new NettyUdp(setPort, new SimpleUdpMessage());
        });
    }

    private void tcpStart(List<BaseInfo> list){
        list.forEach(baseInfo -> {
            int port = Integer.parseInt(baseInfo.getDevPort());
            ThreadUtil.execute(() -> new NettyTcpClient(baseInfo.getDevIpAddr(), port, port, new SimpleTcpMessage()));
        });
    }
}
