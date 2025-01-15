import { Assets, Color, Container, Graphics, Point, Sprite, Texture } from "pixi.js";
import SpellPart from "./Fragment/SpellPart";
import Fragment, { fragmentTypes, getKeyByValue } from "./Fragment/Fragment";
import PatternGlyph from "./Fragment/PatternGlyph";
import { patternOf } from "./Pattern";
import { fragmentRenderers } from "./FragmentRenderer";
import { isCircleClickable } from "./SpellPartWidget";

const PATTERN_TO_PART_RATIO = 2.5;
const PART_PIXEL_RADIUS = 48;
const CLICK_HITBOX_SIZE = 6;

export default class SpellCircleRenderer {
    inUI: boolean;
    inEditor: boolean;
    precisionOffset: number;
    animated: boolean;

    drawingPartGetter: () => SpellPart | null;
    drawingPatternGetter: () => Array<number> | null;
    mouseX: number = Number.MAX_VALUE;
    mouseY: number = Number.MAX_VALUE;

    r = 1;
    g = 1;
    b = 1;
    circleTransparency = 1;

    constructor(
        drawingPartGetter: () => SpellPart | null,
        drawingPatternGetter: () => Array<number> | null,
        precisionOffset: number,
        animated: boolean
    ) {
        this.drawingPartGetter = drawingPartGetter;
        this.drawingPatternGetter = drawingPatternGetter;
        this.animated = animated;
        this.inUI = true;
        this.inEditor = true;
        this.precisionOffset = precisionOffset;
    }

    setMousePosition(mouseX: number, mouseY: number) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    setColor(r: number, g: number, b: number) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    toLocalSpace(value: number): number {
        return value * this.precisionOffset;
    }

    renderPart(
        container: Container,
        entry: SpellPart,
        x: number,
        y: number,
        size: number,
        startingAngle: number,
        delta: number,
        alphaGetter: (size: number) => number,
        textures: Map<string, Texture>
    ) {
        const alpha = alphaGetter(this.toLocalSpace(size));

        const circle = new Sprite(textures.get("circle_48")!);
        circle.x = this.toLocalSpace(x - size / 2);
        circle.y = this.toLocalSpace(y - size / 2);
        circle.width = this.toLocalSpace(size);
        circle.height = this.toLocalSpace(size);
        circle.alpha = alpha

        container.addChild(circle);

        this.drawGlyph(container, entry, x, y, size, startingAngle, delta, alphaGetter, textures);

        const partCount = entry.subParts.length;

        this.drawDivider(container, this.toLocalSpace(x), this.toLocalSpace(y), startingAngle, this.toLocalSpace(size), partCount, alpha);

        let i = 0;
        for (const child of entry.getSubParts()) {
            const angle = startingAngle + ((2 * Math.PI) / partCount) * i - Math.PI / 2;

            const nextX = x + size * Math.cos(angle) * 0.5;
            const nextY = y + size * Math.sin(angle) * 0.5;

            const nextSize = Math.min(size / 2, size / ((partCount + 1) / 2));

            this.renderPart(container, child, nextX, nextY, nextSize, angle, delta, alphaGetter, textures);

            i++;
        }
    }

    drawDivider(container: Container, x: number, y: number, startingAngle: number, size: number, partCount: number, alpha: number) {
        const pixelSize = size / PART_PIXEL_RADIUS;
        const lineAngle = startingAngle + ((2 * Math.PI) / partCount) * -0.5 - Math.PI / 2;

        const lineX = x + size * Math.cos(lineAngle) * 0.55;
        const lineY = y + size * Math.sin(lineAngle) * 0.55;

        const toCenter = new Point(lineX - x, lineY - y);
        const toCenterScaled = normalize(toCenter);

        const lineEnd = new Point(lineX - toCenterScaled.x * 8 * pixelSize, lineY - toCenterScaled.y * 8 * pixelSize);

        const g = new Graphics();
        g.poly([new Point(lineX, lineY), lineEnd]);
        g.stroke({ width: 1 * pixelSize, color: [0.5 * this.r, 0.5 * this.g, 1 * this.b, alpha * 0.2] });

        container.addChild(g);
    }

    drawGlyph(
        container: Container,
        parent: SpellPart,
        x: number,
        y: number,
        size: number,
        startingAngle: number,
        delta: number,
        alphaGetter: (size: number) => number,
        textures: Map<string, Texture>
    ) {
        const glyph = parent.glyph;

        if (glyph instanceof SpellPart) {
            this.renderPart(container, glyph, x, y, size / 3, startingAngle, delta, alphaGetter, textures);
        } else {
            this.drawSide(container, parent, this.toLocalSpace(x), this.toLocalSpace(y), this.toLocalSpace(size), alphaGetter, textures, glyph);
        }
    }

