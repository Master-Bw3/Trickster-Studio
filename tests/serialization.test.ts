import { AbstractEndec } from "KEndec"
import { gunzipSync, gzipSync } from "zlib"
import { registerFragments } from "~/fragment/registerFragments"
import SpellPart from "~/fragment/SpellPart"
import StringFragment from "~/fragment/StringFragment"


test("simpleBase64", () => {

    registerFragments()

    const spellPart = new SpellPart(new StringFragment(":3"))
    
    const encoded = spellPart.toBase64()
    const decoded = SpellPart.fromBase64(encoded)


    expect(JSON.stringify(decoded)).toBe(JSON.stringify(spellPart))
})
