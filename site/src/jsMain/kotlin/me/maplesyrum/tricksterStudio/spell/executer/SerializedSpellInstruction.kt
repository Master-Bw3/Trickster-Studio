package me.maplesyrum.tricksterStudio.spell.executer

import me.maplesyrum.tricksterStudio.endec.safeOptionalOf
import me.maplesyrum.tricksterStudio.spell.fragment.Fragment
import tree.maple.kendec.PrimitiveEndecs
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder
import tree.maple.kendec.util.Optional

class SerializedSpellInstruction(val type: SpellInstructionType, val fragment: Fragment?) {
    fun toDeserialized(): SpellInstruction? {
        return when (type) {
            SpellInstructionType.FRAGMENT -> fragment
            SpellInstructionType.ENTER_SCOPE -> EnterScopeInstruction()
            SpellInstructionType.EXIT_SCOPE -> ExitScopeInstruction()
        }
    }

    companion object {
        val ENDEC: StructEndec<SerializedSpellInstruction> = StructEndecBuilder.of(
            PrimitiveEndecs.INT.fieldOf("instruction_id", { s -> s.type.getId() }),
            safeOptionalOf(Fragment.ENDEC)
                .fieldOf("fragment") { s -> Optional.ofNullable(s.fragment) }
        ) { id, optionalFragment ->
            SerializedSpellInstruction(
                SpellInstructionType.Companion.fromId(id),
                optionalFragment.orElse(null)
            )
        }
    }
}

