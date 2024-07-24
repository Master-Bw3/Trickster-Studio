import { ReactElement } from 'react';
import Fragment from './Fragment';

export type DotIndex = 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8;

export type Pattern = Array<DotIndex>;

export default class PatternGlyph implements Fragment {
    public pattern: Pattern;

    constructor(pattern: Pattern) {
        this.pattern = pattern;
    }

    renderAsGlyph(props: { x: number; y: number; size: number }): ReactElement {
        return <></>;
    }
}
