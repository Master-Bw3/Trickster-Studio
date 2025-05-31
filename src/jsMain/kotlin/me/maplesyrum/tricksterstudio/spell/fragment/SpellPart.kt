package me.maplesyrum.tricksterstudio.spell.fragment


import me.maplesyrum.tricksterstudio.SpellUtils
import me.maplesyrum.tricksterstudio.endec.protocolVersionAlternatives
import me.maplesyrum.tricksterstudio.endec.recursiveStruct
import me.maplesyrum.tricksterstudio.endec.withAlternative
import me.maplesyrum.tricksterstudio.spell.executer.SpellInstruction
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder
import tree.maple.kendec.structEndecOf
import tree.maple.kendec.util.Optional

class SpellPart(var glyph: Fragment, var subParts: List<SpellPart>) : Fragment() {

    constructor(glyph: Fragment = PatternGlyph()) : this(glyph, ArrayList())

    override fun type(): FragmentType<out Fragment> {
        return FragmentType.SPELL_PART
    }

    override fun toString(): String {
        return "SpellPart"
    }

    companion object {
        val ENDEC: StructEndec<SpellPart> = recursiveStruct {
            StructEndecBuilder.of(
                Fragment.ENDEC.fieldOf("glyph") { obj: SpellPart -> obj.glyph },
                protocolVersionAlternatives(
                    mapOf(1 to it.listOf()),
                    withAlternative(
                        SpellInstruction.STACK_ENDEC.xmap(
                            { SpellUtils.decodeInstructions(it, listOf<Int>(), listOf<Fragment>(), Optional.empty<Fragment>()) },
                            (SpellUtils::flattenNode)
                        ), it
                    ).listOf()
                ).fieldOf ("sub_parts", SpellPart::subParts),
                ::SpellPart
            )
        }
    }
}
