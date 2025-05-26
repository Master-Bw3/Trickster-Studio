package dev.enjarai.trickster.spell

import dev.enjarai.trickster.spell.execution.SerializedSpellInstruction
import dev.enjarai.trickster.spell.execution.SpellInstructionType
import me.maplesyrum.tricksterstudio.spell.executer.SpellInstruction

class ExitScopeInstruction : SpellInstruction {
    override fun asSerialized(): SerializedSpellInstruction {
        return SerializedSpellInstruction(SpellInstructionType.EXIT_SCOPE, null)
    }
}
