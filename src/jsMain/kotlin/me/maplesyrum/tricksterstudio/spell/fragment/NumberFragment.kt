package dev.enjarai.trickster.spell.fragment

import dev.enjarai.trickster.spell.Fragment
import dev.enjarai.trickster.spell.blunder.BlunderException
import dev.enjarai.trickster.spell.blunder.DivideByZeroBlunder
import dev.enjarai.trickster.spell.blunder.IncompatibleTypesBlunder
import dev.enjarai.trickster.spell.blunder.NaNBlunder
import dev.enjarai.trickster.spell.trick.Tricks
import me.maplesyrum.tricksterstudio.spell.fragment.FragmentType
import tree.maple.kendec.Endec
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder
import net.minecraft.text.Text
import org.joml.Vector3d
import java.util.Objects

class NumberFragment(number: kotlin.Double) : AddableFragment, SubtractableFragment, MultiplicableFragment,
    DivisibleFragment, RoundableFragment {
    private val number: kotlin.Double

    init {
        if (Double.isNaN(number)) throw NaNBlunder()

        this.number = number
    }

    @Override
    fun type(): FragmentType<*> {
        return FragmentType.NUMBER
    }

    @Override
    fun asText(): Text {
        return Text.literal(String.format("%.2f", number))
    }

    @get:Override
    val weight: Int
        get() = 8

    @Override
    @Throws(BlunderException::class)
    fun add(other: Fragment?): AddableFragment? {
        if (other is dev.enjarai.trickster.spell.fragment.NumberFragment) {
            return dev.enjarai.trickster.spell.fragment.NumberFragment(number + other.number)
        } else if (other is VectorFragment) {
            val vector: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = other.vector()
            return VectorFragment(Vector3d(number + vector.x(), number + vector.y(), number + vector.z()))
        }

        throw IncompatibleTypesBlunder(Tricks.ADD)
    }

    @Override
    @Throws(BlunderException::class)
    fun subtract(other: Fragment?): SubtractableFragment? {
        if (other is dev.enjarai.trickster.spell.fragment.NumberFragment) {
            return dev.enjarai.trickster.spell.fragment.NumberFragment(number - other.number)
        } else if (other is VectorFragment) {
            val vector: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = other.vector()
            return VectorFragment(Vector3d(number - vector.x(), number - vector.y(), number - vector.z()))
        }

        throw IncompatibleTypesBlunder(Tricks.SUBTRACT)
    }

    @Override
    @Throws(BlunderException::class)
    fun multiply(other: Fragment?): MultiplicableFragment? {
        if (other is dev.enjarai.trickster.spell.fragment.NumberFragment) {
            return dev.enjarai.trickster.spell.fragment.NumberFragment(number * other.number)
        } else if (other is VectorFragment) {
            val vector: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = other.vector()
            return VectorFragment(Vector3d(number * vector.x(), number * vector.y(), number * vector.z()))
        }

        throw IncompatibleTypesBlunder(Tricks.MULTIPLY)
    }

    @Override
    @Throws(BlunderException::class)
    fun divide(other: Fragment?): DivisibleFragment? {
        if (other is dev.enjarai.trickster.spell.fragment.NumberFragment) {
            if (other.number == 0.0) {
                throw DivideByZeroBlunder(Tricks.DIVIDE)
            }

            return dev.enjarai.trickster.spell.fragment.NumberFragment(number / other.number)
        } else if (other is VectorFragment) {
            val vector: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = other.vector()

            if (vector.x() * vector.y() * vector.z() === 0) {
                throw DivideByZeroBlunder(Tricks.DIVIDE)
            }

            return VectorFragment(Vector3d(number / vector.x(), number / vector.y(), number / vector.z()))
        }

        throw IncompatibleTypesBlunder(Tricks.DIVIDE)
    }

    @Override
    @Throws(BlunderException::class)
    fun floor(): RoundableFragment? {
        return dev.enjarai.trickster.spell.fragment.NumberFragment(Math.floor(number))
    }

    @Override
    @Throws(BlunderException::class)
    fun ceil(): RoundableFragment? {
        return dev.enjarai.trickster.spell.fragment.NumberFragment(Math.ceil(number))
    }

    @Override
    @Throws(BlunderException::class)
    fun round(): RoundableFragment? {
        return dev.enjarai.trickster.spell.fragment.NumberFragment(Math.round(number))
    }

    @Override
    fun fuzzyEquals(other: Fragment?): kotlin.Boolean {
        if (other is dev.enjarai.trickster.spell.fragment.NumberFragment) {
            val precision = 1 / 16.0
            return other.number > number - precision && other.number < number + precision
        }

        return false
    }

    @Override
    fun equals(obj: Object?): kotlin.Boolean {
        return obj is dev.enjarai.trickster.spell.fragment.NumberFragment && obj.number == number
    }

    val isInteger: Boolean
        get() = number - Math.floor(number) === 0

    fun asInt(): kotlin.Int {
        return Math.floor(number) as kotlin.Int
    }

    fun number(): kotlin.Double {
        return number
    }

    @Override
    fun hashCode(): kotlin.Int {
        return Objects.hash(number)
    }

    @Override
    fun toString(): String? {
        return "NumberFragment[" + "number=" + number + "]"
    }

    companion object {
        val ENDEC: StructEndec<dev.enjarai.trickster.spell.fragment.NumberFragment?>? = StructEndecBuilder.of(
            Endec.DOUBLE.fieldOf(
                "number",
                { obj: dev.enjarai.trickster.spell.fragment.NumberFragment? -> obj!!.number() }),
            { number: kotlin.Double -> NumberFragment(number) }
        )
    }
}
