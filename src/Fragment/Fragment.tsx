class Fragment {}

class SpellPart extends Fragment {
    public glyph: Fragment;
    public subParts: Array<SpellPart>;

    constructor(glyph: Fragment, subParts: Array<SpellPart>) {
        super();
        this.glyph = glyph;
        this.subParts = subParts;
    }
}

class NumberFragment extends Fragment {
    public value: number;

    constructor(value: number) {
        super();
        this.value = value;
    }
}

export { Fragment, SpellPart, NumberFragment };
