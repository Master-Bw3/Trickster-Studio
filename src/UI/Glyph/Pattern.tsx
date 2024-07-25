import { ReactElement } from 'react';
import Fragment from './Glyph';
import { GlyphLines } from '../SpellCircle/Lines';
import { Point } from 'pixi.js';
import { getPatternDotPosition } from '../SpellCircle/Dots';
import { getPatternSize, getPixelSize } from '../SpellCircle/SpellCircle';

export type Pattern = Array<number>;

export default class PatternGlyph implements Fragment {
    public pattern: Pattern;

    constructor(pattern: Pattern) {
        this.pattern = pattern;
    }

    renderAsGlyph(props: { x: number; y: number; size: number }): ReactElement {
        const dotPositions = new Array(9);
        for (let i = 0; i < 9; i++) {
            const pos = getPatternDotPosition(0, 0, i, getPatternSize(props.size));
            dotPositions[i] = pos;
        }

        return (
            <GlyphLines
                dotPositions={[]}
                pattern={this.pattern}
                pixelSize={getPixelSize(props.size)}
            />
        );
    }
}
