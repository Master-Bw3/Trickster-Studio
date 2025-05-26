import me.maplesyrum.tricksterstudio.external.pixi.Point
import tree.maple.kendec.AbstractStructEndec
import tree.maple.kendec.Deserializer
import tree.maple.kendec.Endec
import tree.maple.kendec.PrimitiveEndecs
import tree.maple.kendec.SerializationAttribute
import tree.maple.kendec.SerializationContext
import tree.maple.kendec.Serializer
import tree.maple.kendec.StructDeserializer
import tree.maple.kendec.StructEndec
import tree.maple.kendec.StructSerializer
import tree.maple.kendec.endecOf
import tree.maple.kendec.ifAttr
import tree.maple.kendec.impl.StructEndecBuilder
import tree.maple.kendec.impl.StructField
import tree.maple.kendec.util.Optional
import tree.maple.kendec.util.uuidFromString

class UndashedUuid {
    companion object {
        fun fromString(string: String): dynamic {
            if (string.indexOf('-') != -1) {
                throw IllegalArgumentException("Invalid undashed UUID string: $string")
            }
            return fromStringLenient(string)
        }

        fun fromStringLenient(string: String): dynamic {
            return uuidFromString(
                string.replace(Regex("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})"), "$1-$2-$3-$4-$5")
            )
        }

        fun toString(uuid: dynamic): String {
            return uuid.toString().replace("-", "")
        }
    }
}

val POINT: Endec<Point> = PrimitiveEndecs.BYTES.xmap(
    { list -> Point(list[0].toDouble(), list[1].toDouble()) },
    { entry -> byteArrayOf(entry.x.toInt().toByte(), entry.y.toInt().toByte()) }
)

class Vector(val x: Double, val y: Double, val z: Double) {
    override fun toString(): String = "Vector[x=$x, y=$y, z=$z]"
}

class VectorI(val x: Int, val y: Int, val z: Int) {
    init {
        if (x.toDouble() != x.toDouble().toInt().toDouble() ||
            y.toDouble() != y.toDouble().toInt().toDouble() ||
            z.toDouble() != z.toDouble().toInt().toDouble()
        ) throw IllegalArgumentException("only integer components are allowed")
    }

    override fun toString(): String = "Vector[x=$x, y=$y, z=$z]"
}

val ALWAYS_READABLE_BLOCK_POS: Endec<VectorI> = vectorEndec(
    PrimitiveEndecs.INT,
    { x, y, z -> VectorI(x, y, z) },
    { vec -> vec.x },
    { vec -> vec.y },
    { vec -> vec.z }
)

val UUID: Endec<dynamic> = PrimitiveEndecs.STRING.xmap(
    { UndashedUuid.fromStringLenient(it) },
    { it.toString() }
)

val UBER_COMPACT_ATTRIBUTE: SerializationAttribute.Marker =
    SerializationAttribute.marker("uber_compact")

val PROTOCOL_VERSION_ATTRIBUTE: SerializationAttribute.WithValue<Int> =
    SerializationAttribute.withValue("protocol_version")

val VECTOR_3D_ENDEC: Endec<Vector> = vectorEndec(
    PrimitiveEndecs.DOUBLE,
    { x, y, z -> Vector(x, y, z) },
    { vec -> vec.x },
    { vec -> vec.y },
    { vec -> vec.z }
)

val CODEC_SAFE: SerializationAttribute.Marker =
    SerializationAttribute.marker("codec_safe")

fun <C, V> vectorEndec(
    componentEndec: Endec<C>,
    constructor: (C, C, C) -> V,
    xGetter: (V) -> C,
    yGetter: (V) -> C,
    zGetter: (V) -> C
): Endec<V> {
    return componentEndec
        .listOf()
        .validate { ints ->
            if (ints.size != 3) {
                throw IllegalArgumentException("vector array must have three elements")
            }
        }
        .xmap(
            { components -> constructor(components[0], components[1], components[2]) },
            { vector -> listOf(xGetter(vector), yGetter(vector), zGetter(vector)) }
        )
}

fun <T: Any, A : T> withAlternative(
    primary: Endec<T>,
    alternative: Endec<A>
): Endec<T> {
    return EitherEndec(primary, alternative, false).xmap(
        { either -> either.map({ it }, { it }) },
        { Either.left(it) }
    )
}

