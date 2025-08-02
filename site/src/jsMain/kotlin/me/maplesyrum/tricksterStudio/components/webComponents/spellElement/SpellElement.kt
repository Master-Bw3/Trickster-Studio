package me.maplesyrum.tricksterStudio.components.webComponents.spellElement

import js.core.JsAny
import kotlinx.browser.window
import me.maplesyrum.tricksterStudio.external.pixi.Application
import me.maplesyrum.tricksterStudio.external.pixi.Assets
import me.maplesyrum.tricksterStudio.external.pixi.Texture
import me.maplesyrum.tricksterStudio.jsObject
import me.maplesyrum.tricksterStudio.spell.fragment.SpellPart
import web.components.CustomElement
import web.components.ShadowRoot
import web.components.ShadowRootInit
import web.components.ShadowRootMode
import kotlin.math.hypot
import web.html.HTMLElement
import kotlinx.browser.document
import me.maplesyrum.tricksterStudio.DefaultedAttribute
import web.events.EventHandler
import web.events.EventType
import web.events.addEventListener
import web.html.HTMLCanvasElement
import web.html.HTMLDivElement
import web.uievents.MouseButton
import web.uievents.WheelEvent
import web.uievents.PointerEvent
import web.window.Window
import kotlin.js.Promise

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("SpellElement")
class SpellElement : HTMLElement(), CustomElement.WithCallbacks {

    var spellPart: SpellPart = SpellPart()
    var onUpdateSpellPart: (SpellPart) -> Unit = {}

    private val shadow: ShadowRoot = this.attachShadow(ShadowRootInit(mode = ShadowRootMode.closed))
    private val app: Application by lazy { Application() }
    private lateinit var widget: SpellPartWidget

    private var hasFixedPosition: Boolean by DefaultedAttribute("fixed", String::toBoolean, false)
    private var isMutable: Boolean by DefaultedAttribute("mutable", String::toBoolean, false)
    private var initialScale: Double by DefaultedAttribute("scale", String::toDouble, 1.0)

    override fun connectedCallback() {
        val containerElement = document.createElement("div") as HTMLDivElement
        val canvasElement = document.createElement("canvas") as HTMLCanvasElement
        canvasElement.height = 100
        canvasElement.width = 100

        containerElement.appendChild(canvasElement)
        shadow.appendChild(containerElement)

        canvasElement.style.setProperty("width", "100%")
        canvasElement.style.setProperty("height", "100%")

        app.init(jsObject {
            canvas = canvasElement
            resolution = 1
            autoDensity = true
            backgroundAlpha = 0
            width = containerElement.getBoundingClientRect().width
            height = containerElement.getBoundingClientRect().height
            resizeTo = containerElement
            preference = "webgpu"
        }).then {
            loadFont()
        }.then {
            loadTextures()
        }.then { textures ->
            setupWidget(textures)
            setupInputHandlers(canvasElement, containerElement)
        }
    }

    private fun loadFont(): Promise<dynamic> {
        return Assets.load<dynamic>(
            jsObject {
                src = "/slkscr.ttf"
                data = jsObject {
                    family = "slkscr"
                }
            }
        )
    }

    private fun loadTextures(): Promise<MutableMap<String, Texture>> {
        val textures = mutableMapOf<String, Texture>()
        return Assets.load<Texture>("/circle_48.png").then { circleTexture ->
            textures["circle_48"] = circleTexture
        }.then {
            Assets.load<Texture>("/pattern_literal.png").then { overlayTexture ->
                textures["overlay"] = overlayTexture
            }
        }.then {
            textures
        }
    }

    private fun setupWidget(
        textures: MutableMap<String, Texture>
    ) {
        widget = SpellPartWidget(
            this@SpellElement::spellPart::get,
            this@SpellElement::updateSpellPart,
            app.canvas.width / 2,
            app.canvas.height / 2,
            app.canvas.height * (initialScale),
            RevisionContext(),
            true,
            hasFixedPosition,
            isMutable
        )

        app.ticker.add {
            if (widget.fixedPosition) {
                widget.size = widget.toScaledSpace(app.canvas.height * (initialScale))
                widget.x = widget.toScaledSpace(app.canvas.width / 2)
                widget.y = widget.toScaledSpace(app.canvas.height / 2)
            }

            console.log("tick")

            widget.render(app.stage, 0.0, app.canvas.height, textures)

            app.queueResize()
        }
    }

