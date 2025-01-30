import { Text } from "pixi.js";
import Fragment, { FragmentType, register } from "./Fragment";
import { StructEndecBuilder, PrimitiveEndecs } from 'KEndec';


export default class NumberFragment extends Fragment {
    number: number;

    constructor(number: number) {
        super();

        this.number = number;
    }

    override toString(): string {
        return this.number.toFixed(2);
    }

    override type(): FragmentType<NumberFragment> {
        return NUMBER;
    }
}


const NUMBER = register("number", 0xddaa00, 
    StructEndecBuilder.of1(
        PrimitiveEndecs.DOUBLE.fieldOf("number", (fragment: NumberFragment) => fragment.number),
        (number) => new NumberFragment(number)
    )
);