package me.maplesyrum.tricksterStudio.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.Height
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.Width
import com.varabyte.kobweb.compose.css.height
import com.varabyte.kobweb.compose.css.width
import com.varabyte.kobweb.compose.dom.ref
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.position
import com.varabyte.kobweb.compose.ui.modifiers.top
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.Page
import me.maplesyrum.tricksterStudio.components.webComponents.spellElement.SpellElement
import me.maplesyrum.tricksterStudio.spell.fragment.Fragment
import me.maplesyrum.tricksterStudio.spell.fragment.SpellPart
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.css.vw
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.TagElement
import org.w3c.dom.Element

@Page
@Composable
fun Editor() {
    var spellPart: SpellPart by remember { mutableStateOf(Fragment.fromBase64("tVZBSsNAFP0zTaUNVWLpIqALEQ/g2o2DuMiyeAGp0kUxLSGJ+1FQEnDjDWK3buoNepaewCM4rYtJmD+/ScWQTCD/zZ/333+TxBmk8eT+IUnH8UUSjcPwNhrF6aF+GI1SdZstAt5mrA8ALXUBYyYEroWr42jemosBcHWy1pZs5sTVs/hcT+DAehTVr+CqTcVvsiU5f0OwPDSjOQxEztg5tcKiI1+oeCHFE83Qb87rt8l05dub7OmHs8fp3TiG0rGDWnPJlRn2aFaSjlf6xboUloHIddwo5vIbrQVbU1Q90hBuRXrZ8rWxiKIm5/p0caShlzir6kWnfX+TlPgWIyHldoA0RD+TJZv/r4Y7WH7tQc6OqGXmADlNw6dpCqOWP/YuypYOFS9kmfGBjoeTJK2jq0CJ7GvkdBS5mJjcvo2RLqs3q6kE2OCg0pvwYxu84PBhtTXaZdfMPrDBFZeuCT+1wp0WmHDPSkaK3ISf2LljQnbs2RFlhI84UL3BS78GdXegdWj47e5tPg9bzDoEyetS3PUXRw8/rvky+sUJAAA=") as SpellPart) }

    Row(Modifier.height(100.vw)) {
        //SidePanel()

        Div(Modifier.overflow(Overflow.Hidden).toAttrs()) {
            TagElement<Element>(
                "spell-editor",
                {
                    attr("mutable", "true")
                    attr("fixed", "false")
                    attr("scale", "0.5")
                    style {
                        height(Height.of(100.vh))
                        width(Width.of(100.vw))
                        display(DisplayStyle.Block)
                    }
                }
                ) {

                ref(spellPart) { element: Element ->
                    val spellElement = element.unsafeCast<SpellElement>()

                    spellElement.spellPart = spellPart
                    spellElement.onUpdateSpellPart = { spellPart = it }
                }
            }


            Div(Modifier
                .position(Position.Relative)
                .top((-8).cssRem)
                .toAttrs()) {


                //editorToolbar()
            }
        }
    }
}