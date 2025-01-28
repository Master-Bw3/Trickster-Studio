import { HTMLText, Text } from "pixi.js";
import Fragment, { FragmentType, register } from "./Fragment";
import { StructEndecBuilder, PrimitiveEndecs, KtList, mapEndecOf, KtMap } from 'KEndec';


export default class MapFragment extends Fragment {
    map: ReadonlyMap<Fragment, Fragment>;

    constructor(map: ReadonlyMap<Fragment, Fragment>) {
        super();

        this.map = map;
    }

    override asFormattedText(): HTMLText {
        let out = "{";

        const entries = this.map.entries();
        const iterator = entries[Symbol.iterator]();

        let result = iterator.next();
        while (!result.done) {
            const [key, value] = result.value;
            out += key.asFormattedText().text + ": " + value.asFormattedText().text;

            result = iterator.next();
            if (!result.done) {
                out += ", ";
            }
        }

        out += "}";

        return new HTMLText({
            text: `<span style="color: #${this.type().color.toString(16)}">${out}</span>`,
        });
    }

    override toString(): string {
        let out = "{";

        const entries = this.map.entries();
        const iterator = entries[Symbol.iterator]();

        let result = iterator.next();
        while (!result.done) {
            const [key, value] = result.value;
            out += key.toString() + ": " + value.toString();

            result = iterator.next();
            if (!result.done) {
                out += ", ";
            }
        }

        out += "}";

        return out;
    }

    override type(): FragmentType<MapFragment> {
        return MAP;
    }
}

const MAP = register("map", 0xffffff, 
    StructEndecBuilder.of1(
        mapEndecOf(Fragment.ENDEC, Fragment.ENDEC).fieldOf("entries", (fragment: MapFragment) => KtMap.getInstance().fromJsMap(fragment.map)),
        (map) => new MapFragment(map.asJsReadonlyMapView())
    )
);
