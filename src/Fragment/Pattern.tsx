import { ReactElement } from 'react';
import Fragment from './Fragment';

export type Pattern = Array<number>;

export default class PatternGlyph implements Fragment {
    public pattern: Pattern;

    constructor(pattern: Pattern) {
        this.pattern = pattern;
    }

    renderAsGlyph(props: { x: number; y: number; size: number }): ReactElement {
        return <></>;
    }
}
