class Fragment {}

class SpellPart extends Fragment {
    public glyph: Fragment;

    constructor(glyph: Fragment) {
        super();
        this.glyph = glyph;
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
