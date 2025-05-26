package dev.enjarai.trickster.spell.fragment

import dev.enjarai.trickster.EndecTomfoolery
import dev.enjarai.trickster.spell.Fragment
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder
import io.wispforest.owo.serialization.endec.MinecraftEndecs
import me.maplesyrum.tricksterstudio.spell.fragment.Fragment
import me.maplesyrum.tricksterstudio.spell.fragment.FragmentType
import net.minecraft.text.Text

class TypeFragment(typeType: FragmentType<*>?) : Fragment {
    @Override
    fun type(): FragmentType<*> {
        return FragmentType.TYPE
    }

    @Override
    fun asText(): Text {
        return typeType.getName()
    }

    @get:Override
    val weight: Int
        get() = 16
    val typeType: FragmentType<*>?

    init {
        this.typeType = typeType
    }

    companion object {
        val ENDEC: StructEndec<TypeFragment> = EndecTomfoolery.lazy(
            {
                StructEndecBuilder.of(
                    MinecraftEndecs.ofRegistry(FragmentType.REGISTRY)
                        .fieldOf("of_type", dev.enjarai.trickster.spell.fragment.TypeFragment::typeType),
                    { typeType: FragmentType<*>? -> TypeFragment(typeType) }
                )
            }
        )
    }
}
