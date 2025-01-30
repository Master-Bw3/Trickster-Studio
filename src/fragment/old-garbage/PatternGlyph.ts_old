import { Text } from "pixi.js";
import Pattern, { PATTERN_ENDEC, patternOf } from "./Pattern";
import Fragment, {FragmentType, register } from "./Fragment";
import { StructEndecBuilder, PrimitiveEndecs } from 'KEndec';


export default class PatternGlyph extends Fragment {
    pattern: Pattern;

    constructor(pattern: Array<number> | Pattern | null = null) {
        super();
        if (pattern === null) {
            this.pattern = new Pattern([]);
        } else if (Array.isArray(pattern)) {
            this.pattern = patternOf(pattern) ?? (() => {throw new Error("invalid pattern")})();
        } else if (pattern instanceof Pattern) {
            this.pattern = pattern;
        } else {
            throw new Error("Invalid pattern type");
        }
    }

    override toString(): string {
        return "pattern glyph";
    }

    override type(): FragmentType<PatternGlyph> {
        return PATTERN_GLYPH;
    }
}

const PATTERN_GLYPH = register("pattern", 0x6644aa, 
    StructEndecBuilder.of1(
        PATTERN_ENDEC.fieldOf("pattern", (fragment: PatternGlyph) => fragment.pattern),
        pattern => new PatternGlyph(pattern)
    )
);