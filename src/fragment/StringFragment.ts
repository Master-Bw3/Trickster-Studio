import { Text } from 'pixi.js';
import Fragment, { FragmentType, register } from './Fragment';
import { StructEndecBuilder, PrimitiveEndecs } from 'KEndec';

export default class StringFragment extends Fragment {
    string: string;

    constructor(string: string) {
        super();

        this.string = string;
    }

    override toString(): string {
        return this.string;
    }

    override type(): FragmentType<StringFragment> {
        return SLOT;
    }
}

const SLOT = register(
    'string',
    0xaabb77,
    StructEndecBuilder.of1(
        PrimitiveEndecs.STRING.fieldOf('value', (fragment: StringFragment) => fragment.string),
        (str) => new StringFragment(str)
    )
);
