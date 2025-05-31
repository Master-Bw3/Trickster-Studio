package me.maplesyrum.tricksterstudio.spell.fragment


import Identifier
import me.maplesyrum.tricksterstudio.spell.executer.SerializedSpellInstruction
import me.maplesyrum.tricksterstudio.spell.executer.SpellInstructionType
import io.kvision.utils.obj
import js.typedarrays.toByteArray
import js.typedarrays.toUint8Array
import me.maplesyrum.tricksterstudio.endec.PROTOCOL_VERSION_ATTRIBUTE
import me.maplesyrum.tricksterstudio.endec.UBER_COMPACT_ATTRIBUTE
import me.maplesyrum.tricksterstudio.endec.lazyEndec
import me.maplesyrum.tricksterstudio.endec.lazyStruct
import me.maplesyrum.tricksterstudio.endec.protocolVersionAlternatives
import me.maplesyrum.tricksterstudio.external.pako.gzip
import me.maplesyrum.tricksterstudio.external.pako.ungzip
import me.maplesyrum.tricksterstudio.external.pixi.HTMLText
import me.maplesyrum.tricksterstudio.spell.executer.SpellInstruction
import tree.maple.kendec.*
import tree.maple.kendec.format.buffer.BufferDeserializer
import tree.maple.kendec.format.buffer.BufferSerializer
import tree.maple.kendec.util.createBuffer
import tree.maple.kendec.util.writeByte
import web.encoding.atob
import web.encoding.btoa

val GZIP_HEADER = byteArrayOf(0x1f, (0x8b).toByte(), 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (0xff).toByte());


abstract class Fragment : SpellInstruction {
    private var formattedText: HTMLText? = null
    private var formattedTextWidth: Double? = null

    open fun asFormattedText(): HTMLText {
        return HTMLText(obj {
            text = """<span style="color: #${type().color.map{ it.toInt() }.orElseGet { 0xaaaaaa }.toString(16)}">${this@Fragment}</span>"""
            style = obj { fill = 0xffffff }
        })
    }

    fun asFormattedTextCached(): Pair<HTMLText, Double> {
        if (formattedText == null) {
            formattedText = asFormattedText()
            formattedTextWidth = formattedText!!.width
        }
        return Pair(formattedText!!, formattedTextWidth!!)
    }

    abstract fun type(): FragmentType<out Fragment>


    override fun asSerialized(): SerializedSpellInstruction {
        return SerializedSpellInstruction(SpellInstructionType.FRAGMENT, this)
    }

    fun toBase64(): String {
        return btoa(this.toBytes().map { it.toInt().toChar() }.joinToString(separator = ""))
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
        var gzippedBytes = gzip(bytes.toUint8Array())
        // Remove GZIP header (first 10 bytes)
        return gzippedBytes.toByteArray().copyOfRange(10, gzippedBytes.length)
    }

    companion object {
        val ENDEC: StructEndec<Fragment> = lazyStruct {
            dispatchedStructEndecOf(
                { it!!.endec },
                { obj: Fragment -> obj.type() as FragmentType<Fragment> },
                ifAttr(
                    UBER_COMPACT_ATTRIBUTE,
                    protocolVersionAlternatives(
                        mapOf<Int, Endec<FragmentType<*>>>(
                            1 to FragmentType.INT_ID_ENDEC,
                            2 to FragmentType.INT_ID_ENDEC,
                            3 to FragmentType.INT_ID_ENDEC
                        ),
                        Identifier.ENDEC.xmap({ FragmentType.REGISTRY[it]!! }, { it.getId()!! })
                )
                ).orElse(
                    Identifier.ENDEC.xmap({ FragmentType.REGISTRY[it]!! }, { it.getId()!! })
                )
            )
        }


        fun fromBase64(string: String): Fragment {
            return fromBytes(atob(string).split("").drop(1).map { it[0].code.toByte() }.toByteArray());
        }

        fun fromBytes(bytes: ByteArray): Fragment {
            val unzippedBytes = ungzip(GZIP_HEADER.plus(bytes).toUint8Array()).toByteArray()

            val buf = createBuffer(unzippedBytes)
            val protocolVersion = buf.readByte().toInt()
            return if (protocolVersion < 3) {
                fromBytesOld(protocolVersion, buf)
            } else {
                ENDEC.decode(
                    SerializationContext.empty().withAttributes(
                        UBER_COMPACT_ATTRIBUTE,
                            PROTOCOL_VERSION_ATTRIBUTE.instance(protocolVersion)
                    ),
                    BufferDeserializer(buf)
                )
            }
        }

        fun fromBytesOld(protocolVersion: Int, buf: dynamic): Fragment {
            return FragmentType.REGISTRY[Identifier("trickster", "spell_part")]!!.endec.decode(
                SerializationContext.empty().withAttributes(
                    UBER_COMPACT_ATTRIBUTE,
                        PROTOCOL_VERSION_ATTRIBUTE.instance(protocolVersion)
                ),
                BufferDeserializer(buf)
            )
        }
    }

    abstract override fun toString(): String
}


