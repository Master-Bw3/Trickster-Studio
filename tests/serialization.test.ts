import { AbstractEndec } from "KEndec"
import { expect } from "vitest"
import { gunzipSync, gzipSync } from "zlib"
import SpellPart from "~/fragment/SpellPart"
import StringFragment from "~/fragment/StringFragment"


// test("simpleBase64", () => {


//     const spellPart = new SpellPart(new StringFragment(":3"))
    
//     const encoded = spellPart.toBase64()
//     const decoded = SpellPart.fromBase64(encoded)

//     expect(encoded).toBe("Y9F69kUzQHr/BCYrYwYAzR5FuA0AAAA=")
//     expect(JSON.stringify(decoded)).toBe(JSON.stringify(spellPart))
// })


test("encodeBase64", () => {
    const spellPart = new SpellPart(new StringFragment(":3"))
    
    const encoded = spellPart.toBase64()

    expect(encoded).toBe("Y9F69kUzQHr/BCYrYwYAzR5FuA0AAAA=")


})

test("decodeBase64", () => {

    const encoded = "Y9F69kUzQHr/BCYrYwYAzR5FuA0AAAA="
    const decoded = SpellPart.fromBase64(encoded)

})

