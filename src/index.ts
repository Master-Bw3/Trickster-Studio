import { Application, Assets, Container, Matrix, SCALE_MODES, Sprite, Texture } from "pixi.js";
import SpellPartWidget from "./SpellPartWidget";
import SpellPart from "./Fragment/SpellPart";
import PatternGlyph from "./Fragment/PatternGlyph";
import RevisionContext from "./RevisionContext";

(async () => {
    const app = new Application();
    const canvasElement = document.getElementById("pixi-canvas") as HTMLCanvasElement;

    await app.init({
        canvas: canvasElement,
        resolution: window.devicePixelRatio || 1,
        autoDensity: true,
        backgroundColor: 0x151515,
        width: window.innerWidth,
        height: window.innerHeight,
        resizeTo: window,
    });

    const textures: Map<string, Texture> = new Map();

    textures.set("circle_48", await Assets.load("./circle_48.png"));
    textures.get("circle_48")!.source.scaleMode = "nearest";

    const spellPart = new SpellPart(new PatternGlyph(), [new SpellPart(new PatternGlyph(), [new SpellPart(new PatternGlyph(), [new SpellPart(new PatternGlyph(), [])])]), new SpellPart(new PatternGlyph(), []), new SpellPart(new PatternGlyph(), [])]);

    const widget = new SpellPartWidget(spellPart, app.canvas.width / 2, app.canvas.height / 2, 64 * 5, new RevisionContext(), true);

    widget.render(app.stage, 0, 0, 0, app.canvas.height, textures);

    canvasElement.addEventListener("wheel", (e) => {
        widget.mouseScrolled(e.x, e.y, -e.deltaY / 100);

        app.stage.removeChildren();
        widget.render(app.stage, e.x, e.y, 0, app.canvas.height, textures);
    });

    let mouseDown = false;

    canvasElement.addEventListener("mousedown", (e) => {
        const button = e.button;
        widget.mouseClicked(e.x, e.y, button);
        mouseDown = true;

        app.stage.removeChildren();
        widget.render(app.stage, e.x, e.y, 0, app.canvas.height, textures);
    });

    canvasElement.addEventListener("mouseup", (e) => {
        const button = e.button;
        widget.mouseReleased(e.x, e.y, button);
        mouseDown = false;

        app.stage.removeChildren();
        widget.render(app.stage, e.x, e.y, 0, app.canvas.height, textures);
    });

    canvasElement.addEventListener("mousemove", (e) => {
        if (mouseDown) {
            const button = e.button;
            widget.mouseDragged(e.x, e.y, button, e.movementX, e.movementY);
        } else {
            widget.mouseMoved(e.x, e.y)
        }

        app.stage.removeChildren();
        widget.render(app.stage, e.x, e.y, 0, app.canvas.height, textures);
    });
})();