fun <T: Any> safeOptionalOf(endec: Endec<T>): Endec<Optional<T>> {
    return ifAttr(
        CODEC_SAFE,
        endecOf<Optional<T>>(
            { ctx, serializer, value ->
                val struct = serializer.struct()
                struct.field("present", ctx, PrimitiveEndecs.BOOLEAN, value.isPresent())
                value.ifPresent { t -> struct.field("value", ctx, endec, t) }
            },
            { ctx, deserializer ->
                val struct = deserializer.struct()
                if (struct.field("present", ctx, PrimitiveEndecs.BOOLEAN, null)) {
                    Optional.of(struct.field("value", ctx, endec, null))
                } else {
                    Optional.empty()
                }
            }
        )
    ).orElse(endec.optionalOf())
}

fun <T> funnyFieldOf(endec: Endec<T>, key: String): StructEndec<T> {
    return StructEndecBuilder.of(
        endec.fieldOf(key) { it },
        { it }
    )
}

fun <T> recursive(wrapped: (StructEndec<T>) -> StructEndec<T>): StructEndec<T> {
    return RecursiveStructEndec(wrapped)
}

fun <T> lazyEndec(supplier: () -> StructEndec<T>): StructEndec<T> {
    return recursive { supplier() }
}

fun <T> unit(value: T): StructEndec<T> {
    return object : AbstractStructEndec<T>() {
        override fun encodeStruct(
            ctx: SerializationContext,
            serializer: Serializer<*>,
            struct: StructSerializer,
            value: T
        ) {}

        override fun decodeStruct(
            ctx: SerializationContext,
            deserializer: Deserializer<*>,
            struct: StructDeserializer
        ): T = value
    }
}

fun <T> unitFromSupplier(value: () -> T): StructEndec<T> {
    return object : AbstractStructEndec<T>() {
        override fun encodeStruct(
            ctx: SerializationContext,
            serializer: Serializer<*>,
            struct: StructSerializer,
            value: T
        ) {}

        override fun decodeStruct(
            ctx: SerializationContext,
            deserializer: Deserializer<*>,
            struct: StructDeserializer
        ): T = value()
    }
}

fun <T> protocolVersionAlternatives(
    protocols: Map<Int, Endec<T>>,
    defaultProtocol: Endec<T>
): Endec<T> {
    return object : AbstractEndec<T>() {
        override fun encode(ctx: SerializationContext, serializer: Serializer<*>, value: T) {
            val protocolVersion = ctx.getAttributeValue(PROTOCOL_VERSION_ATTRIBUTE)
            if (protocolVersion == null) {
                defaultProtocol.encode(ctx, serializer, value)
                return
            }
            val protocol = protocols[protocolVersion]
            if (protocol == null) {
                defaultProtocol.encode(ctx, serializer, value)
                return
            }
            protocol.encode(ctx, serializer, value)
        }

        override fun decode(ctx: SerializationContext, deserializer: Deserializer<*>): T {
            val protocolVersion = ctx.getAttributeValue(PROTOCOL_VERSION_ATTRIBUTE)
            if (protocolVersion == null) {
                return defaultProtocol.decode(ctx, deserializer)
            }
            val protocol = protocols[protocolVersion]
            if (protocol == null) {
                return defaultProtocol.decode(ctx, deserializer)
            }
            return protocol.decode(ctx, deserializer)
        }
    }
}

open class AbstractEndec<T> : Endec<T> {
    override fun encode(ctx: SerializationContext, serializer: Serializer<*>, value: T) {
        throw NotImplementedError()
    }
    override fun decode(ctx: SerializationContext, deserializer: Deserializer<*>): T {
        throw NotImplementedError()
    }
    override fun <U> xmap(f: (T) -> U, g: (U) -> T): Endec<U> {
        throw NotImplementedError()
    }
    override fun validate(validator: (T) -> Unit): Endec<T> {
        throw NotImplementedError()
    }
    override fun optionalOf(): Endec<Optional<T & Any>> {
        throw NotImplementedError()
    }
    override fun listOf(): Endec<List<T>> {
        throw NotImplementedError()
    }
    override fun <S> fieldOf(key: String, getter: (S) -> T): StructField<S, T> {
        throw NotImplementedError()
    }
}

class RecursiveStructEndec<T>(
    wrapped: (StructEndec<T>) -> StructEndec<T>
) : AbstractStructEndec<T>() {
    private val wrappedMemoized = memoize { wrapped(this) }

    override fun encodeStruct(
        ctx: SerializationContext,
        serializer: Serializer<*>,
        struct: StructSerializer,
        value: T
    ) {
        wrappedMemoized().encodeStruct(ctx, serializer, struct, value)
    }

    override fun decodeStruct(
        ctx: SerializationContext,
        deserializer: Deserializer<*>,
        struct: StructDeserializer
    ): T {
        return wrappedMemoized().decodeStruct(ctx, deserializer, struct)
    }
}
