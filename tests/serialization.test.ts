import { AbstractEndec } from "KEndec"
import { expect } from "vitest"
import { gunzipSync, gzipSync } from "zlib"
import { registerAllFragmentTypes } from "~/fragment/Fragment"
import SpellPart from "~/fragment/SpellPart"
import StringFragment from "~/fragment/StringFragment"


test("encodeBase64", async () => {
    //@ts-ignore
    window.zlib = await(import("https://cdn.jsdelivr.net/npm/pako@2.1.0/+esm"))

    const spellPart = new SpellPart(new StringFragment(":3"))
    
    const encoded = spellPart.toBase64()

    expect(encoded).toBe("Y9F69kUzQHr/BCYrYwYAzR5FuA0AAAA=")


})

test("decodeBase64", async () => {
    //@ts-ignore
    window.zlib = await(import("https://cdn.jsdelivr.net/npm/pako@2.1.0/+esm"))

    const encoded = "Y9F69kUzQHr/BCYrYwYAzR5FuA0AAAA="
    const decoded = SpellPart.fromBase64(encoded)

})


test("simpleBase64", async () => {
    //@ts-ignore
    window.zlib = await(import("https://cdn.jsdelivr.net/npm/pako@2.1.0/+esm"))

    const spellPart = new SpellPart(new StringFragment(":3"))
    
    const encoded = spellPart.toBase64()
    const decoded = SpellPart.fromBase64(encoded)

    expect(encoded).toBe("Y9F69kUzQHr/BCYrYwYAzR5FuA0AAAA=")
    expect(JSON.stringify(decoded)).toBe(JSON.stringify(spellPart))
})