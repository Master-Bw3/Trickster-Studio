package dev.enjarai.trickster.spell.fragment

import dev.enjarai.trickster.spell.Fragment
import me.maplesyrum.tricksterstudio.spell.fragment.FragmentType
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder
import net.minecraft.text.Text

@kotlin.jvm.JvmRecord
data class StringFragment(val value: String?) : Fragment {
    @Override
    fun type(): FragmentType<*> {
        return FragmentType.STRING
    }

    @Override
    fun asText(): Text {
        return Text.literal("\"").append(value).append("\"")
    }

    @get:Override
    val weight: Int
        get() = value.length() * 2

    companion object {
        val ENDEC: StructEndec<dev.enjarai.trickster.spell.fragment.StringFragment?>? = StructEndecBuilder.of(
            StructEndec.STRING.fieldOf("value", dev.enjarai.trickster.spell.fragment.StringFragment::value),
            { value: String? -> StringFragment(value) }
        )
    }
}
