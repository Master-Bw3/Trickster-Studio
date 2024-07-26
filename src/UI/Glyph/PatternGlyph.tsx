import { GlyphLines } from '../SpellCircle/Lines';
import { getPixelSize } from '../SpellCircle/SpellCircle';
import PatternFragment from '../../Interpreter/PatternFragment';
import { GlyphProps } from './Glyph';

export default function NumberGlyph(props: GlyphProps<PatternFragment>) {
    return (
        <GlyphLines
            dotPositions={props.dotPositions}
            pattern={props.fragment.pattern}
            pixelSize={getPixelSize(props.size)}
        />
    );
}
