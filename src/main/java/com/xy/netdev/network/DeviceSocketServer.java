package com.xy.netdev.network;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.network.handler.SimpleTcpMessage;
import com.xy.netdev.network.handler.SimpleUdpMessage;
import com.xy.netdev.network.server.NettyTcpClient;
import com.xy.netdev.network.server.NettyUdp;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.xy.netdev.common.constant.SysConfigConstant.RPT_IP_ADDR;
import static com.xy.netdev.container.BaseInfoContainer.getDevInfos;

/**
 * 设备通讯服务启动类
 * @author cc
 */
@Component
@Slf4j
public class DeviceSocketServer {

    @Autowired
    private ISysParamService sysParamService;

    private Set<Integer> udpPort;

    private List<BaseInfo> tcpList;

    @PostConstruct
    public void start() {
        udpPort = new HashSet<>();
        tcpList = new ArrayList<>();
        Collection<BaseInfo> devInfos = getDevInfos();
        ThreadUtil.execute(() -> run(devInfos));
    }
    @SneakyThrows
    private void run(Collection<BaseInfo> devInfos)  {
        Optional<String> optional = devInfos.stream()
                .map(BaseInfo::getDevIpAddr)
                .filter(StrUtil::isNotBlank)
                .findAny();
        if (!optional.isPresent()){
            int reTrySeconds = 10 * 6;
            log.warn("设备通讯服务启动失败, 未查询到设备远程地址信息, {}秒后重试....", reTrySeconds);
            TimeUnit.SECONDS.sleep(reTrySeconds);
            this.run(devInfos);
        }

        devInfos.stream()
                .filter(baseInfo -> baseInfo.getDevNetPtcl() != null)
                .filter(baseInfo -> baseInfo.getDevPort() != null)
//                .filter(baseInfo -> baseInfo.getDevLocalAddr() != null)
                .forEach(baseInfo -> {
            if (SysConfigConstant.UDP.equals(baseInfo.getDevNetPtcl())) {
                udpPort.add(Integer.parseInt(baseInfo.getDevPort()));
            }
            if (SysConfigConstant.TCP.equalsIgnoreCase(baseInfo.getDevNetPtcl())) {
                tcpList.add(baseInfo);
            }
        });
        Integer port_54 = Integer.parseInt(sysParamService.getParaRemark2(RPT_IP_ADDR));
        udpPort.add(port_54);
        udpStart(udpPort);
        tcpStart(tcpList);
    }


    private void udpStart(Set<Integer> setPort){
        //创建随机端口保活
        setPort.add(0);
        ExecutorService executorService = ThreadUtil.newExecutor(1);
        executorService.execute(() -> new NettyUdp(setPort, new SimpleUdpMessage()).run());
    }

    private void tcpStart(List<BaseInfo> list){
        if (list.isEmpty()){
            return;
        }
        ExecutorService executorService = ThreadUtil.newExecutor(list.size());
        list.forEach(baseInfo -> {
            int port = Integer.parseInt(baseInfo.getDevPort());
            String localportStr = baseInfo.getDevLocalPort();
            int localPort = port;
            if(StringUtils.isNotEmpty(localportStr)){
                localPort = Integer.parseInt(localportStr);
            }
            int finalLocalPort = localPort;
            ThreadUtil.execute(() -> new NettyTcpClient(baseInfo.getDevIpAddr(), port, finalLocalPort, new SimpleTcpMessage()).run());

        });
    }
}
