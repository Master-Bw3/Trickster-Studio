import './App.css';
import { useEffect, useLayoutEffect, useState } from 'react';

import { Assets } from 'pixi.js';
import { Stage, Container, Sprite, Text } from '@pixi/react';
import '@pixi/events';
import { SpellCircle } from './SpellCircle/SpellCircle';
import NumberFragment from './Fragment/NumberFragment';
import SpellPart from './Fragment/SpellPart';

async () => {
    Assets.addBundle('fonts', [
        { alias: 'Monocraft', src: './monocraft/Monocraft-no-ligatures.ttf' },
    ]);

    await Assets.loadBundle('fonts');
};

const testSpellPart = new SpellPart(new SpellPart(new NumberFragment(0), []), [
    new SpellPart(new NumberFragment(1), [
        new SpellPart(new NumberFragment(3), [new SpellPart(new NumberFragment(4), [])]),
        new SpellPart(new NumberFragment(2), []),
    ]),
]);

function App() {
    const [width, height] = useWindowSize();

    return (
        <>
            <Stage width={width} height={height} options={{ background: 0x222223 }}>
                <SpellCircle
                    size={Math.min(width, height) / 5}
                    x={width / 2}
                    y={height / 2}
                    spellPart={testSpellPart}
                    startingAngle={0}
                />
            </Stage>
        </>
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
