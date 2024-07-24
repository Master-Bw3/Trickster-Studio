import '../App.css';
import { Container, Sprite, Graphics } from '@pixi/react';
import '@pixi/events';
import { Point, SCALE_MODES, Texture, Graphics as PixiGraphics } from 'pixi.js';
import SpellPart from '../Fragment/SpellPart';
import Fragment from '../Fragment/Fragment';
import '@pixi/events';
import Dots, { getPatternDotPosition } from './Dots';
import { ReactNode, useCallback, useState } from 'react';
import React from 'react';
import { Pattern } from '../Fragment/Pattern';
import { Drawing } from '../App';
import { Vector2 } from '@amandaghassaei/vector-math';

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

        const isDrawing = props.drawing != null && props.drawing.circle == props.spellPart;

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
                    isDrawing={isDrawing}
                    startDrawing={(point: number) => {
                        props.setDrawing({ circle: props.spellPart, pattern: [point] });
                    }}
                    patternSize={patternSize}
                    pixelSize={pixelSize}
                    dotPositions={dotPositions}
                />
                {isDrawing ? (
                    <GlyphLine
                        startPos={
                            dotPositions[props.drawing.pattern[props.drawing.pattern.length - 1]]
                        }
                        endPos={localMousePos}
                        size={pixelSize}
                    />
                ) : null}
                {subCircles}
            </Container>
        );
    }
}

function GlyphLine({ startPos, endPos, size }: { startPos: Point; endPos: Point; size: number }) {
    const draw = useCallback(
        (g: PixiGraphics) => {
            console.log(startPos, endPos);

            var parallelVec = new Vector2(startPos.y - endPos.y, endPos.x - startPos.x)
                .normalize()
                .multiplyScalar(Math.floor(size / 2));
            var directionVec = new Vector2(startPos.x - endPos.x, startPos.y - endPos.y)
                .normalize()
                .multiplyScalar(size * 3);

            g.clear();
            g.beginFill(0xffffff, 0.5);
            g.drawPolygon([
                new Point(
                    startPos.x - parallelVec.x - directionVec.x,
                    startPos.y - parallelVec.y - directionVec.y
                ),
                new Point(
                    startPos.x + parallelVec.x - directionVec.x,
                    startPos.y + parallelVec.y - directionVec.y
                ),
                new Point(
                    endPos.x + parallelVec.x + directionVec.x,
                    endPos.y + parallelVec.y + directionVec.y
                ),
                new Point(
                    endPos.x - parallelVec.x + directionVec.x,
                    endPos.y - parallelVec.y + directionVec.y
                ),
            ]);
            g.endFill();
        },
        [startPos, endPos, size]
    );

    return <Graphics draw={draw} />;
}

function Glyph(props: { glyph: Fragment; x: number; y: number; size: number }) {
    return props.glyph.renderAsGlyph(props);
}
