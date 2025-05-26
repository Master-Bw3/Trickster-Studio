package dev.enjarai.trickster.spell.execution

import dev.enjarai.trickster.EndecTomfoolery
import dev.enjarai.trickster.spell.EnterScopeInstruction
import dev.enjarai.trickster.spell.ExitScopeInstruction
import dev.enjarai.trickster.spell.Fragment
import me.maplesyrum.tricksterstudio.spell.executer.SpellInstruction
import tree.maple.kendec.Endec
import tree.maple.kendec.PrimitiveEndecs
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder
import tree.maple.kendec.util.Optional

class SerializedSpellInstruction(val type: SpellInstructionType, fragment: Fragment?) {
    fun toDeserialized(): SpellInstruction? {
        return when (type) {
            SpellInstructionType.FRAGMENT -> fragment
            SpellInstructionType.ENTER_SCOPE -> EnterScopeInstruction()
            SpellInstructionType.EXIT_SCOPE -> ExitScopeInstruction()
        }
    }

    val fragment: Fragment?

    init {
        this.fragment = fragment
    }

    companion object {
        val ENDEC: StructEndec<SerializedSpellInstruction> = StructEndecBuilder.of(
            PrimitiveEndecs.INT.fieldOf("instruction_id", { s -> s.type.getId() }),
            EndecTomfoolery.forcedSafeOptionalOf(Fragment.ENDEC)
                .fieldOf("fragment", { s -> Optional.ofNullable(s.fragment) }),
            { id, optionalFragment ->
                dev.enjarai.trickster.spell.execution.SerializedSpellInstruction(
                    SpellInstructionType.fromId(id),
                    optionalFragment.orElse(null)
                )
            }
        )
    }
}

