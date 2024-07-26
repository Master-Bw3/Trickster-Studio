import { GlyphProps } from './Glyph';
import { Text } from '@pixi/react';
import { TextStyle } from 'pixi.js';
import NumberFragment from '../../Interpreter/NumberFragment';

export default function NumberGlyph(props: GlyphProps<NumberFragment>) {
    const x = props.x;
    const y = props.y;
    const size = props.size;
    const number = props.fragment;

    const color = 0xdaa800;
    const scale = 0.5;
    return (
        <Text
            alpha={0.8}
            text={number.toString()}
            anchor={0.5}
            x={x}
            y={y}
            roundPixels={false}
            resolution={10}
            style={
                new TextStyle({
                    fontFamily: 'Monocraft',
                    fontSize: size * scale,
                    fill: color,
                })
            }
        />
    );
}
