import { ReactElement } from 'react';
import { GlyphLines } from '../SpellCircle/Lines';
import { getPixelSize } from '../SpellCircle/SpellCircle';
import Glyph from './Glyph';
import PatternFragment from '../../Interpreter/PatternFragment';

export default class PatternGlyph extends Glyph<PatternFragment> {
    render(): ReactElement {
        return (
            <GlyphLines
                dotPositions={this.props.dotPositions}
                pattern={this.props.fragment.pattern}
                pixelSize={getPixelSize(this.props.size)}
            />
        );
    }
}
