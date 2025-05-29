package me.maplesyrum.tricksterstudio.ui
import io.kvision.core.Container
import io.kvision.core.CssSize
import io.kvision.core.Overflow
import io.kvision.core.style
import io.kvision.html.div
import io.kvision.panel.splitPanel
import io.kvision.state.ObservableValue
import io.kvision.utils.asString
import io.kvision.utils.rem
import io.kvision.utils.vh
import me.maplesyrum.tricksterstudio.spell.fragment.SpellPart


fun Container.editor() {
    val spellPart = ObservableValue(SpellPart())

    splitPanel(className = "bg-black text-white") {
        height = 100.vh

        sidePanel()
        div() {
            overflow = Overflow.HIDDEN

            spellDisplay(spellPart, isMutable = true, initialScale = 0.5, className = "h-full")
            div(className = "relative") {
                style {
                    top = (-8).rem

                }

                editorToolbar()
            }
        }
    }
}