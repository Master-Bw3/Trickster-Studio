package me.maplesyrum.tricksterstudio.spell.fragment


import Identifier
import PROTOCOL_VERSION_ATTRIBUTE
import UBER_COMPACT_ATTRIBUTE
import dev.enjarai.trickster.spell.execution.SerializedSpellInstruction
import dev.enjarai.trickster.spell.execution.SpellInstructionType
import io.kvision.utils.Object
import io.kvision.utils.obj
import lazyEndec
import me.maplesyrum.tricksterstudio.spell.executer.SpellInstruction
import tree.maple.kendec.*
import tree.maple.kendec.format.buffer.BufferDeserializer
import tree.maple.kendec.format.buffer.BufferSerializer
import tree.maple.kendec.util.createBuffer
import tree.maple.kendec.util.writeByte

// External declarations for JS interop
external class HTMLText(options: Object) {
    var width: Int
}

external object window {
    val zlib: dynamic
}

abstract class Fragment : SpellInstruction {
    private var _formattedText: HTMLText? = null
    private var _formattedTextWidth: Int? = null

    open fun asFormattedText(): HTMLText {
        return HTMLText(obj{
            text = """<span style="color: #${type().color.toString(16)}">${this@Fragment}</span>"""
            style = jsObject { fill = 0xffffff }
        })
    }

    abstract fun type(): FragmentType<Fragment>

    fun asFormattedTextCached(): Pair<HTMLText, Int> {
        if (_formattedText == null) {
            _formattedText = asFormattedText()
            _formattedTextWidth = _formattedText!!.width
        }
        return Pair(_formattedText!!, _formattedTextWidth!!)
    }

    override fun asSerialized(): SerializedSpellInstruction {
        return SerializedSpellInstruction(SpellInstructionType.FRAGMENT, this)
    }

    fun toBase64(): String {
        return encodeBase64(toBytes())
    }

    fun toBytes(): ByteArray {
        val buf = createBuffer()
        writeByte(buf, 4)
        ENDEC.encode(
            SerializationContext.empty().withAttributes(
                    UBER_COMPACT_ATTRIBUTE,
                    PROTOCOL_VERSION_ATTRIBUTE.instance(4)

            ),
            BufferSerializer(buf),
            this
        )
        val bytes = buf.readByteArray()
        val gzippedBytes = window.zlib.gzip(bytes)
        // Remove GZIP header (first 10 bytes)
        return gzippedBytes.unsafeCast<ByteArray>().copyOfRange(10, gzippedBytes.length)
    }

    companion object {
        val ENDEC: StructEndec<Fragment> = lazyEndec(
            {
                dispatchedEndecOf(
                    { it!!.endec },
                    { obj: Fragment -> obj.type() },
                    ifAttr(
                        UBER_COMPACT_ATTRIBUTE,
                        PrimitiveEndecs.INT.xmap (::getFragmentTypeFromInt) {
                            it.intId
                        }
                    ).orElse(
                        Identifier.ENDEC.xmap({ fragmentTypes[it]!!}, { it.id })
                    )
                )
            }
        )


        fun fromBase64(string: String): Fragment {
            return fromBytes(decodeBase64(string))
        }

        fun fromBytes(bytes: ByteArray): Fragment {
            val unzippedBytes = window.zlib.ungzip(GZIP_HEADER + bytes)
            val buf = createBuffer(unzippedBytes)
            val protocolVersion = buf.readByte()
            return if (protocolVersion < 3) {
                fromBytesOld(protocolVersion, buf)
            } else {
                ENDEC.decode(
                    SerializationContext.empty().withAttributes(
                        listOf(
                            UBER_COMPACT_ATTRIBUTE,
                            PROTOCOL_VERSION_ATTRIBUTE.instance(protocolVersion)
                        )
                    ),
                    BufferDeserializer(buf)
                )
            }
        }

        fun fromBytesOld(protocolVersion: Int, buf: dynamic): Fragment {
            return getFragmentType(Identifier("trickster", "spell_part"))!!.endec.decode(
                SerializationContext.empty().withAttributes(
                    listOf(
                        UBER_COMPACT_ATTRIBUTE,
                        PROTOCOL_VERSION_ATTRIBUTE.instance(protocolVersion)
                    )
                ),
                BufferDeserializer(buf)
            )
        }
    }

    abstract override fun toString(): String
}


val fragmentTypesIntLookup = HashMap<Int, Identifier>();
val fragmentTypes: Map<Identifier, FragmentType<Fragment>> = HashMap();

fun getFragmentTypeFromInt(intId: Int): FragmentType<Fragment> {
    var id = fragmentTypesIntLookup[intId];
    if (id == null) {
        throw Exception("Not a valid int id for fragment type");
    }
    return fragmentTypes[id]!!;
}