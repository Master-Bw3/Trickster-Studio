// @ts-nocheck

import * as pako from "pako";
import * as wasm from "./WasmEndec-1.0-SNAPSHOT/js/endec";
import Fragment from "./fragment/Fragment";
import SpellPart from "./fragment/SpellPart";
import PatternGlyph from "./fragment/PatternGlyph";
import { Point } from "pixi.js";
import Pattern from "./Pattern";

function decodeSpell(encodedString: string): SpellPart {
    const decoded = wasm.decodeBase64Spell(encodedString);

    return translateType(decoded)
}

function translateType(object): Fragment {
    if (object instanceof wasm.SpellPart) {
        console.log(object)
        return new SpellPart(translateType(object.glyph), object.subParts.map(translateType));

    } else if (object instanceof wasm.PatternGlyph) {
        return new PatternGlyph(translateType(object.pattern));

    } else if (object instanceof wasm.Pattern) {
        return new Pattern(object.entries.map(translateType));

    } else if (object instanceof wasm.PatternEntry) {
        return new Point(object.p1, object.p2);

    }

    throw new Error("unable to convert Fragment");
}

export { decodeSpell };
