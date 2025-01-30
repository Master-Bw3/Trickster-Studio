import {
    PrimitiveEndecs,
    Endec,
    Deserializer,
    SerializationContext,
    Serializer,
    AbstractEndec,
    SelfDescribedDeserializer,
    SelfDescribedSerializer,
    Optional,
} from 'KEndec';

export function getKeyByValue<T, U>(map: Map<T, U>, value: U): T | null {
    for (let [key, val] of map.entries()) {
        if (val === value) {
            return key;
        }
    }
    return null;
}

export class Identifier {
    public static ENDEC: Endec<Identifier> = PrimitiveEndecs.STRING.xmap(
        (str) => Identifier.of(str),
        (ident) => ident.toString()
    );

    private readonly namespace: string;
    private readonly path: string;

    public constructor(namespace: string, path: string) {
        this.namespace = namespace;
        this.path = path;
    }

    public static of(id: string): Identifier {
        return Identifier.splitOn(id, ':');
    }

    public static splitOn(id: String, delimiter: string): Identifier {
        const i = id.indexOf(delimiter);
        if (i >= 0) {
            const string = id.substring(i + 1);
            if (i != 0) {
                const string2 = id.substring(0, i);
                return new Identifier(string2, string);
            }
        }
        throw new Error('invalid identifier');
    }

    public hashCode(): number {
        return ((31 * hashCodeOfString(this.namespace)) | 0) + hashCodeOfString(this.path) | 0;
    }

    public equals(obj: any): boolean {
        if (obj instanceof Identifier) {
            return this.namespace === obj.namespace && this.path === obj.path;
        } else return false;
    }

    public toString(): string {
        return this.namespace + ':' + this.path;
    }
}

function hashCodeOfString(s: string): number {
    let hash = 0;
    for (let i = 0; i < s.length; i++) {
      hash = (hash * 31 + s.charCodeAt(i)) | 0;
    }
    return hash;
  }

export abstract class Either<L, R> {
    static left<L, R>(value: L): Either<L, R> {
        return new Left(value);
    }

    static right<L, R>(value: R): Either<L, R> {
        return new Right(value);
    }

    abstract left(): Optional<L>

    abstract right(): Optional<R>

    abstract ifLeft(consumer: (value: L) => void): Either<L, R>;

    abstract ifRight(consumer: (value: R) => void): Either<L, R>;

    abstract map<T>(l: (value: L) => T, r: (value: R) => T,): T
}

class Left<L, R> extends Either<L, R> {

    private readonly value: L;

    constructor(value: L) {
        super();
        this.value = value;
    }

    ifLeft(consumer: (value: L) => void): Either<L, R> {
        consumer(this.value);
        return this;
    }

    ifRight(consumer: (value: R) => void): Either<L, R> {
        return this;
    }

    map<T>(l: (value: L) => T, r: (value: R) => T): T {
        return l(this.value)
    }

    left(): Optional<L> {
        return Optional.of(this.value)
    }

    right(): Optional<R> {
        return Optional.empty()
    }
}

class Right<L, R> extends Either<L, R> {
    private readonly value: R;

    constructor(value: R) {
        super();
        this.value = value;
    }

    ifLeft(consumer: (value: L) => void): Either<L, R> {
        return this;
    }

    ifRight(consumer: (value: R) => void): Either<L, R> {
        consumer(this.value);
        return this;
    }

    map<T>(l: (value: L) => T, r: (value: R) => T): T {
        return r(this.value)
    }

    left(): Optional<L> {
        return Optional.empty()
    }

    right(): Optional<R> {
        return Optional.of(this.value)
    }
}

function isSelfDescribedSerializer<T>(
    serializer: Serializer<T>
): serializer is SelfDescribedSerializer<T> {
    return 'readAny' in serializer;
}

function isSelfDescribedDeserializer<T>(
    deserializer: Deserializer<T>
): deserializer is SelfDescribedDeserializer<T> {
    return 'isSelfDescribedDescribedSerializer' in deserializer;
}

export class EitherEndec<L, R> extends AbstractEndec<Either<L, R>> {
    private readonly leftEndec: Endec<L>;
    private readonly rightEndec: Endec<R>;

    private readonly exclusive: boolean;

    constructor(leftEndec: Endec<L>, rightEndec: Endec<R>, exclusive: boolean) {
        super();
        this.leftEndec = leftEndec;
        this.rightEndec = rightEndec;
        this.exclusive = exclusive;
    }

    encode(ctx: SerializationContext, serializer: Serializer<unknown>, either: Either<L, R>): void {
        if (isSelfDescribedSerializer(serializer)) {
            either
                .ifLeft((left) => this.leftEndec.encode(ctx, serializer, left))
                .ifRight((right) => this.rightEndec.encode(ctx, serializer, right));
        } else {
            either
                .ifLeft((left) => {
                    serializer
                        .struct()
                        .field('is_left', ctx, PrimitiveEndecs.BOOLEAN, true)
                        .field('left', ctx, this.leftEndec, left);
                })
                .ifRight((right) => {
                    serializer
                        .struct()
                        .field('is_left', ctx, PrimitiveEndecs.BOOLEAN, false)
                        .field('right', ctx, this.rightEndec, right);
                });
        }
    }

    decode(ctx: SerializationContext, deserializer: Deserializer<any>): Either<L, R> {
        const selfDescribing = isSelfDescribedDeserializer(deserializer);

        if (selfDescribing) {
            var leftResult: Either<L, R> | null = null;
            try {
                leftResult = Either.left(
                    deserializer.tryRead((deserializer1) =>
                        this.leftEndec.decode(ctx, deserializer1)
                    )
                );
            } catch (e) {}

            if (!this.exclusive && leftResult != null) return leftResult;

            var rightResult: Either<L, R> | null = null;
            try {
                rightResult = Either.right(
                    deserializer.tryRead((deserializer1) =>
                        this.rightEndec.decode(ctx, deserializer1)
                    )
                );
            } catch (e) {}

            if (this.exclusive && leftResult != null && rightResult != null) {
                throw new Error(
                    'Both alternatives read successfully, can not pick the correct one; first: ' +
                        leftResult +
                        ' second: ' +
                        rightResult
                );
            }

            if (leftResult != null) return leftResult;
            if (rightResult != null) return rightResult;

            throw new Error('Neither alternative read successfully');
        } else {
            var struct = deserializer.struct();
            if (struct.field('is_left', ctx, PrimitiveEndecs.BOOLEAN, null)) {
                return Either.left(struct.field('left', ctx, this.leftEndec, null));
            } else {
                return Either.right(struct.field('right', ctx, this.rightEndec, null));
            }
        }
    }
}

export function memoize<T>(supplier: () => T):  () => T {
    let value: T | undefined = undefined;
    return () => {
        if (value === undefined) value = supplier()
        return value
    }
}