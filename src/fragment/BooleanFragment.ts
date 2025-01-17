import { Text } from "pixi.js";
import Fragment, { decode, FragmentType, register } from "./Fragment";

//@ts-ignore
import * as wasm from "../WasmEndec-1.0-SNAPSHOT/js/endec.js";

const BOOLEAN = register("trickster:boolean", (object: any) => {
    if (object instanceof wasm.BooleanFragment) {
        return new BooleanFragment(object.bool)
    }
    return null;
});

export default class BooleanFragment extends Fragment {
    bool: boolean

    constructor(bool: boolean) {
        super();

        this.bool = bool;
    }

    override asFormattedText(): Text {
        return new Text({
            text: this.bool.toString(),
        });
    }

    override type(): FragmentType {
        return BOOLEAN;
    }
}
