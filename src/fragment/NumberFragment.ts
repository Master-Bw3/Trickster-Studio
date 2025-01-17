import { Text } from "pixi.js";
import Fragment, { decode, FragmentType, register } from "./Fragment";

//@ts-ignore
import * as wasm from "../WasmEndec-1.0-SNAPSHOT/js/endec.js";

const NUMBER = register("trickster:number", (object: any) => {
    if (object instanceof wasm.NumberFragment) {
        return new NumberFragment(object.number)
    }
    return null;
});

export default class NumberFragment extends Fragment {
    number: number

    constructor(number: number) {
        super();

        this.number = number;
    }

    override asFormattedText(): Text {
        return new Text({
            text: this.number.toFixed(2),
            style: {
                fill: 0xddaa00
            }
        });
    }

    override type(): FragmentType {
        return NUMBER;
    }
}
