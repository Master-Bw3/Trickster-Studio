import { View } from 'pixi.js';
import { Fragment } from '~/fragment/fragment';
import * as fragment from '~/fragment/fragment';
import { StructEndec, StructEndecBuilder, Optional, listOf, Endec } from 'KEndec';
import { recursive, protocolVersionAlternatives, withAlternative } from '~/endecTomfoolery';
import * as spellInstruction from '~/spellInstruction';
import { SpellUtils } from '~/SpellUtils';
import { Identifier } from '~/util';

export type SpellPartData = {
    readonly glyph: Fragment<unknown>;
    readonly subParts: ReadonlyArray<fragment.Fragment<SpellPartData>>;
};

export const COLOR = 0xaa44aa;

export const endec = (fragmentEndecs: Map<Identifier, StructEndec<Fragment<unknown>>>) =>
    recursive((self: StructEndec<Fragment<SpellPartData>>) =>
        StructEndecBuilder.of2(
            fragment
                .endec(fragmentEndecs)
                .fieldOf('glyph', (spellPart: Fragment<SpellPartData>) => spellPart.data.glyph),
            protocolVersionAlternatives(
                new Map([[1, self.listOf()]]),
                withAlternative(
                    spellInstruction.stackEndec(fragmentEndecs).xmap(
                        (instructions) =>
                            SpellUtils.decodeInstructions(instructions, [], [], Optional.empty()),
                        SpellUtils.flattenNode
                    ),
                    self
                ).listOf()
            ).fieldOf('sub_parts', (spellPart: Fragment<SpellPartData>) =>
                listOf([...spellPart.data.subParts])
            ),
            (glyph, subParts) => of(glyph, subParts.asJsReadonlyArrayView())
        )
    );


function displayOf(data: SpellPartData): () => View {
    return () => {
        throw new Error('todo');
    };
}

export function of(
    glyph: Fragment<unknown>,
    subParts: ReadonlyArray<Fragment<SpellPartData>>
): Fragment<SpellPartData> {
    const data = { glyph, subParts };
    return {
        type: new Identifier('trickster', 'spell_part'),
        display: displayOf(data),
        data: data,
    };
}
