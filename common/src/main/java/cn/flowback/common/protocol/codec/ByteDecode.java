package cn.flowback.common.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 解码器
 * @author 唐警威
 **/
public class ByteDecode extends ByteToMessageDecoder {

    static volatile AtomicInteger count = new AtomicInteger(0);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            int data = in.readableBytes();
            if (data < 4) {
                return;
            }
            //标记当前的readIndex的位置
            in.markReaderIndex();
            // 读取传送过来的消息的长度。ByteBuf 的readInt()方法会让他的readIndex增加4
            int dataLength = in.readIntLE();
            // 读到的消息体长度为0，这是不应该出现的情况，这里出现这情况，关闭连接。
            if (dataLength < 0) {
                ctx.close();
            }
            //读到的消息体长度如果小于传送过来的消息长度，则resetReaderIndex. 这个配合markReaderIndex使用的。把readIndex重置到mark的地方
            if (in.readableBytes() < dataLength) {
                in.resetReaderIndex();
                return;
            }
            byte[] body = new byte[dataLength];
            in.readBytes(body);
            //将byte数据转化为需要的对象
            //out.add(JSONObject.parseObject(new String(body)));
            //使用zstd解压数据包此处不直接解压，到真正落库时再处理解压
            //JSONObject object = JSONObject.parseObject(ZstdUtils.decompress(body));
            out.add(body);
            //System.out.println("receive message:"+object.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
