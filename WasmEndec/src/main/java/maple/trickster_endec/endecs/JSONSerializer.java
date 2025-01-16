package maple.trickster_endec.endecs;

import io.wispforest.endec.*;
import io.wispforest.endec.util.RecursiveSerializer;
import org.teavm.flavour.json.JSON;
import org.teavm.flavour.json.tree.ArrayNode;
import org.teavm.flavour.json.tree.Node;
import org.teavm.flavour.json.tree.NullNode;
import org.teavm.flavour.json.tree.ObjectNode;

import java.util.ArrayList;
import java.util.Optional;

public class JSONSerializer extends RecursiveSerializer<Node> implements SelfDescribedSerializer<Node> {

    private Node prefix;

    protected JSONSerializer(Node prefix) {
        super(null);
        this.prefix = prefix;
    }

    public static JSONSerializer of(Node prefix) {
        return new JSONSerializer(prefix);
    }

    public static JSONSerializer of() {
        return of(null);
    }

    @Override
    public SerializationContext setupContext(SerializationContext ctx) {
        return super.setupContext(ctx).withAttributes(SerializationAttributes.HUMAN_READABLE);
    }

    // ---

    @Override
    public void writeByte(SerializationContext ctx, byte value) {
        this.consume(JSON.serialize(value));
    }

    @Override
    public void writeShort(SerializationContext ctx, short value) {
        this.consume(JSON.serialize(value));
    }

    @Override
    public void writeInt(SerializationContext ctx, int value) {
        this.consume(JSON.serialize(value));
    }

    @Override
    public void writeLong(SerializationContext ctx, long value) {
        this.consume(JSON.serialize(value));
    }

    @Override
    public void writeFloat(SerializationContext ctx, float value) {
        this.consume(JSON.serialize(value));
    }

    @Override
    public void writeDouble(SerializationContext ctx, double value) {
        this.consume(JSON.serialize(value));
    }

    // ---

    @Override
    public void writeVarInt(SerializationContext ctx, int value) {
        this.writeInt(ctx, value);
    }

    @Override
    public void writeVarLong(SerializationContext ctx, long value) {
        this.writeLong(ctx, value);
    }

    // ---

    @Override
    public void writeBoolean(SerializationContext ctx, boolean value) {
        this.consume(JSON.serialize(value));
    }

    @Override
    public void writeString(SerializationContext ctx, String value) {
        this.consume(JSON.serialize(value));
    }

    @Override
    public void writeBytes(SerializationContext ctx, byte[] bytes) {
        var result = ArrayNode.create();
        for (int i = 0; i < bytes.length; i++) {
            result.add(JSON.serialize(bytes[i]));
        }

        this.consume(result);
    }

    @Override
    public <V> void writeOptional(SerializationContext ctx, Endec<V> endec, Optional<V> optional) {
        optional.ifPresentOrElse(
                value -> endec.encode(ctx, this, value),
                () -> this.consume(NullNode.instance())
        );
    }

    // ---

    @Override
    public <E> Serializer.Sequence<E> sequence(SerializationContext ctx, Endec<E> elementEndec, int size) {
        return new Sequence<>(ctx, elementEndec, size);
    }

    @Override
    public <V> Serializer.Map<V> map(SerializationContext ctx, Endec<V> valueEndec, int size) {
        return new Map<>(ctx, valueEndec);
    }

    @Override
    public Struct struct() {
        return new Map<>(null, null);
    }

    // ---

    private class Map<V> implements Serializer.Map<V>, Struct {

        private final SerializationContext ctx;
        private final Endec<V> valueEndec;
        private final ObjectNode result;

        private Map(SerializationContext ctx, Endec<V> valueEndec) {
            this.ctx = ctx;
            this.valueEndec = valueEndec;

            if (JSONSerializer.this.prefix != null) {
                if (JSONSerializer.this.prefix instanceof ObjectNode prefixObject) {
                    this.result = prefixObject;
                    JSONSerializer.this.prefix = null;
                } else {
                    throw new IllegalStateException("Incompatible prefix of type " + JSONSerializer.this.prefix.getClass().getSimpleName() + " used for JSON map/struct");
                }
            } else {
                this.result = ObjectNode.create();
            }
        }

        @Override
        public void entry(String key, V value) {
            JSONSerializer.this.frame(encoded -> {
                this.valueEndec.encode(this.ctx, JSONSerializer.this, value);
                this.result.set(key, encoded.require("map value"));
            });
        }

        @Override
        public <F> Struct field(String name, SerializationContext ctx, Endec<F> endec, F value, boolean mayOmit) {
            JSONSerializer.this.frame(encoded -> {
                endec.encode(ctx, JSONSerializer.this, value);

                var element = encoded.require("struct field");

                if (mayOmit && element.equals(NullNode.instance())) return;

                this.result.set(name, element);
            });

            return this;
        }

        @Override
        public void end() {
            JSONSerializer.this.consume(result);
        }
    }

    private class Sequence<V> implements Serializer.Sequence<V> {

        private final SerializationContext ctx;
        private final Endec<V> valueEndec;
        private final ArrayNode result;

        private Sequence(SerializationContext ctx, Endec<V> valueEndec, int size) {
            this.ctx = ctx;
            this.valueEndec = valueEndec;

            if (JSONSerializer.this.prefix != null) {
                if (JSONSerializer.this.prefix instanceof ArrayNode prefixArray) {
                    this.result = prefixArray;
                    JSONSerializer.this.prefix = null;
                } else {
                    throw new IllegalStateException("Incompatible prefix of type " + JSONSerializer.this.prefix.getClass().getSimpleName() + " used for JSON sequence");
                }
            } else {
                this.result = ArrayNode.create();
            }
        }

        @Override
        public void element(V element) {
            JSONSerializer.this.frame(encoded -> {
                this.valueEndec.encode(this.ctx, JSONSerializer.this, element);
                this.result.add(encoded.require("sequence element"));
            });
        }

        @Override
        public void end() {
            JSONSerializer.this.consume(result);
        }
    }
}
