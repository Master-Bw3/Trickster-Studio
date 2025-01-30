import { Fragment } from '~/fragment/fragment';
import { Identifier } from '~/util';
import * as pattern from './pattern';
import { View } from 'pixi.js';
import { StructEndec, StructEndecBuilder } from 'KEndec';
import { PatternData } from './pattern';

export type PatternGlyphData = {
    pattern: Fragment<PatternData>;
};

export const endec = (fragmentEndecs: Map<Identifier, StructEndec<Fragment<unknown>>>) =>
    StructEndecBuilder.of1(
        pattern.endec(fragmentEndecs).fieldOf("pattern", (fragment: Fragment<PatternGlyphData>) => fragment.data.pattern),
        pattern => of(pattern)
    )

function displayOf(data: PatternGlyphData): () => View {
    return () => {
        throw new Error('todo');
    };
}

export function of(pattern: Fragment<PatternData>): Fragment<PatternGlyphData> {
    const data = { pattern };
    return {
        type: new Identifier('trickster', 'pattern'),
        display: displayOf(data),
        data,
    };
}
