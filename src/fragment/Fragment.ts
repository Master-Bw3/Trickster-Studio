import {
    BufferDeserializer,
    BufferSerializer,
    createBuffer,
    dispatchedStructEndecOf,
    hashCodeOf,
    ifAttr,
    readByte,
    SerializationContext,
    toBytes,
    writeByte,
} from 'KEndec';
import { HTMLText } from 'pixi.js';
import { StructEndec, PrimitiveEndecs } from 'KEndec';
import { lazy, PROTOCOL_VERSION_ATTRIBUTE, UBER_COMPACT_ATTRIBUTE } from '~/endecTomfoolery';
import { Identifier } from '~/util';
import {
    SerializedSpellInstruction,
    SpellInstruction,
    SpellInstructionType,
} from '~/spellInstruction';
import { gunzipSync, gzipSync } from 'zlib';
import SpellPart from './SpellPart';

export default abstract class Fragment extends SpellInstruction {
    _formattedText: HTMLText | null = null;
    _formattedTextWidth: number | null = null;

    static readonly ENDEC: StructEndec<Fragment> = lazy(() =>
        dispatchedStructEndecOf(
            (fragmentType) => fragmentType!.endec,
            (fragment) => fragment.type(),
            ifAttr(
                UBER_COMPACT_ATTRIBUTE,
                PrimitiveEndecs.INT.xmap(getFragmentTypeFromInt, (fragmentType) =>
                    fragmentType.getIntId()
                )
            ).orElse(
                Identifier.ENDEC.xmap(
                    (x) => fragmentTypes.get(x)!,
                    (fragmentType) => fragmentType.getId()
                )
            )
        )
    );

    asFormattedText(): HTMLText {
        return new HTMLText({
            text: `<span style="color: #${this.type().color.toString(
                16
            )}">${this.toString()}</span>`,
            style: { fill: 0xffffff },
        });
    }

    asFormattedTextCached(): [HTMLText, number] {
        if (this._formattedText === null) {
            this._formattedText = this.asFormattedText();
            this._formattedTextWidth = this._formattedText.width;
        }

        return [this._formattedText, this._formattedTextWidth!];
    }

    override asSerialized(): SerializedSpellInstruction {
        return new SerializedSpellInstruction(SpellInstructionType.FRAGMENT, this);
    }

    toBase64(): string {
        return Buffer.from(this.toBytes()).toString('base64');
    }

    toBytes(): Uint8Array {
        const buf = createBuffer();

        writeByte(buf, 4);

        Fragment.ENDEC.encode(
            SerializationContext.empty().withAttributes([
                UBER_COMPACT_ATTRIBUTE,
                PROTOCOL_VERSION_ATTRIBUTE.instance(4),
            ]),
            new BufferSerializer(buf),
            this
        );

        const bytes = toBytes(buf)

        return gzipSync(bytes);
    }

    static fromBase64(string: string): Fragment {
        return Fragment.fromBytes(Buffer.from(string, 'base64'));
    }

    static fromBytes(bytes: Uint8Array): Fragment {
        const buf = createBuffer(new Int8Array(gunzipSync(bytes)));

        const protocalVersion = readByte(buf);
        if (protocalVersion < 3) {
            return SpellPart.fromBytesOld(protocalVersion, buf);
        } else {
            return Fragment.ENDEC.decode(
                SerializationContext.empty().withAttributes([
                    UBER_COMPACT_ATTRIBUTE,
                    PROTOCOL_VERSION_ATTRIBUTE.instance(protocalVersion),
                ]),
                new BufferDeserializer(buf)
            );
        }
    }

    abstract toString(): string;

    abstract type<T>(): FragmentType<any>;
}

export class FragmentType<T extends Fragment> {
    readonly endec: StructEndec<T>;
    readonly color: number;

    constructor(endec: StructEndec<T>, color: number) {
        this.endec = endec;
        this.color = color;
    }

    getIntId(): number {
        return hashCodeOf(this.getId());
    }

    getId(): Identifier {
        return Array.from(fragmentTypes.entries()).find(([k, v]) => v === this)![0];
    }
}

const fragmentTypesIntLookup = new Map<number, Identifier>();

const fragmentTypes: Map<Identifier, FragmentType<any>> = new Map();

function register<T extends Fragment>(
    name: string,
    color: number,
    endec: StructEndec<T>,
    namespace = 'trickster'
): FragmentType<any> {
    const type = new FragmentType<T>(endec, color);
    const id = new Identifier(namespace, name);
    fragmentTypes.set(id, type);

    var hash = id.hashCode();

    fragmentTypesIntLookup.set(hash, id);

    return type;
}

function getKeyByValue<T, U>(map: Map<T, U>, value: U): T | null {
    for (let [key, val] of map.entries()) {
        if (val === value) {
            return key;
        }
    }
    return null;
}

function getFragmentTypeFromInt(intId: number): FragmentType<Fragment> {
    var id = fragmentTypesIntLookup.get(intId);
    if (id == null) {
        throw new Error('Not a valid int id for fragment type');
    }

    return fragmentTypes.get(id)!;
}

export { fragmentTypes, fragmentTypesIntLookup, getKeyByValue, register };
