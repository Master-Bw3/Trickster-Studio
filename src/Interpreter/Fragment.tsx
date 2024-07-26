import { Identifier } from '../Identifier';

export default abstract class Fragment {
    abstract toString(): string;

    static get type(): Identifier {
        return new Identifier('trickster', 'fragment');
    }

    get type(): Identifier {
        return this.type;
    }
}
