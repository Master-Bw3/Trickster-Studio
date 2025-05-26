package dev.enjarai.trickster.spell.fragment

import dev.enjarai.trickster.EndecTomfoolery
import dev.enjarai.trickster.spell.Fragment
import me.maplesyrum.tricksterstudio.spell.fragment.FragmentType
import tree.maple.kendec.StructEndec
import net.minecraft.text.Text

class VoidFragment : Fragment {
    @Override
    fun type(): FragmentType<*> {
        return FragmentType.VOID
    }

    @Override
    fun asText(): Text {
        return Text.literal("void")
    }

    @Override
    fun asBoolean(): kotlin.Boolean {
        return false
    }

    @get:Override
    val weight: Int
        get() = 1

    companion object {
        val INSTANCE: dev.enjarai.trickster.spell.fragment.VoidFragment =
            dev.enjarai.trickster.spell.fragment.VoidFragment()
        val ENDEC: StructEndec<dev.enjarai.trickster.spell.fragment.VoidFragment?>? =
            EndecTomfoolery.unit(dev.enjarai.trickster.spell.fragment.VoidFragment.Companion.INSTANCE)
    }
}
