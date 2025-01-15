import Fragment from "./Fragment";
import PatternGlyph from "./PatternGlyph";

export default class SpellPart extends Fragment {
    getSubParts(): Array<SpellPart> {
        return this.subParts;
    }

    glyph: Fragment;
    subParts: Array<SpellPart>;

    constructor(glyph?: Fragment, subParts?: Array<SpellPart>) {
        super();

        this.glyph = glyph != undefined ? glyph : new PatternGlyph()
        this.subParts = subParts != undefined ? subParts : []
    }
}
