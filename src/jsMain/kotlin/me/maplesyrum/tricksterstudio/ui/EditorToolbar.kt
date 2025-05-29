package me.maplesyrum.tricksterstudio.ui

import io.kvision.core.Background
import io.kvision.core.Color
import io.kvision.core.Container
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.image
import io.kvision.utils.perc
import io.kvision.utils.px

fun Container.editorToolbar() {
            div(className = "flex gap-8 ml-4 mr-5 h-15 w-60 p-2 rounded-full border-solid border-2 border-zinc-900 justify-center") {
                this.setStyle("margin-left", "auto")
                this.setStyle("margin-right", "auto")
                this.setStyle("backdrop-filter", "blur(4px)")
                background = Background(color = Color.rgba(0, 0, 0, 0xAA))


                button("", className = "hover:bg-zinc-900 p-2 rounded-full aspect-square") {
                    image("modules/assets/icons/RunIcon.svg", className = "invert size-full")
                }
                button("", className = "hover:bg-zinc-900 p-2 rounded-full aspect-square") {
                    image("modules/assets/icons/DebugIcon.svg", className = "invert size-full")
                }
                button("", className = "hover:bg-zinc-900 p-2 rounded-full aspect-square") {
                    image("modules/assets/icons/DownloadIcon.svg", className = "invert size-full")
                }
            }
}