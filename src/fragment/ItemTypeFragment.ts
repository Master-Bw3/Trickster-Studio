import { Text } from "pixi.js";
import Fragment, { decode, FragmentType, register } from "./Fragment";

//@ts-ignore
import * as wasm from "../WasmEndec-1.0-SNAPSHOT/js/endec.js";

const ITEM_TYPE = register("trickster:Item_type", (object: any) => {
    if (object instanceof wasm.ItemTypeFragment) {
        return new ItemTypeFragment(object.item)
    }
    return null;
});

export default class ItemTypeFragment extends Fragment {
    item: string

    constructor(item: string) {
        super();

        this.item = item;
    }

    override asFormattedText(): Text {
        return new Text({
            text: this.item,
        });
    }

    override type(): FragmentType {
        return ITEM_TYPE;
    }
}
