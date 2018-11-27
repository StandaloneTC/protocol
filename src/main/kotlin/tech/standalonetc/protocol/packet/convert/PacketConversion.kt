package tech.standalonetc.protocol.packet.convert

import tech.standalonetc.protocol.packet.*

abstract class PacketConversion<T> {

    protected val byteConverter: MutableSet<Converter<BytePacket, T>> = mutableSetOf()
    protected val byteArrayConverter: MutableSet<Converter<ByteArrayPacket, T>> = mutableSetOf()
    protected val intConverter: MutableSet<Converter<IntPacket, T>> = mutableSetOf()
    protected val doubleConverter: MutableSet<Converter<DoublePacket, T>> = mutableSetOf()
    protected val stringConverter: MutableSet<Converter<StringPacket, T>> = mutableSetOf()
    protected val booleanConverter: MutableSet<Converter<BooleanPacket, T>> = mutableSetOf()
    protected val longConverter: MutableSet<Converter<LongPacket, T>> = mutableSetOf()
    protected val combinedConverter: MutableSet<Converter<CombinedPacket, T>> = mutableSetOf()

    open fun wrap(packet: Packet<*>) =
            when (packet) {
                is BytePacket      -> byteConverter.tryConvert(packet)
                is ByteArrayPacket -> byteArrayConverter.tryConvert(packet)
                is DoublePacket    -> doubleConverter.tryConvert(packet)
                is IntPacket       -> intConverter.tryConvert(packet)
                is BooleanPacket   -> booleanConverter.tryConvert(packet)
                is StringPacket    -> stringConverter.tryConvert(packet)
                is LongPacket      -> longConverter.tryConvert(packet)
                is CombinedPacket  -> combinedConverter.tryConvert(packet)
            }


    private fun <U : Packet<*>> Iterable<Converter<U, T>>.tryConvert(packet: U) =
            map { with(it) { packet.wrap() } }.filterNot { it == null }.firstOrNull()

}