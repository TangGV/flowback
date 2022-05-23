package cn.flowback.common.protocol.codec;

import cn.flowback.common.utils.ZstdUtils;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


/**
 * 编码器
 * @author 唐警威
 **/
public class ByteEncoder extends MessageToByteEncoder<Object> {


    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        try {
            //将对象转换为byte，伪代码，具体用什么进行序列化，你们自行选择。此处用的是fastJson
            //byte[] body = JSONObject.toJSONBytes(msg);
            byte[] body;
            if(msg instanceof  byte []){
                body = (byte []) msg;
            }else{
                byte[] bytes = JSONObject.toJSONBytes(msg);
                body = ZstdUtils.compress(bytes);
            }
            //System.out.println("编码原始大小:"+bytes.length+" 压缩后大小:"+body.length);
            //读取消息的长度
            int dataLength = body.length;
            //先将消息长度写入，也就是消息头
            out.writeIntLE(dataLength);
            //消息体中包含我们要发送的数据
            out.writeBytes(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}