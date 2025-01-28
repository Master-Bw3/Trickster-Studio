import { Text } from "pixi.js";
import Fragment, { FragmentType, register } from "./Fragment";
import PatternGlyph from "./PatternGlyph";
import { StructEndecBuilder, PrimitiveEndecs, StructEndec, Optional, KtList } from 'KEndec';
import { protocolVersionAlternatives, recursive, withAlternative } from "~/endecTomfoolery";
import { SpellInstruction } from "~/spellInstruction";
import { SpellUtils } from "~/SpellUtils";


export default class SpellPart extends Fragment {
    glyph: Fragment;
    subParts: ReadonlyArray<SpellPart>;

    constructor(glyph?: Fragment, subParts?: ReadonlyArray<SpellPart>) {
        super();

        this.glyph = glyph != undefined ? glyph : new PatternGlyph();
        this.subParts = subParts != undefined ? subParts : [];
    }

    override toString(): string {
        return "spell";
    }

    override type(): FragmentType<SpellPart> {
        return SPELL_PART;
    }

    getSubParts(): ReadonlyArray<SpellPart> {
        return this.subParts;
    }
}

const SPELL_PART = register("spell_part", 0xaa44aa, 
    recursive((self: StructEndec<SpellPart>) => StructEndecBuilder.of2(
        Fragment.ENDEC.fieldOf("glyph", (fragment: SpellPart) => fragment.glyph),
        protocolVersionAlternatives(
            new Map([[1, self.listOf()]]),
            withAlternative(SpellInstruction.STACK_ENDEC.xmap(
                instructions => SpellUtils.decodeInstructions(instructions, [], [], Optional.empty()),
                SpellUtils.flattenNode
            ), self).listOf()
        ).fieldOf("sub_parts", (fragment: SpellPart) => KtList.getInstance().fromJsArray(fragment.subParts)),
        (glyph, subparts) => new SpellPart(glyph, subparts.asJsReadonlyArrayView()) 
    ))
);
