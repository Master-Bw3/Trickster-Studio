import {
    AbstractEndec,
    Endec,
    PrimitiveEndecs,
    uuidFromString,
    uuidToString,
    SerializationAttribute,
    ifAttr,
    endecOf,
    Optional,
    SerializationContext,
    Serializer,
    Deserializer,
    StructEndec,
    StructEndecBuilder,
    AbstractStructEndec,
    StructDeserializer,
    StructSerializer,
    listOf,
} from 'KEndec';
import { Either, EitherEndec, memoize } from './util';
import { Point } from 'pixi.js';

export class UndashedUuid {
    public static fromString(string: string): any {
        if (string.indexOf('-') != -1) {
            throw new Error('Invalid undashed UUID string: ' + string);
        }
        return UndashedUuid.fromStringLenient(string);
    }

    public static fromStringLenient(string: string): any {
        return uuidFromString(
            string.replace(/(\w{8})(\w{4})(\w{4})(\w{4})(\w{12})/, '$1-$2-$3-$4-$5')
        );
    }

    public static toString(uuid: any): string {
        return uuid.toString().replace('-', '');
    }
}

export const POINT: Endec<Point> = PrimitiveEndecs.BYTES.xmap(
    (list) => new Point(list[0], list[1]),
    (entry) => Int8Array.of(entry.x, entry.y)
);

export class Vector {
    x: number;
    y: number;
    z: number;

    constructor(x: number, y: number, z: number) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public toString(): string {
        return 'Vector[' + 'x=' + this.x + ', ' + 'y=' + this.y + ', ' + 'z=' + this.z + ']';
    }
}

export class VectorI {
    x: number;
    y: number;
    z: number;

    constructor(x: number, y: number, z: number) {
        if (Math.floor(x) != x || Math.floor(y) != y || Math.floor(z) != z)
            throw Error('only integer components are allowed');

        this.x = x;
        this.y = y;
        this.z = z;
    }

    public toString(): string {
        return 'Vector[' + 'x=' + this.x + ', ' + 'y=' + this.y + ', ' + 'z=' + this.z + ']';
    }
}

export const ALWAYS_READABLE_BLOCK_POS: Endec<VectorI> = vectorEndec(
    PrimitiveEndecs.INT,
    (x, y, z) => new VectorI(x, y, z),
    (vec: Vector) => vec.x,
    (vec: Vector) => vec.y,
    (vec: Vector) => vec.z
);

export const UUID: Endec<any> = PrimitiveEndecs.STRING.xmap(
    UndashedUuid.fromStringLenient,
    uuidToString
);
export const UBER_COMPACT_ATTRIBUTE: SerializationAttribute.Marker =
    SerializationAttribute.marker('uber_compact');
export const PROTOCOL_VERSION_ATTRIBUTE: SerializationAttribute.WithValue<number> =
    SerializationAttribute.withValue('protocol_version');
export const VECTOR_3D_ENDEC: Endec<Vector> = vectorEndec(
    PrimitiveEndecs.DOUBLE,
    (x, y, z) => new Vector(x, y, z),
    (vec: Vector) => vec.x,
    (vec: Vector) => vec.y,
    (vec: Vector) => vec.z
);
export const CODEC_SAFE: SerializationAttribute.Marker =
    SerializationAttribute.marker('codec_safe');

export function vectorEndec<C, V>(
    componentEndec: Endec<C>,
    constructor: (x: C, y: C, z: C) => V,
    xGetter: (x: V) => C,
    yGetter: (x: V) => C,
    zGetter: (x: V) => C
): Endec<V> {
    return componentEndec
        .listOf()
        .validate((ints) => {
            if (ints.asJsReadonlyArrayView().length != 3) {
                throw new Error('vector array must have three elements');
            }
        })
        .xmap(
            (components) =>
                constructor(
                    components.asJsReadonlyArrayView()[0],
                    components.asJsReadonlyArrayView()[1],
                    components.asJsReadonlyArrayView()[2]
                ),
            (vector) =>
                listOf([
                    xGetter(vector),
                    yGetter(vector),
                    zGetter(vector),
                ])
        );
}

export function withAlternative<T, A extends T>(
    primary: Endec<T>,
    alternative: Endec<A>
): Endec<T> {
    return new EitherEndec<T, A>(primary, alternative, false).xmap(
        (either) =>
            either.map(
                (x) => x,
                (x) => x
            ),
        Either.left
    );
}

