import { ReactElement } from 'react';
import { SpellCircle } from '../SpellCircle/SpellCircle';
import Glyph from './Glyph';
import SpellPart from '../../Interpreter/SpellPart';

export default class CircleGlyph extends Glyph<SpellPart> {
    render(): ReactElement {
        return (
            <SpellCircle
                spellPart={this.props.fragment}
                x={this.props.x}
                y={this.props.y}
                size={this.props.size / 3}
                startingAngle={0}
                zIndex={this.props.zIndex}
                drawing={this.props.drawing}
                setDrawing={this.props.setDrawing}
            ></SpellCircle>
        );
    }
}