    drawSide(
        container: Container,
        parent: SpellPart,
        x: number,
        y: number,
        size: number,
        alphaGetter: (size: number) => number,
        textures: Map<string, Texture<import("pixi.js").TextureSource<any>>>,
        glyph: Fragment
    ) {
        const alpha = alphaGetter(size);
        const patternSize = size / PATTERN_TO_PART_RATIO;
        const pixelSize = patternSize / PART_PIXEL_RADIUS;

        if (glyph instanceof PatternGlyph) {
            const isDrawing = this.inEditor && this.drawingPartGetter() == parent;
            const drawingPattern = this.inEditor ? this.drawingPatternGetter() : null;
            const patternList = isDrawing ? patternOf(drawingPattern!) : glyph.pattern;

            for (let i = 0; i < 9; i++) {
                const pos = getPatternDotPosition(x, y, i, patternSize);

                const isLinked = isDrawing ? drawingPattern?.includes(i) : patternList.contains(i);
                let dotScale = 1;

                if (this.inEditor && isInsideHitbox(pos, pixelSize, this.mouseX, this.mouseY) && isCircleClickable(size)) {
                    dotScale = 1.6;
                } else if (!isLinked) {
                    if (this.inEditor && isCircleClickable(size)) {
                        var mouseDistance = magnitude(new Point(this.mouseX - pos.x, this.mouseY - pos.y));
                        dotScale = Math.min(Math.max(patternSize / mouseDistance - 0.2, 0), 1);
                    } else {
                        // Skip the dot if its too small to click
                        continue;
                    }
                }

                var dotSize = pixelSize * dotScale;

                const g = new Graphics();

                g.poly([
                    new Point(pos.x - dotSize, pos.y - dotSize),
                    new Point(pos.x - dotSize, pos.y + dotSize),
                    new Point(pos.x + dotSize, pos.y + dotSize),
                    new Point(pos.x + dotSize, pos.y - dotSize),
                ]);

                g.fill([(isDrawing && isLinked ? 0.8 : 1) * this.r, (isDrawing && isLinked ? 0.5 : 1) * this.g, 1 * this.b, 0.7 * alpha]);

                container.addChild(g);
            }

            for (var line of patternList.entries) {
                var first = getPatternDotPosition(x, y, line.x, patternSize);
                var second = getPatternDotPosition(x, y, line.y, patternSize);
                drawGlyphLine(container, first, second, pixelSize, isDrawing, 1, this.r, this.g, this.b, 0.7 * alpha, this.animated);
            }

            if (this.inEditor && isDrawing) {
                var last = getPatternDotPosition(x, y, drawingPattern![drawingPattern!.length - 1], patternSize);
                var now = new Point(this.mouseX, this.mouseY);
                drawGlyphLine(container, last, now, pixelSize, true, 1, this.r, this.g, this.b, 0.7 * alpha, this.animated);
            }
        } else {
            const renderer = fragmentRenderers.get(getKeyByValue(fragmentTypes, glyph.type())!)!;

            var renderDots = true;

            if (renderer != null) {
                renderer.render(glyph, container, x, y, size, alpha, textures, this);
                renderDots = renderer.renderRedrawDots();
            } else {
                var text = glyph.asFormattedText();
                var height = 7;
                var width = text.width;

                text.x += x;
                text.y += y;
                text.setSize(size / 1.3 / width, size / 1.3 / width);

                var color = 0xffffff;

                container.addChild(text);
            }

            if (this.inEditor && this.inUI && renderDots) {
                for (let i = 0; i < 9; i++) {
                    const pos = getPatternDotPosition(x, y, i, patternSize);

                    let dotScale;

                    if (isInsideHitbox(pos, pixelSize, this.mouseX, this.mouseY) && isCircleClickable(size)) {
                        dotScale = 1.6;
                    } else {
                        if (isCircleClickable(size)) {
                            const mouseDistance = magnitude(new Point(this.mouseX - pos.x, this.mouseY - pos.y));
                            dotScale = Math.min(Math.max(patternSize / (mouseDistance * 2) - 0.2, 0), 1);
                        } else {
                            // Skip the dot if its too small to click
                            continue;
                        }
                    }

                    const dotSize = pixelSize * dotScale;

                    const g = new Graphics();

                    g.poly([
                        new Point(pos.x - dotSize, pos.y - dotSize),
                        new Point(pos.x - dotSize, pos.y + dotSize),
                        new Point(pos.x + dotSize, pos.y + dotSize),
                        new Point(pos.x + dotSize, pos.y - dotSize),
                    ]);

                    g.fill([this.r, this.g, this.b, 0.25]);

                    container.addChild(g);
                }
            }
        }
    }
}

function drawGlyphLine(
    container: Container,
    last: Point,
    now: Point,
    pixelSize: number,
    isDrawing: boolean,
    tone: number,
    r: number,
    g: number,
    b: number,
    opacity: number,
    animated: boolean
) {
    let graphics = new Graphics();

    graphics.poly([last, now], true);

    graphics.stroke({ width: pixelSize, color: 0xffffff });

    container.addChild(graphics);
}

function getPatternDotPosition(x: number, y: number, i: number, size: number): Point {
    let xSign = (i % 3) - 1;
    let ySign = Math.floor(i / 3) - 1;

    if (xSign != 0 && ySign != 0) {
        xSign *= 0.7;
        ySign *= 0.7;
    }

    return new Point(x + xSign * size * 0.5, y + ySign * size * 0.5);
}

function isInsideHitbox(pos: Point, pixelSize: number, mouseX: number, mouseY: number): boolean {
    var hitboxSize = CLICK_HITBOX_SIZE * pixelSize;
    return mouseX >= pos.x - hitboxSize && mouseX <= pos.x + hitboxSize && mouseY >= pos.y - hitboxSize && mouseY <= pos.y + hitboxSize;
}

function perpendicular(point: Point): Point {
    const xTemp = point.y;
    point.y = point.x * -1;
    point.x = xTemp;
    return point;
}

function magnitude(point: Point): number {
    const { x, y } = point;

    return Math.sqrt(x * x + y * y);
}

function normalize(point: Point): Point {
    const { x, y } = point;
    const mag = magnitude(point);

    if (mag === 0) {
        throw new Error("Cannot normalize a zero-length vector");
    }

    return new Point(x / mag, y / mag);
}

function multiplyScalar(point: Point, multiplier: number): Point {
    return new Point(point.x * multiplier, point.y * multiplier);
}

export { getPatternDotPosition, isInsideHitbox, PATTERN_TO_PART_RATIO, PART_PIXEL_RADIUS };