export function safeOptionalOf<T>(endec: Endec<T>): Endec<Optional<T>> {
    return ifAttr(
        CODEC_SAFE,
        endecOf<Optional<T>>(
            (ctx: SerializationContext, serializer: Serializer<any>, value: Optional<T>) => {
                const struct = serializer.struct();
                struct.field('present', ctx, PrimitiveEndecs.BOOLEAN, value.isPresent());
                value.ifPresent((t: T) => struct.field('value', ctx, endec, t));
            },

            (ctx: SerializationContext, deserializer: Deserializer<T>) => {
                var struct = deserializer.struct();
                //noinspection DataFlowIssue
                if (struct.field('present', ctx, PrimitiveEndecs.BOOLEAN, null)) {
                    //noinspection DataFlowIssue
                    return Optional.of(struct.field('value', ctx, endec, null));
                } else {
                    return Optional.empty();
                }
            }
        )
    ).orElse(endec.optionalOf());
}

export function funnyFieldOf<T>(endec: Endec<T>, key: string): StructEndec<T> {
    return StructEndecBuilder.of1(
        endec.fieldOf(key, (x) => x),
        (x) => x
    );
}

export function recursive<T>(wrapped: (x: StructEndec<T>) => StructEndec<T>): StructEndec<T> {
    return new RecursiveStructEndec(wrapped);
}

export function lazy<T>(supplier: () => StructEndec<T>): StructEndec<T> {
    return recursive((e) => supplier());
}

export function unit<T>(value: T): StructEndec<T> {
    return new (class extends AbstractStructEndec<T> {
        encodeStruct(
            ctx: SerializationContext,
            serializer: Serializer<any>,
            struct: StructSerializer,
            value: T
        ): void {}

        decodeStruct(
            ctx: SerializationContext,
            deserializer: Deserializer<any>,
            struct: StructDeserializer
        ): T {
            return value;
        }
    })();
}

export function unitFromSupplier<T>(value: () => T): StructEndec<T> {
    return new (class extends AbstractStructEndec<T> {
        encodeStruct(
            ctx: SerializationContext,
            serializer: Serializer<any>,
            struct: StructSerializer,
            value: T
        ): void {}

        decodeStruct(
            ctx: SerializationContext,
            deserializer: Deserializer<any>,
            struct: StructDeserializer
        ): T {
            return value();
        }
    })();
}

export function protocolVersionAlternatives<T>(
    protocols: Map<number, Endec<T>>,
    defaultProtocol: Endec<T>
): Endec<T> {
    return new (class extends AbstractEndec<T> {
        encode(ctx: SerializationContext, serializer: Serializer<any>, value: T): void {
            const protocolVersion = ctx.getAttributeValue(PROTOCOL_VERSION_ATTRIBUTE);
            if (protocolVersion == null) {
                defaultProtocol.encode(ctx, serializer, value);
                return;
            }

            const protocol = protocols.get(protocolVersion);
            if (protocol == null) {
                defaultProtocol.encode(ctx, serializer, value);
                return;
            }

            protocol.encode(ctx, serializer, value);
        }

        decode(ctx: SerializationContext, deserializer: Deserializer<any>): T {
            const protocolVersion = ctx.getAttributeValue(PROTOCOL_VERSION_ATTRIBUTE);
            if (protocolVersion == null) {
                return defaultProtocol.decode(ctx, deserializer);
            }

            const protocol = protocols.get(protocolVersion);
            if (protocol == null) {
                return defaultProtocol.decode(ctx, deserializer);
            }

            return protocol.decode(ctx, deserializer);
        }
    })();
}



export class RecursiveStructEndec<T> extends AbstractStructEndec<T> {
    private readonly wrapped: () => StructEndec<T>;

    constructor(wrapped: (structEndec: StructEndec<T>) => StructEndec<T>) {
        super();
        this.wrapped = memoize(() => wrapped(this));
    }

    override encodeStruct(
        ctx: SerializationContext,
        serializer: Serializer<any>,
        struct: StructSerializer,
        value: T
    ): void {
        this.wrapped().encodeStruct(ctx, serializer, struct, value);
    }

    override decodeStruct(
        ctx: SerializationContext,
        deserializer: Deserializer<any>,
        struct: StructDeserializer
    ): T {
        return this.wrapped().decodeStruct(ctx, deserializer, struct);
    }
}
