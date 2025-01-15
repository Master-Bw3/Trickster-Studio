import SpellPart from "./Fragment/SpellPart";


const PATTERN_TO_PART_RATIO = 2.5;
const PART_PIXEL_RADIUS = 24;

export default class SpellCircleRenderer {



    constructor(drawingPartGetter: () => (SpellPart | null), drawingPatternGetter: () => (Array<number> | null), precisionOffset: number, animated: boolean) {

    }


    setMousePosition(mouseX: number, mouseY: number) {
        throw new Error("Method not implemented.");
    }
    renderPart(spellPart: SpellPart, x: number, y: number, size: number, arg4: number, delta: number, arg6: (size: number) => number) {
        throw new Error("Method not implemented.");
    }
}

function getPatternDotPosition(x: number, y: number, i: number, patternSize: number) {
    throw new Error("Function not implemented.");
}

function isInsideHitbox(pos: void, pixelSize: number, mouseX: number, mouseY: number): boolean {
throw new Error("Function not implemented.");
}

export {
    getPatternDotPosition,
    isInsideHitbox,
    PATTERN_TO_PART_RATIO,
    PART_PIXEL_RADIUS
}