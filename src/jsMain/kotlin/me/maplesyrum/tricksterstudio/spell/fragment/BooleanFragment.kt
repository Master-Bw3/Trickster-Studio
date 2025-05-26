package dev.enjarai.trickster.spell.fragment

import dev.enjarai.trickster.spell.Fragment
import me.maplesyrum.tricksterstudio.spell.fragment.FragmentType
import tree.maple.kendec.Endec
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder
import net.minecraft.text.Text

class BooleanFragment private constructor(val bool: kotlin.Boolean) : Fragment {
    @Override
    fun type(): FragmentType<*> {
        return FragmentType.BOOLEAN
    }

    @Override
    fun asText(): Text {
        return Text.literal("" + bool)
    }

    @Override
    fun asBoolean(): kotlin.Boolean {
        return bool
    }

    @Override
    fun hashCode(): kotlin.Int {
        return if (bool) 1 else 0
    }

    @Override
    fun equals(obj: Object?): kotlin.Boolean {
        return obj is dev.enjarai.trickster.spell.fragment.BooleanFragment && obj.bool == bool
    }

    @Override
    fun toString(): String? {
        return "BooleanFragment[bool=" + bool + ']'
    }

    @get:Override
    val weight: Int
        get() = 1

    companion object {
        val ENDEC: StructEndec<dev.enjarai.trickster.spell.fragment.BooleanFragment?>? = StructEndecBuilder.of(
            Endec.BOOLEAN.fieldOf(
                "bool",
                { obj: dev.enjarai.trickster.spell.fragment.BooleanFragment? -> obj!!.asBoolean() }),
            { bool: kotlin.Boolean -> dev.enjarai.trickster.spell.fragment.BooleanFragment.Companion.of(bool) }
        )
        val TRUE: dev.enjarai.trickster.spell.fragment.BooleanFragment =
            dev.enjarai.trickster.spell.fragment.BooleanFragment(true)
        val FALSE: dev.enjarai.trickster.spell.fragment.BooleanFragment =
            dev.enjarai.trickster.spell.fragment.BooleanFragment(false)

        fun of(bool: kotlin.Boolean): dev.enjarai.trickster.spell.fragment.BooleanFragment? {
            return if (bool) dev.enjarai.trickster.spell.fragment.BooleanFragment.Companion.TRUE else dev.enjarai.trickster.spell.fragment.BooleanFragment.Companion.FALSE
        }
    }
}
