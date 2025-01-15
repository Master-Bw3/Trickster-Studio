import Pattern, { patternOf } from "../Pattern";
import Fragment from "./Fragment";

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
}
