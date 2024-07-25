import './App.css';
import { useLayoutEffect, useState } from 'react';

import { Assets, SCALE_MODES, Texture } from 'pixi.js';
import { Stage } from '@pixi/react';
import '@pixi/events';
import { SpellCircle } from './UI/SpellCircle/SpellCircle';
import SpellPart from './UI/Glyph/SpellPart';
import PatternGlyph, { Pattern } from './UI/Glyph/Pattern';

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

    return (
        <Stage width={props.width} height={props.height} options={{ background: 0x222223 }}>
            <SpellCircle
                size={Math.min(props.width, props.height) / 5}
                x={props.width / 2}
                y={props.height / 2}
                spellPart={testSpellPart}
                startingAngle={0}
                zIndex={0}
                drawing={drawing}
                setDrawing={setDrawing}
            />
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
