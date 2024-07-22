import { ReactElement } from 'react';
import Fragment from './Fragment';
import { Text } from '@pixi/react';
import { TextStyle } from 'pixi.js';

class NumberFragment implements Fragment {
    public value: number;

    constructor(value: number) {
        this.value = value;
    }

    renderAsGlyph(props: { x: number; y: number; size: number }): ReactElement {
        const color = 0xdaa800;
        const scale = 0.5;
        return (
            <Text
                alpha={0.8}
                text={this.value.toString()}
                anchor={0.5}
                x={props.x}
                y={props.y}
                roundPixels={false}
                resolution={10}
                style={
                    new TextStyle({
                        fontFamily: 'Monocraft',
                        fontSize: props.size * scale,
                        fill: color,
                    })
                }
            />
        );
    }
}

export default NumberFragment;
