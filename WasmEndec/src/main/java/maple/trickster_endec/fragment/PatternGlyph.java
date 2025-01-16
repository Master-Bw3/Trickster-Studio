package maple.trickster_endec.fragment;


import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import org.teavm.jso.JSExport;
import org.teavm.jso.JSProperty;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public record PatternGlyph(@JSExport @JSProperty Pattern pattern) implements Fragment {
    public static final StructEndec<PatternGlyph> ENDEC = StructEndecBuilder.of(
            Pattern.ENDEC.fieldOf("pattern", PatternGlyph::pattern),
            PatternGlyph::new
    );

    public PatternGlyph() {
        this(Pattern.EMPTY);
    }

    public PatternGlyph(int... pattern) {
        this(Stream.of(Arrays.stream(pattern)
                .boxed()
                .toArray(Integer[]::new)).map(Integer::byteValue).toList());
    }

    public PatternGlyph(List<Byte> pattern) {
        this(Pattern.from(pattern));
    }


    @Override
    public FragmentType<?> type() {
        return FragmentType.PATTERN;
    }
}
