package dev.enjarai.trickster.spell.fragment

import dev.enjarai.trickster.spell.Fragment
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder
import io.wispforest.owo.serialization.endec.MinecraftEndecs
import me.maplesyrum.tricksterstudio.spell.fragment.FragmentType
import net.minecraft.entity.EntityType
import net.minecraft.registry.Registries
import net.minecraft.text.Text

class EntityTypeFragment(entityType: EntityType<*>?) : Fragment {
    @Override
    fun type(): FragmentType<*> {
        return FragmentType.ENTITY_TYPE
    }

    @Override
    fun asText(): Text {
        return entityType.getName()
    }

    @get:Override
    val weight: Int
        get() = 16
    val entityType: EntityType<*>?

    init {
        this.entityType = entityType
    }

    companion object {
        val ENDEC: StructEndec<dev.enjarai.trickster.spell.fragment.EntityTypeFragment?>? = StructEndecBuilder.of(
            MinecraftEndecs.ofRegistry(Registries.ENTITY_TYPE)
                .fieldOf("entity_type", dev.enjarai.trickster.spell.fragment.EntityTypeFragment::entityType),
            { entityType: EntityType<*>? -> EntityTypeFragment(entityType) }
        )
    }
}
