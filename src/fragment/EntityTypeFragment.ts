import { Text } from "pixi.js";
import Fragment, { decode, FragmentType, register } from "./Fragment";

//@ts-ignore
import * as wasm from "../WasmEndec-1.0-SNAPSHOT/js/endec.js";

const ENTITY_TYPE = register("trickster:entity_type", 0x8877bb, (object: any) => {
    if (object instanceof wasm.EntityTypeFragment) {
        return new EntityTypeFragment(object.entityType);
    }
    return null;
});

export default class EntityTypeFragment extends Fragment {
    entityType: string;

    constructor(entityType: string) {
        super();

        this.entityType = entityType;
    }

    override toString(): string {
        return this.entityType;
    }

    override type(): FragmentType {
        return ENTITY_TYPE;
    }
}
