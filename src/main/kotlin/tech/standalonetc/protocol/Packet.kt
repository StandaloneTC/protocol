package tech.standalonetc.protocol

sealed class Packet<T>(val type: Byte, val id: Byte, val data: T) {
    override fun toString(): String = "${javaClass.simpleName}[id: $id, data: $data]"
}

open class BytePacket(id: Byte, data: Byte) : Packet<Byte>(0, id, data)

open class DoublePacket(id: Byte, data: Double) : Packet<Double>(1, id, data)

open class IntPacket(id: Byte, data: Int) : Packet<Int>(2, id, data)

open class BooleanPacket(id: Byte, data: Boolean) : Packet<Boolean>(3, id, data)

open class StringPacket(id: Byte, data: String) : Packet<String>(4, id, data)

open class LongPacket(id: Byte, data: Long) : Packet<Long>(5, id, data)

open class ByteArrayPacket(id: Byte, data: ByteArray) : Packet<ByteArray>(6, id, data)

open class CombinedPacket(id: Byte, vararg packets: Packet<*>) : Packet<Array<out Packet<*>>>(7, id, packets) {
    init {
        assert(packets.map { it.id }.distinct().size == 1)
    }
}