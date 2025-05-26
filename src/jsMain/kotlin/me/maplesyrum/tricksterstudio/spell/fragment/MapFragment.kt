package dev.enjarai.trickster.spell.fragment

import dev.enjarai.trickster.spell.execution.executor.FoldingSpellExecutor
import io.vavr.Tuple2
import me.maplesyrum.tricksterstudio.spell.fragment.FragmentType
import tree.maple.kendec.Endec
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import java.util.Stack

class MapFragment(map: HashMap<Fragment?, Fragment?>?) : FoldableFragment {
    @Override
    fun type(): FragmentType<*> {
        return FragmentType.MAP
    }

    @Override
    fun asText(): Text {
        val out: MutableText = Text.empty()
        out.append("{")
        val iterator: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = map.iterator()

        iterator.forEachRemaining({ entry ->
            out.append(entry._1().asFormattedText()).append(": ").append(entry._2().asFormattedText())
            if (iterator.hasNext()) out.append(", ")
        })

        out.append("}")

        return out
    }

    @get:Override
    val weight: Int
        get() {
            var weight = 16

            for (kv in map) {
                weight += kv._1().getWeight()
                weight += kv._2().getWeight()
            }

            return weight
        }

    @Override
    fun applyEphemeral(): dev.enjarai.trickster.spell.fragment.MapFragment {
        return dev.enjarai.trickster.spell.fragment.MapFragment(map.map({ key, value ->
            Tuple2(
                key.applyEphemeral(),
                value.applyEphemeral()
            )
        }))
    }

    val macroMap: HashMap<Pattern, SpellPart>
        get() = map.filter({ key, value -> key is PatternGlyph && value is SpellPart })
            .mapKeys({ k -> (k as PatternGlyph).pattern() })
            .mapValues(SpellPart::class.java::cast)

    @Override
    fun fold(ctx: SpellContext?, executable: SpellPart?, identity: Fragment?): FoldingSpellExecutor? {
        val keys: Stack<Fragment?> = Stack<Fragment?>()
        val values: Stack<Fragment?> = Stack<Fragment?>()

        for (kv in map) {
            keys.addFirst(kv._1())
            values.addFirst(kv._2())
        }

        return FoldingSpellExecutor(ctx, executable, identity, values, keys, this)
    }

    fun mergeWith(other: dev.enjarai.trickster.spell.fragment.MapFragment): dev.enjarai.trickster.spell.fragment.MapFragment {
        return dev.enjarai.trickster.spell.fragment.MapFragment(map.merge(other.map))
    }

    val map: HashMap<Fragment?, Fragment?>?

    init {
        this.map = map
    }

    companion object {
        val ENDEC: StructEndec<dev.enjarai.trickster.spell.fragment.MapFragment?>? = StructEndecBuilder.of(
            Endec.map(Fragment.ENDEC, Fragment.ENDEC).xmap(HashMap::ofAll, HashMap::toJavaMap)
                .fieldOf("entries", dev.enjarai.trickster.spell.fragment.MapFragment::map),
            { map: HashMap<Fragment?, Fragment?>? -> MapFragment(map) }
        )
    }
}
