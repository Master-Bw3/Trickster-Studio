package me.maplesyrum.tricksterstudio.spell.fragment

import tree.maple.kendec.PrimitiveEndecs
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder

data class StringFragment(val value: String) : Fragment() {
    override fun type(): FragmentType<*> {
        return FragmentType.STRING
    }

    override fun toString(): String {
        return value
    }

    companion object {
        val ENDEC: StructEndec<StringFragment> = StructEndecBuilder.of(
            PrimitiveEndecs.STRING.fieldOf("value", StringFragment::value),
            { value: String -> StringFragment(value) }
        )
    }
}
