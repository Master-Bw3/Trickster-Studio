import { HTMLText, Text } from "pixi.js";
import Fragment, { FragmentType, register } from "./Fragment";
import { StructEndecBuilder, PrimitiveEndecs } from 'KEndec';



import NumberFragment from "./NumberFragment";
import { Vector, vectorEndec } from "~/endecTomfoolery";

export default class VectorFragment extends Fragment {
    vector: Vector;

    constructor(vector: Vector) {
        super();

        this.vector = vector;
    }

    override asFormattedText(): HTMLText {
        const result =
            "(" +
            new NumberFragment(this.vector.x).asFormattedText().text +
            ", " +
            new NumberFragment(this.vector.y).asFormattedText().text +
            ", " +
            new NumberFragment(this.vector.z).asFormattedText().text +
            ")";

        return new HTMLText({
            text: `<span style="color: #${this.type().color.toString(16)}">${result}</span>`,
        });
    }

    override toString(): string {
        return "(" + this.vector.x + ", " + this.vector.y + ", " + this.vector.z + ")";
    }

    override type(): FragmentType<VectorFragment> {
        return VECTOR;
    }
}


const VECTOR = register("vector", 0xffffff, 
    StructEndecBuilder.of1(
        vectorEndec(PrimitiveEndecs.DOUBLE, (x, y, z) => new Vector(x, y, z), (vec) => vec.x, (vec) => vec.y, (vec) => vec.z)
        .fieldOf("vector", (fragment: VectorFragment) => fragment.vector),
        (vector) => new VectorFragment(vector)
    )
);