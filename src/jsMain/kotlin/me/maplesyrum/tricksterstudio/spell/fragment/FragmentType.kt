package me.maplesyrum.tricksterstudio.spell.fragment


import Identifier
import me.maplesyrum.tricksterstudio.endec.funnyFieldOf
import tree.maple.kendec.Endec
import tree.maple.kendec.PrimitiveEndecs
import tree.maple.kendec.StructEndec
import tree.maple.kendec.util.Optional

class FragmentType<T : Fragment>(val endec: StructEndec<T>, val color: Optional<Int>) {
    val intId: Int = getId().hashCode()

    fun getId(): Identifier? {
        return REGISTRY.entries.firstOrNull { it.value == this }?.key
    }

    companion object {
        val INT_ID_FALLBACK = object : HashMap<Int, Identifier>() {
            init {
                put(-777274987, Identifier("trickster", "entity_type"))
                put(-2055452291, Identifier("trickster", "slot"))
                put(719778857, Identifier("trickster", "spell_part"))
                put(1201617608, Identifier("trickster", "number"))
                put(1343995792, Identifier("trickster", "string"))
                put(94838885, Identifier("trickster", "item_type"))
                put(-839744897, Identifier("trickster", "pattern_literal"))
                put(937706338, Identifier("trickster", "entity"))
                put(-2055409991, Identifier("trickster", "type"))
                put(-1943319220, Identifier("trickster", "zalgo"))
                put(1415594178, Identifier("trickster", "vector"))
                put(-772426965, Identifier("trickster", "block_type"))
                put(-2058877733, Identifier("trickster", "map"))
                put(1444891407, Identifier("trickster", "pattern"))
                put(1140968677, Identifier("trickster", "dimension"))
                put(-2055360237, Identifier("trickster", "void"))
                put(-2055663587, Identifier("trickster", "list"))
                put(-1994273881, Identifier("trickster", "boolean"))
            }
        }
        val INT_ID_LOOKUP: MutableMap<Int, Identifier> = HashMap()

        val INT_ID_ENDEC: Endec<FragmentType<*>> =
            PrimitiveEndecs.INT.xmap({ intId: Int ->
                getFromInt(intId)
            }, { obj: FragmentType<*>? -> obj!!.intId })
        val REGISTRY = HashMap<Identifier, FragmentType<*>>()


        val TYPE: FragmentType<TypeFragment> =
            register<TypeFragment>(
                "type",
                TypeFragment.ENDEC,
                0x66cc00
            )
        val NUMBER: FragmentType<NumberFragment> =
            register<NumberFragment>(
                "number",
                NumberFragment.ENDEC,
                0xddaa00
            )
        val BOOLEAN: FragmentType<BooleanFragment> =
            register<BooleanFragment>(
                "boolean",
                BooleanFragment.Companion.ENDEC,
                0xaa3355
            )
        val VECTOR: FragmentType<VectorFragment> =
            register<VectorFragment>(
                "vector",
                VectorFragment.Companion.ENDEC,
                0xaa7711
            )
        val LIST: FragmentType<ListFragment> =
            register<ListFragment>(
                "list",
                ListFragment.Companion.ENDEC
            )
        val VOID: FragmentType<VoidFragment> =
            register<VoidFragment>(
                "void",
                VoidFragment.Companion.ENDEC,
                0x4400aa
            )
        val PATTERN: FragmentType<PatternGlyph> =
            register<PatternGlyph>(
                "pattern",
                PatternGlyph.ENDEC,
                0x6644aa
            )
        val PATTERN_LITERAL: FragmentType<Pattern> =
            register(
                "pattern_literal",
                funnyFieldOf(Pattern.ENDEC, "pattern"), 0xbbbbaa
            )
        val SPELL_PART: FragmentType<SpellPart> =
            register<SpellPart>(
                "spell_part",
                SpellPart.ENDEC,
                0xaa44aa
            )
        val ENTITY: FragmentType<EntityFragment> =
            register<EntityFragment>(
                "entity",
                EntityFragment.ENDEC,
                0x338888
            )
        val ZALGO: FragmentType<ZalgoFragment> =
            register<ZalgoFragment>(
                "zalgo",
                ZalgoFragment.ENDEC,
                0x444444
            )
        val ITEM_TYPE: FragmentType<ItemTypeFragment> =
            register<ItemTypeFragment>(
                "item_type", ItemTypeFragment.ENDEC,
                0x2266aa
            )
        val SLOT: FragmentType<SlotFragment> =
            register<SlotFragment>(
                "slot",
                SlotFragment.ENDEC,
                0x77aaee
            )
        val BLOCK_TYPE: FragmentType<BlockTypeFragment> =
            register<BlockTypeFragment>(
                "block_type", BlockTypeFragment.ENDEC,
                0x44aa33
            )
        val ENTITY_TYPE: FragmentType<EntityTypeFragment> =
            register<EntityTypeFragment>(
                "entity_type", EntityTypeFragment.ENDEC,
                0x8877bb
            )
        val DIMENSION: FragmentType<DimensionFragment> =
            register<DimensionFragment>(
                "dimension", DimensionFragment.ENDEC,
                0xdd55bb
            )
        val STRING: FragmentType<StringFragment> =
            register<StringFragment>(
                "string",
                StringFragment.ENDEC,
                0xaabb77
            )
        val MAP: FragmentType<MapFragment> =
            register<MapFragment>(
                "map",
                MapFragment.ENDEC
            )

        private fun <T : Fragment> register(
            name: String,
            codec: StructEndec<T>,
            color: Int = 0xaaaaaa
        ): FragmentType<T> {
            val fragmentType = FragmentType<T>(codec, Optional.of(color))
            val id = Identifier("trickster", name)
            REGISTRY.put(id, fragmentType)

            var hash = id.hashCode();
            INT_ID_LOOKUP.put(hash, id)
            return fragmentType
        }

        fun register() {
        }

        fun getFromInt(intId: Int): FragmentType<*> {
            var id = INT_ID_LOOKUP[intId]
            if (id == null) {
                id = INT_ID_FALLBACK[intId]

                requireNotNull(id) { "Not a valid int id for fragment type: $intId" }
            }

            return REGISTRY[id]!!
        }
    }
}
