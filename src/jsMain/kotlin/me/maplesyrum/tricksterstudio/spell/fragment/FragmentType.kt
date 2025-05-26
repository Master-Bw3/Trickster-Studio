package me.maplesyrum.tricksterstudio.spell.fragment


import Identifier
import dev.enjarai.trickster.spell.fragment.*
import tree.maple.kendec.Endec
import tree.maple.kendec.PrimitiveEndecs
import tree.maple.kendec.StructEndec
import tree.maple.kendec.util.Optional

class FragmentType<T : Fragment>(endec: StructEndec<T>, color: Optional<Int>) {
    val name: String
        get() {
            val id: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
                REGISTRY.getId(this)
            if (id == null) {
                return Text.literal("Unregistered")
            }
            var text: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
                Text.translatable(Trickster.MOD_ID + ".fragment." + id.getNamespace() + "." + id.getPath())
            if (color.isPresent()) {
                text = text.withColor(color.getAsInt())
            }
            return text
        }

    @Override
    fun asText(): MutableText? {
        return this.name
    }

    val intId: Int
        get() = REGISTRY.getId(this).hashCode()

    @Override
    fun argc(fragments: List<Fragment?>?): Int {
        return 1
    }

    @Override
    @SuppressWarnings("unchecked")
    fun compose(trick: Trick<*>?, ctx: SpellContext?, fragments: List<Fragment?>): T? {
        return fragments.get(0) as T?
    }

//    @Override
//    fun match(fragments: List<Fragment?>): Boolean {
//        return fragments.get(0).type() == this
//    }

    @Override
    fun wardOf(): ArgType<T?>? {
        return object : ArgType() {
            @Override
            fun argc(fragments: List<Fragment?>?): Int {
                return this@FragmentType.argc(fragments)
            }

            @Override
            fun compose(trick: Trick<*>?, ctx: SpellContext?, fragments: List<Fragment?>): T? {
                val result = this@FragmentType.compose(trick, ctx, fragments)

                if (result is EntityFragment) {
                    ArgType.tryWard(trick, ctx, result, fragments)
                }

                return result
            }

            @Override
            fun match(fragments: List<Fragment?>): Boolean {
                return this@FragmentType.match(fragments)
            }

            @Override
            fun wardOf(): ArgType<T?>? {
                return this
            }

            @Override
            fun asText(): MutableText? {
                return this@FragmentType.asText()
            }
        }
    }

    val endec: StructEndec<T>
    val color: OptionalInt?

    init {
        this.endec = endec
        this.color = color
    }

    companion object {
        val REGISTRY_KEY: RegistryKey<Registry<FragmentType<*>?>?>? =
            RegistryKey.ofRegistry(Trickster.id("fragment_type"))
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
        val INT_ID_ENDEC: Endec<FragmentType<*>?>? =
            PrimitiveEndecs.INT.xmap({ intId: Int ->
                getFromInt(intId)
            }, { obj: FragmentType<*>? -> obj!!.intId })
        val REGISTRY: MutableMap<Identifier, FragmentType<*>> = HashMap()

        val TYPE: FragmentType<TypeFragment> =
            register<TypeFragment>(
                "type",
                TypeFragment.ENDEC,
                0x66cc00
            )
        val NUMBER: FragmentType<NumberFragment?> =
            Companion.register<NumberFragment?>(
                "number",
                NumberFragment.Companion.ENDEC,
                0xddaa00
            )
        val BOOLEAN: FragmentType<BooleanFragment?> =
            Companion.register<BooleanFragment?>(
                "boolean",
                BooleanFragment.Companion.ENDEC,
                0xaa3355
            )
        val VECTOR: FragmentType<VectorFragment?> =
            Companion.register<VectorFragment?>(
                "vector",
                VectorFragment.Companion.ENDEC,
                0xaa7711
            )
        val LIST: FragmentType<ListFragment?> =
            Companion.register<ListFragment?>(
                "list",
                ListFragment.Companion.ENDEC
            )
        val VOID: FragmentType<VoidFragment?> =
            Companion.register<VoidFragment?>(
                "void",
                VoidFragment.Companion.ENDEC,
                0x4400aa
            )
        val PATTERN: FragmentType<PatternGlyph?> =
            Companion.register<PatternGlyph?>(
                "pattern",
                PatternGlyph.ENDEC,
                0x6644aa
            )
        val PATTERN_LITERAL: FragmentType<Pattern?> =
            Companion.register<T?>(
                "pattern_literal",
                EndecTomfoolery.funnyFieldOf(Pattern.ENDEC, "pattern"), 0xbbbbaa
            )
        val SPELL_PART: FragmentType<SpellPart?> =
            Companion.register<SpellPart?>(
                "spell_part",
                SpellPart.ENDEC,
                0xaa44aa
            )
        val ENTITY: FragmentType<EntityFragment?> =
            Companion.register<EntityFragment?>(
                "entity",
                EntityFragment.Companion.ENDEC,
                0x338888
            )
        val ZALGO: FragmentType<ZalgoFragment?> =
            Companion.register<ZalgoFragment?>(
                "zalgo",
                ZalgoFragment.Companion.ENDEC,
                0x444444
            )
        val ITEM_TYPE: FragmentType<ItemTypeFragment?> =
            Companion.register<ItemTypeFragment?>(
                "item_type", ItemTypeFragment.Companion.ENDEC,
                0x2266aa
            )
        val SLOT: FragmentType<SlotFragment?> =
            Companion.register<SlotFragment?>(
                "slot",
                SlotFragment.Companion.ENDEC,
                0x77aaee
            )
        val BLOCK_TYPE: FragmentType<BlockTypeFragment> =
            register<BlockTypeFragment?>(
                "block_type", BlockTypeFragment.ENDEC,
                0x44aa33
            )
        val ENTITY_TYPE: FragmentType<EntityTypeFragment?> =
            Companion.register<EntityTypeFragment?>(
                "entity_type", EntityTypeFragment.Companion.ENDEC,
                0x8877bb
            )
        val DIMENSION: FragmentType<DimensionFragment?> =
            Companion.register<DimensionFragment?>(
                "dimension", DimensionFragment.Companion.ENDEC,
                0xdd55bb
            )
        val STRING: FragmentType<StringFragment?> =
            Companion.register<StringFragment?>(
                "string",
                StringFragment.Companion.ENDEC,
                0xaabb77
            )
        val MAP: FragmentType<MapFragment?> =
            Companion.register<MapFragment?>("map", MapFragment.Companion.ENDEC)

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
            var id: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
                INT_ID_LOOKUP.get(intId)
            if (id == null) {
                id = INT_ID_FALLBACK.get(intId)

                requireNotNull(id) { "Not a valid int id for fragment type: " + intId }
            }

            return REGISTRY.get(id)
        }
    }
}
