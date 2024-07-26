import { Identifier } from '../Identifier';
import Fragment from './Fragment';

export default class SpellPart extends Fragment {
    static get type(): Identifier {
        return new Identifier('trickster', 'spellpart');
    }

    get type(): Identifier {
        return SpellPart.type;
    }

    public glyph: Fragment;
    public subParts: Array<SpellPart>;

    constructor(glyph: Fragment, subParts: Array<SpellPart>) {
        super();
        this.glyph = glyph;
        this.subParts = subParts;
    }

    toString(): string {
        throw new Error('Method not implemented.');
    }
}
