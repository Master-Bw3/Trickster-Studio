package me.maplesyrum.tricksterstudio.spell.fragment

import me.maplesyrum.tricksterstudio.endec.unit
import me.maplesyrum.tricksterstudio.endec.unitFromSupplier
import tree.maple.kendec.StructEndec
import kotlin.invoke
import kotlin.random.Random


data class ZalgoFragment(val index: Int) : Fragment() {
    constructor() : this(Random.nextInt(SILLIES.size))

    override fun type(): FragmentType<*> {
        return FragmentType.ZALGO
    }

    override fun toString(): String {
        return SILLIES[index]
    }

    companion object {
        val ENDEC: StructEndec<ZalgoFragment> = unitFromSupplier { ZalgoFragment() }
        val SILLIES: List<String> = listOf(
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
