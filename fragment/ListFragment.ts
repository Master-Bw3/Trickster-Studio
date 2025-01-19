import { HTMLText, Text } from "pixi.js";
import Fragment, { decode, FragmentType, register } from "./Fragment";

//@ts-ignore
import * as wasm from "../WasmEndec-1.0-SNAPSHOT/js/endec";



const LIST = register("trickster:list", 0xffffff, (object: any) => {
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

    override asFormattedText(): HTMLText {
        let result = "[";

        for (let i = 0; i < this.fragments.length; i++) {
            const fragment = this.fragments[i];
            if (i !== 0) {
                result += ", ";
            }
            result += fragment.asFormattedText().text;
        }

        result += "]";

        return new HTMLText({
            text: `<span style="color: #${this.type().color.toString(16)}">${result}</span>`,
        });
    }

    override toString(): string {
        let result = "[";

        for (let i = 0; i < this.fragments.length; i++) {
            const fragment = this.fragments[i];
            if (i !== 0) {
                result += ", ";
            }
            result += fragment.asFormattedText().text;
        }

        result += "]";

        return result;
    }

    override type(): FragmentType {
        return LIST;
    }
}
