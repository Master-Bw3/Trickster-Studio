package dev.enjarai.trickster.spell.fragment

import com.mojang.datafixers.util.Either
import dev.enjarai.trickster.EndecTomfoolery
import dev.enjarai.trickster.item.component.FragmentComponent
import dev.enjarai.trickster.pond.SlotHolderDuck
import dev.enjarai.trickster.spell.Fragment
import dev.enjarai.trickster.spell.SpellContext
import dev.enjarai.trickster.spell.blunder.*
import dev.enjarai.trickster.spell.trick.Trick
import tree.maple.kendec.Endec
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder
import io.wispforest.owo.serialization.endec.EitherEndec
import me.maplesyrum.tricksterstudio.spell.fragment.FragmentType
import net.minecraft.entity.Entity
import net.minecraft.inventory.Inventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import java.util.Optional
import java.util.UUID
import kotlin.Exception
import kotlin.Int
import kotlin.Throws
import kotlin.Unit
import kotlin.UnsupportedOperationException
import kotlin.collections.get
import kotlin.collections.map
import kotlin.compareTo
import kotlin.js.get
import kotlin.js.reset
import kotlin.map
import kotlin.sequences.map
import kotlin.text.get
import kotlin.text.map
import kotlin.toString

class SlotFragment(val slot: kotlin.Int, source: Optional<Either<BlockPos?, UUID?>?>?) : Fragment {
    @Override
    fun type(): FragmentType<*> {
        return FragmentType.SLOT
    }

    @Override
    fun asText(): Text {
        return Text.literal(
            "slot %d at %s".formatted(
                slot,
                source.map({ either ->
                    val mapped: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = either
                        .mapLeft({ blockPos ->
                            "(%d, %d, %d)".formatted(
                                blockPos.getX(),
                                blockPos.getY(),
                                blockPos.getZ()
                            )
                        })
                        .mapRight({ uuid -> uuid.toString() })
                    mapped.right().orElseGet({ mapped.left().get() })
                }).orElse("caster")
            )
        )
    }

    @get:Override
    val weight: Int
        get() = 64

    fun setStack(itemStack: ItemStack?, trick: Trick<*>?, ctx: SpellContext) {
        val inventory: SlotHolderDuck = getInventory(trick, ctx)
        inventory.`trickster$slot_holder$setStack`(slot, itemStack)
    }

    @Throws(BlunderException::class)
    fun writeFragment(
        fragment: Fragment?,
        closed: kotlin.Boolean,
        name: Optional<Text?>?,
        player: Optional<ServerPlayerEntity?>?,
        trick: Trick<*>?,
        ctx: SpellContext
    ) {
        val inventory: SlotHolderDuck = getInventory(trick, ctx)
        val stack: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
            inventory.`trickster$slot_holder$getStack`(slot)
        val updated: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
            FragmentComponent.write(stack, fragment, closed, player, name)

        inventory.`trickster$slot_holder$setStack`(slot, updated.orElseThrow({ ImmutableItemBlunder(trick) }))
    }

    @Throws(BlunderException::class)
    fun resetFragment(trick: Trick<*>?, ctx: SpellContext) {
        val inventory: SlotHolderDuck = getInventory(trick, ctx)
        val stack: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
            inventory.`trickster$slot_holder$getStack`(slot)
        val updated: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
            FragmentComponent.reset(stack)

        inventory.`trickster$slot_holder$setStack`(slot, updated.orElseThrow({ ImmutableItemBlunder(trick) }))
    }

