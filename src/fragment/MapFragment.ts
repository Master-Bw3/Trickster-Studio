import { Text } from "pixi.js";
import Fragment, { decode, FragmentType, register } from "./Fragment";

//@ts-ignore
import * as wasm from "../WasmEndec-1.0-SNAPSHOT/js/endec.js";

const MAP = register("trickster:map", (object: any) => {
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

    override asFormattedText(): Text {
        let out = "{";

        const entries = this.map.entries();
        const iterator = entries[Symbol.iterator]();

        let result = iterator.next();
        while (!result.done) {
            const [key, value] = result.value;
            out += key.asFormattedText() + ": " + value.asFormattedText();

            result = iterator.next();
            if (!result.done) {
                out += ", ";
            }
        }

        out += "}";

        return new Text({
            text: out,
        });
    }

    override type(): FragmentType {
        return MAP;
    }
}
