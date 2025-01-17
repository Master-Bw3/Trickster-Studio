import { Text } from "pixi.js";
import Fragment, { decode, FragmentType, register } from "./Fragment";

//@ts-ignore
import * as wasm from "../WasmEndec-1.0-SNAPSHOT/js/endec.js";

const ENTITY = register("trickster:entity", 0x338888, (object: any) => {
    if (object instanceof wasm.EntityFragment) {
        return new EntityFragment(object.uuid, object.name);
    }
    return null;
});

export default class EntityFragment extends Fragment {
    uuid: string;
    name: string;

    constructor(uuid: string, name: string) {
        super();

        this.uuid = uuid;
        this.name = name;
    }

    override toString(): string {
        const match = this.name.match(/"([^"]+)"/);
        const name = match ? match[1] : "entity";

        return name;
    }

    override type(): FragmentType {
        return ENTITY;
    }
}
