import { SpellCircle } from '../SpellCircle/SpellCircle';
import { GlyphProps } from './Glyph';
import SpellPart from '../../Interpreter/SpellPart';

export default function CircleGlyph(props: GlyphProps<SpellPart>) {
    return (
        <SpellCircle
            spellPart={props.fragment}
            x={props.x}
            y={props.y}
            size={props.size / 3}
            startingAngle={0}
            zIndex={props.zIndex}
            drawing={props.drawing}
            setDrawing={props.setDrawing}
        ></SpellCircle>
    );
}
