package me.maplesyrum.tricksterstudio.spell.fragment

import io.kvision.utils.obj
import me.maplesyrum.tricksterstudio.external.pixi.HTMLText
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder
import tree.maple.kendec.mapEndecOf


class MapFragment(val map: Map<Fragment, Fragment>) : Fragment() {
    override fun type(): FragmentType<*> {
        return FragmentType.MAP
    }

    override fun asFormattedText(): HTMLText {
        val out = buildString {
            append("{")
            val iterator = map.entries.iterator()
            while (iterator.hasNext()) {
                val (key, value) = iterator.next()
                append(key.asFormattedText().text)
                append(": ")
                append(value.asFormattedText().text)
                if (iterator.hasNext()) {
                    append(", ")
                }
            }
            append("}")
        }
        return HTMLText(
           obj {
               text = "<span style=\"color: #" + type().color.map{ it.toInt() }.orElseGet { 0xaaaaaa }.toString(16) + "\">$out</span>"
           }
        )
    }

    override fun toString(): String {
        return buildString {
            append("{")
            val iterator = map.entries.iterator()
            while (iterator.hasNext()) {
                val (key, value) = iterator.next()
                append(key.toString())
                append(": ")
                append(value.toString())
                if (iterator.hasNext()) {
                    append(", ")
                }
            }
            append("}")
        }
    }

    companion object {
        val ENDEC: StructEndec<MapFragment> = StructEndecBuilder.of(
            mapEndecOf(Fragment.ENDEC, Fragment.ENDEC)
                .fieldOf("entries", MapFragment::map)
        ) { map: Map<Fragment, Fragment> -> MapFragment(map) }
    }
}
