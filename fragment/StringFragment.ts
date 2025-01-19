import { Text } from "pixi.js";
import Fragment, { decode, FragmentType, register } from "./Fragment";

//@ts-ignore
import * as wasm from "../WasmEndec-1.0-SNAPSHOT/js/endec";



const SLOT = register("trickster:string", 0xaabb77, (object: any) => {
    if (object instanceof wasm.StringFragment) {
        return new StringFragment(object.value);
    }
    return null;
});

export default class StringFragment extends Fragment {
    string: string;

    constructor(string: string) {
        super();

        this.string = string;
    }

    override toString(): string {
        return this.string;
    }

    override type(): FragmentType {
        return SLOT;
    }
}
