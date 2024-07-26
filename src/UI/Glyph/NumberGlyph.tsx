import { ReactElement } from 'react';
import Fragment from './Glyph';
import { Text } from '@pixi/react';
import { TextStyle } from 'pixi.js';
import Glyph from './Glyph';
import NumberFragment from '../../Interpreter/NumberFragment';

export default class NumberGlyph extends Glyph<NumberFragment> {
    render(): ReactElement {
        const x = this.props.x;
        const y = this.props.y;
        const size = this.props.size;
        const number = this.props.fragment;

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
}
