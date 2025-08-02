package me.maplesyrum.tricksterStudio.spell.fragment

import me.maplesyrum.tricksterStudio.Either
import me.maplesyrum.tricksterStudio.EitherEndec
import me.maplesyrum.tricksterStudio.Identifier
import com.benasher44.uuid.Uuid
import me.maplesyrum.tricksterStudio.endec.ALWAYS_READABLE_BLOCK_POS
import me.maplesyrum.tricksterStudio.endec.UUID
import me.maplesyrum.tricksterStudio.endec.Vector
import me.maplesyrum.tricksterStudio.endec.VectorI
import me.maplesyrum.tricksterStudio.endec.safeOptionalOf
import me.maplesyrum.tricksterStudio.external.pixi.HTMLText
import me.maplesyrum.tricksterStudio.jsObject
import tree.maple.kendec.PrimitiveEndecs
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder
import tree.maple.kendec.util.Optional


class SlotFragment(val slot: Int, val source: Optional<Either<VectorI, Uuid>>) : Fragment() {
    override fun type(): FragmentType<*> {
        return FragmentType.SLOT
    }

    override fun asFormattedText(): HTMLText {
        val entityColor = FragmentType.REGISTRY[Identifier("trickster", "entity")]!!.color.get()
        val numColor = FragmentType.REGISTRY[Identifier("trickster", "number")]!!.color.get()
        fun coloredInt(value: Int) = "<span style=\"color: #${numColor.toString(16)}\">$value</span>"

        val sourceText = when {
            source.isEmpty() -> "<span style=\"color: #${entityColor.toString(16)}\">Caster</span>"
            source.get().left().isPresent() -> {
                val src = source.get().left().get()
                "(${coloredInt(src.x)}, ${coloredInt(src.y)}, ${coloredInt(src.z)})"
            }

            source.get().right().isPresent() -> {
                val src = source.get().right().get()
                "<span style=\"color: #${entityColor.toInt().toString(16)}\">$src</span>"
            }

            else -> ""
        }

        val slotText = coloredInt(slot)

        return HTMLText(
            jsObject {
                text = "<span style=\"color: #${type().color.map{ it.toInt() }.orElseGet { 0xaaaaaa }.toString(16)}\">slot $slotText at $sourceText</span>"
            }
        )
    }

    override fun toString(): String {
        var sourceText = "Caster"
        if (source.isPresent()) {
            if (source.get().right().isPresent()) {
                sourceText = source.get().right().get().toString()
            } else if (source.get().left().isPresent()) {
                val src = source.get().left().get()
                sourceText = "(${src.x}, ${src.y}, ${src.z})"
            }
        }
        return "slot $slot at $sourceText"
    }

    companion object {
        val ENDEC: StructEndec<SlotFragment> = StructEndecBuilder.of(
            PrimitiveEndecs.INT.fieldOf("slot", SlotFragment::slot),
            safeOptionalOf(
                EitherEndec(
                    ALWAYS_READABLE_BLOCK_POS,
                    UUID,
                    true
                )
            ).fieldOf("source", SlotFragment::source),
            { slot: Int, source: Optional<Either<VectorI, Uuid>> -> SlotFragment(slot, source) }
        )
    }
}
