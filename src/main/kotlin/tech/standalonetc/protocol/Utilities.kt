package tech.standalonetc.protocol

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.experimental.and
import kotlin.math.min

fun Packet<*>.toByteArray(): ByteArray =
    when (this) {
        is BytePacket -> encodePacket(type, id, data.encode())
        is DoublePacket -> encodePacket(type, id, data.encodeZigZag())
        is IntPacket -> encodePacket(type, id, data.encodeZigZag())
        is BooleanPacket -> encodePacket(type, id, data.encode())
        is StringPacket -> encodePacket(type, id, data.encode())
        is LongPacket -> encodePacket(type, id, data.encodeZigZag())
        is ByteArrayPacket -> encodePacket(type, id, data)
        is CombinedPacket -> {
            val buffer = ByteArrayOutputStream()
            val encoded = data.map { it.toByteArray() }
            buffer.apply {
                write(type.toInt())
                write(id.toInt())
                write(encoded.size)
                data.forEach { write(it.type.toInt()) }
                encoded.forEach {
                    write(it.size)//TODO: 需要吗？
                    write(it)
                }
            }
            buffer.toByteArray()
        }
    }

internal fun InputStream.waitNBytes(n: Int): ByteArray {
    val buffer = ByteArray(n)
    for (i in 0 until n) {
        buffer[i] = read()
            .takeIf { it in 0..255 }
            ?.toByte()
                ?: return buffer.copyOfRange(0, i)
    }
    return buffer
}

internal fun InputStream.readNBytes(len: Int): ByteArray {
    val count = min(available(), len)
    val buffer = ByteArray(count)
    read(buffer, 0, count)
    return buffer
}

private fun decodePacket(
    type: Byte,
    id: Byte,
    buffer: ByteArrayInputStream,
    max: Int = Int.MAX_VALUE/* TODO: 星吗 */
): Packet<*> {
    return when (type.toInt()) {
        0 -> BytePacket(id, buffer.read().toByte())
        1 -> DoublePacket(id, Double.fromBits(buffer.readNBytes(max).decodeZigZag()))
        2 -> IntPacket(id, buffer.readNBytes(max).decodeZigZag().toInt())
        3 -> BooleanPacket(id, buffer.read() != 0)
        4 -> StringPacket(id, buffer.readNBytes(max).decodeToString())
        5 -> LongPacket(id, buffer.readNBytes(max).decodeZigZag())
        6 -> ByteArrayPacket(id, buffer.readNBytes(max))
        7 -> {
            val types = ByteArray(buffer.read()) { buffer.read().toByte() }
            types.map {
                decodePacket(it, id, buffer, buffer.read())
                //TODO: 布星
            }.let { CombinedPacket(id, *it.toTypedArray()) }
        }
        else -> throw IllegalArgumentException()
    }
}

fun ByteArray.toPacket(): Packet<*> {
    val buffer = ByteArrayInputStream(this)
    val type = buffer.read().toByte()
    val id = buffer.read().toByte()
    return decodePacket(type, id, buffer)
}


private fun encodePacket(type: Byte, id: Byte, data: ByteArray) =
    ByteArray(data.size + 2).apply {
        data.copyInto(this, 2)
        set(0, type)
        set(1, id)
    }

fun Byte.encode() = byteArrayOf(this)

fun Int.encodeZigZag(): ByteArray = toLong().encodeZigZag()

fun String.encode() =
    toByteArray().let { src ->
        assert(src.size <= 127)
        ByteArray(1 + src.size).apply {
            src.copyInto(this, 1)
            set(0, src.size.toByte())
        }
    }

fun ByteArray.decodeToString() =
    String(copyOfRange(1, this[0].toInt() + 1))

fun Boolean.encode() = (if (this) 1 else 0).toByte().encode()

fun Double.encodeZigZag() =
    java.lang.Double.doubleToRawLongBits(this).encodeZigZag()

fun Long.encodeZigZag(): ByteArray {
    var temp = (this shr 63) xor (this shl 1)
    return generateSequence {
        when {
            temp > 0x7f -> (temp or 0x80).toByte().also { temp = temp ushr 7 }
            temp > 0 -> temp.toByte().also { temp = 0 }
            else -> null
        }
    }.toList().toByteArray()
}

fun ByteArray.decodeZigZag() =
    foldRight(0L) { byte, acc -> (acc shl 7) or (byte and 0x7f).toLong() }
        .let { (it ushr 1) xor -(it and 1) }

