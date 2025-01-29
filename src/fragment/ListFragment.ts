import { HTMLText, Text } from "pixi.js";
import Fragment, { FragmentType, register } from "./Fragment";
import { StructEndecBuilder, PrimitiveEndecs, listOf } from 'KEndec';


export default class ListFragment extends Fragment {
    readonly fragments: ReadonlyArray<Fragment>;

    constructor(fragments: ReadonlyArray<Fragment>) {
        super();

        this.fragments = fragments;
    }

    override asFormattedText(): HTMLText {
        let result = "[";

        for (let i = 0; i < this.fragments.length; i++) {
            const fragment = this.fragments[i];
            if (i !== 0) {
                result += ", ";
            }
            result += fragment.asFormattedText().text;
        }

        result += "]";

        return new HTMLText({
            text: `<span style="color: #${this.type().color.toString(16)}">${result}</span>`,
        });
    }

    override toString(): string {
        let result = "[";

        for (let i = 0; i < this.fragments.length; i++) {
            const fragment = this.fragments[i];
            if (i !== 0) {
                result += ", ";
            }
            result += fragment.asFormattedText().text;
        }

        result += "]";

        return result;
    }

    override type(): FragmentType<ListFragment> {
        return LIST;
    }
}

const LIST = register("list", 0xffffff, 
    StructEndecBuilder.of1(
        Fragment.ENDEC.listOf().fieldOf("fragments", (fragment: ListFragment) => listOf([...fragment.fragments])),
        (list) => new ListFragment(list.asJsReadonlyArrayView())
    )
);
