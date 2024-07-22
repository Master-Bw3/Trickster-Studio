import '../App.css';
import { Container, Sprite } from '@pixi/react';
import '@pixi/events';
import { SCALE_MODES, Texture } from 'pixi.js';
import { Glyph } from './Glyph';
import SpellPart from '../Fragment/SpellPart';

const circle = Texture.from('/circle_48.png');
circle.baseTexture.scaleMode = SCALE_MODES.NEAREST;

function SpellCircleComponent(props: {
    spellPart: SpellPart;
    x: number;
    y: number;
    size: number;
    startingAngle: number;
    zIndex: number;
}) {
    const scale = 0.045;
    const partCount = props.spellPart.subParts.length;

    const subCircles = props.spellPart.subParts.map((spellPart, index) => {
        const angle = props.startingAngle + ((2 * Math.PI) / partCount) * index - Math.PI / 2;

        const x = props.x + props.size * Math.cos(angle);
        const y = props.y + props.size * Math.sin(angle);

        const size = Math.min(props.size / 2, props.size / (partCount / 2));

        return (
            <SpellCircleComponent
                key={index}
                spellPart={spellPart}
                x={x}
                y={y}
                size={size}
                startingAngle={angle}
                zIndex={props.zIndex - 1}
            />
        );
    });

    return (
        <Container sortableChildren={true} zIndex={props.zIndex}>
            <Sprite
                texture={circle}
                anchor={0.5}
                x={props.x}
                y={props.y}
                scale={props.size * scale}
            />
            <Glyph
                glyph={props.spellPart.glyph}
                x={props.x}
                y={props.y}
                size={props.size * scale}
            />
            {subCircles}
        </Container>
    );
}

SpellCircleComponent.defaultProps = {
    zIndex: 0,
};

export { SpellCircleComponent as SpellCircle };
