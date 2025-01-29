import {
    BufferDeserializer,
    BufferSerializer,
    createBuffer,
    dispatchedStructEndecOf,
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

const GZIP_HEADER = new Uint8Array([0x1f, 0x8b, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xff]);


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
        return btoa(String.fromCharCode.apply(null, [...this.toBytes()]));
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
        const gzippedBytes = window.zlib.gzip(bytes);

        return gzippedBytes.subarray(10, gzippedBytes.length)
    }

    static fromBase64(string: string): Fragment {
        return Fragment.fromBytes(new Uint8Array(atob(string).split('').map(char => char.charCodeAt(0))));
    }

    static fromBytes(bytes: Uint8Array): Fragment {
        const unzippedBytes = new Int8Array(window.zlib.ungzip(new Uint8Array([...GZIP_HEADER, ...bytes])))
        const buf = createBuffer(unzippedBytes);

        const protocalVersion = readByte(buf);
        if (protocalVersion < 3) {
            return this.fromBytesOld(protocalVersion, buf);
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

    static fromBytesOld(protocolVersion: number, buf: any /*Buffer*/): Fragment {
        return getFragmentType(new Identifier("trickster", "spell_part"))!.endec.decode(
            SerializationContext.empty().withAttributes([
                UBER_COMPACT_ATTRIBUTE,
                PROTOCOL_VERSION_ATTRIBUTE.instance(protocolVersion),
            ]),
            new BufferDeserializer(buf)
        );
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
        return this.getId().hashCode();
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

function getFragmentType(id: Identifier): FragmentType<any> | null {
    return Array.from(fragmentTypes.entries()).find(([k, v]: [Identifier, FragmentType<any>]) => k.equals(id))?.[1] ?? null
}

function getFragmentTypeFromInt(intId: number): FragmentType<Fragment> {
    var id = fragmentTypesIntLookup.get(intId);
    if (id == null) {
        throw new Error('Not a valid int id for fragment type');
    }
    console.table(Array.from(fragmentTypesIntLookup.entries().map(entry => [entry[0], entry[1].toString()])))
    return fragmentTypes.get(id)!;
}


async function registerAllFragmentTypes() {
    await import("./BlockTypeFragment")
    await import("./BooleanFragment")
    await import("./DimensionFragment")
    await import("./EntityFragment")
    await import("./EntityTypeFragment")
    await import("./ItemTypeFragment")
    await import("./ListFragment")
    await import("./MapFragment")
    await import("./NumberFragment")
    await import("./Pattern")
    await import("./PatternGlyph")
    await import("./SlotFragment")
    await import("./SpellPart")
    await import("./StringFragment")
    await import("./TypeFragment")
    await import("./VectorFragment")
    await import("./VoidFragment")
    await import("./ZalgoFragment")
}

export { fragmentTypes, fragmentTypesIntLookup, getKeyByValue, register, registerAllFragmentTypes };
