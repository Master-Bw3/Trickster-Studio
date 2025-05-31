package me.maplesyrum.tricksterstudio.spell.fragment

import me.maplesyrum.tricksterstudio.endec.UBER_COMPACT_ATTRIBUTE
import tree.maple.kendec.Endec
import tree.maple.kendec.PrimitiveEndecs
import tree.maple.kendec.ifAttr


data class Pattern(val entries: List<PatternEntry>) : Fragment() {
    val isEmpty: Boolean
        get() = this.entries.isEmpty()

    fun contains(point: Int): Boolean {
        val realPoint = point.toByte()
        for (entry in entries) {
            if (entry!!.p1 == realPoint || entry.p2 == realPoint) {
                return true
            }
        }
        return false
    }

    fun toInt(): Int {
        var result = 0
        for (i in 0..31) {
            if (entries.contains(possibleLines[i])) {
                result = result or (1 shl i)
            }
        }
        return result
    }

    override fun type(): FragmentType<out Fragment> {
        return FragmentType.PATTERN_LITERAL
    }

    override fun toString(): String {
        return "pattern";
    }

    data class PatternEntry(val p1: Byte, val p2: Byte) : Comparable<PatternEntry> {

        override fun compareTo(o: PatternEntry): Int {
            val p1Compare = p1.compareTo(o.p1)
            return if (p1Compare == 0) p2.compareTo(o.p2) else p1Compare
        }

        companion object {
            val ENDEC: Endec<PatternEntry> = PrimitiveEndecs.BYTES
                .xmap(
                    { list -> PatternEntry(list[0], list[1]) },
                    { entry -> byteArrayOf(entry.p1, entry.p2) })
        }
    }

    companion object {
        val ENDEC = ifAttr(UBER_COMPACT_ATTRIBUTE, PrimitiveEndecs.INT.xmap(Pattern::from, Pattern::toInt))
            .orElse(PatternEntry.ENDEC.listOf().xmap(::Pattern, Pattern::entries));

        val EMPTY: Pattern = of()

        private val possibleLines: Array<PatternEntry?> =
            kotlin.arrayOfNulls<PatternEntry>(32)

        init {
            var i = 0
            for (p1 in 0..8) {
                for (p2 in 0..8) {
                    if (p2 > p1 && p1 + p2 != 8) {
                        possibleLines[i] =
                            PatternEntry(p1.toByte(), p2.toByte())
                        i++
                    }
                }
            }
        }

        fun from(pattern: List<Byte?>): Pattern {
            val list = ArrayList<PatternEntry>()
            var last: Byte? = null
            for (current in pattern) {
                if (last != null) {
                    if (last < current!!) {
                        list.add(PatternEntry(last, current))
                    } else {
                        list.add(PatternEntry(current, last))
                    }
                }
                last = current
            }
            list.sortWith(compareBy({ it.p1 }, { it.p2 }))
            return Pattern(list)
        }

        fun from(pattern: Int): Pattern {
            val builder: MutableList<PatternEntry> = ArrayList();
            for (i in 0..31) {
                if ((pattern shr i and 0x1) == 1) {
                    builder.add(possibleLines[i]!!)
                }
            }
            return Pattern(builder)
        }

        fun of(vararg pattern: Int): Pattern {
            val result: Pattern = from(pattern.map(Int::toByte).toList())

            for (line in result.entries) {
                var b = false

                for (line2 in possibleLines) {
                    if (line == line2) {
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
