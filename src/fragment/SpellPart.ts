import { Text } from 'pixi.js';
import Fragment, { FragmentType, register } from './Fragment';
import PatternGlyph from './PatternGlyph';
import {
    StructEndecBuilder,
    PrimitiveEndecs,
    StructEndec,
    Optional,
    SerializationContext,
    BufferDeserializer,
    listOf,
} from 'KEndec';
import {
    PROTOCOL_VERSION_ATTRIBUTE,
    protocolVersionAlternatives,
    recursive,
    UBER_COMPACT_ATTRIBUTE,
    withAlternative,
} from '~/endecTomfoolery';
import { SpellInstruction } from '~/spellInstruction';
import { SpellUtils } from '~/SpellUtils';

type Buffer = any;

export default class SpellPart extends Fragment {
    glyph: Fragment;
    subParts: ReadonlyArray<SpellPart>;

    constructor(glyph?: Fragment, subParts?: ReadonlyArray<SpellPart>) {
        super();

        this.glyph = glyph != undefined ? glyph : new PatternGlyph();
        this.subParts = subParts != undefined ? subParts : [];
    }

    override toString(): string {
        return 'spell';
    }

    override type(): FragmentType<SpellPart> {
        return SPELL_PART;
    }

    getSubParts(): ReadonlyArray<SpellPart> {
        return this.subParts;
    }

    static fromBytesOld(protocolVersion: number, buf: Buffer): SpellPart {
        return SPELL_PART.endec.decode(
            SerializationContext.empty().withAttributes([
                UBER_COMPACT_ATTRIBUTE,
                PROTOCOL_VERSION_ATTRIBUTE.instance(protocolVersion),
            ]),
            new BufferDeserializer(buf)
        );
    }
}

const SPELL_PART = register(
    'spell_part',
    0xaa44aa,
    recursive((self: StructEndec<SpellPart>) =>
        StructEndecBuilder.of2(
            Fragment.ENDEC.fieldOf('glyph', (fragment: SpellPart) => fragment.glyph),
            protocolVersionAlternatives(
                new Map([[1, self.listOf()]]),
                withAlternative(
                    SpellInstruction.stackEndec().xmap(
                        (instructions) =>
                            SpellUtils.decodeInstructions(instructions, [], [], Optional.empty()),
                        SpellUtils.flattenNode
                    ),
                    self
                ).listOf()
            ).fieldOf('sub_parts', (fragment: SpellPart) =>
                listOf([...fragment.subParts])
            ),
            (glyph, subparts) => new SpellPart(glyph, subparts.asJsReadonlyArrayView())
        )
    )
);
