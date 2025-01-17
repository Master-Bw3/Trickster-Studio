import { Text } from "pixi.js";
import Fragment, { decode, FragmentType, register } from "./Fragment";

//@ts-ignore
import * as wasm from "../WasmEndec-1.0-SNAPSHOT/js/endec.js";

const SLOT = register("trickster:slot", (object: any) => {
    if (object instanceof wasm.SlotFragment) {
        return new SlotFragment(object.slot, object.source);
    }
    return null;
});

type Vector = { x: number; y: number; z: number };

export default class SlotFragment extends Fragment {
    slot: number;
    source: string | Vector | null;

    constructor(slot: number, source: string | Vector | null) {
        super();

        this.slot = slot;
        this.source = source;
    }

    override asFormattedText(): Text {
        return new Text({
            text: this.slot,
        });
    }

    override type(): FragmentType {
        return SLOT;
    }
}
