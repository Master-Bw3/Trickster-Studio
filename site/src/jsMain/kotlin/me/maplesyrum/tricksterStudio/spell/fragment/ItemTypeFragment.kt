package me.maplesyrum.tricksterStudio.spell.fragment

import me.maplesyrum.tricksterStudio.Identifier
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder


class ItemTypeFragment(val item: Identifier) : Fragment() {
    override fun type(): FragmentType<*> {
        return FragmentType.ITEM_TYPE
    }

    override fun toString(): String {
        return item.toString()
    }

    companion object {
        val ENDEC: StructEndec<ItemTypeFragment> = StructEndecBuilder.of(
            Identifier.ENDEC
            .fieldOf("entityType") { fragment: ItemTypeFragment -> fragment.item }
        ) { item -> ItemTypeFragment(item) }
    }
}
