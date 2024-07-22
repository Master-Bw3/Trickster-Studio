import './App.css';
import { useLayoutEffect, useState } from 'react';

import { BlurFilter, TextStyle } from 'pixi.js';
import { Stage, Container, Sprite, Text } from '@pixi/react';
import '@pixi/events';
import { SpellCircle } from './SpellCircle/SpellCircle';

const App = () => {
    const [width, height] = useWindowSize();

    return (
        <>
            <Stage width={width} height={height} options={{ background: 0x222223 }}>
                <SpellCircle scale={Math.min(width, height) / 100} x={width / 2} y={height / 2} />
            </Stage>
        </>
    );
};

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
