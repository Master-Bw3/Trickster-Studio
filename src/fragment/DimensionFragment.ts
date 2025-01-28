import { Text } from "pixi.js";
import Fragment, { FragmentType, register } from "./Fragment";
import { StructEndecBuilder, PrimitiveEndecs } from 'KEndec';
import { Identifier } from "~/util";



export default class DimensionFragment extends Fragment {
    world: string;

    constructor(world: string) {
        super();

        this.world = world;
    }

    override toString(): string {
        return this.world.replace(/^[^a-zA-Z0-9]+/, "");
    }

    override type(): FragmentType<DimensionFragment> {
        return DIMENSION;
    }
}

const DIMENSION = register("dimension", 0xdd55bb, 
    StructEndecBuilder.of1(
        Identifier.ENDEC
            .xmap((ident) => ident.toString(), Identifier.of)
            .fieldOf("block", (fragment: DimensionFragment) => fragment.world),
        (str) => new DimensionFragment(str) 
    )
);