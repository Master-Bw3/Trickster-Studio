package dev.enjarai.trickster.spell.fragment

import dev.enjarai.trickster.spell.Fragment
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder
import io.wispforest.owo.serialization.CodecUtils
import me.maplesyrum.tricksterstudio.spell.fragment.FragmentType
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.text.Text
import net.minecraft.world.World
import org.apache.commons.lang3.text.WordUtils

class DimensionFragment(world: RegistryKey<World?>?) : Fragment {
    @Override
    fun type(): FragmentType<*> {
        return FragmentType.DIMENSION
    }

    @SuppressWarnings("deprecation")
    @Override
    fun asText(): Text {
        return Text.literal(WordUtils.capitalize(world.getValue().getPath().replace('_', ' ')))
    }

    @get:Override
    val weight: Int
        get() = 16

    val world: RegistryKey<World?>?

    init {
        this.world = world
    }

    companion object {
        var ENDEC: StructEndec<dev.enjarai.trickster.spell.fragment.DimensionFragment?>? = StructEndecBuilder.of(
            CodecUtils.toEndec(RegistryKey.createCodec(RegistryKeys.WORLD))
                .fieldOf("world", dev.enjarai.trickster.spell.fragment.DimensionFragment::world),
            { world: RegistryKey<World?>? -> DimensionFragment(world) }
        )

        fun of(world: World): dev.enjarai.trickster.spell.fragment.DimensionFragment {
            return dev.enjarai.trickster.spell.fragment.DimensionFragment(world.getRegistryKey())
        }
    }
}
