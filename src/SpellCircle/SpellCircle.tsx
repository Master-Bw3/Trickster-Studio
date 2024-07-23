import '../App.css';
import { Container, Sprite } from '@pixi/react';
import '@pixi/events';
import { SCALE_MODES, Texture } from 'pixi.js';
import SpellPart from '../Fragment/SpellPart';
import Fragment from '../Fragment/Fragment';
import '@pixi/events';
import Dots from './Dots';
import { useState } from 'react';

const circle = Texture.from('/circle_48.png');
circle.baseTexture.scaleMode = SCALE_MODES.NEAREST;

export function SpellCircle(props: {
    spellPart: SpellPart;
    x: number;
    y: number;
    size: number;
    startingAngle: number;
    zIndex: number;
}) {
    const scale = 0.04;
    const partCount = props.spellPart.subParts.length;

    const subCircles = props.spellPart.subParts.map((spellPart, index) => {
        const angle = props.startingAngle + ((2 * Math.PI) / partCount) * index - Math.PI / 2;

        const x = props.x + props.size * Math.cos(angle);
        const y = props.y + props.size * Math.sin(angle);

        const size = Math.min(props.size / 2, props.size / (partCount / 2));

        return (
            <SpellCircle
                key={index}
                spellPart={spellPart}
                x={x}
                y={y}
                size={size}
                startingAngle={angle}
                zIndex={props.zIndex + 1}
            />
        );
    });

    const [isDrawing, setDrawing] = useState(false);

    return (
        <Container sortableChildren={true} zIndex={props.zIndex}>
            <Sprite
                texture={circle}
                anchor={0.5}
                x={props.x}
                y={props.y}
                scale={props.size * scale}
            />
            <Glyph glyph={props.spellPart.glyph} x={props.x} y={props.y} size={props.size} />
            <Dots
                glyph={props.spellPart.glyph}
                x={props.x}
                y={props.y}
                size={props.size}
                isDrawing={isDrawing}
                setDrawing={setDrawing}
            />
            {subCircles}
        </Container>
    );
}

SpellCircle.defaultProps = {
    zIndex: 0,
};

function Glyph(props: { glyph: Fragment; x: number; y: number; size: number }) {
    return props.glyph.renderAsGlyph(props);
}
