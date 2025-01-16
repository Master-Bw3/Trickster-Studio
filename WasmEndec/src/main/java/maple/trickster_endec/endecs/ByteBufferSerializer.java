package maple.trickster_endec.endecs;


import io.netty.buffer.ByteBufUtil;
import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.Serializer;
import io.wispforest.endec.util.VarInts;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class ByteBufferSerializer<B extends ByteBuffer> implements Serializer<B> {

    private final B buffer;

    protected ByteBufferSerializer(B buffer) {
        this.buffer = buffer;
    }

    public static <B extends ByteBuffer> ByteBufferSerializer<B> of(B buffer) {
        return new ByteBufferSerializer<>(buffer);
    }

    // ---

    @Override
    public void writeByte(SerializationContext ctx, byte value) {
        this.buffer.put(value);
    }

    @Override
    public void writeShort(SerializationContext ctx, short value) {
        this.buffer.putShort(value);
    }

    @Override
    public void writeInt(SerializationContext ctx, int value) {
        this.buffer.putInt(value);
    }

    @Override
    public void writeLong(SerializationContext ctx, long value) {
        this.buffer.putLong(value);
    }

    @Override
    public void writeFloat(SerializationContext ctx, float value) {
        this.buffer.putFloat(value);
    }

    @Override
    public void writeDouble(SerializationContext ctx, double value) {
        this.buffer.putDouble(value);
    }

    // ---

    @Override
    public void writeVarInt(SerializationContext ctx, int value) {
        VarInts.writeInt(value, b -> this.writeByte(ctx, b));
    }

    @Override
    public void writeVarLong(SerializationContext ctx, long value) {
        VarInts.writeLong(value, b -> this.writeByte(ctx, b));
    }

    // ---

    @Override
    public void writeBoolean(SerializationContext ctx, boolean value) {
        this.buffer.put(value ? (byte) 1 : (byte) 0);
    }

    @Override
    public void writeString(SerializationContext ctx, String value) {
        this.writeVarInt(ctx, ByteBufUtil.utf8Bytes(value));

        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        this.buffer.put(bytes);
    }

    @Override
    public void writeBytes(SerializationContext ctx, byte[] bytes) {
        this.writeVarInt(ctx, bytes.length);
        this.buffer.put(bytes);
    }

    @Override
    public <V> void writeOptional(SerializationContext ctx, Endec<V> endec, Optional<V> optional) {
        this.writeBoolean(ctx, optional.isPresent());
        optional.ifPresent(value -> endec.encode(ctx, this, value));
    }

    // ---

    @Override
    public <V> Map<V> map(SerializationContext ctx, Endec<V> valueEndec, int size) {
        this.writeVarInt(ctx, size);
        return new Sequence<>(ctx, valueEndec);
    }

    @Override
    public <E> Serializer.Sequence<E> sequence(SerializationContext ctx, Endec<E> elementEndec, int size) {
        this.writeVarInt(ctx, size);
        return new Sequence<>(ctx, elementEndec);
    }

    @Override
    public Struct struct() {
        return new Sequence<>(null, null);
    }

    // ---

    @Override
    public B result() {
        return this.buffer;
    }

    // ---

    private class Sequence<V> implements Serializer.Sequence<V>, Struct, Map<V> {

        private final SerializationContext ctx;
        private final Endec<V> valueEndec;

        private Sequence(SerializationContext ctx, Endec<V> valueEndec) {
            this.ctx = ctx;
            this.valueEndec = valueEndec;
        }

        @Override
        public void element(V element) {
            this.valueEndec.encode(this.ctx, ByteBufferSerializer.this, element);
        }

        @Override
        public void entry(String key, V value) {
            ByteBufferSerializer.this.writeString(this.ctx, key);
            this.valueEndec.encode(this.ctx, ByteBufferSerializer.this, value);
        }

        @Override
        public <F> Struct field(String name, SerializationContext ctx, Endec<F> endec, F value, boolean mayOmit) {
            endec.encode(ctx, ByteBufferSerializer.this, value);
            return this;
        }

        @Override
        public void end() {}
    }
}
