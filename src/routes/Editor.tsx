import { Title } from "@solidjs/meta";
import { createSignal } from "solid-js";
import { SpellDisplay } from "~/components/SpellDisplay";
import SpellPart from "~/fragment/SpellPart";
import { decodeSpell } from "~/serialization";
import "../style.css"
import { Sidebar, SidebarContent, SidebarFooter, SidebarGroup, SidebarHeader, SidebarProvider, SidebarTrigger, useSideBar } from "~/components/ui/sidebar";
import { SpellSidebar } from "~/components/EditorSidebar";
import { FilesIcon } from "~/components/icon/FilesIcon";
import { SettingsIcon } from "~/components/icon/SettingsIcon";
import { SaveIcon } from "~/components/icon/SaveIcon";
import { Button } from "~/components/ui/button";
import { EditorToolbar } from "~/components/EditorToolbar";


export default function Editor() {
  const params = new URLSearchParams(window.location.search);
  const encoded = params.get("spell");
  const decoded = encoded === null ? new SpellPart() : decodeSpell(encoded);


  const [spellPart, setSpellPart] = createSignal(decoded)

  const [toggleSidebar, setToggleSidebar] = createSignal(() => { })

  return <div class="flex">
    <div class="w-12 flex flex-col items-center gap-8 p-5">
      <button onClick={() => toggleSidebar()()}>
        <FilesIcon />
      </button>
      <SettingsIcon />
      <SaveIcon />

    </div>
    <SidebarProvider>
      <SpellSidebar setToggleSidebar={setToggleSidebar} />
      <main class="w-full">

        <div id="editor" class="w-full flex flex-col items-center">
          <SpellDisplay spellPart={() => new SpellPart()} setSpellPart={() => { }} fixedPosition={true} class="spell_name"></SpellDisplay>
          <SpellDisplay spellPart={spellPart} initialScale={0.5} setSpellPart={setSpellPart} class="spell"></SpellDisplay>
          <EditorToolbar class="relative" style="top: -8rem"></EditorToolbar>
        </div>
      </main>
    </SidebarProvider>
  </div>
}
