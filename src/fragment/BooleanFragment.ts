import { Text } from 'pixi.js';
import Fragment, { FragmentType, register } from './Fragment';
import { StructEndecBuilder, PrimitiveEndecs } from 'KEndec';

export default class BooleanFragment extends Fragment {
    bool: boolean;

    constructor(bool: boolean) {
        super();
        this.bool = bool;
    }

    override toString(): string {
        return this.bool ? 'True' : 'False';
    }

    override type(): FragmentType<BooleanFragment> {
        return BOOLEAN;
    }
}

const BOOLEAN = register(
    'boolean',
    0xaa3355,
    StructEndecBuilder.of1(
        PrimitiveEndecs.BOOLEAN.fieldOf('bool', (fragment: BooleanFragment) => fragment.bool),
        (bool) => new BooleanFragment(bool)
    )
);
