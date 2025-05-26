package me.maplesyrum.tricksterstudio.spell.fragment

import Identifier
import tree.maple.kendec.impl.StructEndecBuilder


class BlockTypeFragment(
    val blockType: Identifier
) : Fragment() {

    override fun toString(): String = blockType.toString()

    override fun type(): FragmentType<BlockTypeFragment> = FragmentType.BLOCK_TYPE

    companion object {
        val BLOCK_TYPE = StructEndecBuilder.of(
            Identifier.ENDEC.fieldOf("block", BlockTypeFragment::blockType),
            ::BlockTypeFragment
        )
    }
}