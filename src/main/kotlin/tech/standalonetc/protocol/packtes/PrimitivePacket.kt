package tech.standalonetc.protocol.packtes

sealed class Packet<T>(val type: Byte, val id: Byte, val label: Byte, val data: T) {
    override fun toString(): String = "${javaClass.simpleName}[id: $id, data: $data]"
}

open class BytePacket(id: Byte, data: Byte, label: Byte = -1) : Packet<Byte>(0, id, label, data)

open class DoublePacket(id: Byte, data: Double, label: Byte = -1) : Packet<Double>(1, id, label, data)

open class IntPacket(id: Byte, data: Int, label: Byte = -1) : Packet<Int>(2, id, label, data)

open class BooleanPacket(id: Byte, data: Boolean, label: Byte = -1) : Packet<Boolean>(3, id, label, data)

open class StringPacket(id: Byte, data: String, label: Byte = -1) : Packet<String>(4, id, label, data)

open class LongPacket(id: Byte, data: Long, label: Byte = -1) : Packet<Long>(5, id, label, data)

open class ByteArrayPacket(id: Byte, data: ByteArray, label: Byte = -1) : Packet<ByteArray>(6, id, label, data)

open class CombinedPacket(id: Byte, vararg packets: Packet<*>, label: Byte = -1) : Packet<Array<out Packet<*>>>(7, id, label, packets) {
    init {
        assert(packets.map { it.id }.distinct().size == 1)
    }
}