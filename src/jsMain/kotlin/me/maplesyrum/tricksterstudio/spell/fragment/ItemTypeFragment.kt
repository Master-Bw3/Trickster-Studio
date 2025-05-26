package dev.enjarai.trickster.spell.fragment

import dev.enjarai.trickster.spell.Fragment
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder
import io.wispforest.owo.serialization.endec.MinecraftEndecs
import me.maplesyrum.tricksterstudio.spell.fragment.FragmentType
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.text.Text

class ItemTypeFragment(item: Item?) : Fragment {
    @Override
    fun type(): FragmentType<*> {
        return FragmentType.ITEM_TYPE
    }

    @Override
    fun asText(): Text {
        return item.getName()
    }

    @get:Override
    val weight: Int
        get() = 16
    val item: Item?

    init {
        this.item = item
    }

    companion object {
        val ENDEC: StructEndec<dev.enjarai.trickster.spell.fragment.ItemTypeFragment?>? = StructEndecBuilder.of(
            MinecraftEndecs.ofRegistry(Registries.ITEM)
                .fieldOf("item", dev.enjarai.trickster.spell.fragment.ItemTypeFragment::item),
            { item: Item? -> ItemTypeFragment(item) }
        )
    }
}
