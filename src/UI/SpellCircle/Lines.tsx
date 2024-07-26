import { Vector2 } from '@amandaghassaei/vector-math';
import { Graphics } from '@pixi/react';
import { Point, Graphics as PixiGraphics } from 'pixi.js';
import { useCallback } from 'react';

export function GlyphLines({
    dotPositions,
    pattern,
    pixelSize,
    color,
}: {
    dotPositions: Array<Point>;
    pattern: Array<number>;
    pixelSize: number;
    color: number;
}) {
    if (pattern.length < 2) return;

    var lines = new Array(pattern.length - 1);

    for (let i = 1; i < pattern.length; i++) {
        const start = pattern[i - 1];
        const end = pattern[i];

        lines[i - 1] = (
            <GlyphLine
                key={start + '' + end}
                startPos={dotPositions[start]}
                endPos={dotPositions[end]}
                pixelSize={pixelSize}
                color={color}
            />
        );
    }
    return lines;
}

export function GlyphLine({
    startPos,
    endPos,
    pixelSize,
    color,
}: {
    startPos: Point;
    endPos: Point;
    pixelSize: number;
    color: number;
}) {
    const draw = useCallback(
        (g: PixiGraphics) => {
            var parallelVec = new Vector2(startPos.y - endPos.y, endPos.x - startPos.x);
            parallelVec =
                parallelVec.length() == 0
                    ? parallelVec
                    : parallelVec.normalize().multiplyScalar(pixelSize / 2);

            var directionVec = new Vector2(startPos.x - endPos.x, startPos.y - endPos.y);

            directionVec =
                directionVec.length() == 0
                    ? parallelVec
                    : directionVec.normalize().multiplyScalar(pixelSize * 3);

            g.clear();
            g.beginFill(color, 0.5);
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
        [startPos, endPos, pixelSize]
    );

    return <Graphics draw={draw} />;
}
