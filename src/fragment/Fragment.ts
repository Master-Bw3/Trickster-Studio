import { Point, Text } from "pixi.js";

import SpellPart from "./SpellPart";
import PatternGlyph from "./PatternGlyph.js";
import Pattern from "./Pattern.js";
//@ts-ignore
import * as wasm from "../WasmEndec-1.0-SNAPSHOT/js/endec.js";

export default abstract class Fragment {
    abstract asFormattedText(): Text;

    abstract type(): FragmentType;
}

export abstract class FragmentType {
    abstract decode(object: any): Fragment | null;
}

const fragmentTypes: Map<string, FragmentType> = new Map();

const TYPES = {
    SPELL_PART: register("trickster:spell_part", (object: any) => {
        if (object instanceof wasm.SpellPart) {
            const glyph = decode(object.glyph);
            const subparts: Array<Fragment | null> = object.subParts.map(decode);
            if (glyph != null && subparts.every((x) => x instanceof SpellPart)) {
                return new SpellPart(glyph, subparts);
            }
        }
        return null;
    }),

    PATTERN_GLYPH: register("trickster:pattern_glyph", (object: any) => {
        if (object instanceof wasm.PatternGlyph) {
            const pattern = decode(object.pattern);
            if (pattern instanceof Pattern) {
                return new PatternGlyph(pattern);
            }
        }
        return null;
    }),

    PATTERN: register("trickster:pattern_glyph", (object: any) => {
        if (object instanceof wasm.Pattern) {
            const entries: Array<Point | null> = object.entries.map((x: any) => {
                if (object instanceof wasm.PatternEntry) {
                    return new Point(object.p1, object.p2);
                } else return null;
            });

            if (entries.every((x) => x != null)) {
                return new Pattern(entries);
            }
        }
        return null;
    }),
};

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
        decoded = type.decode(object);
        if (decoded != null) break;
    }

    return decoded;
}

export { fragmentTypes, getKeyByValue, decode, TYPES };
