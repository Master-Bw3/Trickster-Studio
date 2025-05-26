package me.maplesyrum.tricksterstudio.spell.fragment

import Identifier
import me.maplesyrum.tricksterstudio.external.pixi.HTMLText
import tree.maple.kendec.impl.StructEndecBuilder


class BlockTypeFragment(
    val blockType: Identifier
) : Fragment() {
    override fun type(): FragmentType<*> = FragmentType.BLOCK_TYPE

    companion object {
        val ENDEC = StructEndecBuilder.of(
            Identifier.ENDEC.fieldOf("block", BlockTypeFragment::blockType),
            ::BlockTypeFragment
        )
    }

    override fun toString(): String = blockType.toString()
}