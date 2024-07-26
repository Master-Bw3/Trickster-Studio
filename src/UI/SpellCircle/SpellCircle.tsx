import '../../App.css';
import { Container, Sprite } from '@pixi/react';
import '@pixi/events';
import { Point, SCALE_MODES, Texture } from 'pixi.js';
import Fragment from '../Glyph/Glyph';
import '@pixi/events';
import Dots, { getPatternDotPosition } from './Dots';
import { useState } from 'react';
import { Drawing } from '../../App';
import { GlyphLine, GlyphLines } from './Lines';
import PatternGlyph from '../Glyph/PatternGlyph';
import SpellPart from '../../Interpreter/SpellPart';
import Glyph from '../Glyph/Glyph';
import { Pattern } from '../../Interpreter/PatternFragment';
import GlyphRegistry from '../Glyph/GlyphRegistry';

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
};

export function SpellCircle(props: SpellCirclePros) {
    const scale = 0.04;
    const partCount = props.spellPart.subParts.length;
    const [mousePos, setMousePos] = useState(new Point());
    const localMousePos = new Point(mousePos.x - props.x, mousePos.y - props.y);
    const drawing = props.drawing;

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
                drawing={drawing}
                setDrawing={drawing}
            />
        );
    });

    const patternSize = getPatternSize(props.size);
    const pixelSize = getPixelSize(props.size);
    const dotPositions = new Array(9);
    for (let i = 0; i < 9; i++) {
        const pos = getPatternDotPosition(0, 0, i, patternSize);
        dotPositions[i] = pos;
    }

    const glyphComponent = GlyphRegistry.find(props.spellPart.glyph.type);

    return (
        <Container
            x={props.x}
            y={props.y}
            sortableChildren={true}
            zIndex={props.zIndex}
            eventMode={'static'}
            onglobalmousemove={(e) => {
                setMousePos(new Point(e.x, e.y));
            }}
        >
            <Sprite texture={circle} anchor={0.5} scale={props.size * scale} />
            <Glyph glyph={props.spellPart.glyph} x={props.x} y={props.y} size={props.size} />
            <Dots
                glyph={props.spellPart.glyph}
                x={props.x}
                y={props.y}
                size={props.size}
                mousePos={mousePos}
                isDrawing={drawing != null && drawing.circle == props.spellPart}
                startDrawing={(point: number) => {
                    props.setDrawing({ circle: props.spellPart, pattern: [point] });
                }}
                patternSize={patternSize}
                pixelSize={pixelSize}
                dotPositions={dotPositions}
                stopDrawing={function (): void {
                    if (drawing != null) props.spellPart.glyph = new PatternGlyph(drawing.pattern);
                    props.setDrawing(null);
                }}
                addPoint={function (p: number): void {
                    if (
                        drawing != null &&
                        !hasOverlappingLines(
                            drawing.pattern,
                            drawing.pattern[drawing.pattern.length - 1],
                            p
                        )
                    )
                        drawing?.pattern.push(p);
                }}
            />

            {drawing != null && drawing.circle == props.spellPart ? (
                <>
                    <GlyphLines
                        dotPositions={dotPositions}
                        pattern={drawing.pattern}
                        pixelSize={pixelSize}
                    />
                    <GlyphLine
                        startPos={dotPositions[drawing.pattern[drawing.pattern.length - 1]]}
                        endPos={localMousePos}
                        pixelSize={pixelSize}
                    />
                </>
            ) : null}
            {subCircles}
        </Container>
    );
}

function hasOverlappingLines(pattern: Pattern, p1: number, p2: number): boolean {
    var first = null;

    for (const second of pattern) {
        if (first != null && ((first == p1 && second == p2) || (first == p2 && second == p1))) {
            return true;
        }

        first = second;
    }

    return false;
}

export function getPatternSize(size: number) {
    return size / 2.5;
}
export function getPixelSize(size: number) {
    return getPatternSize(size) / 24;
}
