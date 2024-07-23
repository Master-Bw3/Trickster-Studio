import { Vector2 } from '@amandaghassaei/vector-math';
import { Point, Graphics as PixiGraphics } from 'pixi.js';
import { useState, useCallback } from 'react';
import Fragment from '../Fragment/Fragment';
import { Graphics, Container } from '@pixi/react';

export default function Dots(props: { glyph: Fragment; x: number; y: number; size: number }) {
    const x = props.x;
    const y = props.y;
    const size = props.size;
    const patternSize = size / 2.5;
    const pixelSize = patternSize / 24;

    //temporary
    const isDrawing = false;

    const [globalMousePos, setMousePos] = useState(new Point(0, 0));
    const mousePos = new Point(globalMousePos.x - x, globalMousePos.y - y);

    const draw = useCallback(
        (g: PixiGraphics) => {
            g.clear();
            for (let i = 0; i < 9; i++) {
                var pos = getPatternDotPosition(0, 0, i, patternSize);

                var isLinked = false; //isDrawing ? drawingPattern.contains((byte) i) : patternList.contains(i);
                var dotScale = 1;

                if (
                    isInsideHitbox(new Point(x, y), pixelSize, mousePos.x, mousePos.y) &&
                    isCircleClickable(size)
                ) {
                    dotScale = 1.6;
                } else if (!isLinked) {
                    if (isCircleClickable(size)) {
                        var mouseDistance = new Vector2(
                            mousePos.x - pos.x,
                            mousePos.y - pos.y
                        ).length();
                        dotScale = Math.min(Math.max(patternSize / mouseDistance - 0.2, 0), 1);
                    } else {
                        // Skip the dot if its too small to click
                        continue;
                    }
                }

                var dotSize = pixelSize * dotScale;

                g.beginFill({
                    r: (isDrawing && isLinked ? 0.5 : 1) * 255,
                    g: (isDrawing && isLinked ? 0.5 : 1) * 255,
                    b: 255,
                    a: 0.5,
                });
                g.drawPolygon([
                    new Point(pos.x - dotSize, pos.y - dotSize),
                    new Point(pos.x - dotSize, pos.y + dotSize),
                    new Point(pos.x + dotSize, pos.y + dotSize),
                    new Point(pos.x + dotSize, pos.y - dotSize),
                ]);
                g.endFill();
            }
        },
        [props, globalMousePos]
    );

    return (
        <Container
            eventMode={'static'}
            onglobalmousemove={(e) => {
                setMousePos(new Point(e.x, e.y));
            }}
        >
            <Graphics x={x} y={y} width={size} height={size} scale={1.2} draw={draw} />
        </Container>
    );
}

function getPatternDotPosition(x: number, y: number, i: number, size: number): Point {
    let xSign = (i % 3) - 1;
    let ySign = Math.floor(i / 3) - 1;

    if (xSign != 0 && ySign != 0) {
        xSign *= 0.7;
        ySign *= 0.7;
    }

    return new Point(x + xSign * size, y + ySign * size);
}

function isInsideHitbox(pos: Point, pixelSize: number, mouseX: number, mouseY: number): boolean {
    var hitboxSize = 6 * pixelSize;
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
