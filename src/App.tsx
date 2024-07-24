import './App.css';
import { Dispatch, SetStateAction, useEffect, useLayoutEffect, useState } from 'react';

import { Assets, Point, SCALE_MODES, Texture, Graphics as PixiGraphics } from 'pixi.js';
import { Stage, Container, Sprite, Text, Graphics } from '@pixi/react';
import '@pixi/events';
import { SpellCircle } from './SpellCircle/SpellCircle';
import NumberFragment from './Fragment/NumberFragment';
import SpellPart from './Fragment/SpellPart';
import PatternGlyph, { Pattern } from './Fragment/Pattern';

async () => {
    Assets.addBundle('fonts', [
        { alias: 'Monocraft', src: './monocraft/Monocraft-no-ligatures.ttf' },
    ]);

    await Assets.loadBundle('fonts');
};

const testSpellPart = new SpellPart(new PatternGlyph([]), []);

// const testSpellPart = new SpellPart(new PatternGlyph([]), [
//     new SpellPart(new NumberFragment(1), [
//         new SpellPart(new NumberFragment(3), [new SpellPart(new NumberFragment(4), [])]),
//         new SpellPart(new NumberFragment(2), []),
//     ]),
// ]);

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
    const [mousePos, setMousePos] = useState(new Point(0, 0));

    return (
        <Stage width={props.width} height={props.height} options={{ background: 0x222223 }}>
            <Container
                eventMode={'static'}
                onglobalmousemove={(e) => {
                    setMousePos(new Point(e.x, e.y));
                }}
            >
                <SpellCircle
                    size={Math.min(props.width, props.height) / 5}
                    x={props.width / 2}
                    y={props.height / 2}
                    spellPart={testSpellPart}
                    startingAngle={0}
                    zIndex={0}
                    drawing={drawing}
                    setDrawing={setDrawing}
                    mousePos={mousePos}
                />
            </Container>
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
