package dev.enjarai.trickster.spell

import dev.enjarai.trickster.EndecTomfoolery
import dev.enjarai.trickster.spell.blunder.BlunderException
import dev.enjarai.trickster.spell.execution.executor.DefaultSpellExecutor
import me.maplesyrum.tricksterstudio.spell.fragment.FragmentType
import dev.enjarai.trickster.spell.fragment.VoidFragment
import dev.enjarai.trickster.util.SpellUtils
import io.netty.buffer.ByteBuf
import tree.maple.kendec.SerializationContext
import tree.maple.kendec.StructEndec
import tree.maple.kendec.format.bytebuf.ByteBufDeserializer
import tree.maple.kendec.impl.StructEndecBuilder
import me.maplesyrum.tricksterstudio.spell.executer.SpellInstruction
import net.minecraft.text.Text
import java.util.stream.Collectors
import kotlin.Any
import kotlin.Boolean
import kotlin.Byte
import kotlin.Function
import kotlin.Int
import kotlin.String
import kotlin.Throws
import kotlin.Unit
import kotlin.UnsupportedOperationException
import kotlin.apply
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.containsKey
import kotlin.collections.forEach
import kotlin.collections.get
import kotlin.collections.isEmpty
import kotlin.collections.map
import kotlin.collections.plusAssign
import kotlin.collections.remove
import kotlin.collections.toList
import kotlin.invoke
import kotlin.js.get
import kotlin.jvm.JvmOverloads
import kotlin.map
import kotlin.sequences.map
import kotlin.sequences.toList
import kotlin.text.append
import kotlin.text.clear
import kotlin.text.equals
import kotlin.text.get
import kotlin.text.isEmpty
import kotlin.text.set
import kotlin.text.toList
import kotlin.toList

class SpellPart(glyph: Fragment, subParts: List<dev.enjarai.trickster.spell.SpellPart?>?) : Fragment {
    var glyph: Fragment
    var subParts: List<dev.enjarai.trickster.spell.SpellPart?>

    init {
        this.glyph = glyph
        this.subParts = ArrayList(subParts)
    }

    @JvmOverloads
    constructor(glyph: Fragment = PatternGlyph()) : this(glyph, ArrayList())

    @Override
    @Throws(BlunderException::class)
    fun activateAsGlyph(ctx: SpellContext, fragments: List<Fragment?>): EvaluationResult? {
        if (fragments.isEmpty()) {
            return super.activateAsGlyph(ctx, fragments)
        } else {
            return DefaultSpellExecutor(this, ctx.state().recurseOrThrow(fragments))
        }
    }

    /**
     * Since spell parts are mutable, this implementation deeply clones the entire object.
     */
    @Override
    fun applyEphemeral(): dev.enjarai.trickster.spell.SpellPart {
        return SpellPart(
            glyph.applyEphemeral(), subParts.stream()
                .map({ obj: dev.enjarai.trickster.spell.SpellPart? -> obj!!.applyEphemeral() }).toList()
        )
    }

    @get:Override
    val weight: Int
        get() {
            var weight = 8
            weight += glyph.getWeight()

            for (subPart in subParts) {
                weight += subPart.getWeight()
            }

            return weight
        }

    fun destructiveRun(ctx: SpellContext?): Fragment {
        val arguments: ArrayList<Fragment?> = ArrayList<Fragment?>()

        for (subpart in subParts) {
            arguments.add(subpart!!.destructiveRun(ctx))
        }

        val result: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
            glyph.activateAsGlyph(ctx, arguments)
        val value: Fragment

        if (result is SpellExecutor) {
            value = result.singleTickRun(ctx) //TODO: should account for ticks so far
        } else if (result is Fragment) {
            value = result
        } else {
            throw UnsupportedOperationException()
        }

        if (!value.equals(VoidFragment.INSTANCE)) {
            if (glyph !== value) {
                subParts.clear()
            }

            glyph = value
        }

        return value
    }

    fun buildClosure(replacements: io.vavr.collection.Map<Fragment?, Fragment?>): dev.enjarai.trickster.spell.SpellPart {
        subParts.forEach({ part -> part!!.buildClosure(replacements) })

        if (glyph is dev.enjarai.trickster.spell.SpellPart) {
            glyph.buildClosure(replacements)
        } else if (replacements.containsKey(glyph)) {
            glyph = replacements.get(glyph).get()
        }

        return this
    }

