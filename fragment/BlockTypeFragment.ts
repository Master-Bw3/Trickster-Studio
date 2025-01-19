import { HTMLText, Text } from "pixi.js";
import Fragment, { decode, FragmentType, register } from "./Fragment";

//@ts-ignore
import * as wasm from "../WasmEndec-1.0-SNAPSHOT/js/endec";



const BLOCK_TYPE = register("trickster:block_type", 0x44aa33, (object: any) => {
    if (object instanceof wasm.BlockTypeFragment) {
        return new BlockTypeFragment(object.block);
    }
    return null;
});

export default class BlockTypeFragment extends Fragment {
    blockType: string;

    constructor(blockType: string) {
        super();

        this.blockType = blockType;
    }

    override toString(): string {
        return this.blockType
    }

    override type(): FragmentType {
        return BLOCK_TYPE;
    }
}
