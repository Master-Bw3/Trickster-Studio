package me.maplesyrum.tricksterStudio.spell.executer

class EnterScopeInstruction : SpellInstruction {
    override fun asSerialized(): SerializedSpellInstruction {
        return SerializedSpellInstruction(SpellInstructionType.ENTER_SCOPE, null)
    }
}
