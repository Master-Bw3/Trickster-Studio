package me.maplesyrum.tricksterstudio

import io.kvision.core.Container
import io.kvision.core.onEvent
import io.kvision.html.Canvas
import io.kvision.html.div
import io.kvision.state.ObservableValue
import io.kvision.utils.obj
import kotlinx.browser.window
import me.maplesyrum.tricksterstudio.external.pixi.Application
import me.maplesyrum.tricksterstudio.external.pixi.Assets
import me.maplesyrum.tricksterstudio.external.pixi.Texture
import me.maplesyrum.tricksterstudio.spell.fragment.SpellPart
import me.maplesyrum.tricksterstudio.spellEditor.RevisionContext
import me.maplesyrum.tricksterstudio.spellEditor.SpellPartWidget
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise
import kotlin.math.hypot


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

                    app.ticker.add {
                        if (widget.fixedPosition) {
                            widget.size = widget.toScaledSpace(app.canvas.height * (initialScale));
                            widget.x = widget.toScaledSpace(app.canvas.width / 2);
                            widget.y = widget.toScaledSpace(app.canvas.height / 2);
                        }

                        widget.render(app.stage, 0.0, app.canvas.height, textures);

                        app.queueResize();
                    }

                    var pinchZooming = false
                    var lastPinchDistance = 0.0

                    canvas.setEventListener<Canvas> {
                        wheel = { e ->
                            val rect = container.getElement()!!.getBoundingClientRect()
                            val x = e.x - rect.left
                            val y = e.y - rect.top

                            widget.mouseScrolled(x, y, -e.deltaY / 100);

                            app.stage.removeChildren();
                        }
                    }

                    val activeTouches = mutableMapOf<Int, Pair<Double, Double>>()
                    var mouseDown = activeTouches::isNotEmpty

                    canvas.setEventListener<Canvas> {
                        pointerdown = { e ->
                            val rect = container.getElement()!!.getBoundingClientRect()
                            val x = e.x as Double - rect.left
                            val y = e.y as Double - rect.top

                            activeTouches[e.pointerId] = Pair(x, y)

                            if (e.pointerType == "touch" && activeTouches.size == 2) {
                                pinchZooming = true;
                                val touchPoints = activeTouches.values.toList()
                                lastPinchDistance = hypot(
                                    touchPoints[1].first - touchPoints[0].first,
                                    touchPoints[1].second - touchPoints[0].second
                                )
                            } else {
                                pinchZooming = false
                            }

                            widget.dragDrawing = e.pointerType == "touch"

                            val button: Short = e.button;
                            widget.mouseClicked(x, y, button.toInt());
                        }
                    }


                    window.addEventListener("pointerup", {
                        val e = it as PointerEvent

                        val rect = container.getElement()!!.getBoundingClientRect()
                        val x = e.x - rect.left
                        val y = e.y - rect.top


                        if (mouseDown()) {
                            val button = e.button
                            widget.mouseReleased(x, y, button.toInt());
                        }

                        activeTouches.remove(e.pointerId);
                        if (activeTouches.size < 2) {
                            pinchZooming = false;
                        }
                    });

                    window.addEventListener("pointermove", {
                        val e = it as PointerEvent

                        val rect = container.getElement()!!.getBoundingClientRect()
                        val x = e.x - rect.left
                        val y = e.y - rect.top
                        if (pinchZooming && activeTouches.size === 2) {
                            widget.stopDrawing();

                            val previousTouchPoints = activeTouches.toList().map {
                                (id, touch) ->
                                Pair(touch.first, touch.second)
                            }

                            if (activeTouches.containsKey(e.pointerId)) {
                                activeTouches[e.pointerId] = Pair(x, y);
                            }

                            val touchPoints = activeTouches.values.toList()

                            val currentPinchDistance = hypot(
                                    touchPoints[1].first - touchPoints[0].first,
                            touchPoints[1].second - touchPoints[0].second
                            )

                            val zoomFactor = currentPinchDistance / lastPinchDistance;
                            val scrollDelta = (zoomFactor - 1) * 20;

                            widget.mouseScrolled(
                                (touchPoints[0].first + touchPoints[1].first) / 2,
                                (touchPoints[0].second + touchPoints[1].second) / 2,
                                scrollDelta
                            );

                            lastPinchDistance = currentPinchDistance;

                            val prevMidX = (previousTouchPoints[0].first + previousTouchPoints[1].first) / 2;
                            val prevMidY = (previousTouchPoints[0].second + previousTouchPoints[1].second) / 2;

                            val currentMidX = (touchPoints[0].first + touchPoints[1].first) / 2;
                            val currentMidY = (touchPoints[0].second + touchPoints[1].second) / 2;

                            val dragX = currentMidX - prevMidX;
                            val dragY = currentMidY - prevMidY;
                            widget.mouseDragged(
                                (touchPoints[0].first + touchPoints[1].first) / 2,
                                (touchPoints[0].second + touchPoints[1].second) / 2,
                                0,
                                dragX,
                                dragY
                            );
                        } else if (mouseDown()) {
                            if (activeTouches.containsKey(e.pointerId)) {
                                activeTouches[e.pointerId] = Pair(x, y);
                            }

                            val button = e.button;
                            widget.mouseDragged(x, y, button.toInt(), e.asDynamic().movementX, e.asDynamic().movementY);
                        }

                        widget.mouseMoved(x, y);
                    });
                }
            }
        }

    }
    container.add(canvas)

}


