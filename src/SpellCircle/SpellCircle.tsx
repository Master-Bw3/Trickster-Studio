import '../App.css';
import { Container, Sprite } from '@pixi/react';
import '@pixi/events';
import { SCALE_MODES, Texture } from 'pixi.js';

const circle = Texture.from('/circle_48.png');
circle.baseTexture.scaleMode = SCALE_MODES.NEAREST;

function SpellCircle(props: { x: number; y: number; scale: number }) {
    return (
        <Container>
            <Sprite texture={circle} anchor={0.5} x={props.x} y={props.y} scale={props.scale} />
        </Container>
    );
}

export { SpellCircle };