    private fun setupInputHandlers(canvasElement: HTMLCanvasElement, containerElement: HTMLDivElement) {
        var pinchZooming = false
        var lastPinchDistance = 0.0

        canvasElement.addEventListener(EventType("wheel"), EventHandler { e: WheelEvent ->
            val rect = containerElement.getBoundingClientRect()
            val x = e.x - rect.left
            val y = e.y - rect.top

            widget.mouseScrolled(x, y, -e.deltaY / 100)

            app.stage.removeChildren()
        })

        val activeTouches = mutableMapOf<Int, Pair<Double, Double>>()
        var mouseDown = activeTouches::isNotEmpty

        canvasElement.addEventListener(EventType("pointerdown"), EventHandler { e: PointerEvent ->
            val rect = containerElement.getBoundingClientRect()
            val x = e.x as Double - rect.left
            val y = e.y as Double - rect.top

            activeTouches[e.pointerId] = Pair(x, y)

            if (e.pointerType == "touch" && activeTouches.size == 2) {
                pinchZooming = true
                val touchPoints = activeTouches.values.toList()
                lastPinchDistance = hypot(
                    touchPoints[1].first - touchPoints[0].first,
                    touchPoints[1].second - touchPoints[0].second
                )
            } else {
                pinchZooming = false
            }

            widget.dragDrawing = e.pointerType == "touch"

            val button: MouseButton = e.button

            widget.mouseClicked(x, y, button.toInt())
        })

        window.unsafeCast<Window>().addEventListener(EventType("pointerup"), EventHandler { e: PointerEvent ->

            val rect = containerElement.getBoundingClientRect()
            val x = e.x - rect.left
            val y = e.y - rect.top

            if (mouseDown()) {
                val button = e.button
                widget.mouseReleased(x, y, button.toInt())
            }

            activeTouches.remove(e.pointerId)
            if (activeTouches.size < 2) {
                pinchZooming = false
            }
        })

        window.unsafeCast<Window>().addEventListener(EventType("pointermove"), { e: PointerEvent ->

            val rect = containerElement.getBoundingClientRect()
            val x = e.x - rect.left
            val y = e.y - rect.top
            if (pinchZooming && activeTouches.size == 2) {
                widget.stopDrawing()

                val previousTouchPoints = activeTouches.toList().map { (_, touch) ->
                    Pair(touch.first, touch.second)
                }

                if (activeTouches.containsKey(e.pointerId)) {
                    activeTouches[e.pointerId] = Pair(x, y)
                }

                val touchPoints = activeTouches.values.toList()

                val currentPinchDistance = hypot(
                    touchPoints[1].first - touchPoints[0].first,
                    touchPoints[1].second - touchPoints[0].second
                )

                val zoomFactor = currentPinchDistance / lastPinchDistance
                val scrollDelta = (zoomFactor - 1) * 20

                widget.mouseScrolled(
                    (touchPoints[0].first + touchPoints[1].first) / 2,
                    (touchPoints[0].second + touchPoints[1].second) / 2,
                    scrollDelta
                )

                lastPinchDistance = currentPinchDistance

                val prevMidX = (previousTouchPoints[0].first + previousTouchPoints[1].first) / 2
                val prevMidY = (previousTouchPoints[0].second + previousTouchPoints[1].second) / 2

                val currentMidX = (touchPoints[0].first + touchPoints[1].first) / 2
                val currentMidY = (touchPoints[0].second + touchPoints[1].second) / 2

                val dragX = currentMidX - prevMidX
                val dragY = currentMidY - prevMidY
                widget.mouseDragged(
                    (touchPoints[0].first + touchPoints[1].first) / 2,
                    (touchPoints[0].second + touchPoints[1].second) / 2,
                    0,
                    dragX,
                    dragY
                )
            } else if (mouseDown()) {
                if (activeTouches.containsKey(e.pointerId)) {
                    activeTouches[e.pointerId] = Pair(x, y)
                }

                val button = e.button
                widget.mouseDragged(x, y, button.toInt(), e.asDynamic().movementX, e.asDynamic().movementY)
            }

            widget.mouseMoved(x, y)
        })
    }

    override fun disconnectedCallback() {
        app.destroy()
    }

    override fun adoptedCallback() {
    }

    override fun attributeChangedCallback(name: String, oldValue: JsAny?, newValue: JsAny?) {
        widget.setMutable(isMutable)
        widget.fixedPosition = hasFixedPosition
    }

    private fun updateSpellPart(spellPart: SpellPart) {
        this.spellPart = spellPart
        onUpdateSpellPart(spellPart)
    }
}

fun MouseButton.toInt(): Int {
    return when (this) {
        MouseButton.MAIN -> 0
        MouseButton.SECONDARY -> 1
    }
}