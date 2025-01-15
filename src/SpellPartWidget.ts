import SpellPart from "./Fragment/SpellPart";
import { Container, Point, Texture } from "pixi.js";
import RevisionContext from "./RevisionContext";
import Fragment from "./Fragment/Fragment";
import SpellCircleRenderer, { getPatternDotPosition, isInsideHitbox, PART_PIXEL_RADIUS, PATTERN_TO_PART_RATIO } from "./SpellCircleRenderer";
import PatternGlyph from "./Fragment/PatternGlyph";
import Pattern, { patternOf } from "./Pattern";
import Revisions from "./Revisions";

const PRECISION_OFFSET = Math.pow(2, 50);

export default class SpellPartWidget {
    dragDrawing = false;

    rootSpellPart: SpellPart;
    spellPart: SpellPart;
    parents: Array<SpellPart> = [];
    angleOffsets: Array<number> = [];

    x: number;
    y: number;
    size: number;

    amountDragged: number = 0;
    isMutable: boolean = true;

    toBeReplaced: SpellPart | null = null;

    originalPosition: Point;
    revisionContext: RevisionContext;
    drawingPart: SpellPart | null = null;
    oldGlyph: Fragment | null = null;
    drawingPattern: Array<number> | null = null;

    renderer: SpellCircleRenderer;

    constructor(spellPart: SpellPart, x: number, y: number, size: number, revisionContext: RevisionContext, animated: boolean) {
        this.rootSpellPart = spellPart;
        this.spellPart = spellPart;
        this.originalPosition = new Point(this.toScaledSpace(x), this.toScaledSpace(y));
        this.x = this.toScaledSpace(x);
        this.y = this.toScaledSpace(y);
        this.size = this.toScaledSpace(size);
        this.revisionContext = revisionContext;
        this.renderer = new SpellCircleRenderer(
            () => this.drawingPart,
            () => this.drawingPattern,
            PRECISION_OFFSET,
            animated
        );
        this.angleOffsets.push(0);
    }

    setSpell(spellPart: SpellPart) {
        const newParents: Array<SpellPart> = [];
        const newAngleOffsets: Array<number> = [];
        newParents.push(spellPart);

        const currentParents: Array<SpellPart> = [...this.parents];
        const currentAngleOffsets: Array<number> = [...this.angleOffsets];
        newAngleOffsets.push(this.angleOffsets.shift()!);

        for (let i = currentParents.length - 1; i >= 0; i--) {
            let currentParent = currentParents.shift();
            let currentChild = !(currentParents.length == 0) ? currentParents[0] : this.spellPart;

            const spellGlyph = currentParent!.glyph;

            if (spellGlyph instanceof SpellPart && spellGlyph == currentChild) {
                const newSpellGlyph: Fragment = newParents[newParents.length - 1].glyph;

                if (newSpellGlyph instanceof SpellPart) newParents.push(newSpellGlyph);
                else break;
            } else {
                let failed = true;
                let i2 = 0;

                for (const child of currentParent!.subParts) {
                    if (child == currentChild) {
                        if (newParents[newParents.length - 1].subParts.length > i2) {
                            newParents.push(newParents[newParents.length - 1].subParts[i2]);
                            failed = false;
                        }

                        break;
                    }

                    i2++;
                }

                if (failed) {
                    this.x = this.originalPosition.x;
                    this.y = this.originalPosition.y;
                    break;
                }
            }

            newAngleOffsets.push(currentAngleOffsets.shift()!);
        }

        this.rootSpellPart = spellPart;
        this.spellPart = newParents.pop()!;
        this.parents = [];
        this.angleOffsets = [];
        this.parents.push(...newParents);
        this.angleOffsets.push(...newAngleOffsets);
    }

    render(container: Container, mouseX: number, mouseY: number, delta: number, height: number, textures: Map<string, Texture>) {
        if (this.isMutable) {
            this.renderer.setMousePosition(mouseX, mouseY);
        }

        this.renderer.renderPart(
            //
            container,
            this.spellPart,
            this.x,
            this.y,
            this.size,
            this.angleOffsets[this.angleOffsets.length - 1],
            delta,
            (size: number) => Math.min(Math.max(1 / ((size / height) * 3) - 0.2, 0), 1),
            textures
        );
    }

    setMutable(mutable: boolean) {
        this.isMutable = mutable;
        if (!mutable) {
            this.renderer.setMousePosition(Number.MAX_VALUE, Number.MAX_VALUE);
        }
    }

    isMouseOver(mouseX: number, mouseY: number) {
        return true;
    }

