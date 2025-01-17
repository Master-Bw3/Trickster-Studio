import { Text } from "pixi.js";
import Fragment, { decode, FragmentType, register } from "./Fragment";

//@ts-ignore
import * as wasm from "../WasmEndec-1.0-SNAPSHOT/js/endec.js";

const LIST = register("trickster:list", (object: any) => {
    if (object instanceof wasm.ListFragment) {
        const fragments: Array<Fragment | null> = object.fragments.map(decode);

        if (fragments.every((x) => x instanceof Fragment)) {
            return new ListFragment(fragments);
        }
    }
    return null;
});

export default class ListFragment extends Fragment {
    fragments: Array<Fragment>;

    constructor(fragments: Array<Fragment>) {
        super();

        this.fragments = fragments;
    }

    override asFormattedText(): Text {
        let result = "[";

        for (let i = 0; i < this.fragments.length; i++) {
            const fragment = this.fragments[i];
            if (i !== 0) {
                result += ", ";
            }
            result += fragment.asFormattedText();
        }

        result += "]";

        return new Text({
            text: result,
        });
    }

    override type(): FragmentType {
        return LIST;
    }
}
