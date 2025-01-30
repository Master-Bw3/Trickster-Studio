import { Application, Assets, Texture, Ticker } from 'pixi.js';
import SpellPartWidget from './spellDisplay/SpellPartWidget';
import RevisionContext from '../RevisionContext';
import SpellPart from '../fragment/old-garbage/SpellPart';
import { Accessor, createEffect, createUniqueId, JSX, onMount, Setter, splitProps } from 'solid-js';

type Props = {
    spellPart: Accessor<SpellPart>;
    setSpellPart: Setter<SpellPart>;
    fixedPosition?: boolean;
    isMutable?: boolean;
    initialScale?: number;
} & JSX.HTMLAttributes<HTMLDivElement>;

function SpellDisplay(props: Props) {
    const [local, other] = splitProps(props, [
        'spellPart',
        'setSpellPart',
        'fixedPosition',
        'isMutable',
        'initialScale',
    ]);

    let container: HTMLDivElement;
    let canvas: HTMLCanvasElement;

    onMount(async () => {
        const app = new Application();
        await app.init({
            canvas: canvas!,
            resolution: 1,
            autoDensity: true,
            backgroundAlpha: 0,
            width: container!.clientWidth,
            height: container!.clientHeight,
            resizeTo: container!,
        });

        await Assets.load({
            src: 'slkscr.ttf',
            data: {
                family: 'slkscr',
            },
        });

        const textures: Map<string, Texture> = new Map();

        textures.set('circle_48', await Assets.load('./circle_48.png'));
        textures.get('circle_48')!.source.scaleMode = 'nearest';
        textures.set('overlay', await Assets.load('./pattern_literal.png'));
        textures.get('overlay')!.source.scaleMode = 'nearest';

        const widget = new SpellPartWidget(
            local.spellPart,
            local.setSpellPart,
            app.canvas.width / 2,
            app.canvas.height / 2,
            app.canvas.height * (local.initialScale ?? 1),
            new RevisionContext(),
            true,
            local.fixedPosition ?? false,
            local.isMutable ?? true
        );

        createEffect(() => {
            widget.setSpell(local.spellPart());
            console.log('changed');
        });

        app.ticker.add(() => {
            if (widget.fixedPosition) {
                widget.size = widget.toScaledSpace(app.canvas.height * (local.initialScale ?? 1));
                widget.x = widget.toScaledSpace(app.canvas.width / 2);
                widget.y = widget.toScaledSpace(app.canvas.height / 2);
            }

            widget.render(app.stage, 0, app.canvas.height, textures);

            app.queueResize();
        });

        let pinchZooming = false;
        let lastPinchDistance = 0;

        canvas!.addEventListener('wheel', (e) => {
            const rect = container!.getBoundingClientRect();
            const [x, y] = [e.x - rect.left, e.y - rect.top];

            widget.mouseScrolled(x, y, -e.deltaY / 100);

            app.stage.removeChildren();
        });

        const activeTouches = new Map();
        let mouseDown = () => activeTouches.size != 0;

        canvas!.addEventListener('pointerdown', (e) => {
            const rect = container!.getBoundingClientRect();
            const [x, y] = [e.x - rect.left, e.y - rect.top];

            activeTouches.set(e.pointerId, { x: x, y: y });

            if (e.pointerType === 'touch' && activeTouches.size === 2) {
                pinchZooming = true;
                const touchPoints = Array.from(activeTouches.values());
                lastPinchDistance = Math.hypot(
                    touchPoints[1].x - touchPoints[0].x,
                    touchPoints[1].y - touchPoints[0].y
                );
            } else {
                pinchZooming = false;
            }

            widget.dragDrawing = e.pointerType === 'touch';

            const button = e.button;
            widget.mouseClicked(x, y, button);
        });

        window.addEventListener('pointerup', (e) => {
            const rect = container!.getBoundingClientRect();
            const [x, y] = [e.x - rect.left, e.y - rect.top];

            if (mouseDown()) {
                const button = e.button;
                widget.mouseReleased(x, y, button);
            }

            activeTouches.delete(e.pointerId);
            if (activeTouches.size < 2) {
                pinchZooming = false;
            }
        });

        window.addEventListener('pointermove', (e) => {
            const rect = container!.getBoundingClientRect();
            const [x, y] = [e.x - rect.left, e.y - rect.top];

            if (pinchZooming && activeTouches.size === 2) {
                widget.stopDrawing();

                const previousTouchPoints = Array.from(activeTouches.entries()).map(
                    ([id, touch]) => ({
                        x: touch.x,
                        y: touch.y,
                    })
                );

                if (activeTouches.has(e.pointerId)) {
                    activeTouches.set(e.pointerId, { x: x, y: y });
                }

                const touchPoints = Array.from(activeTouches.values());

                const currentPinchDistance = Math.hypot(
                    touchPoints[1].x - touchPoints[0].x,
                    touchPoints[1].y - touchPoints[0].y
                );
                const zoomFactor = currentPinchDistance / lastPinchDistance;
                const scrollDelta = (zoomFactor - 1) * 20;

                widget.mouseScrolled(
                    (touchPoints[0].x + touchPoints[1].x) / 2,
                    (touchPoints[0].y + touchPoints[1].y) / 2,
                    scrollDelta
                );

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
                    activeTouches.set(e.pointerId, { x: x, y: y });
                }

                const button = e.button;
                widget.mouseDragged(x, y, button, e.movementX, e.movementY);
            }

            widget.mouseMoved(x, y);
        });
    });

    return (
        <div ref={container!} {...other}>
            <canvas ref={canvas!} />
        </div>
    );
}

export { SpellDisplay };