    mouseScrolled(mouseX: number, mouseY: number, verticalAmount: number) {
        this.size += (verticalAmount * this.size) / 10;
        this.x += (verticalAmount * (this.x - this.toScaledSpace(mouseX))) / 10;
        this.y += (verticalAmount * (this.y - this.toScaledSpace(mouseY))) / 10;

        if (this.toLocalSpace(this.size) > 600 * 5) {
            this.pushNewRoot(this.toScaledSpace(mouseX), this.toScaledSpace(mouseY));
        } else if (this.toLocalSpace(this.size) < 300 && !(this.parents.length == 0)) {
            this.popOldRoot();
        }

        return true;
    }

    popOldRoot() {
        const result = this.parents.pop();
        this.angleOffsets.pop();

        const partCount = result!.subParts.length;
        let parentSize = this.size * 3;
        let i = 0;

        const inner = result!.glyph;
        if (!(inner instanceof SpellPart && inner == this.spellPart)) {
            parentSize = Math.max(this.size * 2, this.size * ((partCount + 1) / 2));

            for (const child of result!.subParts) {
                if (child == this.spellPart) {
                    let angle = this.angleOffsets[this.angleOffsets.length - 1] + ((2 * Math.PI) / partCount) * i - Math.PI / 2;
                    this.x -= parentSize * Math.cos(angle);
                    this.y -= parentSize * Math.sin(angle);
                    break;
                }

                i++;
            }
        }

        this.size = parentSize;
        this.spellPart = result!;
    }

    pushNewRoot(mouseX: number, mouseY: number) {
        let closest = this.spellPart;
        let closestAngle = this.angleOffsets[this.angleOffsets.length - 1];
        let closestDiffX = 0;
        let closestDiffY = 0;
        let closestDistanceSquared = Number.MAX_VALUE;
        let closestSize = this.size / 3;

        const partCount = this.spellPart.subParts.length;
        const nextSize = Math.min(this.size / 2, this.size / ((partCount + 1) / 2));
        let i = 0;

        const inner = this.spellPart.glyph;

        if (inner instanceof SpellPart) {
            const mDiffX = this.x - mouseX;
            const mDiffY = this.y - mouseY;
            const distanceSquared = mDiffX * mDiffX + mDiffY * mDiffY;
            closest = inner;
            closestDistanceSquared = distanceSquared;
        }

        for (const child of this.spellPart.subParts) {
            const angle = this.angleOffsets[this.angleOffsets.length - 1] + ((2 * Math.PI) / partCount) * i - Math.PI / 2;
            const nextX = this.x + this.size * Math.cos(angle);
            const nextY = this.y + this.size * Math.sin(angle);
            const diffX = nextX - this.x;
            const diffY = nextY - this.y;
            const mDiffX = nextX - mouseX;
            const mDiffY = nextY - mouseY;
            const distanceSquared = mDiffX * mDiffX + mDiffY * mDiffY;

            if (distanceSquared < closestDistanceSquared) {
                closest = child;
                closestAngle = angle;
                closestDiffX = diffX;
                closestDiffY = diffY;
                closestDistanceSquared = distanceSquared;
                closestSize = nextSize;
            }

            i++;
        }

        this.parents.push(this.spellPart);
        this.angleOffsets.push(closestAngle);
        this.spellPart = closest;
        this.size = closestSize;
        this.x += closestDiffX;
        this.y += closestDiffY;
    }

    mouseDragged(mouseX: number, mouseY: number, button: number, deltaX: number, deltaY: number): boolean {
        if (!this.isDrawing()) {
            this.x += this.toScaledSpace(deltaX);
            this.y += this.toScaledSpace(deltaY);

            this.amountDragged! += Math.abs(deltaX) + Math.abs(deltaY);
            return true;
        }

        return false;
    }

    mouseClicked(mouseX: number, mouseY: number, button: number) {
        if (this.isMutable || this.isDrawing()) {
            if (this.dragDrawing && button == 0 && !this.isDrawing()) {
                if (
                    this.propagateMouseEvent(
                        this.spellPart,
                        this.x,
                        this.y,
                        this.size,
                        this.angleOffsets[this.angleOffsets.length - 1],
                        this.toScaledSpace(mouseX),
                        this.toScaledSpace(mouseY),
                        (part: SpellPart, x: number, y: number, size: number) => this.selectPattern(part, x, y, size, mouseX, mouseY)
                    )
                ) {
                    return true;
                }
            } else {
                // We need to return true on the mouse down event to make sure the screen knows if we're on a clickable node
                if (
                    this.propagateMouseEvent(
                        this.spellPart,
                        this.x,
                        this.y,
                        this.size,
                        this.angleOffsets[this.angleOffsets.length - 1],
                        this.toScaledSpace(mouseX),
                        this.toScaledSpace(mouseY),
                        (part: SpellPart, x: number, y: number, size: number) => true
                    )
                ) {
                    return true;
                }
            }
        }

        return true;
    }

