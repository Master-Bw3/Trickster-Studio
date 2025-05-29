package me.maplesyrum.tricksterstudio.ui

import io.kvision.core.Container
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.image
import io.kvision.panel.gridPanel
import io.kvision.panel.simplePanel
import io.kvision.state.ObservableValue
import io.kvision.utils.perc
import io.kvision.utils.px
import io.kvision.utils.vw
import me.maplesyrum.tricksterstudio.ui.panels.explorer
import me.maplesyrum.tricksterstudio.ui.panels.settings
import io.kvision.state.bind

enum class Panel {
    EXPLORER,
    SETTINGS
}

fun Container.sidePanel() {

    val openPanel = ObservableValue(Panel.EXPLORER)

    gridPanel(className = "bg-black text-white") {
        width = 15.vw
        minWidth = 50.px

        gridTemplateColumns = "50px auto"

        options(1, 1) {
            simplePanel(className = "bg-black") {


                button("", className = "bg-black text-white hover:bg-zinc-900") {
                    height = 50.px
                    width = 100.perc
                    padding = 10.px


                    image("modules/assets/icons/ExplorerIcon.svg", className = "invert")
                }.onClick {
                    openPanel.value = Panel.EXPLORER
                }

                button("", className = "bg-black text-white hover:bg-zinc-900") {
                    height = 50.px
                    width = 100.perc
                    padding = 10.px

                    image("modules/assets/icons/SettingsIcon.svg").setStyle("transform", "scale(1.5)")
                }.onClick {
                    openPanel.value = Panel.SETTINGS
                }

                button("", className = "bg-black text-white hover:bg-zinc-900") {
                    height = 50.px
                    width = 100.perc
                    padding = 10.px

                    image("modules/assets/icons/SaveIcon.svg", className = "invert")
                }.onClick {
                    //openPanel.value = Panel.SETTINGS
                    TODO()
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