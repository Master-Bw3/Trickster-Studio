import { HTMLText, Text } from "pixi.js";
import Fragment, { FragmentType, register } from "./Fragment";
import { StructEndecBuilder } from "KEndec";
import { Identifier } from "~/util";


export default class BlockTypeFragment extends Fragment {

    blockType: string;

    constructor(blockType: string) {
        super();

        this.blockType = blockType;
    }

    override toString(): string {
        return this.blockType
    }

    override type(): FragmentType<BlockTypeFragment> {
        return BLOCK_TYPE;
    }
}

const BLOCK_TYPE = register("block_type", 0x44aa33, 
    StructEndecBuilder.of1(
        Identifier.ENDEC
            .xmap((ident) => ident.toString(), Identifier.of)
            .fieldOf("block", (fragment: BlockTypeFragment) => fragment.blockType),
        (str) => new BlockTypeFragment(str) 
    )
);