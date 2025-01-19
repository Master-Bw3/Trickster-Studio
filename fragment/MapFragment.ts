import { HTMLText, Text } from "pixi.js";
import Fragment, { decode, FragmentType, register } from "./Fragment";

//@ts-ignore
import * as wasm from "../WasmEndec-1.0-SNAPSHOT/js/endec";



const MAP = register("trickster:map", 0xffffff, (object: any) => {
    if (object instanceof wasm.MapFragment) {
        const fragments: Array<[Fragment, Fragment] | null> = object.entries.map((entry: Array<any>) => {
            const key = decode(entry[0]);
            const value = decode(entry[1]);
            return key != null && value != null ? [key, value] : null;
        });

        if (fragments.every((x) => x != null)) {
            return new MapFragment(new Map(fragments));
        }
    }
    return null;
});

export default class MapFragment extends Fragment {
    map: Map<Fragment, Fragment>;

    constructor(map: Map<Fragment, Fragment>) {
        super();

        this.map = map;
    }

    override asFormattedText(): HTMLText {
        let out = "{";

        const entries = this.map.entries();
        const iterator = entries[Symbol.iterator]();

        let result = iterator.next();
        while (!result.done) {
            const [key, value] = result.value;
            out += key.asFormattedText().text + ": " + value.asFormattedText().text;

            result = iterator.next();
            if (!result.done) {
                out += ", ";
            }
        }

        out += "}";

        return new HTMLText({
            text: `<span style="color: #${this.type().color.toString(16)}">${out}</span>`,
        });
    }

    override toString(): string {
        let out = "{";

        const entries = this.map.entries();
        const iterator = entries[Symbol.iterator]();

        let result = iterator.next();
        while (!result.done) {
            const [key, value] = result.value;
            out += key.toString() + ": " + value.toString();

            result = iterator.next();
            if (!result.done) {
                out += ", ";
            }
        }

        out += "}";

        return out;
    }

    override type(): FragmentType {
        return MAP;
    }
}
