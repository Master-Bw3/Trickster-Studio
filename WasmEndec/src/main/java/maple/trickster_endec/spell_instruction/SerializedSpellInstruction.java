package maple.trickster_endec.spell_instruction;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import maple.trickster_endec.endecs.EndecTomfoolery;
import maple.trickster_endec.fragment.Fragment;

import java.util.Optional;

public record SerializedSpellInstruction(SpellInstructionType type, Fragment fragment) {
    public static final StructEndec<SerializedSpellInstruction> ENDEC = StructEndecBuilder.of(
            Endec.INT.fieldOf("instruction_id", s -> s.type.getId()),
            EndecTomfoolery.safeOptionalOf(Fragment.ENDEC).optionalFieldOf("fragment", s -> Optional.ofNullable(s.fragment), Optional.empty()),
            (id, optionalFragment) -> new SerializedSpellInstruction(SpellInstructionType.fromId(id), optionalFragment.orElse(null))
    );

    public SpellInstruction toDeserialized() {
        return switch (type) {
            case FRAGMENT -> fragment;
            case ENTER_SCOPE -> new EnterScopeInstruction();
            case EXIT_SCOPE -> new ExitScopeInstruction();
        };

    }

}