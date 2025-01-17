import { Text } from "pixi.js";
import Fragment, { decode, FragmentType, register } from "./Fragment";

//@ts-ignore
import * as wasm from "../WasmEndec-1.0-SNAPSHOT/js/endec.js";

const DIMENSION = register("trickster:dimension", (object: any) => {
    if (object instanceof wasm.DimensionFragment) {
        return new DimensionFragment(object.bool);
    }
    return null;
});

export default class DimensionFragment extends Fragment {
    world: string;

    constructor(world: string) {
        super();

        this.world = world;
    }

    override asFormattedText(): Text {
        return new Text({
            text: this.world
                .replace("_", " ")
                .split(" ")
                .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
                .join(" "),
        });
    }

    override type(): FragmentType {
        return DIMENSION;
    }
}
