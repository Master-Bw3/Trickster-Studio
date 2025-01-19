import { Text } from "pixi.js";
import Fragment, { decode, FragmentType, register } from "./Fragment";

//@ts-ignore
import * as wasm from "../WasmEndec-1.0-SNAPSHOT/js/endec";



const BOOLEAN = register("trickster:boolean", 0xaa3355, (object: any) => {
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

    override toString(): string {
        return this.bool ? "True" : "False";
    }

    override type(): FragmentType {
        return BOOLEAN;
    }
}
