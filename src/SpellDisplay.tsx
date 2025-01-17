import { Application, Assets, Texture } from "pixi.js";
import SpellPartWidget from "./SpellPartWidget";
import RevisionContext from "./RevisionContext";
import SpellPart from "./fragment/SpellPart";
import { createUniqueId, onMount } from "solid-js";

type Props = { spellPart: SpellPart; fixedPosition?: boolean; isMutable?: boolean; id?: string }

function SpellDisplay({ spellPart, fixedPosition = false, isMutable = true, id = createUniqueId() }: Props) {
    let container: HTMLDivElement;
    let canvas: HTMLCanvasElement;


    onMount(async () => {
        console.log(canvas!)
        const app = new Application();
        await app.init({
            canvas: canvas!,
            resolution: 1,
            autoDensity: true,
            background: 0x151515,
            width: container!.clientWidth,
            height: container!.clientHeight,
            resizeTo: container!,
        });

        await Assets.load({
            src: "slkscr.ttf",
            data: {
                family: "slkscr",
            },
        });

        const textures: Map<string, Texture> = new Map();

        textures.set("circle_48", await Assets.load("./circle_48.png"));
        textures.get("circle_48")!.source.scaleMode = "nearest";
        textures.set("overlay", await Assets.load("./pattern_literal.png"));
        textures.get("overlay")!.source.scaleMode = "nearest";

        //query
        const params = new URLSearchParams(window.location.search);

        const widget = new SpellPartWidget(spellPart, app.canvas.width / 2, app.canvas.height / 2, 64 * 5, new RevisionContext(), true, fixedPosition, isMutable);

        widget.render(app.stage, 0, 0, 0, app.canvas.height, textures);

        let pinchZooming = false;
        let lastPinchDistance = 0;

        canvas!.addEventListener("wheel", (e) => {
            widget.mouseScrolled(e.x, e.y, -e.deltaY / 100);

            app.stage.removeChildren();
            widget.render(app.stage, e.x, e.y, 0, app.canvas.height, textures);
        });

        const activeTouches = new Map();
        let mouseDown = () => activeTouches.size != 0;

        canvas!.addEventListener("pointerdown", (e) => {
            activeTouches.set(e.pointerId, { x: e.x, y: e.y });

            if (e.pointerType === "touch" && activeTouches.size === 2) {
                pinchZooming = true;
                const touchPoints = Array.from(activeTouches.values());
                lastPinchDistance = Math.hypot(touchPoints[1].x - touchPoints[0].x, touchPoints[1].y - touchPoints[0].y);
            } else {
                pinchZooming = false;
            }

            widget.dragDrawing = e.pointerType === "touch";

            const button = e.button;
            widget.mouseClicked(e.x, e.y, button);

            app.stage.removeChildren();
            widget.render(app.stage, e.x, e.y, 0, app.canvas.height, textures);
        });

        canvas!.addEventListener("pointerup", (e) => {
            activeTouches.delete(e.pointerId);

            if (activeTouches.size < 2) {
                pinchZooming = false;
            }

            const button = e.button;
            widget.mouseReleased(e.x, e.y, button);

            app.stage.removeChildren();
            widget.render(app.stage, e.x, e.y, 0, app.canvas.height, textures);
        });

        canvas!.addEventListener("pointermove", (e) => {
            if (pinchZooming && activeTouches.size === 2) {
                widget.stopDrawing()

                const previousTouchPoints = Array.from(activeTouches.entries()).map(([id, touch]) => ({
                    x: touch.x,
                    y: touch.y,
                }));

                if (activeTouches.has(e.pointerId)) {
                    activeTouches.set(e.pointerId, { x: e.x, y: e.y });
                }

                const touchPoints = Array.from(activeTouches.values());

                const currentPinchDistance = Math.hypot(touchPoints[1].x - touchPoints[0].x, touchPoints[1].y - touchPoints[0].y);
                const zoomFactor = currentPinchDistance / lastPinchDistance;
                const scrollDelta = (zoomFactor - 1) * 20;

                widget.mouseScrolled((touchPoints[0].x + touchPoints[1].x) / 2, (touchPoints[0].y + touchPoints[1].y) / 2, scrollDelta);

                lastPinchDistance = currentPinchDistance;

                const prevMidX = (previousTouchPoints[0].x + previousTouchPoints[1].x) / 2;
                const prevMidY = (previousTouchPoints[0].y + previousTouchPoints[1].y) / 2;

                const currentMidX = (touchPoints[0].x + touchPoints[1].x) / 2;
                const currentMidY = (touchPoints[0].y + touchPoints[1].y) / 2;

                const dragX = currentMidX - prevMidX;
                const dragY = currentMidY - prevMidY;
                widget.mouseDragged(
                    (touchPoints[0].x + touchPoints[1].x) / 2,
                    (touchPoints[0].y + touchPoints[1].y) / 2,
                    0,
                    dragX,
                    dragY
                );
            } else if (mouseDown()) {
                if (activeTouches.has(e.pointerId)) {
                    activeTouches.set(e.pointerId, { x: e.x, y: e.y });
                }

                const button = e.button;
                widget.mouseDragged(e.x, e.y, button, e.movementX, e.movementY);
            }

            widget.mouseMoved(e.x, e.y);

            app.stage.removeChildren();
            widget.render(app.stage, e.x, e.y, 0, app.canvas.height, textures);
        });
    })

    return <div id={id} ref={container!}>
        <canvas ref={canvas!} />
    </div>;
}

export { SpellDisplay };
