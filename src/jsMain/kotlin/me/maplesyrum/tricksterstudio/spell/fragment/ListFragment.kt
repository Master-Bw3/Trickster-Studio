package me.maplesyrum.tricksterstudio.spell.fragment


import io.kvision.utils.obj
import me.maplesyrum.tricksterstudio.external.pixi.HTMLText
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder


class ListFragment(val fragments: List<Fragment>) : Fragment() {
    override fun type(): FragmentType<*> {
        return FragmentType.LIST
    }

    override fun asFormattedText(): HTMLText {
        val result = buildString {
            append("[")
            for ((i, fragment) in fragments.withIndex()) {
                if (i != 0) append(", ")
                append(fragment.asFormattedText().text)
            }
            append("]")
        }
        return HTMLText(
           obj {
               text = "<span style=\"color: #${type().color.map(::toInt).orElse(0xaaaaaa).toString(16)}\">$result</span>"
           }
        )
    }

    override fun toString(): String {
        return buildString {
            append("[")
            for ((i, fragment) in fragments.withIndex()) {
                if (i != 0) append(", ")
                append(fragment.asFormattedText().text)
            }
            append("]")
        }
    }

    companion object {
        val ENDEC: StructEndec<ListFragment> = StructEndecBuilder.of(
            Fragment.ENDEC.listOf().fieldOf("fragments", ListFragment::fragments)
        ) { fragments: List<Fragment> -> ListFragment(fragments) }
    }
}
