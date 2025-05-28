package me.maplesyrum.tricksterstudio.spell.fragment


import me.maplesyrum.tricksterstudio.SpellUtils
import me.maplesyrum.tricksterstudio.endec.protocolVersionAlternatives
import me.maplesyrum.tricksterstudio.endec.recursive
import me.maplesyrum.tricksterstudio.endec.withAlternative
import me.maplesyrum.tricksterstudio.spell.executer.EvaluationResult
import tree.maple.kendec.SerializationContext
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder
import me.maplesyrum.tricksterstudio.spell.executer.SpellInstruction
import tree.maple.kendec.util.Optional
import tree.maple.kendec.util.mapOf
import kotlin.Any
import kotlin.Boolean
import kotlin.Byte
import kotlin.Function
import kotlin.Int
import kotlin.String
import kotlin.Throws
import kotlin.Unit
import kotlin.UnsupportedOperationException
import kotlin.apply
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.containsKey
import kotlin.collections.forEach
import kotlin.collections.get
import kotlin.collections.isEmpty
import kotlin.collections.map
import kotlin.collections.plusAssign
import kotlin.collections.remove
import kotlin.collections.toList
import kotlin.invoke
import kotlin.js.get
import kotlin.jvm.JvmOverloads
import kotlin.map
import kotlin.sequences.map
import kotlin.sequences.toList
import kotlin.text.append
import kotlin.text.clear
import kotlin.text.equals
import kotlin.text.get
import kotlin.text.isEmpty
import kotlin.text.set
import kotlin.text.toList
import kotlin.toList

class SpellPart(var glyph: Fragment, var subParts: List<SpellPart>) : Fragment() {

    constructor(glyph: Fragment = PatternGlyph()) : this(glyph, ArrayList())

    override fun type(): FragmentType<out Fragment> {
        return FragmentType.SPELL_PART
    }

    override fun toString(): String {
        return "SpellPart"
    }

    companion object {
        val ENDEC: StructEndec<SpellPart> = recursive { self ->
            StructEndecBuilder.of(
                Fragment.ENDEC.fieldOf(
                    "glyph"
                ) { obj: SpellPart? -> obj!!.glyph },
                protocolVersionAlternatives(
                    mapOf(1 to self.listOf()),
                    withAlternative(
                        SpellInstruction.STACK_ENDEC.xmap(
                            { instructions ->
                                SpellUtils.decodeInstructions(
                                    instructions,
                                    listOf(),
                                    listOf(),
                                    Optional.empty()
                                )
                            },
                            SpellUtils::flattenNode
                        ), self
                    ).listOf()
                ).fieldOf("sub_parts", { obj: SpellPart? -> obj!!.subParts }),
                ::SpellPart
            )
        }
    }
}
