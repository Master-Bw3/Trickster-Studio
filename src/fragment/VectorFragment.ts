import { Text } from "pixi.js";
import Fragment, { decode, FragmentType, register } from "./Fragment";

//@ts-ignore
import * as wasm from "../WasmEndec-1.0-SNAPSHOT/js/endec.js";

const VECTOR = register("trickster:vector", (object: any) => {
    if (object instanceof wasm.VectorFragment) {
        return new VectorFragment(object.vector);
    }
    return null;
});

type Vector = { x: number; y: number; z: number };

export default class VectorFragment extends Fragment {
    vector: Vector;

    constructor(vector: Vector) {
        super();

        this.vector = vector;
    }

    override asFormattedText(): Text {
        const result = "(" + this.vector.x + ", " + this.vector.y + ", " + this.vector.z + ")";

        return new Text({
            text: result,
        });
    }

    override type(): FragmentType {
        return VECTOR;
    }
}
