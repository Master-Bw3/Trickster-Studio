import { Text } from "pixi.js";
import { StructEndecBuilder, PrimitiveEndecs } from 'KEndec';
import Fragment, { FragmentType, register } from "./Fragment";
import { Identifier } from "~/util";


export default class ItemTypeFragment extends Fragment {
    item: string;

    constructor(item: string) {
        super();

        this.item = item;
    }

    override toString(): string {
        return this.item;
    }

    override type(): FragmentType<ItemTypeFragment> {
        return ITEM_TYPE;
    }
}

const ITEM_TYPE = register("Item_type", 0x2266aa,     
    StructEndecBuilder.of1(
        Identifier.ENDEC
            .xmap((ident) => ident.toString(), Identifier.of)
            .fieldOf("entityType", (fragment: ItemTypeFragment) => fragment.item),
        (item) => new ItemTypeFragment(item)
    )
);

