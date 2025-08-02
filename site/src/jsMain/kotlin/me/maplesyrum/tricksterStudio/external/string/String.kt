package me.maplesyrum.tricksterStudio.external.string



fun stringFromCharCode(byte: Byte): dynamic {
    return js("String.fromCharCode(byte)")
}

fun stringFromCharCodes(bytes: ByteArray): dynamic {
    return bytes.joinToString("") { stringFromCharCode(it) }
}