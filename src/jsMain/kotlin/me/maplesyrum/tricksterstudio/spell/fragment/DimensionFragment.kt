package me.maplesyrum.tricksterstudio.spell.fragment

import me.maplesyrum.tricksterstudio.Identifier
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder


class DimensionFragment(val world: String) : Fragment() {

    override fun type(): FragmentType<*> {
        return FragmentType.DIMENSION
    }

    override fun toString(): String {
        return world.replace(Regex("^[^a-zA-Z0-9]+"), "")
    }

    companion object {
        var ENDEC: StructEndec<DimensionFragment> = StructEndecBuilder.of(
            Identifier.ENDEC
                .xmap({ id -> id.toString() }, { str -> Identifier.of(str) })
                .fieldOf("block", DimensionFragment::world)
        ) { str -> DimensionFragment(str) }
    }
}
