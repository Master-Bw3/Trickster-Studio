package dev.enjarai.trickster.spell

import dev.enjarai.trickster.spell.blunder.BlunderException
import dev.enjarai.trickster.spell.blunder.OverweightFragmentBlunder
import dev.enjarai.trickster.spell.blunder.UnknownTrickBlunder
import me.maplesyrum.tricksterstudio.spell.fragment.FragmentType
import dev.enjarai.trickster.spell.fragment.VoidFragment
import dev.enjarai.trickster.spell.trick.Tricks
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder
import net.minecraft.text.Text
import org.apache.commons.lang3.ArrayUtils
import java.util.stream.Stream

class PatternGlyph(pattern: Pattern?) : Fragment {
    constructor() : this(Pattern.EMPTY)

    constructor(vararg pattern: Int) : this(Stream.of(ArrayUtils.toObject(pattern)).map(Integer::byteValue).toList())

    constructor(pattern: List<Byte?>?) : this(Pattern.from(pattern))

    @Override
    @Throws(BlunderException::class)
    fun activateAsGlyph(ctx: SpellContext?, fragments: List<Fragment?>?): EvaluationResult? {
        if (pattern.equals(Pattern.EMPTY)) {
            return VoidFragment.INSTANCE
        }

        val trick: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
            Tricks.lookup(pattern)

        if (trick == null) {
            throw UnknownTrickBlunder()
        }

        val result: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
            trick.activate(ctx, fragments)

        if (result is Fragment && result.getWeight() > Fragment.MAX_WEIGHT) {
            throw OverweightFragmentBlunder(trick, result)
        }

        return result
    }

    @Override
    fun type(): FragmentType<*> {
        return FragmentType.PATTERN
    }

    @Override
    fun asText(): Text {
        val trick: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
            Tricks.lookup(pattern)
        if (trick != null) {
            return trick.getName()
        }
        return Text.of("Unknown")
    }

    @get:Override
    val weight: Int
        get() = pattern.getWeight()
    val pattern: Pattern?

    init {
        this.pattern = pattern
    }

    companion object {
        val ENDEC: StructEndec<dev.enjarai.trickster.spell.PatternGlyph?>? = StructEndecBuilder.of(
            Pattern.ENDEC.fieldOf("pattern", dev.enjarai.trickster.spell.PatternGlyph::pattern),
            { PatternGlyph() }
        )
    }
}