    @Throws(BlunderException::class)
    fun swapWith(trickSource: Trick<*>?, ctx: SpellContext, other: dev.enjarai.trickster.spell.fragment.SlotFragment) {
        val otherInv: SlotHolderDuck = other.getInventory(trickSource, ctx)
        val inv: SlotHolderDuck = getInventory(trickSource, ctx)

        if (equals(other)) {
            val stack: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
                inv.`trickster$slot_holder$takeFromSlot`(slot, getStack(trickSource, ctx).getCount())
            inv.`trickster$slot_holder$setStack`(slot, stack)
        } else {
            val otherStack: ItemStack = other.getStack(trickSource, ctx)
            val stack: ItemStack = getStack(trickSource, ctx)

            val movedOtherStack: ItemStack =
                other.move(trickSource, ctx, otherStack.getCount(), getSourcePos(trickSource, ctx))
            val movedStack: ItemStack?

            try {
                movedStack = move(trickSource, ctx, stack.getCount(), other.getSourcePos(trickSource, ctx))
            } catch (e: Exception) {
                ctx.source().offerOrDropItem(movedOtherStack)
                throw e
            }

            try {
                if (!inv.`trickster$slot_holder$setStack`(slot, movedOtherStack)) throw ItemInvalidBlunder(trickSource)
            } catch (e: Exception) {
                ctx.source().offerOrDropItem(movedOtherStack)
                ctx.source().offerOrDropItem(movedStack)
                throw e
            }

            try {
                if (!otherInv.`trickster$slot_holder$setStack`(other.slot, movedStack)) throw ItemInvalidBlunder(
                    trickSource
                )
            } catch (e: UnsupportedOperationException) {
                throw ItemInvalidBlunder(trickSource)
            } catch (e: Exception) {
                ctx.source().offerOrDropItem(movedStack)
                throw e
            }
        }
    }

    @Throws(BlunderException::class)
    fun move(trickSource: Trick<*>?, ctx: SpellContext): ItemStack {
        return move(trickSource, ctx, 1)
    }

    fun move(trickSource: Trick<*>?, ctx: SpellContext, amount: kotlin.Int): ItemStack {
        return move(trickSource, ctx, amount, ctx.source().getBlockPos())
    }

    @Throws(BlunderException::class)
    fun move(trickSource: Trick<*>?, ctx: SpellContext, amount: kotlin.Int, pos: BlockPos): ItemStack {
        val stack: ItemStack = getStack(trickSource, ctx)

        if (stack.getCount() < amount) throw MissingItemBlunder(trickSource)

        ctx.useMana(trickSource, getMoveCost(trickSource, ctx, pos, amount))
        return takeFromSlot(trickSource, ctx, amount)
    }

    /**
     * Instead of taking items from the slot, directly reference the stored stack to modify it. This may return an empty ItemStack if applicable.
     */
    fun reference(trickSource: Trick<*>?, ctx: SpellContext): ItemStack {
        return getStack(trickSource, ctx)
    }

    @Throws(BlunderException::class)
    fun getItem(trickSource: Trick<*>?, ctx: SpellContext): Item {
        return getStack(trickSource, ctx).getItem()
    }

    fun getSourcePos(trickSource: Trick<*>?, ctx: SpellContext): BlockPos {
        return source
            .map(
                { either ->
                    Either.unwrap(
                        either
                            .mapRight(
                                { uuid ->
                                    EntityFragment(uuid, Text.literal(""))
                                        .getEntity(ctx)
                                        .orElseThrow({ UnknownEntityBlunder(trickSource) })
                                        .getBlockPos()
                                }
                            )
                    )
                }
            )
            .orElseGet(
                {
                    ctx
                        .source()
                        .getPlayer()
                        .orElseThrow({ NoPlayerBlunder(trickSource) })
                        .getBlockPos()
                }
            )
    }

    @Throws(BlunderException::class)
    private fun getStack(trickSource: Trick<*>?, ctx: SpellContext): ItemStack {
        val inventory: SlotHolderDuck = getInventory(trickSource, ctx)

        if (slot < 0 || slot >= inventory.`trickster$slot_holder$size`()) throw NoSuchSlotBlunder(trickSource)

        return inventory.`trickster$slot_holder$getStack`(slot)
    }

