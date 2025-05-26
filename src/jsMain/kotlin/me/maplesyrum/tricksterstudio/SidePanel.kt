package me.maplesyrum.tricksterstudio

import io.kvision.core.Background
import io.kvision.core.Container
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.image
import io.kvision.panel.gridPanel
import io.kvision.panel.simplePanel
import io.kvision.state.ObservableValue
import io.kvision.utils.perc
import io.kvision.utils.px
import io.kvision.utils.vw
import me.maplesyrum.tricksterstudio.panels.explorer
import me.maplesyrum.tricksterstudio.panels.settings
import io.kvision.state.bind

enum class Panel {
    EXPLORER,
    SETTINGS
}

fun Container.sidePanel() {

    val openPanel = ObservableValue(Panel.EXPLORER)

    gridPanel(className = "bg-black text-white") {
        width = 20.vw
        minWidth = 50.px

        gridTemplateColumns = "50px auto"

        options(1, 1) {
            simplePanel(className = "bg-black") {


                button("", className = "bg-black text-white hover:bg-zinc-900") {
                    height = 50.px
                    width = 100.perc
                    padding = 5.px


                    image("./modules/assets/icons/ExplorerIcon.svg", className = "invert")
                }.onClick {
                    openPanel.value = Panel.EXPLORER
                }

                button("", className = "bg-black text-white hover:bg-zinc-900") {
                    height = 50.px
                    width = 100.perc
                    padding = 5.px

                    image("./modules/assets/icons/DebugIcon.svg", className = "invert")
                }.onClick {
                    openPanel.value = Panel.SETTINGS
                }
            }
        }

        options(2, 1) {
            div(className = "border-solid border-l-2 border-zinc-900").bind(openPanel) { openPanel ->
                when (openPanel) {
                    Panel.EXPLORER -> explorer()
                    Panel.SETTINGS -> settings()
                }
            }
        }
    }

}