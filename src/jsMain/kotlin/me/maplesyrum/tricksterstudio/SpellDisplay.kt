package me.maplesyrum.tricksterstudio

import io.kvision.core.Container
import io.kvision.html.canvas
import io.kvision.html.coolerCanvas
import io.kvision.html.div
import io.kvision.state.MutableState
import io.kvision.state.ObservableState
import io.kvision.state.ObservableValue
import io.kvision.utils.obj
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import me.maplesyrum.tricksterstudio.external.pixi.Application
import me.maplesyrum.tricksterstudio.external.pixi.Assets
import me.maplesyrum.tricksterstudio.external.pixi.Texture
import me.maplesyrum.tricksterstudio.spell.fragment.SpellPart
import me.maplesyrum.tricksterstudio.spellEditor.RevisionContext
import me.maplesyrum.tricksterstudio.spellEditor.SpellPartWidget
import org.w3c.dom.HTMLCanvasElement
import tree.maple.kendec.util.mapOf
import web.timers.setTimeout
import kotlin.js.Promise


fun Container.spellDisplay(
    spellPart: ObservableValue<SpellPart>,
    fixedPosition: Boolean = false,
    isMutable: Boolean = false,
    initialScale: Double = 1.0,
) {
    val container = div()
    val canvas = coolerCanvas(100, 100)

    canvas.addAfterInsertHook {
        val app = Application()
        app.init(obj {
            this.canvas = it.elm
            resolution = 1
            autoDensity = true
            backgroundAlpha = 0
            width = container.width
            height = container.height
            resizeTo = container.getElement()

        }).then<Unit> {
            Assets.load<dynamic>(obj {
                src = "slkscr.ttf"
                data = obj {
                    family = "slkscr"
                }
            }).then<Unit> {
                val textures = mutableMapOf<String, Texture>()
                Promise.all(
                    arrayOf(
                        Assets.load<Texture>("./circle_48.png").then {
                            textures.put("circle_48", it)
                            it.source.scaleMode = "nearest"
                        },
                        Assets.load<Texture>("./pattern_literal.png").then {
                            textures.put("overlay", it)
                            it.source.scaleMode = "nearest"
                        })
                ).then {
                    val widget = SpellPartWidget(
                        spellPart::getState,
                        spellPart::setState,
                        app.canvas.width / 2,
                        app.canvas.height / 2,
                        app.canvas.height * (initialScale),
                        RevisionContext(),
                        true,
                        fixedPosition,
                        isMutable
                    )

                    //spellPart.subscribe { widget.setSpell(it) }

                    app.ticker.add {
                        if (widget.fixedPosition) {
                            widget.size = widget.toScaledSpace(app.canvas.height * (initialScale));
                            widget.x = widget.toScaledSpace(app.canvas.width / 2);
                            widget.y = widget.toScaledSpace(app.canvas.height / 2);
                        }

                        widget.render(app.stage, 0.0, app.canvas.height, textures);

                        app.queueResize();
                    }
                }
            }
        }

    }
    container.add(canvas)

}


