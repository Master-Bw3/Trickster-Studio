package me.maplesyrum.tricksterstudio.spellEditor

import me.maplesyrum.tricksterstudio.external.pixi.*;
import me.maplesyrum.tricksterstudio.fragment.SpellPart
import me.maplesyrum.tricksterstudio.fragment.Fragment
import me.maplesyrum.tricksterstudio.fragment.PatternGlyph
import me.maplesyrum.tricksterstudio.fragment.Pattern
import me.maplesyrum.tricksterstudio.fragment.fragmentTypes
import me.maplesyrum.tricksterstudio.fragment.getKeyByValue
import me.maplesyrum.tricksterstudio.fragment.patternOf
import me.maplesyrum.tricksterstudio.FragmentRenderer.fragmentRenderers
import me.maplesyrum.tricksterstudio.spellEditor.spellRenderingUtils.isCircleClickable

const val PATTERN_TO_PART_RATIO = 2.5
const val PART_PIXEL_RADIUS = 48
const val CLICK_HITBOX_SIZE = 6

class SpellCircleRenderer(
    val drawingPartGetter: () -> SpellPart?,
    val drawingPatternGetter: () -> List<Int>?,
    var precisionOffset: Double,
    var animated: Boolean
) {
    var inUI: Boolean = true
    var inEditor: Boolean = true
    var mouseX: Double = Double.MAX_VALUE
    var mouseY: Double = Double.MAX_VALUE

    var r: Double = 1.0
    var g: Double = 1.0
    var b: Double = 1.0
    var circleTransparency: Double = 1.0

    fun setMousePosition(mouseX: Double, mouseY: Double) {
        this.mouseX = mouseX
        this.mouseY = mouseY
    }

    fun setColor(r: Double, g: Double, b: Double) {
        this.r = r
        this.g = g
        this.b = b
    }

    fun toLocalSpace(value: Double): Double = value * precisionOffset

    fun renderPart(
        container: Container,
        entry: SpellPart,
        x: Double,
        y: Double,
        size: Double,
        startingAngle: Double,
        delta: Double,
        alphaGetter: (Double) -> Double,
        textures: Map<String, Texture>
    ) {
        val alpha = alphaGetter(toLocalSpace(size))

        val circle = Sprite(textures["circle_48"]!!)
        circle.x = toLocalSpace(x - size / 2)
        circle.y = toLocalSpace(y - size / 2)
        circle.width = toLocalSpace(size)
        circle.height = toLocalSpace(size)
        circle.alpha = alpha

        container.addChild(circle)

        drawGlyph(container, entry, x, y, size, startingAngle, delta, alphaGetter, textures)

        val partCount = entry.subParts.size

        drawDivider(container, toLocalSpace(x), toLocalSpace(y), startingAngle, toLocalSpace(size), partCount, alpha)

        entry.getSubParts().forEachIndexed { i, child ->
            val angle = startingAngle + ((2 * Math.PI) / partCount) * i - Math.PI / 2
            val nextX = x + size * kotlin.math.cos(angle) * 0.5
            val nextY = y + size * kotlin.math.sin(angle) * 0.5
            val nextSize = minOf(size / 2, size / ((partCount + 1) / 2.0))
            renderPart(container, child, nextX, nextY, nextSize, angle, delta, alphaGetter, textures)
        }
    }

    fun drawDivider(
        container: Container,
        x: Double,
        y: Double,
        startingAngle: Double,
        size: Double,
        partCount: Int,
        alpha: Double
    ) {
        val pixelSize = size / PART_PIXEL_RADIUS
        val lineAngle = startingAngle + ((2 * Math.PI) / partCount) * -0.5 - Math.PI / 2

        val lineX = x + size * kotlin.math.cos(lineAngle) * 0.55
        val lineY = y + size * kotlin.math.sin(lineAngle) * 0.55

        val toCenter = Point(lineX - x, lineY - y)
        val toCenterScaled = normalize(toCenter)

        val lineEnd = Point(lineX - toCenterScaled.x * 8 * pixelSize, lineY - toCenterScaled.y * 8 * pixelSize)

        val g = Graphics()
        g.poly(arrayOf(Point(lineX, lineY), lineEnd))
        g.stroke(StrokeOptions(width = 1 * pixelSize, color = arrayOf(0.5 * r, 0.5 * g, 1 * b, alpha * 0.2)))
        container.addChild(g)
    }

    fun drawGlyph(
        container: Container,
        parent: SpellPart,
        x: Double,
        y: Double,
        size: Double,
        startingAngle: Double,
        delta: Double,
        alphaGetter: (Double) -> Double,
        textures: Map<String, Texture>
    ) {
        val glyph = parent.glyph
        when (glyph) {
            is SpellPart -> renderPart(container, glyph, x, y, size / 3, startingAngle, delta, alphaGetter, textures)
            else -> drawSide(container, parent, toLocalSpace(x), toLocalSpace(y), toLocalSpace(size), alphaGetter, textures, glyph)
        }
    }

    fun drawSide(
        container: Container,
        parent: SpellPart,
        x: Double,
        y: Double,
        size: Double,
        alphaGetter: (Double) -> Double,
        textures: Map<String, Texture>,
        glyph: Fragment
    ) {
        val alpha = alphaGetter(size)
        val patternSize = size / PATTERN_TO_PART_RATIO
        val pixelSize = patternSize / PART_PIXEL_RADIUS

        if (glyph is PatternGlyph || glyph is Pattern) {
            val pattern = when (glyph) {
                is Pattern -> {
                    val overlay = Sprite(textures["overlay"]!!)
                    overlay.x = x - size / 2
                    overlay.y = y - size / 2
                    overlay.width = size
                    overlay.height = size
                    overlay.alpha = alpha
                    container.addChild(overlay)
                    glyph
                }
                is PatternGlyph -> glyph.pattern
                else -> null
            } ?: return

            val isDrawing = inEditor && drawingPartGetter() == parent
            val drawingPattern = if (inEditor) drawingPatternGetter() else null
            val patternList = if (isDrawing) patternOf(drawingPattern!!)!! else pattern

            for (i in 0 until 9) {
                val pos = getPatternDotPosition(x, y, i, patternSize)
                val isLinked = if (isDrawing) drawingPattern?.contains(i) == true else patternList.contains(i)
                var dotScale = 1.0

                if (inEditor && isInsideHitbox(pos, pixelSize, mouseX, mouseY) && isCircleClickable(size)) {
                    dotScale = 1.6
                } else if (!isLinked) {
                    if (inEditor && isCircleClickable(size)) {
                        val mouseDistance = magnitude(Point(mouseX - pos.x, mouseY - pos.y))
                        dotScale = minOf(maxOf(patternSize / mouseDistance - 0.2, 0.0), 1.0)
                    } else {
                        continue
                    }
                }

                val dotSize = pixelSize * dotScale
                if (dotSize > 1) {
                    val g = Graphics()
                    g.poly(
                        arrayOf(
                            Point(pos.x - dotSize, pos.y - dotSize),
                            Point(pos.x - dotSize, pos.y + dotSize),
                            Point(pos.x + dotSize, pos.y + dotSize),
                            Point(pos.x + dotSize, pos.y - dotSize)
                        )
                    )
                    g.fill(arrayOf((if (isDrawing && isLinked) 0.8 else 1.0) * r, (if (isDrawing && isLinked) 0.5 else 1.0) * g, 1.0 * b, 0.7 * alpha))
                    container.addChild(g)
                }
            }

            for (line in patternList.entries) {
                val first = getPatternDotPosition(x, y, line.x, patternSize)
                val second = getPatternDotPosition(x, y, line.y, patternSize)
                drawGlyphLine(container, first, second, pixelSize, isDrawing, 1.0, r, g, b, 0.7 * alpha, animated)
            }

            if (inEditor && isDrawing) {
                val last = getPatternDotPosition(x, y, drawingPattern!!.last(), patternSize)
                val now = Point(mouseX, mouseY)
                drawGlyphLine(container, last, now, pixelSize, true, 1.0, r, g, b, 0.7 * alpha, animated)
            }
        } else {
            val renderer = fragmentRenderers[getKeyByValue(fragmentTypes, glyph.type())!!]
            var renderDots = true

            if (renderer != null) {
                renderer.render(glyph, container, x, y, size, alpha, textures, this)
                renderDots = renderer.renderRedrawDots()
            } else {
                val (text: HTMLText, width: Double) = glyph.asFormattedTextCached()
                val scale = size / 2 / maxOf(width, 100.0)
                text.anchor = js("{x: 0.5, y: 0.3}")
                text.resolution = 20
                text.style.fontFamily = "slkscr"
                text.alpha = alpha
                text.x = x
                text.y = y
                text.scale = scale
                container.addChild(text)
            }

            if (inEditor && inUI && renderDots) {
                for (i in 0 until 9) {
                    val pos = getPatternDotPosition(x, y, i, patternSize)
                    val dotScale = if (isInsideHitbox(pos, pixelSize, mouseX, mouseY) && isCircleClickable(size)) {
                        1.6
                    } else {
                        if (isCircleClickable(size)) {
                            val mouseDistance = magnitude(Point(mouseX - pos.x, mouseY - pos.y))
                            minOf(maxOf(patternSize / (mouseDistance * 2) - 0.2, 0.0), 1.0)
                        } else continue
                    }
                    val dotSize = pixelSize * dotScale
                    if (dotSize > 1) {
                        val g = Graphics()
                        g.poly(
                            arrayOf(
                                Point(pos.x - dotSize, pos.y - dotSize),
                                Point(pos.x - dotSize, pos.y + dotSize),
                                Point(pos.x + dotSize, pos.y + dotSize),
                                Point(pos.x + dotSize, pos.y - dotSize)
                            )
                        )
                        g.fill(arrayOf(this.r, this.g, this.b, 0.25))
                        container.addChild(g)
                    }
                }
            }
        }
    }
}

