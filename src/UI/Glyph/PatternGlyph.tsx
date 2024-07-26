import { GlyphLines } from '../SpellCircle/Lines';
import { getPixelSize } from '../SpellCircle/SpellCircle';
import PatternFragment from '../../Interpreter/PatternFragment';
import { GlyphProps } from './Glyph';
import { Point } from 'pixi.js';

export default function PatternGlyph(props: GlyphProps<PatternFragment>) {
    const globalDotPositions = props.dotPositions.map((pos) => {
        return new Point(props.x + pos.x, props.y + pos.y);
    });

    return (
        <GlyphLines
            dotPositions={globalDotPositions}
            pattern={props.fragment.pattern}
            pixelSize={getPixelSize(props.size)}
        />
    );
}
