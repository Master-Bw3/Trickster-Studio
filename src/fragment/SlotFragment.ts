import { HTMLText, Text } from 'pixi.js';
import Fragment, { FragmentType, fragmentTypes, register } from './Fragment';
import { StructEndecBuilder, PrimitiveEndecs, Optional } from 'KEndec';
import { Either, EitherEndec, Identifier } from '~/util';
import { ALWAYS_READABLE_BLOCK_POS, safeOptionalOf, UUID, VectorI } from '~/endecTomfoolery';

type Vector = { x: number; y: number; z: number };

export default class SlotFragment extends Fragment {
    slot: number;
    source: Optional<Either<VectorI, any /*UUID*/>>;

    constructor(slot: number, source: Optional<Either<VectorI, any /*UUID*/>>) {
        super();

        this.slot = slot;
        this.source = source;
    }

    override asFormattedText(): HTMLText {
        const entityColor = fragmentTypes.get(new Identifier('trickster', 'entity'))!.color;
        const numColor = fragmentTypes.get(new Identifier('trickster', 'number'))!.color;
        const coloredInt = (value: number) =>
            `<span style="color: #${numColor.toString(16)}">${value}</span>`;

        var sourceText;
        if (this.source.isEmpty()) {
            sourceText = `<span style="color: #${entityColor.toString(16)}">Caster</span>`;
        } else if (this.source.get().left().isPresent()) {
            const source = this.source.get().left().get();
            sourceText = `(${coloredInt(source.x)}, ${coloredInt(source.y)}, ${coloredInt(source.z)})`;
        } else if (this.source.get().right().isPresent()) {
            const source = this.source.get().right().get();
            sourceText = `<span style="color: #${entityColor.toString(16)}">${source}</span>`;
        }

        const slotText = coloredInt(this.slot);

        return new HTMLText({
            text: `<span style="color: #${this.type().color.toString(
                16
            )}">slot ${slotText} at ${sourceText}</span>`,
        });
    }

    override toString(): string {
        var sourceText = 'Caster';
        if (this.source.isPresent()) {
            if (this.source.get().right().isPresent()) {
                sourceText = this.source.get().right().get();
            } else if (this.source.get().left().isPresent()) {
                const source = this.source.get().right().get();

                sourceText = `(${source.x}, ${source.y}, ${source.z})`;
            }
        }

        return `slot ${this.slot} at ${sourceText}`;
    }

    override type(): FragmentType<SlotFragment> {
        return SLOT;
    }
}

const SLOT = register(
    'slot',
    0x77aaee,
    StructEndecBuilder.of2(
        PrimitiveEndecs.INT.fieldOf('slot', (framgent: SlotFragment) => framgent.slot),
        safeOptionalOf(new EitherEndec(ALWAYS_READABLE_BLOCK_POS, UUID, true)).fieldOf(
            'source',
            (fragment: SlotFragment) => fragment.source
        ),
        (slot, source) => new SlotFragment(slot, source)
    )
);
