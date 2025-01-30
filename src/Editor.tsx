import { createSignal, onMount } from 'solid-js';
import { SpellDisplay } from '~/components/SpellDisplay';
import SpellPart from '~/fragment/old-garbage/SpellPart';
import './style.css';
import { SidebarProvider } from '~/components/ui/sidebar';
import { SpellSidebar } from '~/components/EditorSidebar';
import { FilesIcon } from '~/components/icon/FilesIcon';
import { SettingsIcon } from '~/components/icon/SettingsIcon';
import { SaveIcon } from '~/components/icon/SaveIcon';
import { EditorToolbar } from '~/components/EditorToolbar';
import Pattern from './fragment/old-garbage/Pattern';
import Fragment, { registerAllFragmentTypes } from './fragment/old-garbage/Fragment';

type SpellFolder = {
    [path: string]: SpellData | SpellFolder;
};

type SpellData = {
    name: string;
    pattern: Pattern;
    spell: SpellPart;
};  

type ProgramData = {
    spells: SpellFolder;
    macros: SpellFolder;
};

export default function Editor() {
    registerAllFragmentTypes()


    const [spellPart, setSpellPart] = createSignal(new SpellPart());

    const [toggleSidebar, setToggleSidebar] = createSignal(() => {});

    const [loaded, setLoaded] = createSignal(false)

    onMount(async () => {
        await registerAllFragmentTypes()
        const params = new URLSearchParams(window.location.search);
        const encoded = params.get('spell');
        const decoded = encoded !== null ? Fragment.fromBase64(encoded) : new SpellPart()
        const decodedSpellPart = decoded instanceof SpellPart ? decoded : new SpellPart()
        setSpellPart(decodedSpellPart)
        console.log(spellPart())

        setLoaded(true)
    })

    return (
        <div class="flex">
            <div class="w-12 flex flex-col items-center gap-8 p-5">
                <button onClick={() => toggleSidebar()()}>
                    <FilesIcon />
                </button>
                <SettingsIcon />
                <SaveIcon />
            </div>
            <SidebarProvider>
                <SpellSidebar setToggleSidebar={setToggleSidebar} />
                <main class="w-full overflow-hidden">
                    <div id="editor" class="w-full flex flex-col items-center">
                        <SpellDisplay spellPart={() => new SpellPart()} setSpellPart={() => {}} fixedPosition={true} class="spell_name"></SpellDisplay>
                        <SpellDisplay spellPart={spellPart} initialScale={0.5} setSpellPart={setSpellPart} class="spell"></SpellDisplay>
                        <EditorToolbar class="relative" style="top: -8rem"></EditorToolbar>
                    </div>
                </main>
            </SidebarProvider>
        </div>
    );
}