fun drawGlyphLine(
    container: Container,
    last: Point,
    now: Point,
    pixelSize: Double,
    isDrawing: Boolean,
    tone: Double,
    r: Double,
    g: Double,
    b: Double,
    opacity: Double,
    animated: Boolean
) {
    val directionVec = Point(last.x - now.x, last.y - now.y)
    if (magnitude(directionVec) >= pixelSize * 8) {
        val unitDirectionVec = normalize(directionVec)
        val start = Point(last.x - unitDirectionVec.x * pixelSize * 4, last.y - unitDirectionVec.y * pixelSize * 4)
        val end = Point(now.x + unitDirectionVec.x * pixelSize * 4, now.y + unitDirectionVec.y * pixelSize * 4)
        val graphics = Graphics()
        graphics.poly(arrayOf(start, end), true)
        graphics.stroke(StrokeOptions(width = pixelSize, color = arrayOf((if (isDrawing) 0.5 else tone) * r, (if (isDrawing) 0.5 else tone) * g, tone * b, opacity)))
        container.addChild(graphics)
    }
}

fun getPatternDotPosition(x: Double, y: Double, i: Int, size: Double): Point {
    var xSign = (i % 3) - 1
    var ySign = (i / 3) - 1
    if (xSign != 0 && ySign != 0) {
        xSign = (xSign * 0.7).toInt()
        ySign = (ySign * 0.7).toInt()
    }
    return Point(x + xSign * size * 0.5, y + ySign * size * 0.5)
}

fun isInsideHitbox(pos: Point, pixelSize: Double, mouseX: Double, mouseY: Double): Boolean {
    val hitboxSize = CLICK_HITBOX_SIZE * pixelSize
    return mouseX >= pos.x - hitboxSize && mouseX <= pos.x + hitboxSize && mouseY >= pos.y - hitboxSize && mouseY <= pos.y + hitboxSize
}

fun perpendicular(point: Point): Point {
    val xTemp = point.y
    point.y = point.x * -1
    point.x = xTemp
    return point
}

fun magnitude(point: Point): Double {
    return kotlin.math.sqrt(point.x * point.x + point.y * point.y)
}

fun normalize(point: Point): Point {
    val mag = magnitude(point)
    require(mag != 0.0) { "Cannot normalize a zero-length vector" }
    return Point(point.x / mag, point.y / mag)
}

fun multiplyScalar(point: Point, multiplier: Double): Point =
    Point(point.x * multiplier, point.y * multiplier)
