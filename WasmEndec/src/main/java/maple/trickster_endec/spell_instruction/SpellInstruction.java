package maple.trickster_endec.spell_instruction;

import io.wispforest.endec.Endec;
import maple.trickster_endec.fragment.Fragment;

import java.util.Stack;

public interface SpellInstruction {
    public static final Endec<Stack<SpellInstruction>> STACK_ENDEC = SerializedSpellInstruction.ENDEC.listOf().xmap(l -> {
        var s = new Stack<SpellInstruction>();
        s.addAll(l.stream().map(SerializedSpellInstruction::toDeserialized).toList());
        return s;
    }, (s) -> s.stream().map(SpellInstruction::asSerialized).toList());

    SerializedSpellInstruction asSerialized();
}