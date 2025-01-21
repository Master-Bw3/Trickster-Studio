// @ts-nocheck

import * as pako from "pako";
import * as wasm from "./WasmEndec-1.0-SNAPSHOT/js/endec";
import Fragment, { decode } from "./fragment/Fragment";
import SpellPart from "./fragment/SpellPart";
import PatternGlyph from "./fragment/PatternGlyph";
import { Point } from "pixi.js";
import Pattern from "./fragment/Pattern";


function decodeSpell(encodedString: string): SpellPart {
    const decoded = wasm.decodeBase64Spell(encodedString);

    const x = wasm.NumberFragment(1.0)

    console.log(x)

    return decode(decoded)
}



export { decodeSpell };

