import { Title } from "@solidjs/meta";
import { createSignal } from "solid-js";
import { SpellDisplay } from "~/components/SpellDisplay";
import SpellPart from "~/fragment/SpellPart";
import { decodeSpell } from "~/serialization";
import "../style.css"

export default function Editor() {
  const params = new URLSearchParams(window.location.search);
  const encoded = params.get("spell");
  const decoded = encoded === null ? new SpellPart() : decodeSpell(encoded);
  

  const [spellPart, setSpellPart] = createSignal(decoded)


  return <>
      <div id="editor">
          <SpellDisplay spellPart={() => new SpellPart()} setSpellPart={() => {}} fixedPosition={true} class="spell_name"></SpellDisplay>
          <SpellDisplay spellPart={spellPart} initialScale={0.5} setSpellPart={setSpellPart} class="spell"></SpellDisplay>
      </div>
  </>
}