    mouseReleased(mouseX: number, mouseY: number, button: number): boolean {
        if (this.isMutable || this.isDrawing()) {
            let dragged = this.amountDragged;
            this.amountDragged = 0;

            if (dragged > 5) {
                return false;
            }

            if (!this.dragDrawing && button == 0 && !this.isDrawing()) {
                if (
                    this.propagateMouseEvent(
                        this.spellPart,
                        this.x,
                        this.y,
                        this.size,
                        this.angleOffsets[this.angleOffsets.length - 1],
                        this.toScaledSpace(mouseX),
                        this.toScaledSpace(mouseY),
                        (part: SpellPart, x: number, y: number, size: number) => this.selectPattern(part, x, y, size, mouseX, mouseY)
                    )
                ) {
                    return true;
                }
            }

            if (this.drawingPart != null) {
                this.stopDrawing();
                return true;
            }
        }

        return false;
    }

    mouseMoved(mouseX: number, mouseY: number) {
        if (this.isDrawing()) {
            this.propagateMouseEvent(
                this.spellPart,
                this.x,
                this.y,
                this.size,
                this.angleOffsets[this.angleOffsets.length - 1],
                this.toScaledSpace(mouseX),
                this.toScaledSpace(mouseY),
                (part: SpellPart, x: number, y: number, size: number) => this.selectPattern(part, x, y, size, mouseX, mouseY)
            );
        }
    }

    selectPattern(part: SpellPart, x: number, y: number, size: number, mouseX: number, mouseY: number): boolean {
        if (this.drawingPart != null && this.drawingPart != part) {
            // Cancel early if we're already drawing in another part
            return false;
        }

        let patternSize = size / PATTERN_TO_PART_RATIO;
        let pixelSize = patternSize / PART_PIXEL_RADIUS;

        for (let i = 0; i < 9; i++) {
            const pos = getPatternDotPosition(x, y, i, patternSize);

            if (isInsideHitbox(pos, pixelSize, mouseX, mouseY)) {
                if (this.drawingPart == null) {
                    this.drawingPart = part;
                    this.oldGlyph = part.glyph;
                    part.glyph = new PatternGlyph([]);
                    this.drawingPattern = [];
                }

                if (this.drawingPattern!.length >= 2 && this.drawingPattern![this.drawingPattern!.length - 2] == i) {
                    this.drawingPattern!.pop();
                    // MinecraftClient.getInstance().player.playSoundToPlayer(
                    //         ModSounds.DRAW, SoundCategory.MASTER,
                    //         1f, ModSounds.randomPitch(0.6f, 0.2f)
                    // );
                } else if (
                    this.drawingPattern!.length == 0 ||
                    (this.drawingPattern![this.drawingPattern!.length - 1] != i &&
                        !this.hasOverlappingLines(this.drawingPattern!, this.drawingPattern![this.drawingPattern!.length - 1], i))
                ) {
                    this.drawingPattern!.push(i);

                    //add middle point to path if connecting opposite corners
                    if (this.drawingPattern!.length > 1 && this.drawingPattern![this.drawingPattern!.length - 2] == 8 - i)
                        this.drawingPattern!.push(this.drawingPattern!.length - 1, 4);

                    // TODO click sound?
                    // MinecraftClient.getInstance().player.playSoundToPlayer(
                    //         ModSounds.DRAW, SoundCategory.MASTER,
                    //         1f, ModSounds.randomPitch(1f, 0.2f)
                    // );
                }

                return true;
            }
        }

        return false;
    }

