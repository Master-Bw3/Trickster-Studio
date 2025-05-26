package dev.enjarai.trickster.spell

import com.google.common.collect.ImmutableList
import dev.enjarai.trickster.EndecTomfoolery
import dev.enjarai.trickster.spell.Pattern.Companion.from
import dev.enjarai.trickster.spell.blunder.BlunderException
import me.maplesyrum.tricksterstudio.spell.fragment.FragmentType
import tree.maple.kendec.Endec
import net.minecraft.text.Text
import org.apache.commons.lang3.ArrayUtils
import java.util.stream.Stream
import kotlin.js.get
import kotlin.jvm.JvmRecord

@JvmRecord
data class Pattern(val entries: List<dev.enjarai.trickster.spell.Pattern.PatternEntry?>?) : Fragment {
    val isEmpty: Boolean
        get() = this.entries!!.isEmpty()

    fun contains(point: Int): Boolean {
        val realPoint = point.toByte()
        for (entry in entries!!) {
            if (entry!!.p1 == realPoint || entry.p2 == realPoint) {
                return true
            }
        }
        return false
    }

    fun toInt(): Int {
        var result = 0
        for (i in 0..31) {
            if (entries!!.contains(dev.enjarai.trickster.spell.Pattern.Companion.possibleLines[i])) {
                result = result or (1 shl i)
            }
        }
        return result
    }

    @Override
    @Throws(BlunderException::class)
    fun activateAsGlyph(ctx: SpellContext?, fragments: List<Fragment?>?): Fragment? {
        return PatternGlyph(this)
    }

    @Override
    fun type(): FragmentType<*> {
        return FragmentType.PATTERN_LITERAL
    }

    @Override
    fun asText(): Text {
        return Text.literal("<").append(PatternGlyph(this).asText()).append(">")
    }

    @get:Override
    val weight: Int
        get() = 32

    @JvmRecord
    data class PatternEntry(val p1: Byte, val p2: Byte) :
        Comparable<dev.enjarai.trickster.spell.Pattern.PatternEntry?> {
        @Override
        fun compareTo(@NotNull o: dev.enjarai.trickster.spell.Pattern.PatternEntry): Int {
            val p1Compare: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
                Integer.compare(p1, o.p1)
            return if (p1Compare == 0) Integer.compare(p2, o.p2) else p1Compare
        }

        companion object {
            val ENDEC: Endec<dev.enjarai.trickster.spell.Pattern.PatternEntry?> = Endec.BYTES
                .xmap(
                    { list -> dev.enjarai.trickster.spell.Pattern.PatternEntry(list[0], list[1]) },
                    { entry -> byteArrayOf(entry.p1, entry.p2) })
        }
    }

    companion object {
        val ENDEC: Endec<dev.enjarai.trickster.spell.Pattern?>? = Endec.ifAttr(
            EndecTomfoolery.UBER_COMPACT_ATTRIBUTE,
            Endec.INT.xmap(
                dev.enjarai.trickster.spell.Pattern::from,
                { obj: dev.enjarai.trickster.spell.Pattern? -> obj!!.toInt() })
        )
            .orElse(
                dev.enjarai.trickster.spell.Pattern.PatternEntry.Companion.ENDEC.listOf().xmap(
                    { entries: List<dev.enjarai.trickster.spell.Pattern.PatternEntry?>? -> Pattern(entries) },
                    dev.enjarai.trickster.spell.Pattern::entries
                )
            )
        val EMPTY: dev.enjarai.trickster.spell.Pattern = of()

        private val possibleLines: Array<dev.enjarai.trickster.spell.Pattern.PatternEntry> =
            kotlin.arrayOfNulls<dev.enjarai.trickster.spell.Pattern.PatternEntry>(32)

        init {
            var i = 0
            for (p1 in 0..8) {
                for (p2 in 0..8) {
                    if (p2 > p1 && p1 + p2 != 8) {
                        dev.enjarai.trickster.spell.Pattern.Companion.possibleLines[i] =
                            dev.enjarai.trickster.spell.Pattern.PatternEntry(p1, p2)
                        i++
                    }
                }
            }
        }

        fun from(pattern: List<Byte?>): dev.enjarai.trickster.spell.Pattern {
            val list = ArrayList<dev.enjarai.trickster.spell.Pattern.PatternEntry?>()
            var last: Byte? = null
            for (current in pattern) {
                if (last != null) {
                    if (last < current!!) {
                        list.add(dev.enjarai.trickster.spell.Pattern.PatternEntry(last, current))
                    } else {
                        list.add(dev.enjarai.trickster.spell.Pattern.PatternEntry(current, last))
                    }
                }
                last = current
            }
            list.sort({ obj: dev.enjarai.trickster.spell.Pattern.PatternEntry?, o: dev.enjarai.trickster.spell.Pattern.PatternEntry ->
                obj!!.compareTo(
                    o
                )
            })
            return Pattern(ImmutableList.copyOf(list))
        }

        fun from(pattern: Int): dev.enjarai.trickster.spell.Pattern {
            val builder: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
                ImmutableList.< PatternEntry > builder < dev . enjarai . trickster . spell . Pattern . PatternEntry ? > ()
            for (i in 0..31) {
                if ((pattern shr i and 0x1) == 1) {
                    builder.add(dev.enjarai.trickster.spell.Pattern.Companion.possibleLines[i])
                }
            }
            return Pattern(builder.build())
        }

        fun of(vararg pattern: Int): dev.enjarai.trickster.spell.Pattern {
            val result: dev.enjarai.trickster.spell.Pattern =
                from(Stream.of(ArrayUtils.toObject(pattern)).map(Integer::byteValue).toList())

            for (line in result.entries!!) {
                var b = false

                for (line2 in dev.enjarai.trickster.spell.Pattern.Companion.possibleLines) {
                    if (line2.equals(line)) {
                        b = true
                        break
                    }
                }

                require(b) { "Pattern is not valid" }
            }

            return result
        }
    }
}
