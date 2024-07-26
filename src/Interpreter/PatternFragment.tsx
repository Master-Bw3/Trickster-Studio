import { Identifier } from '../Identifier';
import Fragment from './Fragment';

export type Pattern = Array<number>;

export default class PatternFragment extends Fragment {
    static get type(): Identifier {
        return new Identifier('trickster', 'pattern');
    }

    get type(): Identifier {
        return PatternFragment.type;
    }

    public pattern: Pattern;

    constructor(value: Pattern) {
        super();
        this.pattern = value;
    }

    toString(): string {
        return this.pattern.toString();
    }
}
