package cn.flowback.work.netty;

import cn.flowback.common.protocol.MessageType;
import cn.flowback.common.protocol.LogTestWorkProtocol;
import cn.flowback.common.utils.ZstdUtils;
import cn.flowback.work.config.FlowbackClientProperties;
import cn.flowback.common.utils.ProtocolUtils;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 客户端处理消息器
 *
 * @author 唐警威
 **/
public class WorkClientHandler extends ChannelInboundHandlerAdapter {

    public static ChannelHandlerContext serverChanelHandler;

    static Logger logger = LoggerFactory.getLogger(WorkClientHandler.class.getName());

    FlowbackClientProperties flowbackClientProperties;

    ExecutorService executorService = Executors.newWorkStealingPool(1);

    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd");


    public WorkClientHandler(FlowbackClientProperties flowbackClientProperties) {
        this.flowbackClientProperties = flowbackClientProperties;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            String decompress = ZstdUtils.decompress((byte[]) msg);
            logger.debug("receive message :{}", decompress);
            JSONObject jsonObject = JSONObject.parseObject(decompress);
            Integer messageType = jsonObject.getInteger(MessageType.MESSAGE_TYPE_TAG);
            AtomicInteger tag = new AtomicInteger();
            if (MessageType.TEST_LOG_WORK.equals(messageType)) {
                logger.info("====开始执行发送日志任务====");
                executorService.submit(()->{
                    LogTestWorkProtocol testLogWorkProtocol = JSONObject.parseObject(jsonObject.toJSONString(), LogTestWorkProtocol.class);
                    int count = testLogWorkProtocol.getCount();
                    for (int i = 0; i < count; i++) {
                        String s = UUID.randomUUID().toString();
                        JSONObject object = new JSONObject();
                        if(i % 2 == 0 && false){
                            object.put("table","test_work");
                        }else{
                            object.put("table","test_work_2");
                        }
                        object.put("c11",s);
                        object.put("c22",s);
                        object.put("c33",s);
                        object.put("c44",s);
                        object.put("c55",s);
                        object.put("c66",s);
                        object.put("c77",s);
                        object.put("c88",s);
                        object.put("c99",s);
                        object.put("c100",s);
                        object.put("c111",s);
                        object.put("c121",s);
                        object.put("time",simpleDateFormat.format(new Date())+"-"+tag);
                        if(i % 20000000 == 0){
                            tag.getAndIncrement();
                        }
                        object.put("order",i);
                        if(i > 100){
                            object.put("new_columns",100);
                        }
                        ProtocolUtils.sendZstdMsg(ctx,object.toJSONString());
                        //每累计发送10W条就休息下啦
                        if(i % 200000 == 0){
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("建立远程链接:{}", ctx.channel().remoteAddress());
        ProtocolUtils.registerWorkNode(ctx);
        //记录主服务对象
        serverChanelHandler = ctx;
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("断开连接-频道不活动{}", ctx.channel().remoteAddress());
        serverChanelHandler = null;
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        serverChanelHandler = null;
        cause.printStackTrace();
        logger.info("断开连接,{}", ctx.channel().remoteAddress());
        ctx.close();
    }


}
