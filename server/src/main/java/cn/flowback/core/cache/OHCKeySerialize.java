package cn.flowback.core.cache;

import org.caffinitas.ohc.CacheSerializer;

import java.nio.ByteBuffer;

public class OHCKeySerialize implements CacheSerializer<String> {

    @Override
    public void serialize(String value, ByteBuffer buf) {
        buf.put(value.getBytes());
    }

    @Override
    public String deserialize(ByteBuffer buf) {
        byte [] b = new byte[buf.capacity()];
        buf.get(b, 0, buf.capacity());
        return new String(b);
    }

    @Override
    public int serializedSize(String value) {
        return value.getBytes().length;
    }
}