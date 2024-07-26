import React, { ReactElement } from 'react';
import { Identifier } from '../../Identifier';
import Fragment from '../../Interpreter/Fragment';
import NumberFragment from '../../Interpreter/NumberFragment';
import SpellPart from '../../Interpreter/SpellPart';

import { Glyph, GlyphProps } from './Glyph';
import NumberGlyph from './NumberGlyph';
import CircleGlyph from './CircleGlyph';

export default class GlyphRegistry {
    private static glyphs: Map<Identifier, Glyph<any>> = new Map();

    static CIRCLE_GLYPH = this.register(SpellPart.type, CircleGlyph);
    static PATTERN_GlYPH = this.register(NumberFragment.type, NumberGlyph);
    static NUMBER_GLYPH = this.register(NumberFragment.type, NumberGlyph);

    static register<T extends Fragment>(identifier: Identifier, component: Glyph<T>) {
        this.glyphs.set(identifier, component);
        return component;
    }

    static find(identifier: Identifier) {
        return this.glyphs.get(identifier);
    }
}
