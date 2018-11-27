package tech.standalonetc.protocol.packet.convert

import tech.standalonetc.protocol.packet.*

abstract class PacketConversion<T> {

    protected val byteConverters: MutableSet<Converter<BytePacket, T>> = mutableSetOf()
    protected val byteArrayConverters: MutableSet<Converter<ByteArrayPacket, T>> = mutableSetOf()
    protected val intConverters: MutableSet<Converter<IntPacket, T>> = mutableSetOf()
    protected val doubleConverters: MutableSet<Converter<DoublePacket, T>> = mutableSetOf()
    protected val stringConverters: MutableSet<Converter<StringPacket, T>> = mutableSetOf()
    protected val booleanConverters: MutableSet<Converter<BooleanPacket, T>> = mutableSetOf()
    protected val longConverters: MutableSet<Converter<LongPacket, T>> = mutableSetOf()
    protected val combinedConverters: MutableSet<Converter<CombinedPacket, T>> = mutableSetOf()

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


    private fun <U : Packet<*>> Iterable<Converter<U, T>>.tryConvert(packet: U) =
            iterator().let { iterator ->
                var r: T? = null
                while (iterator.hasNext() && r != null)
                    with(iterator.next()) {
                        r = packet.wrap()
                    }
                r
            }
}