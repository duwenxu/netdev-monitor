package com.xy.netdev.websocket.config;

import com.alibaba.fastjson.JSON;
import com.xy.netdev.common.util.ParaHandlerUtil;
import com.xy.netdev.websocket.send.DevIfeMegSend;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * <p>
 * 自定义handle类
 * </p>
 *
 * @author sunchao
 * @since 2020-09-04
 */

@Slf4j
@Component
public class WSHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    /**
     * 单实例缓存
     */
    private ChannelCache cache = ChannelCache.getInstance();


    /**
     * 当接收到消息
     * @param channelHandlerContext
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame msg) throws Exception {
        // 获取客户端传输过来的消息
        String content = msg.text();
        log.debug("后端websocket接收到数据：" + content);
        Map map = JSON.parseObject(content);
        Object interfaceMark = map.get("interfaceMark");
        Object devCd = map.get("devNo");
        Object cmdMark  = map.get("cmdMark");

        //获取接收到消息的通道
        Channel channel = channelHandlerContext.channel();
        //判断此通道是否存在缓存中：若不存在，则增加
        if(interfaceMark != null){
            if(!cache.getAllChannels().contains(channel)){
                cache.setChannel(channel);
                //存放到接口和设备的关系缓存
                if(interfaceMark != null && devCd != null){
                    Object deviceCd = devCd;
                    if(cmdMark != null){
                        deviceCd = ParaHandlerUtil.genLinkKey(devCd.toString(), cmdMark.toString());
                    }
                    cache.setChannelUser(interfaceMark.toString(),deviceCd.toString(),channelHandlerContext.channel());
                }else{
                    //存放接口和所有设备的关系
                    cache.setChannelByIfe(interfaceMark.toString(),channelHandlerContext.channel());
                }
            }
        }
        //当websocket创立连接的时候进行一次发数
        DevIfeMegSend.sendFirstData(interfaceMark,devCd,cmdMark);
    }

    /**
     * 拦截器移除时调用：当连接关闭时
     * @param ctx
     * @throws Exception
     */

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        //移除通道
        cache.removeChannel(ctx.channel());
    }

    /**
     * channel channel捕获到异常
     * @param ctx
     * @param cause
     * @throws Exception
     */

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("channel捕获到异常了，关闭了:"+cause.getMessage());
        //移除通道
        cache.removeChannel(ctx.channel());
    }
}
