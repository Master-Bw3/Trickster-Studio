package me.maplesyrum.tricksterstudio
import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.panel.splitPanel
import io.kvision.state.MutableState
import io.kvision.state.ObservableValue
import io.kvision.utils.vh
import me.maplesyrum.tricksterstudio.spell.fragment.SpellPart


fun Container.editor() {
    val spellPart = ObservableValue(SpellPart())

    splitPanel(className = "bg-black text-white") {
        height = 100.vh

        sidePanel()
        spellDisplay(spellPart)
    }
}