    @Throws(BlunderException::class)
    private fun takeFromSlot(trickSource: Trick<*>?, ctx: SpellContext, amount: kotlin.Int): ItemStack {
        val inventory: SlotHolderDuck = getInventory(trickSource, ctx)

        if (slot < 0 || slot >= inventory.`trickster$slot_holder$size`()) throw NoSuchSlotBlunder(trickSource)

        return inventory.`trickster$slot_holder$takeFromSlot`(slot, amount)
    }

    @Throws(BlunderException::class)
    private fun getInventory(trickSource: Trick<*>?, ctx: SpellContext): SlotHolderDuck {
        return source.map({ s ->
            if (s.left().isPresent()) {
                val e: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
                    ctx.source().getWorld().getBlockEntity(s.left().get())
                if (e is SlotHolderDuck) return@map e
                else if (e is Inventory) return@map dev.enjarai.trickster.spell.fragment.SlotFragment.BridgedSlotHolder(
                    e
                )
                else throw BlockInvalidBlunder(trickSource)
            } else {
                val e: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
                    ctx.source().getWorld().getEntity(s.right().get())
                if (e is SlotHolderDuck) return@map e
                else if (e is Inventory) return@map dev.enjarai.trickster.spell.fragment.SlotFragment.BridgedSlotHolder(
                    e
                )
                else throw EntityInvalidBlunder(trickSource)
            }
        }).orElseGet(
            {
                ctx.source().getPlayer()
                    .map({ player -> dev.enjarai.trickster.spell.fragment.SlotFragment.BridgedSlotHolder(player.getInventory()) })
                    .orElseThrow({ NoPlayerBlunder(trickSource) })
            }
        )
    }

    @Throws(BlunderException::class)
    private fun getMoveCost(
        trickSource: Trick<*>?,
        ctx: SpellContext,
        pos: BlockPos,
        amount: kotlin.Int
    ): kotlin.Float {
        return source.map({ s ->
            if (s.left().isPresent()) {
                return@map s.left().get().toCenterPos()
            } else {
                if (ctx.source().getWorld().getEntity(s.right().get()) is Entity) return@map entity.getBlockPos()
                    .toCenterPos()
                else throw EntityInvalidBlunder(trickSource)
            }
        }).map({ blockPos -> 8 + (pos.toCenterPos().distanceTo(blockPos) * amount * 0.5) as kotlin.Float }).orElse(0f)
    }

    private inner class BridgedSlotHolder(inv: Inventory) : SlotHolderDuck {
        private val inv: Inventory

        init {
            this.inv = inv
        }

        @Override
        fun `trickster$slot_holder$size`(): kotlin.Int {
            return inv.size()
        }

        @Override
        fun `trickster$slot_holder$getStack`(slot: kotlin.Int): ItemStack {
            return inv.getStack(slot)
        }

        @Override
        fun `trickster$slot_holder$setStack`(slot: kotlin.Int, stack: ItemStack?): kotlin.Boolean {
            if (!inv.isValid(slot, stack)) return false

            inv.setStack(slot, stack)
            return true
        }

        @Override
        fun `trickster$slot_holder$takeFromSlot`(slot: kotlin.Int, amount: kotlin.Int): ItemStack? {
            val stack: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
                inv.getStack(slot)
            val result: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
                stack.copyWithCount(amount)
            stack.decrement(amount)
            return result
        }
    }

    val source: Optional<Either<BlockPos?, UUID?>?>?

    init {
        this.source = source
    }

    companion object {
        val ENDEC: StructEndec<dev.enjarai.trickster.spell.fragment.SlotFragment?>? = StructEndecBuilder.of(
            Endec.INT.fieldOf("slot", dev.enjarai.trickster.spell.fragment.SlotFragment::slot),
            EndecTomfoolery.safeOptionalOf(
                EitherEndec(
                    EndecTomfoolery.ALWAYS_READABLE_BLOCK_POS,
                    EndecTomfoolery.UUID,
                    true
                )
            ).fieldOf("source", dev.enjarai.trickster.spell.fragment.SlotFragment::source),
            { slot: kotlin.Int, source: Optional<Either<BlockPos?, UUID?>?>? -> SlotFragment(slot, source) }
        )
    }
}
