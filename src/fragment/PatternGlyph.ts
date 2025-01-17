import { Text } from "pixi.js";
import Pattern, { patternOf } from "./Pattern";
import Fragment, { decode, FragmentType, register } from "./Fragment";

//@ts-ignore
import * as wasm from "../WasmEndec-1.0-SNAPSHOT/js/endec.js";

const PATTERN_GLYPH = register("trickster:pattern_glyph", 0x6644aa, (object: any) => {
    if (object instanceof wasm.PatternGlyph) {
        const pattern = decode(object.pattern);
        if (pattern instanceof Pattern) {
            return new PatternGlyph(pattern);
        }
    }
    return null;
});

export default class PatternGlyph extends Fragment {
    pattern: Pattern;

    constructor(pattern: Array<number> | Pattern | null = null) {
        super();
        if (pattern === null) {
            this.pattern = new Pattern([]);
        } else if (Array.isArray(pattern)) {
            this.pattern = patternOf(pattern);
        } else if (pattern instanceof Pattern) {
            this.pattern = pattern;
        } else {
            throw new Error("Invalid pattern type");
        }
    }

    override toString(): string {
        return "pattern glyph";
    }

    override type(): FragmentType {
        return PATTERN_GLYPH;
    }
}
