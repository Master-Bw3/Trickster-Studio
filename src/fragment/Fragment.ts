import { Point, Text } from "pixi.js";

export default abstract class Fragment {
    abstract asFormattedText(): Text;

    abstract type(): FragmentType;
}

export abstract class FragmentType {
    abstract decode(object: any): Fragment | null;
}

const fragmentTypes: Map<string, FragmentType> = new Map();

function register(id: string, decode: (object: any) => Fragment | null): FragmentType {
    const type: FragmentType = { decode: decode };
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
        decoded = type.decode(object)
        if (decoded != null) break;
    }


    return decoded;
}

export { fragmentTypes, getKeyByValue, decode, register };
