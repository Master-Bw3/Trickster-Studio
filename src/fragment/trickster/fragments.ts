import { Endec, StructEndec } from 'KEndec';
import { Identifier } from '~/util';
import * as spell_part from './spellPart';
import * as pattern from './pattern';
import * as patternGlyph from './patternGlyph';

import { Fragment } from '../fragment';
import { funnyFieldOf } from '~/endecTomfoolery';

type FragmentEndec = StructEndec<Fragment<unknown>>;

export const tricksterFragmentEndecs: Map<Identifier, FragmentEndec> = (() => {
    const map: Map<Identifier, FragmentEndec> = new Map();

    const entries: Array<[string, (x: Map<Identifier, FragmentEndec>) => FragmentEndec]> = [
        //
        ['spell_part', spell_part.endec],
        ['pattern_literal', (endecs) => funnyFieldOf(pattern.endec(endecs), "pattern")],
        ['pattern', patternGlyph.endec],

        //
    ];
    entries.forEach(([k, v]) => map.set(new Identifier('trickster', k), v(map)));
    return map;
})();
