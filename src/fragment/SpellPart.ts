import Fragment from "./Fragment";

export default class SpellPart extends Fragment {
    getSubParts(): Array<SpellPart> {
        return this.subParts;
    }

    glyph: Fragment;
    subParts: Array<SpellPart>;

    constructor(glyph: Fragment, subParts: Array<SpellPart>) {
        super();

        this.glyph = glyph;
        this.subParts = subParts;
    }
}
