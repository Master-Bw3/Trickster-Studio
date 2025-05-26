package me.maplesyrum.tricksterstudio.spell.fragment

import me.maplesyrum.tricksterstudio.endec.unit
import tree.maple.kendec.StructEndec

class VoidFragment : Fragment() {
    override fun type(): FragmentType<*> {
        return FragmentType.VOID
    }

    override fun toString(): String {
        return "Void";
    }

    companion object {
        val INSTANCE: VoidFragment = VoidFragment()

        val ENDEC: StructEndec<VoidFragment> = unit(INSTANCE)
    }
}
