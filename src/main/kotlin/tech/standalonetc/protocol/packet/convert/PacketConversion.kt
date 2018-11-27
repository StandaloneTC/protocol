package tech.standalonetc.protocol.packet.convert

import tech.standalonetc.protocol.packet.*

abstract class PacketConversion<T : Packet<*>> {

    protected val byteConverters: MutableSet<PacketConverter<BytePacket, T>> = mutableSetOf()
    protected val byteArrayConverters: MutableSet<PacketConverter<ByteArrayPacket, T>> = mutableSetOf()
    protected val intConverters: MutableSet<PacketConverter<IntPacket, T>> = mutableSetOf()
    protected val doubleConverters: MutableSet<PacketConverter<DoublePacket, T>> = mutableSetOf()
    protected val stringConverters: MutableSet<PacketConverter<StringPacket, T>> = mutableSetOf()
    protected val booleanConverters: MutableSet<PacketConverter<BooleanPacket, T>> = mutableSetOf()
    protected val longConverters: MutableSet<PacketConverter<LongPacket, T>> = mutableSetOf()
    protected val combinedConverters: MutableSet<PacketConverter<CombinedPacket, T>> = mutableSetOf()

    open fun wrap(packet: Packet<*>) =
            when (packet) {
                is BytePacket      -> byteConverters.convert(packet)
                is ByteArrayPacket -> byteArrayConverters.convert(packet)
                is DoublePacket    -> doubleConverters.convert(packet)
                is IntPacket       -> intConverters.convert(packet)
                is BooleanPacket   -> booleanConverters.convert(packet)
                is StringPacket    -> stringConverters.convert(packet)
                is LongPacket      -> longConverters.convert(packet)
                is CombinedPacket  -> combinedConverters.convert(packet)
            }


    private fun <U : Packet<*>> Iterable<PacketConverter<U, T>>.convert(packet: U) =
            iterator().let { iterator ->
                var r: T? = null
                while (r == null && iterator.hasNext()) {
                    with(iterator.next()) {
                        r = packet.wrap()
                    }
                }
                r
            }

    operator fun plus(other: PacketConversion<T>) = apply {
        byteConverters.addAll(other.byteConverters)
        byteArrayConverters.addAll(other.byteArrayConverters)
        intConverters.addAll(other.intConverters)
        doubleConverters.addAll(other.doubleConverters)
        stringConverters.addAll(other.stringConverters)
        booleanConverters.addAll(other.booleanConverters)
        longConverters.addAll(other.longConverters)
        combinedConverters.addAll(other.combinedConverters)
    }

    operator fun minus(other: PacketConversion<T>) = apply {
        byteConverters.removeAll(other.byteConverters)
        byteArrayConverters.removeAll(other.byteArrayConverters)
        intConverters.removeAll(other.intConverters)
        doubleConverters.removeAll(other.doubleConverters)
        stringConverters.removeAll(other.stringConverters)
        booleanConverters.removeAll(other.booleanConverters)
        longConverters.removeAll(other.longConverters)
        combinedConverters.removeAll(other.combinedConverters)
    }

    object EmptyPacketConversion : PacketConversion<Packet<*>>()
}