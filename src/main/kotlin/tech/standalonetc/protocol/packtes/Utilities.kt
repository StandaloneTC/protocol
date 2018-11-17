package tech.standalonetc.protocol.packtes

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

fun Packet<*>.toByteArray(): ByteArray =
        when (this) {
            is BytePacket      -> encodePacket(type, id, label, data.encode())
            is DoublePacket    -> encodePacket(type, id, label, data.encode())
            is IntPacket       -> encodePacket(type, id, label, data.encode())
            is BooleanPacket   -> encodePacket(type, id, label, data.encode())
            is StringPacket    -> encodePacket(type, id, label, data.encode())
            is LongPacket      -> encodePacket(type, id, label, data.encode())
            is ByteArrayPacket -> encodePacket(type, id, label, data)
            is CombinedPacket  -> {
                val buffer = ByteArrayOutputStream()
                val encoded = data.map { it.toByteArray() }
                buffer.apply {
                    write(type.toInt())
                    write(id.toInt())
                    write(label.toInt())
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
        label: Byte,
        buffer: ByteArrayInputStream
): Packet<*> {
    return when (type.toInt()) {
        0    -> BytePacket(id, buffer.read().toByte(), label)
        1    -> DoublePacket(id, buffer.readBytes().decodeToDouble(), label)
        2    -> IntPacket(id, buffer.readBytes().decodeToInt(), label)
        3    -> BooleanPacket(id, buffer.read() != 0, label)
        4    -> StringPacket(id, decodeToString(buffer.readBytes()), label)
        5    -> LongPacket(id, buffer.readBytes().decodeToLong(), label)
        6    -> ByteArrayPacket(id, buffer.readBytes(), label)
        7    -> {
            val sizes = IntArray(buffer.read()) { buffer.read() }
            val childes = sizes.map { ByteArray(it) { buffer.read().toByte() } }
            childes.map {
                it.toPrimitivePacket()
            }.let { CombinedPacket(id, *it.toTypedArray(), label = label) }
        }
        else -> throw IllegalArgumentException()
    }
}

fun ByteArray.toPrimitivePacket(): Packet<*> {
    val buffer = ByteArrayInputStream(this)
    val type = buffer.read().toByte()
    val id = buffer.read().toByte()
    val label = buffer.read().toByte()
    return decodePacket(type, id, label, buffer)
}


private fun encodePacket(type: Byte, id: Byte, label: Byte, data: ByteArray) =
        ByteArray(data.size + 3).apply {
            data.copyInto(this, 3)
            set(0, type)
            set(1, id)
            set(2, label)
        }

fun CombinedPacket.flatten(): List<Packet<*>> =
        data.flatMap {
            if (it is CombinedPacket)
                it.flatten()
            else listOf(it)
        }

operator fun Packet<*>.plus(other: Packet<*>) =
        when {
            this is CombinedPacket  -> CombinedPacket(id, *data, other, label = label)
            other is CombinedPacket -> CombinedPacket(other.id, *other.data, this, label = other.label)
            else                    -> CombinedPacket(id, this, other, label = label)
        }