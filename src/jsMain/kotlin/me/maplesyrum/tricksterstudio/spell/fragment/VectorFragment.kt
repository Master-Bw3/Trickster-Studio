package dev.enjarai.trickster.spell.fragment

import dev.enjarai.trickster.EndecTomfoolery
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
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import org.joml.Vector3d
import org.joml.Vector3dc
import java.util.Objects

class VectorFragment(vector: Vector3dc) : AddableFragment, SubtractableFragment, MultiplicableFragment,
    DivisibleFragment, RoundableFragment {
    private val vector: Vector3dc

    init {
        if (Double.isNaN(vector.x()) || Double.isNaN(vector.y()) || Double.isNaN(vector.z())) {
            throw NaNBlunder()
        }

        this.vector = vector
    }

    fun vector(): Vector3dc {
        return vector
    }

    fun x(): kotlin.Double {
        return vector.x()
    }

    fun y(): kotlin.Double {
        return vector.y()
    }

    fun z(): kotlin.Double {
        return vector.z()
    }

    @Override
    fun type(): FragmentType<*> {
        return FragmentType.VECTOR
    }

    @Override
    fun asText(): Text {
        return Text.literal("(")
            .append(NumberFragment(vector.x()).asFormattedText())
            .append(", ")
            .append(NumberFragment(vector.y()).asFormattedText())
            .append(", ")
            .append(NumberFragment(vector.z()).asFormattedText())
            .append(")")
    }

    @get:Override
    val weight: Int
        get() = 24

    @Override
    @Throws(BlunderException::class)
    fun add(other: Fragment?): AddableFragment? {
        if (other is dev.enjarai.trickster.spell.fragment.VectorFragment) {
            return dev.enjarai.trickster.spell.fragment.VectorFragment(vector.add(other.vector, Vector3d()))
        } else if (other is NumberFragment) {
            return dev.enjarai.trickster.spell.fragment.VectorFragment(
                Vector3d(
                    vector.x() + other.number(),
                    vector.y() + other.number(),
                    vector.z() + other.number()
                )
            )
        }

        throw IncompatibleTypesBlunder(Tricks.ADD)
    }

    @Override
    @Throws(BlunderException::class)
    fun subtract(other: Fragment?): SubtractableFragment? {
        if (other is dev.enjarai.trickster.spell.fragment.VectorFragment) {
            return dev.enjarai.trickster.spell.fragment.VectorFragment(vector.sub(other.vector, Vector3d()))
        } else if (other is NumberFragment) {
            return dev.enjarai.trickster.spell.fragment.VectorFragment(
                Vector3d(
                    vector.x() - other.number(),
                    vector.y() - other.number(),
                    vector.z() - other.number()
                )
            )
        }

        throw IncompatibleTypesBlunder(Tricks.SUBTRACT)
    }

    @Override
    @Throws(BlunderException::class)
    fun multiply(other: Fragment?): MultiplicableFragment? {
        if (other is dev.enjarai.trickster.spell.fragment.VectorFragment) {
            return dev.enjarai.trickster.spell.fragment.VectorFragment(vector.mul(other.vector, Vector3d()))
        } else if (other is NumberFragment) {
            return dev.enjarai.trickster.spell.fragment.VectorFragment(vector.mul(other.number(), Vector3d()))
        }

        throw IncompatibleTypesBlunder(Tricks.MULTIPLY)
    }

    @Override
    @Throws(BlunderException::class)
    fun divide(other: Fragment?): DivisibleFragment? {
        if (other is dev.enjarai.trickster.spell.fragment.VectorFragment) {
            if (other.vector.x() * other.vector.y() * other.vector.z() === 0) {
                throw DivideByZeroBlunder(Tricks.DIVIDE)
            }

            return dev.enjarai.trickster.spell.fragment.VectorFragment(vector.div(other.vector, Vector3d()))
        } else if (other is NumberFragment) {
            if (other.number() === 0) {
                throw DivideByZeroBlunder(Tricks.DIVIDE)
            }

            return dev.enjarai.trickster.spell.fragment.VectorFragment(vector.div(other.number(), Vector3d()))
        }

        throw IncompatibleTypesBlunder(Tricks.DIVIDE)
    }

    @Override
    @Throws(BlunderException::class)
    fun floor(): RoundableFragment? {
        return dev.enjarai.trickster.spell.fragment.VectorFragment(vector.floor(Vector3d()))
    }

    @Override
    @Throws(BlunderException::class)
    fun ceil(): RoundableFragment? {
        return dev.enjarai.trickster.spell.fragment.VectorFragment(vector.ceil(Vector3d()))
    }

    @Override
    @Throws(BlunderException::class)
    fun round(): RoundableFragment? {
        return dev.enjarai.trickster.spell.fragment.VectorFragment(vector.round(Vector3d()))
    }

    @Override
    fun fuzzyEquals(other: Fragment?): kotlin.Boolean {
        if (other is dev.enjarai.trickster.spell.fragment.VectorFragment) {
            return other.vector.equals(vector, 1 / 16.0)
        }

        return false
    }

    @Override
    fun equals(obj: Object?): kotlin.Boolean {
        return obj is dev.enjarai.trickster.spell.fragment.VectorFragment && obj.vector.equals(vector)
    }

    fun toBlockPos(): BlockPos {
        return BlockPos.ofFloored(vector.x(), vector.y(), vector.z())
    }

    fun toDirection(): Direction {
        return Direction.getFacing(vector.x(), vector.y(), vector.z())
    }

    @Override
    fun hashCode(): kotlin.Int {
        return Objects.hash(vector)
    }

    @Override
    fun toString(): String? {
        return "VectorFragment[" + "vector=" + vector + "]"
    }

    companion object {
        val ENDEC: StructEndec<dev.enjarai.trickster.spell.fragment.VectorFragment?>? = StructEndecBuilder.of(
            EndecTomfoolery.< Double,
            Vector3dc > vectorEndec<Double?, Vector3dc?>(
                Endec.DOUBLE,
                { Vector3d() },
                Vector3dc::x,
                Vector3dc::y,
                Vector3dc::z
            )
                .fieldOf("vector", { obj: dev.enjarai.trickster.spell.fragment.VectorFragment? -> obj!!.vector() }),
            { vector: Vector3dc -> VectorFragment(vector) }
        )
        val ZERO: dev.enjarai.trickster.spell.fragment.VectorFragment =
            dev.enjarai.trickster.spell.fragment.VectorFragment(Vector3d())

        fun of(pos: BlockPos): dev.enjarai.trickster.spell.fragment.VectorFragment {
            val dPos: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
                pos.toCenterPos()
            return dev.enjarai.trickster.spell.fragment.VectorFragment(Vector3d(dPos.getX(), dPos.getY(), dPos.getZ()))
        }

        fun of(pos: Vec3d): dev.enjarai.trickster.spell.fragment.VectorFragment {
            return dev.enjarai.trickster.spell.fragment.VectorFragment(Vector3d(pos.getX(), pos.getY(), pos.getZ()))
        }

        fun of(pos: Vec3i): dev.enjarai.trickster.spell.fragment.VectorFragment {
            return dev.enjarai.trickster.spell.fragment.VectorFragment(Vector3d(pos.getX(), pos.getY(), pos.getZ()))
        }
    }
}
