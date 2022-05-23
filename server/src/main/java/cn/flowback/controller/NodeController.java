package cn.flowback.controller;

import cn.flowback.common.protocol.LogTestWorkProtocol;
import cn.flowback.common.utils.ProtocolUtils;
import cn.flowback.core.listener.netty.client.WorkNodeClient;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * 部署后测试落库消息使用
 * @author Tang
 */
@RestController
public class NodeController {


    /**
     * 测试执行发送消息
     * @param count 入库消息量
     */
    @RequestMapping("/logTestWork")
    public void logTestWork(int count){
        try {
            Set<ChannelHandlerContext> clients = WorkNodeClient.clients();
            LogTestWorkProtocol logTestWorkProtocol = new LogTestWorkProtocol();
            logTestWorkProtocol.setCount(count);
            for (ChannelHandlerContext client : clients) {
                ProtocolUtils.sendMsg(client,logTestWorkProtocol);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
