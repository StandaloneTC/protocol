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
                is BytePacket      -> byteConverters.tryConvert(packet)
                is ByteArrayPacket -> byteArrayConverters.tryConvert(packet)
                is DoublePacket    -> doubleConverters.tryConvert(packet)
                is IntPacket       -> intConverters.tryConvert(packet)
                is BooleanPacket   -> booleanConverters.tryConvert(packet)
                is StringPacket    -> stringConverters.tryConvert(packet)
                is LongPacket      -> longConverters.tryConvert(packet)
                is CombinedPacket  -> combinedConverters.tryConvert(packet)
            }


    private fun <U : Packet<*>> Iterable<PacketConverter<U, T>>.tryConvert(packet: U) =
            iterator().let { iterator ->
                var r: T? = null
                while (r == null && iterator.hasNext()) {
                    with(iterator.next()) {
                        r = packet.wrap()
                    }
                }
                r
            }

    object EmptyPacketConversion : PacketConversion<Packet<*>>()
}