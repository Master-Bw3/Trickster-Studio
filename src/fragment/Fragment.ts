import { Endec } from "KEndec";
import { HTMLText, Point, Text } from "pixi.js";

export default abstract class Fragment {
    _formattedText: HTMLText | null = null;
    _formattedTextWidth: number | null = null;

    asFormattedText(): HTMLText {
        return new HTMLText({
            text: `<span style="color: #${this.type().color.toString(16)}">${this.toString()}</span>`,
            style: { fill: 0xffffff },
        });
    }

    asFormattedTextCached(): [HTMLText, number] {
        if (this._formattedText === null) {
            this._formattedText = this.asFormattedText();
            this._formattedTextWidth = this._formattedText.width;
        }

        return [this._formattedText, this._formattedTextWidth!];
    }

    abstract toString(): string;

    abstract type<T>(): FragmentType<any>;

   // abstract encode(): any
}

export class FragmentType<T extends Fragment> {
    endec: Endec<T>;
    color: number;

    constructor(endec: Endec<T>, color: number) {
        this.endec = endec
        this.color = color;
    }
}

const fragmentTypes: Map<string, FragmentType<any>> = new Map();

function register<T extends Fragment>(id: string, endec: Endec<T>, color: number): FragmentType<any> {
    const type = new FragmentType<T>(endec, color)
    fragmentTypes.set(id, type);
    return type;
}

function getKeyByValue<T, U>(map: Map<T, U>, value: U): T | null {
    for (let [key, val] of map.entries()) {
        if (val === value) {
            return key;
        }
    }
    return null;
}


export { fragmentTypes, getKeyByValue, register };
