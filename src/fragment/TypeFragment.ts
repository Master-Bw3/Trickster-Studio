import { Text } from "pixi.js";
import Fragment, { decode, FragmentType, fragmentTypes, getKeyByValue, register } from "./Fragment";

//@ts-ignore
import * as wasm from "../WasmEndec-1.0-SNAPSHOT/js/endec.js";

const TYPE = register("trickster:type", 0x66cc00, (object: any) => {
    if (object instanceof wasm.TypeFragment) {
        return new TypeFragment(object.id);
    }
    return null;
});

export default class TypeFragment extends Fragment {
    typeType: FragmentType;

    constructor(id: string) {
        super();

        this.typeType = fragmentTypes.get(id)!;
    }

    override toString(): string {
        return getKeyByValue(fragmentTypes, this.typeType)!;
    }

    override type(): FragmentType {
        return TYPE;
    }
}
