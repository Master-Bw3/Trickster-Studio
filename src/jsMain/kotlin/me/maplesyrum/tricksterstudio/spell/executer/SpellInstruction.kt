package me.maplesyrum.tricksterstudio.spell.executer

import tree.maple.kendec.Endec


interface SpellInstruction {
    fun asSerialized(): SerializedSpellInstruction

    companion object {
        val STACK_ENDEC: Endec<List<SpellInstruction>> =
            SerializedSpellInstruction.ENDEC.listOf().xmap(
                { l -> l.mapNotNull(SerializedSpellInstruction::toDeserialized)
                },
                { s ->
                    s.map { obj: SpellInstruction -> obj.asSerialized() }
                })
    }
}
