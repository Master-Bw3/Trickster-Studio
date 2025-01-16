package maple.trickster_endec.fragment;


import io.netty.buffer.Unpooled;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.format.bytebuf.ByteBufDeserializer;
import io.wispforest.endec.format.bytebuf.ByteBufSerializer;
import io.wispforest.endec.impl.StructEndecBuilder;
import maple.trickster_endec.SpellUtils;
import maple.trickster_endec.endecs.ByteBufferDeserializer;
import maple.trickster_endec.endecs.ByteBufferSerializer;
import maple.trickster_endec.endecs.EndecTomfoolery;
import maple.trickster_endec.spell_instruction.SpellInstruction;
import org.apache.commons.lang3.ArrayUtils;
import org.teavm.jso.JSExport;
import org.teavm.jso.JSProperty;
import org.teavm.jso.core.JSArray;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public final class SpellPart implements Fragment {
    public static final StructEndec<SpellPart> ENDEC = EndecTomfoolery.recursive(self -> StructEndecBuilder.of(
            Fragment.ENDEC.fieldOf("glyph", SpellPart::getGlyph),
            EndecTomfoolery.protocolVersionAlternatives(
                    Map.of(
                            (byte) 1, self.listOf()
                    ),
                    EndecTomfoolery.withAlternative(SpellInstruction.STACK_ENDEC.xmap(
                            instructions -> SpellUtils.decodeInstructions(instructions, new Stack<>(), new Stack<>(), Optional.empty()),
                            SpellUtils::flattenNode
                    ), self).listOf()
            ).fieldOf("sub_parts", SpellPart::getSubParts), SpellPart::new
    ));

    public Fragment glyph;

    public List<SpellPart> subParts;

    public SpellPart(Fragment glyph, List<SpellPart> subParts) {
        this.glyph = glyph;
        this.subParts = new ArrayList<>(subParts);
    }

    public SpellPart(Fragment glyph) {
        this(glyph, new ArrayList<>());
    }

    public SpellPart() {
        this(new PatternGlyph());
    }

    @JSExport
    @JSProperty
    public Fragment getGlyph() {
        return glyph;
    }

    public List<SpellPart> getSubParts() {
        return subParts;
    }

    @JSExport
    @JSProperty("subParts")
    public JSArray<SpellPart> getSubPartsJS() {
        var array = new JSArray<SpellPart>();
        subParts.forEach(array::push);
        return array;
    }

    @Override
    public FragmentType<?> type() {
        return FragmentType.SPELL_PART;
    }

    @Override
    public int hashCode() {
        return Objects.hash(glyph, subParts);
    }

    @Override
    public String toString() {
        return "SpellPart[" +
                "glyph=" + glyph + ", " +
                "subParts=" + subParts + ']';
    }

    public String toBase64() {
        var buf = ByteBuffer.allocate(255);
        buf.put((byte) 2); // Protocol version
        ENDEC.encode(
                SerializationContext.empty().withAttributes(EndecTomfoolery.UBER_COMPACT_ATTRIBUTE),
                ByteBufferSerializer.of(buf), this
        );

        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream(buf.remaining())) {
            try (GZIPOutputStream zipStream = new GZIPOutputStream(byteStream)) {
                while (buf.hasRemaining()) {
                    zipStream.write(buf.get());
                }
                zipStream.finish();
            }

            byte[] bytes = byteStream.toByteArray();
            String result;

            try {
                result = Base64.getEncoder().encodeToString(Arrays.copyOfRange(bytes, 10, bytes.length));
            } catch (Throwable e) {
                throw new RuntimeException("Base64 encoding failed", e);
            }

            return result;
        } catch (IOException e) {
            throw new RuntimeException("Compression failed", e);
        }
    }

    private static final byte[] base64Header = new byte[]{0x1f, (byte) 0x8b, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xff};

    public static SpellPart fromBase64(String string) {
        var buf = ByteBuffer.allocate(255);

        var byteStream = new ByteArrayInputStream(ArrayUtils.addAll(base64Header, Base64.getDecoder().decode(string.strip())));
        try (byteStream) {
            try (GZIPInputStream zipStream = new GZIPInputStream(byteStream)) {
                buf.put(zipStream.readAllBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException("Spell decoding broke. what.");
        }

        buf.position(0);
        var protocolVersion = buf.get();
        SpellPart result;
        try {
            result = ENDEC.decode(
                    SerializationContext.empty().withAttributes(
                            EndecTomfoolery.UBER_COMPACT_ATTRIBUTE,
                            EndecTomfoolery.PROTOCOL_VERSION_ATTRIBUTE.instance(protocolVersion)
                    ),
                    ByteBufferDeserializer.of(buf)
            );
        } catch (Throwable e) {
            throw e;
        }

        return result;
    }
}

