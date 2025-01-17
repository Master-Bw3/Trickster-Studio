import { Text } from "pixi.js";
import Fragment, { decode, FragmentType, register } from "./Fragment";
import PatternGlyph from "./PatternGlyph";

//@ts-ignore
import * as wasm from "../WasmEndec-1.0-SNAPSHOT/js/endec.js";

const SPELL_PART = register("trickster:spell_part", 0xaa44aa, (object: any) => {
    if (object instanceof wasm.SpellPart) {
        const glyph = decode(object.glyph);
        const subparts: Array<Fragment | null> = object.subParts.map(decode);
        if (glyph != null && subparts.every((x) => x instanceof SpellPart)) {
            return new SpellPart(glyph, subparts);
        }
    }
    return null;
});

export default class SpellPart extends Fragment {
    glyph: Fragment;
    subParts: Array<SpellPart>;

    constructor(glyph?: Fragment, subParts?: Array<SpellPart>) {
        super();

        this.glyph = glyph != undefined ? glyph : new PatternGlyph();
        this.subParts = subParts != undefined ? subParts : [];
    }

    override toString(): string {
        return "spell";
    }

    override type(): FragmentType {
        return SPELL_PART;
    }

    getSubParts(): Array<SpellPart> {
        return this.subParts;
    }
}
