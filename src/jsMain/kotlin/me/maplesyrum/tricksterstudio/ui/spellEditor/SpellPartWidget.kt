package me.maplesyrum.tricksterstudio.ui.spellEditor

import me.maplesyrum.tricksterstudio.external.pixi.Container
import me.maplesyrum.tricksterstudio.external.pixi.Point
import me.maplesyrum.tricksterstudio.external.pixi.Texture
import me.maplesyrum.tricksterstudio.spell.fragment.Fragment
import me.maplesyrum.tricksterstudio.spell.fragment.Pattern
import me.maplesyrum.tricksterstudio.spell.fragment.PatternGlyph
import me.maplesyrum.tricksterstudio.spell.fragment.SpellPart
import kotlin.math.*

val PRECISION_OFFSET = 2.0.pow(50.0);

typealias MouseEventHandler = (part: SpellPart, x: Double, y: Double, size: Double) -> Boolean

class SpellPartWidget(
    val getRootSpellPart: () -> SpellPart,
    val spellPartChangeCallback: (SpellPart) -> Unit,
    x: Double,
    y: Double,
    size: Double,
    val revisionContext: RevisionContext,
    animated: Boolean,
    val fixedPosition: Boolean,
    var isMutable: Boolean
) {
    var dragDrawing = false

    var spellPart: SpellPart = getRootSpellPart()
    var parents: MutableList<SpellPart> = mutableListOf()
    var angleOffsets: MutableList<Double> = mutableListOf()

    var x: Double = toScaledSpace(x)
    var y: Double = toScaledSpace(y)
    var size: Double = toScaledSpace(size)

    var amountDragged: Double = 0.0

    var toBeReplaced: SpellPart? = null

    var originalPosition: Point = Point(this.x, this.y)
    var drawingPart: SpellPart? = null
    var oldGlyph: Fragment? = null
    var drawingPattern: MutableList<Int>? = null

    init {
        angleOffsets.add(0.0)
    }

    val renderer: SpellCircleRenderer = SpellCircleRenderer(
        { drawingPart },
        { drawingPattern },
        PRECISION_OFFSET,
        animated
    )

    fun setSpell(spellPart: SpellPart) {
        //spellPartChangeCallback(spellPart)

        val newParents = mutableListOf<SpellPart>()
        val newAngleOffsets = mutableListOf<Double>()
        newParents.add(spellPart)

        val currentParents = parents.toMutableList()
        val currentAngleOffsets = angleOffsets.toMutableList()
        newAngleOffsets.add(currentAngleOffsets.removeFirst())

        for (i in currentParents.size - 1 downTo 0) {
            val currentParent = currentParents.removeFirst()
            val currentChild = if (currentParents.isNotEmpty()) currentParents[0] else this.spellPart

            val spellGlyph = currentParent.glyph

            if (spellGlyph == currentChild) {
                val newSpellGlyph = newParents.last().glyph
                if (newSpellGlyph is SpellPart) newParents.add(newSpellGlyph)
                else break
            } else {
                var failed = true
                var i2 = 0
                for (child in currentParent.subParts) {
                    if (child == currentChild) {
                        if (newParents.last().subParts.size > i2) {
                            newParents.add(newParents.last().subParts[i2])
                            failed = false
                        }
                        break
                    }
                    i2++
                }
                if (failed) {
                    this.x = originalPosition.x
                    this.y = originalPosition.y
                    break
                }
            }
            newAngleOffsets.add(currentAngleOffsets.removeFirst())
        }

        this.spellPart = newParents.removeLast()
        this.parents.clear()
        this.angleOffsets.clear()
        this.parents.addAll(newParents)
        this.angleOffsets.addAll(newAngleOffsets)
    }

    fun render(container: Container, delta: Double, height: Double, textures: Map<String, Texture>) {
        container.removeChildren()
        renderer.renderPart(
            container,
            spellPart,
            x,
            y,
            size,
            angleOffsets.last(),
            delta,
            { size ->
                val alpha = min(height / (size * 2) - 0.1, size.pow(1.2) / height + 0.1)
                min(max(alpha, 0.0), 0.9)
            },
            textures
        )
    }

    fun setMutable(mutable: Boolean) {
        this.isMutable = mutable
        if (!mutable) {
            renderer.setMousePosition(Double.MAX_VALUE, Double.MAX_VALUE)
        }
    }

    fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
        return true
    }

    fun mouseScrolled(mouseX: Double, mouseY: Double, verticalAmount: Double): Boolean {
        renderer.setMousePosition(mouseX, mouseY)
        if (fixedPosition) return false;

        size += (verticalAmount * size) / 10.0
        x += (verticalAmount * (x - toScaledSpace(mouseX))) / 10.0
        y += (verticalAmount * (y - toScaledSpace(mouseY))) / 10.0

        if (toLocalSpace(size) > 600 * 5) {
            pushNewRoot(toScaledSpace(mouseX), toScaledSpace(mouseY))
        } else if (toLocalSpace(size) < 300 * 5 && parents.isNotEmpty()) {
            popOldRoot()
        }
        return true
    }

    fun popOldRoot() {
        val result = parents.removeLastOrNull() ?: return
        angleOffsets.removeLastOrNull()

        val partCount = result.subParts.size
        var parentSize = size * 3
        var i = 0

        val inner = result.glyph
        if (inner != spellPart) {
            parentSize = max(size * 2, size * ((partCount + 1) / 2))
            for (child in result.subParts) {
                if (child == spellPart) {
                    val angle = angleOffsets.last() + ((2 * PI) / partCount) * i - PI / 2
                    x -= parentSize * cos(angle) * 0.5
                    y -= parentSize * sin(angle) * 0.5
                    break
                }
                i++
            }
        }
        size = parentSize
        spellPart = result
    }

    fun pushNewRoot(mouseX: Double, mouseY: Double) {
        var closest = spellPart
        var closestAngle = angleOffsets.last()
        var closestDiffX = 0.0
        var closestDiffY = 0.0
        var closestDistanceSquared = Double.MAX_VALUE
        var closestSize = size / 3

        val partCount = spellPart.subParts.size
        val nextSize = min(size / 2.0, size / ((partCount + 1) / 2.0))
        var i = 0

        val inner = spellPart.glyph
        if (inner is SpellPart) {
            val mDiffX = x - mouseX
            val mDiffY = y - mouseY
            val distanceSquared = mDiffX * mDiffX + mDiffY * mDiffY
            closest = inner
            closestDistanceSquared = distanceSquared
        }

        for (child in spellPart.subParts) {
            val angle = angleOffsets.last() + ((2 * PI) / partCount) * i - PI / 2.0
            val nextX = x + size * cos(angle) * 0.5
            val nextY = y + size * sin(angle) * 0.5
            val diffX = nextX - x
            val diffY = nextY - y
            val mDiffX = nextX - mouseX
            val mDiffY = nextY - mouseY
            val distanceSquared = mDiffX * mDiffX + mDiffY * mDiffY

            if (distanceSquared < closestDistanceSquared) {
                closest = child
                closestAngle = angle
                closestDiffX = diffX
                closestDiffY = diffY
                closestDistanceSquared = distanceSquared
                closestSize = nextSize
            }
            i++
        }

        parents.add(spellPart)
        angleOffsets.add(closestAngle)
        spellPart = closest
        size = closestSize
        x += closestDiffX
        y += closestDiffY
    }

    fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        renderer.setMousePosition(mouseX, mouseY)
        if (!isDrawing() && !fixedPosition) {
            x += toScaledSpace(deltaX)
            y += toScaledSpace(deltaY)
            amountDragged += abs(deltaX) + abs(deltaY)
            return true
        }
        return false
    }

    fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        renderer.setMousePosition(mouseX, mouseY)
        if (isMutable || isDrawing()) {
            if (dragDrawing && button == 0 && !isDrawing()) {
                if (propagateMouseEvent(
                        spellPart,
                        x,
                        y,
                        size,
                        angleOffsets.last(),
                        toScaledSpace(mouseX),
                        toScaledSpace(mouseY)
                    ) { part, x, y, size -> selectPattern(part, x, y, size, mouseX, mouseY) }
                ) {
                    return true
                }
            } else {
                if (propagateMouseEvent(
                        spellPart,
                        x,
                        y,
                        size,
                        angleOffsets.last(),
                        toScaledSpace(mouseX),
                        toScaledSpace(mouseY)
                    ) { _, _, _, _ -> true }
                ) {
                    return true
                }
            }
        }
        return true
    }

    fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        renderer.setMousePosition(mouseX, mouseY)
        if (isMutable || isDrawing()) {
            val dragged = amountDragged
            amountDragged = 0.0
            if (dragged > 5) return false

            if (!dragDrawing && button == 0 && !isDrawing()) {
                if (propagateMouseEvent(
                        spellPart,
                        x,
                        y,
                        size,
                        angleOffsets.last(),
                        toScaledSpace(mouseX),
                        toScaledSpace(mouseY)
                    ) { part, x, y, size -> selectPattern(part, x, y, size, mouseX, mouseY) }
                ) {
                    return true
                }
            }
            if (drawingPart != null) {
                stopDrawing()
                return true
            }
        }
        return false
    }

    fun mouseMoved(mouseX: Double, mouseY: Double) {
        renderer.setMousePosition(mouseX, mouseY)
        if (isDrawing()) {
            propagateMouseEvent(
                spellPart,
                x,
                y,
                size,
                angleOffsets.last(),
                toScaledSpace(mouseX),
                toScaledSpace(mouseY)
            ) { part, x, y, size -> selectPattern(part, x, y, size, mouseX, mouseY) }
        }
    }

    fun selectPattern(part: SpellPart, x: Double, y: Double, size: Double, mouseX: Double, mouseY: Double): Boolean {
        if (drawingPart != null && drawingPart != part) return false

        val patternSize = size / PATTERN_TO_PART_RATIO
        val pixelSize = patternSize / PART_PIXEL_RADIUS

        for (i in 0 until 9) {
            val pos = getPatternDotPosition(x, y, i, patternSize)
            if (isInsideHitbox(pos, pixelSize, mouseX, mouseY)) {
                if (drawingPart == null) {
                    drawingPart = part
                    oldGlyph = part.glyph
                    part.glyph = PatternGlyph(mutableListOf())
                    drawingPattern = mutableListOf()
                }
                val dp = drawingPattern!!
                if (dp.size >= 2 && dp[dp.size - 2] == i) {
                    dp.removeAt(dp.size - 1)
                } else if (
                    dp.isEmpty() ||
                    (dp.last() != i && !hasOverlappingLines(dp, dp.last(), i))
                ) {
                    dp.add(i)
                    if (dp.size > 1 && dp[dp.size - 2] == 8 - i) {
                        dp.add(4)
                    }
                }
                return true
            }
        }
        return false
    }

    fun stopDrawing() {
        val compiled = drawingPattern?.let { Pattern.from(it.map(Int::toByte)) } ?: return
        val patternSize = drawingPattern!!.size
        val rev = lookup(compiled)
        drawingPart!!.glyph = oldGlyph!!

        if (compiled == EXECUTE_OFF_HAND.pattern()) {
            toBeReplaced = drawingPart
            EXECUTE_OFF_HAND.apply(revisionContext, spellPart, drawingPart!!)
        } else if (rev != null) {
            val result = rev.apply(revisionContext, spellPart, drawingPart!!)
            if (result != spellPart) {
                if (parents.isNotEmpty()) {
                    val parent = parents.last()
                    for (i in parent.subParts.indices) {
                        if (parent.subParts[i] == spellPart) {
                            val temp = parent.subParts.toMutableList()
                            temp[0] = result
                            parent.subParts = temp
                        }
                    }
                }
                if (spellPart == getRootSpellPart()) {
                    spellPartChangeCallback(result)
                }
                spellPart = result
            }
        } else if (revisionContext.getMacros()[compiled] != null) {
            toBeReplaced = drawingPart
            revisionContext.updateSpellWithSpell(drawingPart, revisionContext.getMacros()[compiled]!!)
        } else {
            if (patternSize >= 2) {
                drawingPart!!.glyph = PatternGlyph(compiled)
            } else {
                drawingPart!!.glyph = PatternGlyph()
            }
        }
        drawingPart = null
        drawingPattern = null
        revisionContext.updateSpell(getRootSpellPart())
    }

    fun replaceCallback(fragment: Fragment) {
        toBeReplaced?.let {
            it.glyph = fragment
            toBeReplaced = null
            revisionContext.updateSpell(getRootSpellPart())
        }
    }

    fun updateDrawingPartCallback(spell: SpellPart?) {
        toBeReplaced?.let {
            if (spell != null) {
                it.glyph = spell.glyph
                it.subParts = spell.subParts
            }
            toBeReplaced = null
            revisionContext.updateSpell(getRootSpellPart())
        }
    }

    fun isDrawing(): Boolean = drawingPart != null

    fun hasOverlappingLines(pattern: List<Int>, p1: Int, p2: Int): Boolean {
        var first: Int? = null
        for (second in pattern) {
            if (first != null && ((first == p1 && second == p2) || (first == p2 && second == p1))) {
                return true
            }
            first = second
        }
        return false
    }

    fun propagateMouseEvent(
        part: SpellPart,
        x: Double,
        y: Double,
        size: Double,
        startingAngle: Double,
        mouseX: Double,
        mouseY: Double,
        callback: MouseEventHandler
    ): Boolean {
        var closest = part
        var closestAngle = startingAngle
        var closestX = x
        var closestY = y
        var closestSize = size

        val centerAvailable =
            (isCircleClickable(toLocalSpace(size)) && (drawingPart == null || drawingPart == part)) || part.glyph is SpellPart
        var closestDistanceSquared = Double.MAX_VALUE

        val partCount = part.subParts.size
        val nextSize = min(size / 2, size / ((partCount + 1) / 2))
        var i = 0
        for (child in part.subParts) {
            val angle = startingAngle + ((2 * PI) / partCount) * i - PI / 2
            val nextX = x + size * cos(angle) * 0.5
            val nextY = y + size * sin(angle) * 0.5
            val diffX = nextX - mouseX
            val diffY = nextY - mouseY
            val distanceSquared = diffX * diffX + diffY * diffY

            if (distanceSquared < closestDistanceSquared) {
                closest = child
                closestAngle = angle
                closestX = nextX
                closestY = nextY
                closestSize = nextSize
                closestDistanceSquared = distanceSquared
            }
            i++
        }

        if (centerAvailable) {
            if (part.glyph is SpellPart) {
                if (propagateMouseEvent(part.glyph as SpellPart, x, y, size / 3, startingAngle, mouseX, mouseY, callback)) {
                    return true
                }
            } else {
                if (callback(part, toLocalSpace(x), toLocalSpace(y), toLocalSpace(size))) {
                    return true
                }
            }
        }

        if (sqrt(closestDistanceSquared) <= size && toLocalSpace(size) >= 16) {
            if (closest == part) return false
            return propagateMouseEvent(closest, closestX, closestY, closestSize, closestAngle, mouseX, mouseY, callback)
        }
        return false
    }

    fun toLocalSpace(value: Double): Double = value * PRECISION_OFFSET
    fun toScaledSpace(value: Double): Double = value / PRECISION_OFFSET
}
