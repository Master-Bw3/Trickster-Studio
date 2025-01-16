package maple.trickster_endec.fragment;

import com.mojang.serialization.Lifecycle;

import io.wispforest.endec.StructEndec;
import maple.trickster_endec.Identifier;
import maple.trickster_endec.endecs.EndecTomfoolery;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;

public record FragmentType<T extends Fragment>(StructEndec<T> endec, OptionalInt color) {
    public static final Map<Integer, Identifier> INT_ID_LOOKUP = new HashMap<>();

    public static final Map<Identifier, FragmentType<?>> REGISTRY = new HashMap<>();

    //    public static final FragmentType<TypeFragment> TYPE = register("type", TypeFragment.ENDEC, 0x66cc00);
    public static final FragmentType<NumberFragment> NUMBER = register("number", NumberFragment.ENDEC, 0xddaa00);
    //    public static final FragmentType<BooleanFragment> BOOLEAN = register("boolean", BooleanFragment.ENDEC, 0xaa3355);
//    public static final FragmentType<VectorFragment> VECTOR = register("vector", VectorFragment.ENDEC);
//    public static final FragmentType<ListFragment> LIST = register("list", ListFragment.ENDEC);
//    public static final FragmentType<VoidFragment> VOID = register("void", VoidFragment.ENDEC, 0x4400aa);
    public static final FragmentType<PatternGlyph> PATTERN = register("pattern", PatternGlyph.ENDEC, 0x6644aa);
    public static final FragmentType<Pattern> PATTERN_LITERAL = register("pattern_literal", EndecTomfoolery.funnyFieldOf(Pattern.ENDEC, "pattern"), 0xbbbbaa);
    public static final FragmentType<SpellPart> SPELL_PART = register("spell_part", SpellPart.ENDEC, 0xaa44aa);

    //    public static final FragmentType<EntityFragment> ENTITY = register("entity", EntityFragment.ENDEC, 0x338888);
//    public static final FragmentType<ZalgoFragment> ZALGO = register("zalgo", ZalgoFragment.ENDEC, 0x444444);
//    public static final FragmentType<ItemTypeFragment> ITEM_TYPE = register("item_type", ItemTypeFragment.ENDEC, 0x2266aa);
//    public static final FragmentType<SlotFragment> SLOT = register("slot", SlotFragment.ENDEC, 0x77aaee);
//    public static final FragmentType<BlockTypeFragment> BLOCK_TYPE = register("block_type", BlockTypeFragment.ENDEC, 0x44aa33);
//    public static final FragmentType<EntityTypeFragment> ENTITY_TYPE = register("entity_type", EntityTypeFragment.ENDEC, 0x8877bb);
//    public static final FragmentType<DimensionFragment> DIMENSION = register("dimension", DimensionFragment.ENDEC, 0xdd55bb);
//    public static final FragmentType<StringFragment> STRING = register("string", StringFragment.ENDEC, 0xaabb77);
//    public static final FragmentType<MapFragment> MAP = register("map", MapFragment.ENDEC);
//
    private static <T extends Fragment> FragmentType<T> register(String name, StructEndec<T> codec, int color) {
        var id = new Identifier("trickster", name);
        var hash = id.hashCode();
        INT_ID_LOOKUP.put(hash, id);

        var type = new FragmentType<>(codec, OptionalInt.of(color));
        REGISTRY.put(id, type);
        return type;
    }

    private static <T extends Fragment> FragmentType<T> register(String name, StructEndec<T> codec) {
        var id = new Identifier("trickster", name);
        var hash = id.hashCode();
        INT_ID_LOOKUP.put(hash, id);

        var type = new FragmentType<>(codec, OptionalInt.empty());
        REGISTRY.put(id, type);
        return type;
    }

    public static void register() {
    }

    //
//    public MutableText getName() {
//        var id = REGISTRY.getId(this);
//        if (id == null) {
//            return Text.literal("Unregistered");
//        }
//        var text = Text.translatable(Trickster.MOD_ID + ".fragment." + id.getNamespace() + "." + id.getPath());
//        if (color.isPresent()) {
//            text = text.withColor(color.getAsInt());
//        }
//        return text;
//    }
//
    public static FragmentType<?> getFromInt(int intId) {
        var id = INT_ID_LOOKUP.get(intId);
        if (id == null) {
            throw new IllegalArgumentException("Not a valid int id for fragment type");
        }

        return REGISTRY.get(id);
    }

    public int getIntId() {
        return getID().hashCode();
    }

    public Identifier getID() {
        return REGISTRY.entrySet().stream().filter(x -> x.getValue().equals(this)).findFirst().get().getKey();
    }
}
