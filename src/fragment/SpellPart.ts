import { Text } from "pixi.js";
import Fragment, { FragmentType, TYPES } from "./Fragment";
import PatternGlyph from "./PatternGlyph";

export default class SpellPart extends Fragment {
    glyph: Fragment;
    subParts: Array<SpellPart>;

    constructor(glyph?: Fragment, subParts?: Array<SpellPart>) {
        super();

        this.glyph = glyph != undefined ? glyph : new PatternGlyph();
        this.subParts = subParts != undefined ? subParts : [];
    }

    override asFormattedText(): Text {
        return new Text({
            text: "spell"
        })
    }

    override type(): FragmentType {
        return TYPES.SPELL_PART
    }

    getSubParts(): Array<SpellPart> {
        return this.subParts;
    }
}
