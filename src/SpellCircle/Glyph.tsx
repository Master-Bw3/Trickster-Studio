import { Fragment, NumberFragment } from '../Fragment/Fragment';
import { Stage, Text } from '@pixi/react';
import { TextStyle } from 'pixi.js';

function Glyph(props: { glyph: Fragment; x: number; y: number; scale: number }) {
    const glyph = props.glyph;

    if (glyph instanceof NumberFragment) {
        return (
            <Text
                alpha={0.8}
                text={glyph.value.toString()}
                anchor={0.5}
                x={props.x}
                y={props.y}
                roundPixels={false}
                resolution={10}
                style={
                    new TextStyle({
                        fontFamily: 'Monocraft',
                        fontSize: props.scale * 20,
                        fill: 0xffffff,
                    })
                }
            />
        );
    }
}

export { Glyph };
