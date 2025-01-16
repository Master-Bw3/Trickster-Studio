package maple.trickster_endec.endecs;

import io.wispforest.endec.Deserializer;
import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.util.VarInts;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class ByteBufferDeserializer implements Deserializer<ByteBuffer> {

    private final ByteBuffer buffer;

    protected ByteBufferDeserializer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public static ByteBufferDeserializer of(ByteBuffer buffer) {
        return new ByteBufferDeserializer(buffer);
    }

    // ---

    @Override
    public byte readByte(SerializationContext ctx) {
        return this.buffer.get();
    }

    @Override
    public short readShort(SerializationContext ctx) {
        return this.buffer.getShort();
    }

    @Override
    public int readInt(SerializationContext ctx) {
        return this.buffer.getInt();
    }

    @Override
    public long readLong(SerializationContext ctx) {
        return this.buffer.getLong();
    }

    @Override
    public float readFloat(SerializationContext ctx) {
        return this.buffer.getFloat();
    }

    @Override
    public double readDouble(SerializationContext ctx) {
        return this.buffer.getDouble();
    }

    // ---

    @Override
    public int readVarInt(SerializationContext ctx) {
        return VarInts.readInt(() -> this.readByte(ctx));

    }

    @Override
    public long readVarLong(SerializationContext ctx) {
        return VarInts.readInt(() -> this.readByte(ctx));
    }

    // ---

    @Override
    public boolean readBoolean(SerializationContext ctx) {
        return this.buffer.get() == 1;
    }

    @Override
    public String readString(SerializationContext ctx) {
        var sequenceLength = this.readVarInt(ctx);
        byte[] bytes = new byte[sequenceLength];

        buffer.get(bytes);

        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] readBytes(SerializationContext ctx) {
        var array = new byte[this.readVarInt(ctx)];
        this.buffer.get(array);

        return array;
    }

    @Override
    public <V> Optional<V> readOptional(SerializationContext ctx, Endec<V> endec) {
        return this.readBoolean(ctx)
                ? Optional.of(endec.decode(ctx, this))
                : Optional.empty();
    }

    // ---

    @Override
    public <V> V tryRead(Function<Deserializer<ByteBuffer>, V> reader) {
        var prevReaderIdx = this.buffer.position();

        try {
            return reader.apply(this);
        } catch (Exception e) {
            this.buffer.position(prevReaderIdx);
            throw e;
        }
    }

    // ---

    @Override
    public <E> Deserializer.Sequence<E> sequence(SerializationContext ctx, Endec<E> elementEndec) {
        return new Sequence<>(ctx, elementEndec, this.readVarInt(ctx));
    }

    @Override
    public <V> Deserializer.Map<V> map(SerializationContext ctx, Endec<V> valueEndec) {
        return new Map<>(ctx, valueEndec, this.readVarInt(ctx));
    }

    @Override
    public Struct struct() {
        return new Sequence<>(null, null, 0);
    }

    // ---

    private class Sequence<V> implements Deserializer.Sequence<V>, Struct {

        private final SerializationContext ctx;
        private final Endec<V> valueEndec;
        private final int size;

        private int index = 0;

        private Sequence(SerializationContext ctx, Endec<V> valueEndec, int size) {
            this.ctx = ctx;
            this.valueEndec = valueEndec;
            this.size = size;
        }

        @Override
        public int estimatedSize() {
            return this.size;
        }

        @Override
        public boolean hasNext() {
            return this.index < this.size;
        }

        @Override
        public V next() {
            this.index++;
            return this.valueEndec.decode(this.ctx, ByteBufferDeserializer.this);
        }

        @Override
        public <F> F field(String name, SerializationContext ctx, Endec<F> endec, Supplier<F> defaultValueFactory) {
            return endec.decode(ctx, ByteBufferDeserializer.this);
        }
    }

    private class Map<V> implements Deserializer.Map<V> {

        private final SerializationContext ctx;
        private final Endec<V> valueEndec;
        private final int size;

        private int index = 0;

        private Map(SerializationContext ctx, Endec<V> valueEndec, int size) {
            this.ctx = ctx;
            this.valueEndec = valueEndec;
            this.size = size;
        }

        @Override
        public int estimatedSize() {
            return this.size;
        }

        @Override
        public boolean hasNext() {
            return this.index < this.size;
        }

        @Override
        public java.util.Map.Entry<String, V> next() {
            this.index++;
            return java.util.Map.entry(
                    ByteBufferDeserializer.this.readString(this.ctx),
                    this.valueEndec.decode(this.ctx, ByteBufferDeserializer.this)
            );
        }
    }
}