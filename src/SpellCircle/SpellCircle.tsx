import '../App.css';
import { Container, Sprite, Graphics } from '@pixi/react';
import '@pixi/events';
import { Point, SCALE_MODES, Texture, Graphics as PixiGraphics } from 'pixi.js';
import SpellPart from '../Fragment/SpellPart';
import Fragment from '../Fragment/Fragment';
import '@pixi/events';
import Dots, { getPatternDotPosition } from './Dots';
import { ReactNode, useState } from 'react';
import React from 'react';
import { Pattern } from '../Fragment/Pattern';
import { Drawing } from '../App';

const circle = Texture.from('/circle_48.png');
circle.baseTexture.scaleMode = SCALE_MODES.NEAREST;

type SpellCirclePros = {
    spellPart: SpellPart;
    x: number;
    y: number;
    size: number;
    startingAngle: number;
    zIndex: number;
    drawing: Drawing;
    setDrawing: any;
    mousePos: Point;
};

export class SpellCircle extends React.Component<SpellCirclePros> {
    render(): ReactNode {
        const props = this.props;
        const scale = 0.04;
        const partCount = props.spellPart.subParts.length;
        const localMousePos = new Point(props.mousePos.x - props.x, props.mousePos.y - props.y);

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
                    drawing={props.drawing}
                    setDrawing={props.drawing}
                    mousePos={props.mousePos}
                />
            );
        });

        const patternSize = props.size / 2.5;
        const pixelSize = patternSize / 24;
        const dotPositions = new Array(9);
        for (let i = 0; i < 9; i++) {
            const pos = getPatternDotPosition(0, 0, i, patternSize);
            dotPositions[i] = pos;
        }

        return (
            <Container x={props.x} y={props.y} sortableChildren={true} zIndex={props.zIndex}>
                <Sprite texture={circle} anchor={0.5} scale={props.size * scale} />
                <Glyph glyph={props.spellPart.glyph} x={props.x} y={props.y} size={props.size} />
                <Dots
                    glyph={props.spellPart.glyph}
                    x={props.x}
                    y={props.y}
                    size={props.size}
                    mousePos={props.mousePos}
                    isDrawing={props.drawing != null && props.drawing.circle == props.spellPart}
                    startDrawing={(point: number) => {
                        props.setDrawing({ circle: props.spellPart, pattern: [point] });
                    }}
                    patternSize={patternSize}
                    pixelSize={pixelSize}
                    dotPositions={dotPositions}
                />
                {props.drawing != null ? (
                    <DrawingLine
                        startPos={
                            dotPositions[props.drawing.pattern[props.drawing.pattern.length - 1]]
                        }
                        mousePos={localMousePos}
                    />
                ) : null}
                {subCircles}
            </Container>
        );
    }
}

function DrawingLine({ startPos, mousePos }: { startPos: Point; mousePos: Point }) {
    const draw = (g: PixiGraphics) => {
        g.clear()
            .lineStyle({ width: 2, color: 0xffffff })
            .moveTo(startPos.x, startPos.y)
            .lineTo(mousePos.x, mousePos.y);
    };

    return <Graphics draw={draw} />;
}

function Glyph(props: { glyph: Fragment; x: number; y: number; size: number }) {
    return props.glyph.renderAsGlyph(props);
}
