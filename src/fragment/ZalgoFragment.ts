import { Text } from "pixi.js";
import Fragment, { decode, FragmentType, register } from "./Fragment";

//@ts-ignore
import * as wasm from "../WasmEndec-1.0-SNAPSHOT/js/endec.js";

const ZALGO = register("trickster:zalgo", (object: any) => {
    if (object instanceof wasm.ZalgoFragment) {
        return new ZalgoFragment()
    }
    return null;
});

export default class ZalgoFragment extends Fragment {

    constructor() {
        super();
    }

    override asFormattedText(): Text {
        return new Text({
            text: "ỳ̻͕̏͠o̴̢͙ͣ̑̿̓ụ̧̗̰̘̣̯̟̦͙̮ͣ̑̊͑ͥ̈́ͦͫ̆̈̇̀̓̆̌ͯ̏̒̚͡ j̧̨͓̯̹͙̟̖̭̼̀ͯͮ̂̔̋̐ͤͪ̆͘͜͟͜͝ǔ̫_̷̞̞͐͡_̷̛̖͖̲̙͋͗̅ͧ͗̚͜ͅs̵̝͕̰̳̹̬͕͊͊̿͂̑̆̐̓̋̀̒͗̏̍͒͠ͅt̶̸̛͚̯͖͚̜͙̠̹̺͉̺̓̀͐̂̋̊̓̕̕͡ l̝ͨ̆ͭ͢͜͞ͅo͇̖̩͉̗͔͔̾̊͋̄s̵̰̟̮͖̫͚̆̇͋͐́ͯ̄̋̚͠_̫̝_̥̊t̙̣͙̟̺ͭ́̇͆͊̀̔ͣ̆̅̌͞_ ţ̶̶͈͙̫̯̥͚̞̀̂̌̅͂̚͞_̯̠̮͍ͯͤ͊ͣ̋̔̀̂̀͝h̬̩̗͎̱̤͐ͦ̎̂ͮ̋̉̆͢ě̸̡͓̲͍͇͎̬̰̥͕͕̞̜̭̰͎̞͕̝̺̳̑͂ͨ̍͐̊͋̊̓ͮ̎̇̕͘͜͢͟͞͞ ga̶̵̙͇̅̐me̢̡̩̯̻̱̞̫̱̝̊̾̇̀͒͑ͭͫ͆͘̚̕͠",
        });
    }

    override type(): FragmentType {
        return ZALGO;
    }
}
