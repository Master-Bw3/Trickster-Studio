package me.maplesyrum.tricksterStudio.spell.executer

class ExitScopeInstruction : SpellInstruction {
    override fun asSerialized(): SerializedSpellInstruction {
        return SerializedSpellInstruction(SpellInstructionType.EXIT_SCOPE, null)
    }
}
