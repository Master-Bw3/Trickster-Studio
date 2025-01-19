import { JSX } from "solid-js"
import { Card } from "./ui/card"
import { RunIcon } from "./icon/RunIcon"
import { DebugIcon } from "./icon/DebugIcon"
import { DownloadIcon } from "./icon/DownloadIcon"


function EditorToolbar(props: JSX.HTMLAttributes<HTMLDivElement>) {
    return <div {...props}>
        <Card class="w-fit rounded-full p-2">
                  <div class="flex gap-8 ml-4 mr-5">
                    <button class="hover:bg-zinc-900 p-2 rounded-full"><RunIcon/></button>
                    <button class="hover:bg-zinc-900 p-2 rounded-full"><DebugIcon/></button>
                    <button class="hover:bg-zinc-900 p-2 rounded-full"><DownloadIcon/></button>
                </div>
        </Card>
    </div>

}

export { EditorToolbar }