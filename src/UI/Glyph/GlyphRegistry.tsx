import React, { ReactElement } from 'react';
import { Identifier } from '../../Identifier';
import Fragment from '../../Interpreter/Fragment';
import NumberFragment from '../../Interpreter/NumberFragment';
import SpellPart from '../../Interpreter/SpellPart';

import { Glyph, GlyphProps } from './Glyph';
import NumberGlyph from './NumberGlyph';
import CircleGlyph from './CircleGlyph';
import PatternFragment from '../../Interpreter/PatternFragment';
import PatternGlyph from './PatternGlyph';

export default class GlyphRegistry {
    private static glyphs: Map<String, Glyph<any>> = new Map();

    static CIRCLE_GLYPH = this.register(SpellPart.type, CircleGlyph);
    static PATTERN_GlYPH = this.register(PatternFragment.type, PatternGlyph);
    static NUMBER_GLYPH = this.register(NumberFragment.type, NumberGlyph);

    static register<T extends Fragment>(identifier: Identifier, component: Glyph<T>) {
        this.glyphs.set(identifier.toString(), component);
        return component;
    }

    static find(identifier: Identifier) {
        return this.glyphs.get(identifier.toString());
    }
}
