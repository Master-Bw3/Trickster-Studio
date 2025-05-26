import tree.maple.kendec.Endec
import tree.maple.kendec.PrimitiveEndecs
import tree.maple.kendec.Deserializer
import tree.maple.kendec.SerializationContext
import tree.maple.kendec.Serializer
import tree.maple.kendec.AbstractEndec
import tree.maple.kendec.SelfDescribedDeserializer
import tree.maple.kendec.SelfDescribedSerializer
import tree.maple.kendec.util.Optional

class Identifier(val namespace: String, val path: String) {
    companion object {
        val ENDEC: Endec<Identifier> = PrimitiveEndecs.STRING.xmap(
            { str -> of(str) },
            { ident -> ident.toString() }
        )

        fun of(id: String): Identifier {
            return splitOn(id, ":")
        }

        fun splitOn(id: String, delimiter: String): Identifier {
            val i = id.indexOf(delimiter)
            if (i >= 0) {
                val string = id.substring(i + 1)
                if (i != 0) {
                    val string2 = id.substring(0, i)
                    return Identifier(string2, string)
                }
            }
            throw IllegalArgumentException("invalid identifier")
        }
    }

    override fun hashCode(): Int {
        return 31 * namespace.hashCode() + path.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is Identifier && other.namespace == namespace && other.path == path
    }

    override fun toString(): String {
        return "$namespace:$path"
    }
}

abstract class Either<L : Any, R: Any> {
    companion object {
        fun <L : Any, R: Any> left(value: L): Either<L, R> = Left(value)
        fun <L : Any, R: Any> right(value: R): Either<L, R> = Right(value)
    }

    abstract fun left(): Optional<L>
    abstract fun right(): Optional<R>
    abstract fun ifLeft(consumer: (L) -> Unit): Either<L, R>
    abstract fun ifRight(consumer: (R) -> Unit): Either<L, R>
    abstract fun <T> map(l: (L) -> T, r: (R) -> T): T
}

class Left<L : Any, R: Any>(private val value: L) : Either<L, R>() {
    override fun ifLeft(consumer: (L) -> Unit): Either<L, R> {
        consumer(value)
        return this
    }

    override fun ifRight(consumer: (R) -> Unit): Either<L, R> = this

    override fun <T> map(l: (L) -> T, r: (R) -> T): T = l(value)

    override fun left(): Optional<L> = Optional.of<L>(value)
    override fun right(): Optional<R> = Optional.empty()
}

class Right<L : Any, R: Any>(private val value: R) : Either<L, R>() {
    override fun ifLeft(consumer: (L) -> Unit): Either<L, R> = this

    override fun ifRight(consumer: (R) -> Unit): Either<L, R> {
        consumer(value)
        return this
    }

    override fun <T> map(l: (L) -> T, r: (R) -> T): T = r(value)

    override fun left(): Optional<L> = Optional.empty()
    override fun right(): Optional<R> = Optional.of(value)
}

private fun hashCodeOfString(s: String): Int {
    var hash = 0
    for (c in s) {
        hash = (hash * 31 + c.code)
    }
    return hash
}

private fun <T> isSelfDescribedSerializer(serializer: Serializer<T>): Boolean {
    return serializer is SelfDescribedSerializer<*>
}

private fun <T> isSelfDescribedDeserializer(deserializer: Deserializer<T>): Boolean {
    return deserializer is SelfDescribedDeserializer<*>
}

class EitherEndec<L : Any, R: Any>(
    private val leftEndec: Endec<L>,
    private val rightEndec: Endec<R>,
    private val exclusive: Boolean
) : AbstractEndec<Either<L, R>>() {
    override fun encode(ctx: SerializationContext, serializer: Serializer<*>, either: Either<L, R>) {
        if (isSelfDescribedSerializer(serializer)) {
            either
                .ifLeft { left -> leftEndec.encode(ctx, serializer, left) }
                .ifRight { right -> rightEndec.encode(ctx, serializer, right) }
        } else {
            either
                .ifLeft { left ->
                    val struct = serializer.struct()
                    struct.field("is_left", ctx, PrimitiveEndecs.BOOLEAN, true)
                    struct.field("left", ctx, leftEndec, left)
                }
                .ifRight { right ->
                    val struct = serializer.struct()
                    struct.field("is_left", ctx, PrimitiveEndecs.BOOLEAN, false)
                    struct.field("right", ctx, rightEndec, right)
                }
        }
    }

    override fun decode(ctx: SerializationContext, deserializer: Deserializer<*>): Either<L, R> {
        if (isSelfDescribedDeserializer(deserializer)) {
            var leftResult: Either<L, R>? = null
            try {
                leftResult = Either.left(
                    (deserializer as SelfDescribedDeserializer<*>).tryRead { d1 ->
                        leftEndec.decode(ctx, d1)
                    }
                )
            } catch (_: Throwable) {}

            if (!exclusive && leftResult != null) return leftResult

            var rightResult: Either<L, R>? = null
            try {
                rightResult = Either.right(
                    (deserializer as SelfDescribedDeserializer<*>).tryRead { d1 ->
                        rightEndec.decode(ctx, d1)
                    }
                )
            } catch (_: Throwable) {}

            if (exclusive && leftResult != null && rightResult != null) {
                throw IllegalStateException(
                    "Both alternatives read successfully, can not pick the correct one; first: $leftResult second: $rightResult"
                )
            }

            if (leftResult != null) return leftResult
            if (rightResult != null) return rightResult

            throw IllegalStateException("Neither alternative read successfully")
        } else {
            val struct = deserializer.struct()
            return if (struct.field("is_left", ctx, PrimitiveEndecs.BOOLEAN, null)) {
                Either.left(struct.field("left", ctx, leftEndec, null))
            } else {
                Either.right(struct.field("right", ctx, rightEndec, null))
            }
        }
    }

}

fun <T> memoize(supplier: () -> T): () -> T {
    var value: T? = null
    return {
        if (value == null) value = supplier()
        value!!
    }
}
