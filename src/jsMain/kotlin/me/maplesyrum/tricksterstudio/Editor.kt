package me.maplesyrum.tricksterstudio
import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.panel.splitPanel
import io.kvision.utils.vh


fun Container.editor() {
    splitPanel(className = "bg-black text-white") {

        height = 100.vh
        sidePanel()
        div(className = "bg-black text-white")
    }
}