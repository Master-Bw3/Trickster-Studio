package maple.trickster_endec.fragment;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;

import java.io.UncheckedIOException;
import java.util.Objects;

public final class NumberFragment implements Fragment {
    public static final StructEndec<NumberFragment> ENDEC = StructEndecBuilder.of(
            Endec.DOUBLE.fieldOf("number", NumberFragment::number),
            NumberFragment::new
    );

    private final double number;

    public NumberFragment(double number) {
        if (Double.isNaN(number))
            this.number = 0;
        else
            this.number = number;
    }

    @Override
    public FragmentType<?> type() {
        return FragmentType.NUMBER;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NumberFragment n && n.number == number;
    }

    public double number() {
        return number;
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public String toString() {
        return "NumberFragment[" + "number=" + number + "]";
    }
}
