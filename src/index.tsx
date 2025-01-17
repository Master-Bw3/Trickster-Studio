import { Application, Assets, Container, HTMLTextStyle, Matrix, SCALE_MODES, Sprite, Texture } from "pixi.js";
import SpellPartWidget from "./SpellPartWidget";
import SpellPart from "./fragment/SpellPart";
import PatternGlyph from "./fragment/PatternGlyph";
import RevisionContext from "./RevisionContext";
import { decodeSpell } from "./serialization";
import "./fragment/BlockTypeFragment";
import "./fragment/BooleanFragment";
import "./fragment/DimensionFragment";
import "./fragment/EntityFragment";
import "./fragment/EntityTypeFragment";
import "./fragment/ItemTypeFragment";
import "./fragment/ListFragment";
import "./fragment/MapFragment";
import "./fragment/NumberFragment";
import "./fragment/Pattern";
import "./fragment/PatternGlyph";
import "./fragment/SlotFragment";
import "./fragment/SpellPart";
import "./fragment/StringFragment";
import "./fragment/TypeFragment";
import "./fragment/VectorFragment";
import "./fragment/VoidFragment";
import "./fragment/ZalgoFragment";
import { SpellDisplay } from "./SpellDisplay";


import { render } from "solid-js/web";
import { CounterProvider, useCounter } from "./test/counter-store";
import { MetaProvider, Title } from "@solidjs/meta";
import { Router } from "@solidjs/router"


const params = new URLSearchParams(window.location.search);
const encoded = params.get("spell");
const decoded = encoded === null ? new SpellPart() : decodeSpell(encoded);

const App = () => (
    <>
        <div id="editor">
            <SpellDisplay spellPart={decoded} id="spell"></SpellDisplay>
            <SpellDisplay spellPart={decoded} id="spell_name"></SpellDisplay>
        </div>
    </>
);

render(App, document.body);

(async () => {


    // try {
    //     if (encoded == null) throw new Error();
    //     const decoded = decodeSpell(encoded);
    //     await SpellDisplay(document.getElementById("spellDisplay") as HTMLDivElement, decoded);
    // } catch (e: any) {
    //     console.error(e)
    //     await SpellDisplay(document.getElementById("spellDisplay") as HTMLDivElement, new SpellPart());

    // }
})();

