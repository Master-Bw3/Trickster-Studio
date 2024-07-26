import React, { ReactElement } from 'react';
import { Identifier } from '../../Identifier';
import Fragment from '../../Interpreter/Fragment';
import NumberFragment from '../../Interpreter/NumberFragment';
import Glyph, { GlyphProps } from './Glyph';
import NumberGlyph from './NumberGlyph';

export default class GlyphRegistry {
    private static glyphs: Map<Identifier, typeof React.Component<GlyphProps<any>>> = new Map();

    static NUMBER_GLYPH = this.register<NumberFragment>(NumberFragment.type, NumberGlyph);

    static register<T extends Fragment>(
        identifier: Identifier,
        component: typeof React.Component<GlyphProps<T>>
    ) {
        this.glyphs.set(identifier, component);
        return component;
    }

    static find(identifier: Identifier) {
        return this.glyphs.get(identifier);
    }
}
