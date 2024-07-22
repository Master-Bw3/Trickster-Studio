import './App.css';
import { useEffect, useLayoutEffect, useState } from 'react';

import { Assets } from 'pixi.js';
import { Stage, Container, Sprite, Text } from '@pixi/react';
import '@pixi/events';
import { SpellCircle } from './SpellCircle/SpellCircle';
import { NumberFragment, SpellPart } from './Fragment/Fragment';

async () => {
    Assets.addBundle('fonts', [
        { alias: 'Monocraft', src: './monocraft/Monocraft-no-ligatures.ttf' },
    ]);

    await Assets.loadBundle('fonts');
};

function App() {
    const [width, height] = useWindowSize();

    return (
        <>
            <Stage width={width} height={height} options={{ background: 0x222223 }}>
                <SpellCircle
                    scale={Math.min(width, height) / 100}
                    x={width / 2}
                    y={height / 2}
                    spellPart={new SpellPart(new NumberFragment(10.0))}
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
