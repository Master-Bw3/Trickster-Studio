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

    abstract type(): FragmentType;
}

export abstract class FragmentType {
    color: number;

    constructor(color: number) {
        this.color = color;
    }

    abstract decode(object: any): Fragment | null;
}

const fragmentTypes: Map<string, FragmentType> = new Map();

function register(id: string, color: number, decode: (object: any) => Fragment | null): FragmentType {
    const type: FragmentType = { color: color, decode: decode };
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

function decode(object: any): Fragment | null {
    let decoded: Fragment | null = null;

    for (const type of fragmentTypes.values()) {
        decoded = type.decode(object);
        if (decoded != null) break;
    }

    return decoded;
}

export { fragmentTypes, getKeyByValue, decode, register };
