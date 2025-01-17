import { Text } from "pixi.js";
import Fragment, { decode, FragmentType, register } from "./Fragment";

//@ts-ignore
import * as wasm from "../WasmEndec-1.0-SNAPSHOT/js/endec.js";

const VOID = register("trickster:void", 0x4400aa, (object: any) => {
    if (object instanceof wasm.VoidFragment) {
        return new VoidFragment();
    }
    return null;
});

export default class VoidFragment extends Fragment {
    constructor() {
        super();
    }

    override toString(): string {
        return "Void";
    }

    override type(): FragmentType {
        return VOID;
    }
}
