import { ReactElement } from 'react';
import Fragment from './Fragment';

type DotIndex = 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8;

type Pattern = Array<DotIndex>;

class PatternGlyph implements Fragment {
    public pattern: Pattern;

    constructor(pattern: Pattern) {
        this.pattern = pattern;
    }

    renderAsGlyph(props: { x: number; y: number; size: number }): ReactElement {
        return <></>;
    }
}

export default PatternGlyph;
export type { Pattern, DotIndex };
