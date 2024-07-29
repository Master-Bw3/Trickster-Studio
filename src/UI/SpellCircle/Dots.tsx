import { Vector2 } from '@amandaghassaei/vector-math';
import { Point, Graphics as PixiGraphics, Polygon } from 'pixi.js';
import { useCallback, useEffect } from 'react';
import { Graphics, Container } from '@pixi/react';
import PatternGlyph from '../Glyph/PatternGlyph';
import Fragment from '../../Interpreter/Fragment';
import PatternFragment, { Pattern } from '../../Interpreter/PatternFragment';

type DotsPropsType = {
    glyph: Fragment;
    x: number;
    y: number;
    size: number;
    isDrawing: boolean;
    drawingPattern: Pattern | undefined;
    startDrawing: (p: number) => void;
    stopDrawing: () => void;
    addPoint: (p: number) => void;
    mousePos: Point;
    patternSize: number;
    pixelSize: number;
    dotPositions: Array<Point>;
};

export default function Dots({
    glyph,
    x,
    y,
    size,
    startDrawing,
    stopDrawing,
    isDrawing,
    drawingPattern,
    addPoint,
    mousePos,
    patternSize,
    pixelSize,
    dotPositions,
}: DotsPropsType) {
    const alpha = glyph instanceof PatternGlyph ? 1 : 0.5;
    const hitboxSize = 6 * pixelSize;
    const localMousePos = new Point(mousePos.x - x, mousePos.y - y);

    const draw = (pos: Point, dotIndex: number) =>
        useCallback(
            (g: PixiGraphics) => {
                g.clear();

                var isLinked =
                    (isDrawing && drawingPattern?.includes(dotIndex)) ||
                    (!isDrawing &&
                        glyph instanceof PatternFragment &&
                        glyph.pattern.includes(dotIndex));

                var dotScale = 1;

                if (
                    isInsideHitbox(pos, hitboxSize, localMousePos.x, localMousePos.y) &&
                    isCircleClickable(size)
                ) {
                    dotScale = 1.6;
                } else if (!isLinked) {
                    if (isCircleClickable(size)) {
                        var mouseDistance = new Vector2(
                            localMousePos.x - pos.x,
                            localMousePos.y - pos.y
                        ).length();
                        dotScale = Math.min(Math.max(patternSize / mouseDistance - 0.2, 0), 1);
                    } else {
                        // Skip the dot if its too small to click
                        return;
                    }
                }

                var dotSize = pixelSize * dotScale;

                g.beginFill({
                    r: (isDrawing && isLinked ? 0.5 : 1) * 255,
                    g: (isDrawing && isLinked ? 0.5 : 1) * 255,
                    b: 255,
                    a: 0.5 * alpha,
                });
                g.drawPolygon([
                    new Point(pos.x - dotSize, pos.y - dotSize),
                    new Point(pos.x - dotSize, pos.y + dotSize),
                    new Point(pos.x + dotSize, pos.y + dotSize),
                    new Point(pos.x + dotSize, pos.y - dotSize),
                ]);
                g.endFill();
            },
            [glyph, x, y, size, isDrawing, mousePos]
        );

    useEffect(() => {
        const onMouseDown = (_: Event) => {
            if (isDrawing) {
                stopDrawing();
            } else {
                dotPositions.forEach((dot, index) => {
                    if (isInsideHitbox(dot, hitboxSize, localMousePos.x, localMousePos.y)) {
                        startDrawing(index);
                    }
                });
            }
        };

        window.addEventListener('mousedown', onMouseDown);

        return () => {
            window.removeEventListener('mousedown', onMouseDown);
        };
    });

    const dots = dotPositions.map((pos, i) => {
        const hitArea = new Polygon([
            new Point(pos.x - hitboxSize, pos.y - hitboxSize),
            new Point(pos.x - hitboxSize, pos.y + hitboxSize),
            new Point(pos.x + hitboxSize, pos.y + hitboxSize),
            new Point(pos.x + hitboxSize, pos.y - hitboxSize),
        ]);

        return (
            <Graphics
                key={i}
                width={size}
                height={size}
                x={x}
                y={y}
                scale={1}
                draw={draw(pos, i)}
                hitArea={hitArea}
                eventMode={'static'}
                mouseover={() => {
                    if (isDrawing) addPoint(i);
                }}
            />
        );
    });

    return <Container eventMode={'static'}>{dots}</Container>;
}

export function getPatternDotPosition(x: number, y: number, i: number, size: number): Point {
    let xSign = (i % 3) - 1;
    let ySign = Math.floor(i / 3) - 1;

    if (xSign != 0 && ySign != 0) {
        xSign *= 0.7;
        ySign *= 0.7;
    }

    return new Point(x + xSign * size, y + ySign * size);
}

function isInsideHitbox(pos: Point, hitboxSize: number, mouseX: number, mouseY: number): boolean {
    return (
        mouseX >= pos.x - hitboxSize &&
        mouseX <= pos.x + hitboxSize &&
        mouseY >= pos.y - hitboxSize &&
        mouseY <= pos.y + hitboxSize
    );
}

function isCircleClickable(size: number): boolean {
    return size >= 16 && size <= 256;
}
