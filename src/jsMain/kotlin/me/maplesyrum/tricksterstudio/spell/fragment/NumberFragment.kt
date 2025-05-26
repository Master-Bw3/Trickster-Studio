package me.maplesyrum.tricksterstudio.spell.fragment

import tree.maple.kendec.PrimitiveEndecs
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder


class NumberFragment(val number: Double) : Fragment() {

    override fun type(): FragmentType<out Fragment> {
        return FragmentType.NUMBER
    }

    override fun toString(): String {
        return number.asDynamic().toFixed(2) as String
    }

    companion object {
        val ENDEC: StructEndec<NumberFragment> = StructEndecBuilder.of(
            PrimitiveEndecs.DOUBLE.fieldOf(
                "number",
                { obj: NumberFragment? -> obj!!.number }),
            { number: Double -> NumberFragment(number) }
        )
    }
}
