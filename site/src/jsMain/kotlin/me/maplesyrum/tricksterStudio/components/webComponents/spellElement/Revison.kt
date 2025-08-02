package me.maplesyrum.tricksterStudio.components.webComponents.spellElement

import me.maplesyrum.tricksterStudio.spell.fragment.SpellPart
import me.maplesyrum.tricksterStudio.spell.fragment.Pattern

interface Revision {
    fun pattern(): Pattern
    fun apply(ctx: RevisionContext, root: SpellPart, drawingPart: SpellPart): SpellPart
}

object EXECUTE_OFF_HAND : Revision {
    override fun pattern(): Pattern = Pattern.from(listOf(4, 3, 0, 4, 5, 2, 4, 1))
    override fun apply(ctx: RevisionContext, root: SpellPart, drawingPart: SpellPart): SpellPart = drawingPart
}

object CREATE_SUBCIRCLE : Revision {
    override fun pattern(): Pattern = Pattern.from(listOf(0, 4, 8, 7))
    override fun apply(ctx: RevisionContext, root: SpellPart, drawingPart: SpellPart): SpellPart {
        drawingPart.subParts = drawingPart.subParts + SpellPart()
        return root
    }
}

val revisions: Map<Pattern, Revision> = mapOf(
    EXECUTE_OFF_HAND.pattern() to EXECUTE_OFF_HAND,
    CREATE_SUBCIRCLE.pattern() to CREATE_SUBCIRCLE
)

fun lookup(pattern: Pattern): Revision? {
    return revisions.entries.firstOrNull { it.key == pattern }?.value
}
