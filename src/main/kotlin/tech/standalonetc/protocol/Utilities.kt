package tech.standalonetc.protocol

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

fun Packet<*>.toByteArray(): ByteArray =
        when (this) {
            is BytePacket      -> encodePacket(type, id, data.encode())
            is DoublePacket    -> encodePacket(type, id, data.encodeZigZag())
            is IntPacket       -> encodePacket(type, id, data.encodeZigZag())
            is BooleanPacket   -> encodePacket(type, id, data.encode())
            is StringPacket    -> encodePacket(type, id, data.encode())
            is LongPacket      -> encodePacket(type, id, data.encodeZigZag())
            is ByteArrayPacket -> encodePacket(type, id, data)
            is CombinedPacket  -> {
                val buffer = ByteArrayOutputStream()
                val encoded = data.map { it.toByteArray() }
                buffer.apply {
                    write(type.toInt())
                    write(id.toInt())
                    //how many child packets?
                    write(encoded.size)
                    //size of each child packets
                    encoded.forEach { write(it.size) }
                    encoded.forEach { write(it) }
                }
                buffer.toByteArray()
            }
        }

private fun decodePacket(
        type: Byte,
        id: Byte,
        buffer: ByteArrayInputStream
): Packet<*> {
    return when (type.toInt()) {
        0    -> BytePacket(id, buffer.read().toByte())
        1    -> DoublePacket(id, Double.fromBits(buffer.readBytes().decodeZigZag()))
        2    -> IntPacket(id, buffer.readBytes().decodeZigZag().toInt())
        3    -> BooleanPacket(id, buffer.read() != 0)
        4    -> StringPacket(id, decodeToString(buffer.readBytes()))
        5    -> LongPacket(id, buffer.readBytes().decodeZigZag())
        6    -> ByteArrayPacket(id, buffer.readBytes())
        7    -> {
            val sizes = IntArray(buffer.read()) { buffer.read() }
            val childes = sizes.map { ByteArray(it) { buffer.read().toByte() } }
            childes.map {
                it.toPacket<Packet<*>>()
            }.let { CombinedPacket(id, *it.toTypedArray()) }
        }
        else -> throw IllegalArgumentException()
    }
}

@Suppress("UNCHECKED_CAST")
fun <P : Packet<*>> ByteArray.toPacket(): P {
    val buffer = ByteArrayInputStream(this)
    val type = buffer.read().toByte()
    val id = buffer.read().toByte()
    return decodePacket(type, id, buffer) as P
}


private fun encodePacket(type: Byte, id: Byte, data: ByteArray) =
        ByteArray(data.size + 2).apply {
            data.copyInto(this, 2)
            set(0, type)
            set(1, id)
        }
