import { Title } from "@solidjs/meta";
import { createSignal } from "solid-js";
import { SpellDisplay } from "~/components/SpellDisplay";
import SpellPart from "~/fragment/SpellPart";
import { decodeSpell } from "~/serialization";
import "../style.css"
import { Sidebar, SidebarContent, SidebarFooter, SidebarGroup, SidebarHeader, SidebarProvider, SidebarTrigger } from "~/components/ui/sidebar";
import { SpellSidebar } from "~/components/EditorSidebar";
import { FilesIcon } from "~/components/icon/FilesIcon";
import { SettingsIcon } from "~/components/icon/SettingsIcon";
import { SaveIcon } from "~/components/icon/SaveIcon";


export default function Editor() {
  const params = new URLSearchParams(window.location.search);
  const encoded = params.get("spell");
  const decoded = encoded === null ? new SpellPart() : decodeSpell(encoded);


  const [spellPart, setSpellPart] = createSignal(decoded)


  return <div class="flex">
  <div class="w-16 flex flex-col items-center gap-10 p-5">
    <FilesIcon/>
    <SettingsIcon/>
    <SaveIcon/>

  </div>
  <SidebarProvider>
    <SpellSidebar />
    <main class="w-full">
    <SidebarTrigger />

      <div id="editor" class="w-full">
        <SpellDisplay spellPart={() => new SpellPart()} setSpellPart={() => { }} fixedPosition={true} class="spell_name"></SpellDisplay>
        <SpellDisplay spellPart={spellPart} initialScale={0.5} setSpellPart={setSpellPart} class="spell"></SpellDisplay>
      </div>
    </main>
  </SidebarProvider>
  </div>
}