    stopDrawing() {
        const compiled = patternOf(this.drawingPattern!);
        const patternSize = this.drawingPattern!.length;
        const rev = Revisions.lookup(compiled);

        this.drawingPart!.glyph = this.oldGlyph!;

        if (compiled == Revisions.EXECUTE_OFF_HAND.pattern()) {
            this.toBeReplaced = this.drawingPart;
            Revisions.EXECUTE_OFF_HAND.apply(this.revisionContext, this.spellPart, this.drawingPart!);
        } else if (rev != null) {
            const result = rev.apply(this.revisionContext, this.spellPart, this.drawingPart!);

            if (result != this.spellPart) {
                if (this.parents.length > 0) {
                    const parent = this.parents[this.parents.length - 1];

                    for (let i = 0; i < parent.subParts.length; i++) {
                        if (parent.subParts[i] == this.spellPart) {
                            parent.subParts[i] = result;
                        }
                    }
                }

                if (this.spellPart == this.rootSpellPart) {
                    this.rootSpellPart = result;
                }

                this.spellPart = result;
            }
        } else if (this.revisionContext.getMacros().get(compiled) != undefined) {
            this.toBeReplaced = this.drawingPart;
            this.revisionContext.updateSpellWithSpell(this.drawingPart, this.revisionContext.getMacros().get(compiled)!);
        } else {
            if (patternSize >= 2) {
                this.drawingPart!.glyph = new PatternGlyph(compiled);
            } else {
                this.drawingPart!.glyph = new PatternGlyph();
            }
        }

        this.drawingPart = null;
        this.drawingPattern = null;
        this.revisionContext.updateSpell(this.rootSpellPart);

        // MinecraftClient.getInstance().player.playSoundToPlayer(
        //         ModSounds.COMPLETE, SoundCategory.MASTER,
        //         1f, patternSize > 1 ? 1f : 0.6f
        // );
    }

    replaceCallback(fragment: Fragment) {
        if (this.toBeReplaced != null) {
            this.toBeReplaced.glyph = fragment;
            this.toBeReplaced = null;
            this.revisionContext.updateSpell(this.rootSpellPart);
        }
    }

    updateDrawingPartCallback(spell: SpellPart | null) {
        if (this.toBeReplaced != null) {
            if (spell != null) {
                this.toBeReplaced.glyph = spell.glyph;
                this.toBeReplaced.subParts = spell.subParts;
            }

            this.toBeReplaced = null;
            this.revisionContext.updateSpell(this.rootSpellPart);
        }
    }

    isDrawing() {
        return this.drawingPart != null;
    }

    hasOverlappingLines(pattern: Array<number>, p1: number, p2: number) {
        let first: number | null = null;

        for (const second of pattern) {
            if (first != null && ((first == p1 && second == p2) || (first == p2 && second == p1))) {
                return true;
            }

            first = second;
        }

        return false;
    }

    propagateMouseEvent(
        part: SpellPart,
        x: number,
        y: number,
        size: number,
        startingAngle: number,
        mouseX: number,
        mouseY: number,
        callback: MouseEventHandler
    ): boolean {
        let closest = part;
        let closestAngle = startingAngle;
        let closestX = x;
        let closestY = y;
        let closestSize = size;

        let centerAvailable =
            (isCircleClickable(this.toLocalSpace(size)) && (this.drawingPart == null || this.drawingPart == part)) ||
            part.glyph instanceof SpellPart;
        let closestDistanceSquared = Number.MAX_VALUE;

        let partCount = part.getSubParts().length;
        // We dont change this, its the same for all subcircles
        let nextSize = Math.min(size / 2, size / ((partCount + 1) / 2));
        let i = 0;
        for (const child of part.getSubParts()) {
            let angle = startingAngle + ((2 * Math.PI) / partCount) * i - Math.PI / 2;

            let nextX = x + size * Math.cos(angle);
            let nextY = y + size * Math.sin(angle);
            let diffX = nextX - mouseX;
            let diffY = nextY - mouseY;
            let distanceSquared = diffX * diffX + diffY * diffY;

            if (distanceSquared < closestDistanceSquared) {
                closest = child;
                closestAngle = angle;
                closestX = nextX;
                closestY = nextY;
                closestSize = nextSize;
                closestDistanceSquared = distanceSquared;
            }

            i++;
        }

        if (centerAvailable) {
            if (part.glyph instanceof SpellPart) {
                if (this.propagateMouseEvent(part.glyph, x, y, size / 3, startingAngle, mouseX, mouseY, callback)) {
                    return true;
                }
            } else {
                if (callback(part, this.toLocalSpace(x), this.toLocalSpace(y), this.toLocalSpace(size))) {
                    return true;
                }
            }
        }

        if (Math.sqrt(closestDistanceSquared) <= size && this.toLocalSpace(size) >= 16) {
            if (closest == part) {
                return false;
            }

            return this.propagateMouseEvent(closest, closestX, closestY, closestSize, closestAngle, mouseX, mouseY, callback);
        }

        return false;
    }

    toLocalSpace(value: number) {
        return value * PRECISION_OFFSET;
    }

    toScaledSpace(value: number) {
        return value / PRECISION_OFFSET;
    }
}

function isCircleClickable(size: number) {
    return size >= 16*5 && size <= 256*5;
}

type MouseEventHandler = (part: SpellPart, x: number, y: number, size: number) => boolean;

export { MouseEventHandler, isCircleClickable };
