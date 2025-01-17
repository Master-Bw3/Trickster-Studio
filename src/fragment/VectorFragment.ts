import { HTMLText, Text } from "pixi.js";
import Fragment, { decode, FragmentType, register } from "./Fragment";

//@ts-ignore
import * as wasm from "../WasmEndec-1.0-SNAPSHOT/js/endec.js";
import NumberFragment from "./NumberFragment";

const VECTOR = register("trickster:vector", 0xffffff, (object: any) => {
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

    override asFormattedText(): HTMLText {
        const result =
            "(" +
            new NumberFragment(this.vector.x).asFormattedText().text +
            ", " +
            new NumberFragment(this.vector.y).asFormattedText().text +
            ", " +
            new NumberFragment(this.vector.z).asFormattedText().text +
            ")";

        return new HTMLText({
            text: `<span style="color: #${this.type().color.toString(16)}">${result}</span>`,
        });
    }

    override toString(): string {
        return "(" + this.vector.x + ", " + this.vector.y + ", " + this.vector.z + ")";
    }

    override type(): FragmentType {
        return VECTOR;
    }
}
