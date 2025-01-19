import { Text } from "pixi.js";
import Fragment, { decode, FragmentType, register } from "./Fragment";

//@ts-ignore
import * as wasm from "../WasmEndec-1.0-SNAPSHOT/js/endec";



const DIMENSION = register("trickster:dimension", 0xdd55bb, (object: any) => {
    if (object instanceof wasm.DimensionFragment) {
        return new DimensionFragment(object.world);
    }
    return null;
});

export default class DimensionFragment extends Fragment {
    world: string;

    constructor(world: string) {
        super();

        this.world = world;
    }

    override toString(): string {
        return this.world.replace(/^[^a-zA-Z0-9]+/, "");
    }

    override type(): FragmentType {
        return DIMENSION;
    }
}
