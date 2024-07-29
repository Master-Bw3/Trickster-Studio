import './App.css';
import { Children, PropsWithChildren, useCallback, useLayoutEffect, useRef, useState } from 'react';

import { Application, Assets, SCALE_MODES, Texture } from 'pixi.js';
import { PixiComponent, Stage } from '@pixi/react';
import '@pixi/events';
import { SpellCircle } from './UI/SpellCircle/SpellCircle';
import PatternGlyph from './UI/Glyph/PatternGlyph';
import SpellPart from './Interpreter/SpellPart';
import PatternFragment, { Pattern } from './Interpreter/PatternFragment';
import NumberFragment from './Interpreter/NumberFragment';
import { Viewport } from 'pixi-viewport';
import { PixiViewport } from './UI/Viewport';

async () => {
    Assets.addBundle('fonts', [
        { alias: 'Monocraft', src: './monocraft/Monocraft-no-ligatures.ttf' },
    ]);

    await Assets.loadBundle('fonts');
};

// const testSpellPart = new SpellPart(new PatternFragment([0, 1, 2, 3]), []);

const testSpellPart = new SpellPart(new NumberFragment(0), [
    new SpellPart(new NumberFragment(1), [
        new SpellPart(new NumberFragment(3), [new SpellPart(new NumberFragment(4), [])]),
        new SpellPart(new NumberFragment(2), []),
    ]),
]);

function App() {
    const [width, height] = useWindowSize();
    const circle = Texture.from('/circle_48.png');
    circle.baseTexture.scaleMode = SCALE_MODES.NEAREST;

    return (
        <>
            <SpellCircleEditor width={width} height={height} />
        </>
    );
}

export type Drawing = null | {
    circle: SpellPart;
    pattern: Pattern;
};

function SpellCircleEditor(props: { width: number; height: number }) {
    const [drawing, setDrawing]: [Drawing, any] = useState(null);

    const size = Math.floor(Math.min(props.width, props.height) / 5);
    const [app, setApp]: [null | Application, any] = useState(null);
    const viewportRef = useRef<Viewport>(null);

    viewportRef.current?.on('wheel', (event) => {
        const { client, deltaY } = event;
        const direction = -Math.sign(deltaY);
        // Calculate new scale factor
        let scaleFactor = viewportRef.current!.scale._x + 10 * direction;
        // Clamp scale factor between min and max scale values
        scaleFactor = Math.max(scaleFactor, -1000);
        scaleFactor = Math.min(scaleFactor, 1000);
        const before = viewportRef.current!.width;
        viewportRef.current!.setZoom(scaleFactor);
        const after = viewportRef.current!.width;
        const factor = after / before;
        viewportRef.current!.x -= (client.x - viewportRef.current!.x) * (factor - 1);
        viewportRef.current!.y -= (client.y - viewportRef.current!.y) * (factor - 1);
    });

    return (
        <Stage
            width={props.width}
            height={props.height}
            options={{ background: 0x222223 }}
            onMount={setApp}
        >
            <PixiViewport
                ref={viewportRef}
                screenWidth={props.width}
                screenHeight={props.height}
                worldWidth={2000}
                worldHeight={2000}
                viewportPlugins={['drag', 'wheel']}
            >
                <SpellCircle
                    size={size}
                    x={props.width / 2}
                    y={props.height / 2}
                    spellPart={testSpellPart}
                    startingAngle={0}
                    zIndex={0}
                    drawing={drawing}
                    setDrawing={setDrawing}
                />
            </PixiViewport>
        </Stage>
    );
}

function useWindowSize() {
    const [size, setSize] = useState([0, 0]);
    useLayoutEffect(() => {
        function updateSize() {
            setSize([window.innerWidth, window.innerHeight]);
        }
        window.addEventListener('resize', updateSize);
        updateSize();
        return () => window.removeEventListener('resize', updateSize);
    }, []);
    return size;
}

export default App;
