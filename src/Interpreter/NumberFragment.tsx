import { Identifier } from '../Identifier';
import Fragment from './Fragment';

export default class NumberFragment extends Fragment {
    static get type(): Identifier {
        return new Identifier('trickster', 'number');
    }

    public value: number;

    constructor(value: number) {
        super();
        this.value = value;
    }

    toString(): string {
        return this.value.toString();
    }
}
