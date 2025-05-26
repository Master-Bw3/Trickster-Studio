package dev.enjarai.trickster.spell.fragment

import com.google.common.collect.ImmutableList
import dev.enjarai.trickster.spell.Fragment
import dev.enjarai.trickster.spell.SpellContext
import dev.enjarai.trickster.spell.SpellPart
import dev.enjarai.trickster.spell.blunder.BlunderException
import dev.enjarai.trickster.spell.execution.executor.FoldingSpellExecutor
import me.maplesyrum.tricksterstudio.spell.fragment.FragmentType
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder
import net.minecraft.text.Text
import java.util.Stack

class ListFragment(fragments: List<Fragment?>?) : FoldableFragment {
    @Override
    fun type(): FragmentType<*> {
        return FragmentType.LIST
    }

    @Override
    fun asText(): Text {
        var result: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = Text.literal("[")

        for (i in 0..<this.fragments.size()) {
            val frag: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
                this.fragments!!.get(i)
            if (i != 0) {
                result = result.append(", ")
            }
            result = result.append(frag.asFormattedText())
        }

        return result.append("]")
    }

    @Override
    fun applyEphemeral(): Fragment? {
        return dev.enjarai.trickster.spell.fragment.ListFragment(
            fragments.stream().map(Fragment::applyEphemeral).toList()
        )
    }

    @get:Override
    val weight: Int
        get() {
            var weight = 16

            for (fragment in fragments!!) {
                weight += fragment.getWeight()
            }

            return weight
        }

    @Throws(BlunderException::class)
    fun addRange(other: dev.enjarai.trickster.spell.fragment.ListFragment): dev.enjarai.trickster.spell.fragment.ListFragment {
        return dev.enjarai.trickster.spell.fragment.ListFragment(
            ImmutableList.< Fragment > builder < Fragment ? > ().addAll(
                fragments
            ).addAll(other.fragments).build()
        )
    }

    @Override
    fun fold(ctx: SpellContext?, executable: SpellPart?, identity: Fragment?): FoldingSpellExecutor? {
        val keys: Stack<Fragment?> = Stack<Fragment?>()
        val values: Stack<Fragment?> = Stack<Fragment?>()

        for (i in fragments.size() - 1 downTo 0) keys.push(NumberFragment(i))

        values.addAll(fragments.reversed())
        return FoldingSpellExecutor(ctx, executable, identity, values, keys, this)
    }

    val fragments: List<Fragment?>?

    init {
        this.fragments = fragments
    }

    companion object {
        val ENDEC: StructEndec<dev.enjarai.trickster.spell.fragment.ListFragment?>? = StructEndecBuilder.of(
            Fragment.ENDEC.listOf().fieldOf("fragments", dev.enjarai.trickster.spell.fragment.ListFragment::fragments),
            { fragments: List<Fragment?>? -> ListFragment(fragments) }
        )
    }
}
