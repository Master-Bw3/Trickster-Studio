import { Text } from "pixi.js";
import Fragment, { FragmentType, register } from "./Fragment";
import { StructEndecBuilder, BuiltInEndecs, uuidToString, uuidFromString, PrimitiveEndecs } from 'KEndec';


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

    override type(): FragmentType<EntityFragment> {
        return ENTITY;
    }
}

const ENTITY = register("entity", 0x338888, 
    StructEndecBuilder.of2(
        BuiltInEndecs.Uuid
            .xmap(uuidToString, uuidFromString)
            .fieldOf("uuid", (fragment: EntityFragment) => fragment.uuid),
        PrimitiveEndecs.STRING.fieldOf("name", (fragment: EntityFragment) => fragment.name),
        (uuid, name) => new EntityFragment(uuid, name)
    )
);