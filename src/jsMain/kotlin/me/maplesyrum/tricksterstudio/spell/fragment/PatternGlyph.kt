package me.maplesyrum.tricksterstudio.spell.fragment

import me.maplesyrum.tricksterstudio.spell.executer.EvaluationResult
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder


class PatternGlyph(val pattern: Pattern) : Fragment() {
    constructor() : this(Pattern.EMPTY)
    constructor(vararg pattern: Int) : this(pattern.map(Int::toByte))
    constructor(pattern: List<Byte>) : this(Pattern.from(pattern))

    override fun type(): FragmentType<*> {
        return FragmentType.PATTERN
    }

    override fun toString(): String {
        return "pattern glyph";
    }

    companion object {
        val ENDEC: StructEndec<PatternGlyph> = StructEndecBuilder.of(
            Pattern.ENDEC.fieldOf("pattern", PatternGlyph::pattern),
            { PatternGlyph() }
        )
    }
}
