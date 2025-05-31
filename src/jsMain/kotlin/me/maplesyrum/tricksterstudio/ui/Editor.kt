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
import me.maplesyrum.tricksterstudio.spell.fragment.Fragment
import me.maplesyrum.tricksterstudio.spell.fragment.Pattern
import me.maplesyrum.tricksterstudio.spell.fragment.SpellPart


fun Container.editor() {
    val spellPart = ObservableValue(Fragment.fromBase64("tVZBSsNAFP0zTaUNVWLpIqALEQ/g2o2DuMiyeAGp0kUxLSGJ+1FQEnDjDWK3buoNepaewCM4rYtJmD+/ScWQTCD/zZ/333+TxBmk8eT+IUnH8UUSjcPwNhrF6aF+GI1SdZstAt5mrA8ALXUBYyYEroWr42jemosBcHWy1pZs5sTVs/hcT+DAehTVr+CqTcVvsiU5f0OwPDSjOQxEztg5tcKiI1+oeCHFE83Qb87rt8l05dub7OmHs8fp3TiG0rGDWnPJlRn2aFaSjlf6xboUloHIddwo5vIbrQVbU1Q90hBuRXrZ8rWxiKIm5/p0caShlzir6kWnfX+TlPgWIyHldoA0RD+TJZv/r4Y7WH7tQc6OqGXmADlNw6dpCqOWP/YuypYOFS9kmfGBjoeTJK2jq0CJ7GvkdBS5mJjcvo2RLqs3q6kE2OCg0pvwYxu84PBhtTXaZdfMPrDBFZeuCT+1wp0WmHDPSkaK3ISf2LljQnbs2RFlhI84UL3BS78GdXegdWj47e5tPg9bzDoEyetS3PUXRw8/rvky+sUJAAA=") as SpellPart)

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