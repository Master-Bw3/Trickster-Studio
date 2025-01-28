import { Text } from "pixi.js";
import Fragment, { FragmentType, register } from "./Fragment";
import { StructEndecBuilder, PrimitiveEndecs } from 'KEndec';
import { Identifier } from "~/util";






export default class EntityTypeFragment extends Fragment {
    entityType: string;

    constructor(entityType: string) {
        super();

        this.entityType = entityType;
    }

    override toString(): string {
        return this.entityType;
    }

    override type(): FragmentType<EntityTypeFragment> {
        return ENTITY_TYPE;
    }
}

const ENTITY_TYPE = register("entity_type", 0x8877bb, 
    StructEndecBuilder.of1(
        Identifier.ENDEC
            .xmap((ident) => ident.toString(), Identifier.of)
            .fieldOf("entityType", (fragment: EntityTypeFragment) => fragment.entityType),
        (entityType) => new EntityTypeFragment(entityType)
    )
);
