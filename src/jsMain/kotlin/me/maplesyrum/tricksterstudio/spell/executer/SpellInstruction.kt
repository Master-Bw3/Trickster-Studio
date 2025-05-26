package me.maplesyrum.tricksterstudio.spell.executer

import dev.enjarai.trickster.spell.execution.SerializedSpellInstruction
import tree.maple.kendec.Endec


interface SpellInstruction {
    fun asSerialized(): SerializedSpellInstruction

    companion object {
        val STACK_ENDEC: Endec<List<SpellInstruction>> =
            SerializedSpellInstruction.ENDEC.listOf().xmap(
                { l ->
                    val s: MutableList<SpellInstruction> = mutableListOf()
                    s.addAll(l.map(SerializedSpellInstruction::toDeserialized).toTypedArray())
                    s
                },
                { s ->
                    s.map { obj: SpellInstruction -> obj.asSerialized() }
                })
    }
}
