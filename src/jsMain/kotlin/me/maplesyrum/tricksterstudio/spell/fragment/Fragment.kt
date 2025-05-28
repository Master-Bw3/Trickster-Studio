package me.maplesyrum.tricksterstudio.spell.fragment


import Identifier
import me.maplesyrum.tricksterstudio.spell.executer.SerializedSpellInstruction
import me.maplesyrum.tricksterstudio.spell.executer.SpellInstructionType
import io.kvision.utils.obj
import me.maplesyrum.tricksterstudio.endec.PROTOCOL_VERSION_ATTRIBUTE
import me.maplesyrum.tricksterstudio.endec.UBER_COMPACT_ATTRIBUTE
import me.maplesyrum.tricksterstudio.endec.lazyEndec
import me.maplesyrum.tricksterstudio.external.pixi.HTMLText
import me.maplesyrum.tricksterstudio.spell.executer.SpellInstruction
import tree.maple.kendec.*
import tree.maple.kendec.format.buffer.BufferDeserializer
import tree.maple.kendec.format.buffer.BufferSerializer
import tree.maple.kendec.util.createBuffer
import tree.maple.kendec.util.writeByte


abstract class Fragment : SpellInstruction {
    private var formattedText: HTMLText? = null
    private var formattedTextWidth: Double? = null

    open fun asFormattedText(): HTMLText {
        return HTMLText(obj {
            text = """<span style="color: #${type().color.map(::toInt).orElse(0xaaaaaa).toString(16)}">${this@Fragment}</span>"""
            style = jsObject { fill = 0xffffff }
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
        return TODO(); //js("btoa(String.fromCharCode.apply(null, [...this.toBytes()]))");
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
        val gzippedBytes  = TODO(); //js("window.zlib.gzip(bytes)")
        // Remove GZIP header (first 10 bytes)
        return TODO(); // gzippedBytes.unsafeCast<ByteArray>().copyOfRange(10, gzippedBytes.length)
    }

    companion object {
        val ENDEC: StructEndec<Fragment> = lazyEndec(
            {
                dispatchedEndecOf(
                    { it!!.endec },
                    { obj: Fragment -> obj.type() as FragmentType<Fragment> },
                    ifAttr(
                        UBER_COMPACT_ATTRIBUTE,
                        PrimitiveEndecs.INT.xmap(::getFragmentTypeFromInt) {
                            it.intId
                        }
                    ).orElse(
                        Identifier.ENDEC.xmap({ fragmentTypes[it]!! }, { it.getId()!! })
                    )
                )
            }
        )


        fun fromBase64(string: String): Fragment {
            return TODO(); //fromBytes(js("new Uint8Array(atob(string).split('').map(char => char.charCodeAt(0)))"));
        }

        fun fromBytes(bytes: ByteArray): Fragment {
            val unzippedBytes = TODO(); //js("window.zlib.ungzip(GZIP_HEADER + bytes)")
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
            return FragmentType.REGISTRY.get(Identifier("trickster", "spell_part"))!!.endec.decode(
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


val fragmentTypesIntLookup = HashMap<Int, Identifier>();
val fragmentTypes: Map<Identifier, FragmentType<Fragment>> = HashMap();

fun getFragmentTypeFromInt(intId: Int): FragmentType<Fragment> {
    var id = fragmentTypesIntLookup[intId];
    if (id == null) {
        throw Exception("Not a valid int id for fragment type");
    }
    return fragmentTypes[id]!!;
}