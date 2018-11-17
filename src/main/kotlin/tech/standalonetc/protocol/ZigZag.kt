package tech.standalonetc.protocol

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.experimental.and

//TODO 恶星
fun ByteArray.decodeZigZag() =
        ByteArrayInputStream(this).run {
            ByteArrayOutputStream()
                    .apply {
                        while (true)
                            read().also(this::write)
                                    .takeIf { it > 0x7f }
                                    ?: break
                    }
                    .toByteArray()
                    .foldRight(0L) { byte, acc ->
                        acc shl 7 or ((byte and 0x7f).toLong())
                    }.let { (it ushr 1) xor -(it and 1) }
        }


fun Long.encodeZigZag(): ByteArray {
    var temp = ((this shr 63) xor (this shl 1))
    return sequence {
        while (true) {
            if (temp > 0x7f) {
                yield((temp or 0x80).toByte())
                temp = temp ushr 7
            } else {
                yield(temp.toByte())
                break
            }
        }

    }.toList().toByteArray()
}
