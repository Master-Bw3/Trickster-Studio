package dev.enjarai.trickster.spell.fragment

import dev.enjarai.trickster.EndecTomfoolery
import dev.enjarai.trickster.spell.Fragment
import me.maplesyrum.tricksterstudio.spell.fragment.FragmentType
import tree.maple.kendec.StructEndec
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.math.random.LocalRandom
import net.minecraft.util.math.random.Random

@kotlin.jvm.JvmRecord
data class ZalgoFragment(val index: kotlin.Int) : Fragment {
    constructor() : this(dev.enjarai.trickster.spell.fragment.ZalgoFragment.Companion.RANDOM.nextInt(dev.enjarai.trickster.spell.fragment.ZalgoFragment.Companion.SILLIES.size()))

    @Override
    fun type(): FragmentType<*> {
        return FragmentType.ZALGO
    }

    @Override
    fun asText(): Text {
        return Text.literal(dev.enjarai.trickster.spell.fragment.ZalgoFragment.Companion.SILLIES.get(index))
            .fillStyle(Style.EMPTY.withObfuscated(true))
    }

    @Override
    fun asBoolean(): kotlin.Boolean {
        return false
    }

    @get:Override
    val weight: Int
        get() = 4

    @Override
    fun hashCode(): kotlin.Int {
        return dev.enjarai.trickster.spell.fragment.ZalgoFragment.Companion.RANDOM.nextInt()
    }

    @Override
    fun equals(obj: Object?): kotlin.Boolean {
        return false
    }

    companion object {
        val ENDEC: StructEndec<dev.enjarai.trickster.spell.fragment.ZalgoFragment?>? =
            EndecTomfoolery.unit({ ZalgoFragment() })
        val RANDOM: Random = LocalRandom(0xABABABA)
        val SILLIES: List<String?> = List.of(
            "amogus",
            "what should i put here",
            "mineblock lore",
            "maybe",
            "or",
            "i can think of something funnier",
            "cause mineblock lore is kinda overrated",
            "which obviously means",
            "we should kick louise from the thread",
            ":brombeere:",
            "eh, ill leave that up to beno",
            "i want to work on this now",
            "this mod",
            "cause its a really cool concept",
            "and i shouldnt forget about it",
            "like i do with so many",
            "other mods",
            "yknow",
            "im really bad at this",
            "like",
            "not modding",
            "but maintaining a mod",
            "and continuing to improve it",
            "i get bored too easily",
            "maybe its cause i have",
            "too many other mods",
            "thats probably part of it",
            "i should just drop some of them",
            "or find new maintainers",
            "recursive resources for example",
            "like, its a cool mod",
            "but i dont really...",
            "care all that much?",
            "like",
            "someone would probably make this",
            "and better",
            "even if i didnt",
            "and its not like its",
            "*particularly* fun to work on",
            "ha, particular",
            "get it??",
            "actually",
            "i probably have enough strings now",
            "time to wrap this up"
        )
    }
}
