package dev.enjarai.trickster.spell.fragment

import dev.enjarai.trickster.EndecTomfoolery
import dev.enjarai.trickster.mixin.accessor.WorldAccessor
import dev.enjarai.trickster.spell.Fragment
import dev.enjarai.trickster.spell.SpellContext
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder
import io.wispforest.owo.serialization.CodecUtils
import me.maplesyrum.tricksterstudio.spell.fragment.FragmentType
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.text.TextCodecs
import net.minecraft.world.World
import java.util.Optional
import java.util.UUID
import kotlin.collections.filter
import kotlin.js.get
import kotlin.sequences.filter
import kotlin.text.filter
import kotlin.text.get
import kotlin.text.toLong

class EntityFragment(uuid: UUID?, name: Text?) : Fragment {
    @Override
    fun type(): FragmentType<*> {
        return FragmentType.ENTITY
    }

    @Override
    fun asText(): Text? {
        return name
    }

    fun getEntity(ctx: SpellContext): Optional<Entity?> {
        return Optional
            .ofNullable(ctx.source().getWorld().getEntity(uuid))
            .filter({ entity: Entity ->
                dev.enjarai.trickster.spell.fragment.EntityFragment.Companion.isValidEntity(
                    entity
                )
            })
    }

    fun getEntity(world: World, client: kotlin.Boolean): Optional<Entity?> {
        return Optional
            .ofNullable((world as WorldAccessor).callGetEntityLookup().get(uuid))
            .filter({ e -> client || dev.enjarai.trickster.spell.fragment.EntityFragment.Companion.isValidEntity(e) })
    }

    @Override
    fun applyEphemeral(): Fragment? {
        return ZalgoFragment()
    }

    @get:Override
    val weight: Int
        get() = 32

    val uuid: UUID?
    val name: Text?

    init {
        this.uuid = uuid
        this.name = name
    }

    companion object {
        val ENDEC: StructEndec<dev.enjarai.trickster.spell.fragment.EntityFragment?>? = StructEndecBuilder.of(
            EndecTomfoolery.UUID.fieldOf("uuid", dev.enjarai.trickster.spell.fragment.EntityFragment::uuid),
            CodecUtils.toEndec(TextCodecs.STRINGIFIED_CODEC)
                .fieldOf("name", dev.enjarai.trickster.spell.fragment.EntityFragment::name),
            { uuid: UUID?, name: Text? -> EntityFragment(uuid, name) }
        )

        fun from(entity: Entity): dev.enjarai.trickster.spell.fragment.EntityFragment {
            if (entity is PlayerEntity) {
                return dev.enjarai.trickster.spell.fragment.EntityFragment(entity.getUuid(), entity.getName())
            }

            val name: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
                if (entity.hasCustomName()) entity.getCustomName() else Text.translatable(
                    "trickster.unnamed_entity",
                    entity.getName()
                )
            return dev.enjarai.trickster.spell.fragment.EntityFragment(entity.getUuid(), name)
        }

        fun isValidEntity(entity: Entity): kotlin.Boolean {
            if (entity.getWorld() is ServerWorld) {
                return serverWorld.getChunkManager().chunkLoadingManager.getTicketManager()
                    .shouldTickEntities(entity.getChunkPos().toLong())
            }
            return false
        }
    }
}