    fun setSubPartInTree(
        replace: Function<dev.enjarai.trickster.spell.SpellPart?, dev.enjarai.trickster.spell.SpellPart?>,
        current: dev.enjarai.trickster.spell.SpellPart,
        targetIsInner: Boolean
    ): Boolean {
        if (current.glyph is dev.enjarai.trickster.spell.SpellPart) {
            if (if (targetIsInner) glyph.glyph === this else glyph == this) {
                val newPart: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
                    replace.apply(glyph)
                current.glyph = if (newPart == null) PatternGlyph() else newPart
                return true
            }

            if (setSubPartInTree(replace, glyph, targetIsInner)) {
                return true
            }
        }

        var i = 0
        for (part in current.subParts) {
            if (if (targetIsInner) part!!.glyph === this else part == this) {
                val newPart: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
                    replace.apply(part)
                if (newPart != null) {
                    current.subParts.set(i, newPart)
                } else {
                    current.subParts.remove(i)
                }
                return true
            }

            if (setSubPartInTree(replace, part!!, targetIsInner)) {
                return true
            }
            i++
        }

        return false
    }

    fun getGlyph(): Fragment {
        return glyph
    }

    val isEmpty: Boolean
        get() = subParts.isEmpty() && glyph is PatternGlyph && glyph.pattern().isEmpty()

    @Override
    fun equals(obj: Object?): Boolean {
        if (obj === this) return true
        if (obj == null || obj.getClass() !== this.getClass()) return false
        val that = obj as dev.enjarai.trickster.spell.SpellPart
        return Objects.equals(this.glyph, that.glyph) &&
                Objects.equals(this.subParts, that.subParts)
    }

    @Override
    fun hashCode(): Int {
        return Objects.hash(glyph, subParts)
    }

    @Override
    fun toString(): String? {
        return "SpellPart[" +
                "glyph=" + glyph + ", " +
                "subParts=" + subParts + ']'
    }

    @Override
    fun type(): FragmentType<*> {
        return FragmentType.SPELL_PART
    }

    @Override
    fun asText(): Text? {
        val text: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
            Text.literal("").append(glyph.asFormattedText()).append("{")
        for (i in 0..<subParts.size()) {
            val subPart: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
                subParts.get(i)
            if (i > 0) {
                text.append(", ")
            }
            text.append(subPart.asFormattedText())
        }
        text.append("}")
        return text
    }

    @Override
    fun asFormattedText(): Text? {
        return asText()
    }

    fun deepClone(): dev.enjarai.trickster.spell.SpellPart {
        val glyph: Any? = if (this.glyph is dev.enjarai.trickster.spell.SpellPart) glyph.deepClone() else this.glyph

        return SpellPart(
            glyph, subParts.stream()
                .map({ obj: dev.enjarai.trickster.spell.SpellPart? -> obj!!.deepClone() }).collect(Collectors.toList())
        )
    }

    companion object {
        val ENDEC: StructEndec<dev.enjarai.trickster.spell.SpellPart?> = EndecTomfoolery.recursive(
            { self ->
                StructEndecBuilder.of(
                    Fragment.ENDEC.fieldOf(
                        "glyph",
                        { obj: dev.enjarai.trickster.spell.SpellPart? -> obj!!.getGlyph() }),
                    EndecTomfoolery.protocolVersionAlternatives(
                        Map.of(
                            1.toByte(), self.listOf()
                        ),
                        EndecTomfoolery.withAlternative(
                            SpellInstruction.STACK_ENDEC.xmap(
                                { instructions ->
                                    SpellUtils.decodeInstructions(
                                        instructions,
                                        Stack(),
                                        Stack(),
                                        Optional.empty()
                                    )
                                },
                                SpellUtils::flattenNode
                            ), self
                        ).listOf()
                    ).fieldOf("sub_parts", { obj: dev.enjarai.trickster.spell.SpellPart? -> obj!!.subParts }),
                    { SpellPart() }
                )
            }
        )

        fun fromBytesOld(protocolVersion: Byte, buf: ByteBuf): dev.enjarai.trickster.spell.SpellPart? {
            var result: dev.enjarai.trickster.spell.SpellPart?
            try {
                result = ENDEC.decode(
                    SerializationContext.empty().withAttributes(
                        EndecTomfoolery.UBER_COMPACT_ATTRIBUTE,
                        EndecTomfoolery.PROTOCOL_VERSION_ATTRIBUTE.instance(protocolVersion)
                    ),
                    ByteBufDeserializer.of(buf)
                )
            } finally {
                buf.release()
            }

            return result
        }
    }
}
