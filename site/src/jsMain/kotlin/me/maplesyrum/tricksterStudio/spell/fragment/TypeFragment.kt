package me.maplesyrum.tricksterStudio.spell.fragment

import me.maplesyrum.tricksterStudio.Identifier
import me.maplesyrum.tricksterStudio.endec.lazyEndec
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder

class TypeFragment(val typeType: FragmentType<*>) : Fragment() {
    override fun type(): FragmentType<*> {
        return FragmentType.TYPE
    }

    override fun toString(): String {
        return typeType.getId()!!.toString()
    }

    companion object {
        val ENDEC: StructEndec<TypeFragment> = lazyEndec {
            StructEndecBuilder.of(
                Identifier.ENDEC.xmap({ id -> FragmentType.REGISTRY[id]!! }, { type -> type.getId()!! })
                    .fieldOf("of_type") { fragment -> fragment.typeType }
            ) { type -> TypeFragment(type) }
        }
    }
}
