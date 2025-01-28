import { Text } from "pixi.js";
import Fragment, { FragmentType, fragmentTypes, getKeyByValue, register } from "./Fragment";
import { StructEndecBuilder, PrimitiveEndecs } from 'KEndec';
import { Identifier } from "~/util";
import { lazy } from "~/endecTomfoolery";

export default class TypeFragment extends Fragment {
    typeType: FragmentType<Fragment>;

    constructor(typeType: FragmentType<Fragment>) {
        super();

        this.typeType = typeType;
    }

    override toString(): string {
        return getKeyByValue(fragmentTypes, this.typeType)!.toString();
    }

    override type(): FragmentType<Fragment> {
        return TYPE;
    }
}

const TYPE = register("type", 0x66cc00, 
    lazy(() => StructEndecBuilder.of1(
        Identifier.ENDEC.xmap((id) => fragmentTypes.get(id)!, (type: FragmentType<Fragment>) => type.getId())
        .fieldOf("of_type", (fragment: TypeFragment) => fragment.typeType),
        (type) => new TypeFragment(type)
    ))
);
