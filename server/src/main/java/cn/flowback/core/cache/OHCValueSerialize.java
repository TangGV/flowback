package cn.flowback.core.cache;

import org.caffinitas.ohc.CacheSerializer;

import java.nio.ByteBuffer;

public class OHCValueSerialize implements CacheSerializer<byte[]> {

    @Override
    public void serialize(byte[] value, ByteBuffer buf) {
        buf.put(value);
    }

    @Override
    public byte[] deserialize(ByteBuffer buf) {
        byte [] b = new byte[buf.capacity()];
        buf.get(b, 0, buf.capacity());
        return b;
    }

    @Override
    public int serializedSize(byte[] value) {
        return value.length;
    }
}