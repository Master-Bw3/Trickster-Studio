package me.maplesyrum.tricksterstudio.spell.fragment

import tree.maple.kendec.PrimitiveEndecs
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder

class BooleanFragment private constructor(val bool: Boolean) : Fragment() {

    override fun type(): FragmentType<*> {
        return FragmentType.BOOLEAN
    }

    override fun toString(): String {
        return bool.toString()
    }

    companion object {
        val ENDEC: StructEndec<BooleanFragment> = StructEndecBuilder.of(
            PrimitiveEndecs.BOOLEAN.fieldOf("bool") { obj: BooleanFragment -> obj.bool }
        ) { bool: Boolean -> of(bool) }

        val TRUE: BooleanFragment =
            BooleanFragment(true)
        val FALSE: BooleanFragment =
            BooleanFragment(false)

        fun of(bool: Boolean): BooleanFragment {
            return if (bool) TRUE else FALSE
        }
    }
}
