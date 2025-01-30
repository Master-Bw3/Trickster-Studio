import { AbstractEndec, mapOf } from "KEndec"
import { expect, test } from "vitest"
import { gunzipSync, gzipSync } from "zlib"
import * as spellPart from "~/fragment/trickster/spellPart"
import * as patternGlyph from "~/fragment/trickster/patternGlyph"
import * as pattern from "~/fragment/trickster/pattern"
import * as fragment from "~/fragment/fragment"
import { tricksterFragmentEndecs } from "~/fragment/trickster/fragments"
import { EdmSerializer } from "KEndec"
import assert from "assert"
import { EdmElement } from "KEndec/build/dist/js/productionLibrary/KEndec.mjs"


test("serialize", async () => {       
        const spell = spellPart.of(patternGlyph.of(pattern.fromEntries([])), [])
        
        const encoded = fragment.endec(tricksterFragmentEndecs).encodeFully(EdmSerializer.of, spell)!
    
        console.log(encoded.toString())
})

// test("encodeBase64", async () => {
//     //@ts-ignore
//     window.zlib = await(import("https://cdn.jsdelivr.net/npm/pako@2.1.0/+esm"))

//     const spellPart = new SpellPart(new StringFragment(":3"))
    
//     const encoded = spellPart.toBase64()

//     expect(encoded).toBe("Y9F69kUzQHr/BCYrYwYAzR5FuA0AAAA=")


// })

// test("decodeBase64", async () => {
//     //@ts-ignore
//     window.zlib = await(import("https://cdn.jsdelivr.net/npm/pako@2.1.0/+esm"))

//     const encoded = "Y9F69kUzQHr/BCYrYwYAzR5FuA0AAAA="
//     const decoded = SpellPart.fromBase64(encoded)

// })


// test("simpleBase64", async () => {
//     //@ts-ignore
//     window.zlib = await(import("https://cdn.jsdelivr.net/npm/pako@2.1.0/+esm"))

//     const spellPart = new SpellPart(new StringFragment(":3"))
    
//     const encoded = spellPart.toBase64()
//     const decoded = SpellPart.fromBase64(encoded)

//     expect(encoded).toBe("Y9F69kUzQHr/BCYrYwYAzR5FuA0AAAA=")
//     expect(JSON.stringify(decoded)).toBe(JSON.stringify(spellPart))
// })