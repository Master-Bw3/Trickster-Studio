package maple.trickster_endec.fragment;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import maple.trickster_endec.Identifier;
import maple.trickster_endec.endecs.EndecTomfoolery;
import maple.trickster_endec.spell_instruction.SerializedSpellInstruction;
import maple.trickster_endec.spell_instruction.SpellInstruction;
import maple.trickster_endec.spell_instruction.SpellInstructionType;


public interface Fragment extends SpellInstruction {
    final int MAX_WEIGHT = 64000;

    @SuppressWarnings("unchecked")
    final StructEndec<Fragment> ENDEC = EndecTomfoolery.lazy(() -> (StructEndec<Fragment>) Endec.dispatchedStruct(
                    FragmentType::endec,
                    Fragment::type,
                    Endec.<FragmentType<?>>ifAttr(EndecTomfoolery.UBER_COMPACT_ATTRIBUTE, Endec.INT.xmap(FragmentType::getFromInt, FragmentType::getIntId))
                            .orElse(Identifier.ENDEC.xmap(FragmentType.REGISTRY::get, FragmentType::getID))
            )
    );

    @Override
    default SerializedSpellInstruction asSerialized() {
        return new SerializedSpellInstruction(SpellInstructionType.FRAGMENT, this);
    }

    FragmentType<?> type();
}
