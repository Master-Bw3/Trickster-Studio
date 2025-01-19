import { HTMLText, Text } from "pixi.js";
import Fragment, { decode, FragmentType, fragmentTypes, register } from "./Fragment";

//@ts-ignore
import * as wasm from "../WasmEndec-1.0-SNAPSHOT/js/endec";


import NumberFragment from "./NumberFragment";

const SLOT = register("trickster:slot", 0x77aaee, (object: any) => {
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

    override asFormattedText(): HTMLText {
        const entityColor = fragmentTypes.get("trickster:entity")!.color;
        const numColor = fragmentTypes.get("trickster:number")!.color;
        const coloredInt = (value: number) => `<span style="color: #${numColor.toString(16)}">${value}</span>`;

        var sourceText;
        if (typeof this.source === "string") {
            sourceText = `<span style="color: #${entityColor.toString(16)}">${this.source}</span>`;
        } else if (this.source != null) {
            sourceText = `(${coloredInt(this.source.x)}, ${coloredInt(this.source.y)}, ${coloredInt(this.source.z)})`;
        } else {
            sourceText = `<span style="color: #${entityColor.toString(16)}">Caster</span>`;
        }

        const slotText = coloredInt(this.slot);

        return new HTMLText({
            text: `<span style="color: #${this.type().color.toString(16)}">slot ${slotText} at ${sourceText}</span>`,
        });
    }

    override toString(): string {
        var sourceText = "Caster";
        if (typeof this.source === "string") {
            sourceText = this.source;
        } else if (this.source != null) {
            sourceText = `(${this.source.x}, ${this.source.y}, ${this.source.z})`;
        }

        return `slot ${this.slot} at ${sourceText}`;
    }

    override type(): FragmentType {
        return SLOT;
    }
}
