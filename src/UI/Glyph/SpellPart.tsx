import { ReactElement } from 'react';
import Fragment from './Glyph';
import { SpellCircle } from '../SpellCircle/SpellCircle';

class SpellPart implements Fragment {
    public glyph: Fragment;
    public subParts: Array<SpellPart>;

    constructor(glyph: Fragment, subParts: Array<SpellPart>) {
        this.glyph = glyph;
        this.subParts = subParts;
    }
    renderAsGlyph(props: { x: number; y: number; size: number }): ReactElement {
        return (
            <SpellCircle
                spellPart={this}
                x={props.x}
                y={props.y}
                size={props.size / 3}
                startingAngle={0}
                zIndex={0}
                drawing={null}
                setDrawing={undefined}
            ></SpellCircle>
        );
    }
}

export default SpellPart;
