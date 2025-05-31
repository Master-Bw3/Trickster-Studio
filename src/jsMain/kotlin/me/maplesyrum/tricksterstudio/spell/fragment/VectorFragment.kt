package me.maplesyrum.tricksterstudio.spell.fragment

import io.kvision.utils.obj
import me.maplesyrum.tricksterstudio.endec.Vector
import me.maplesyrum.tricksterstudio.endec.vectorEndec
import me.maplesyrum.tricksterstudio.external.pixi.HTMLText
import tree.maple.kendec.PrimitiveEndecs
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder
import kotlin.text.append


class VectorFragment(vector: Vector) : Fragment() {
    val vector: Vector

    init {
        if (vector.x.isNaN() || vector.y.isNaN() || vector.z.isNaN()) {
            throw Exception("NAN")
        }

        this.vector = vector
    }


    override fun type(): FragmentType<*> {
        return FragmentType.VECTOR;
    }

    override fun asFormattedText(): HTMLText {
        val result = buildString {
            append("(")
            append(NumberFragment(vector.x).asFormattedText().text)
            append(", ")
            append(NumberFragment(vector.y).asFormattedText().text)
            append(", ")
            append(NumberFragment(vector.z).asFormattedText().text)
            append(")")
        }
        return HTMLText(
            obj {
                text = "<span style=\"color: #${type().color.map{ it.toInt() }.orElseGet { 0xaaaaaa }.toString(16)}\">$result</span>"
            }
        )
    }


    override fun toString(): String {
        return "(${vector.x}, ${vector.y}, ${vector.z})"
    }

    companion object {
        val ENDEC: StructEndec<VectorFragment> = StructEndecBuilder.of(
            vectorEndec(PrimitiveEndecs.DOUBLE, {x, y, z -> Vector(x, y, z)}, Vector::x, Vector::y, Vector::z)
        .fieldOf("vector") { fragment -> fragment.vector }
        ) { vector -> VectorFragment(vector) }

        val ZERO: VectorFragment =
            VectorFragment(Vector(0.0, 0.0, 0.0))
    }
}
