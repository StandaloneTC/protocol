package tech.standalonetc.protocol.packet.convert

import tech.standalonetc.protocol.packet.Packet

interface PacketConverter<T : Packet<*>, R : Packet<*>> : Converter<T, R> {

    fun T.withCheckingLabel(label: Byte, block: T.() -> R?) =
            label.takeIf { it == this.label }?.run { block() }

}

fun <T : Packet<*>, R : Packet<*>> (T.() -> R).toConverter() =
        object : PacketConverter<T, R> {
            override fun T.wrap(): R? = this@toConverter()
        }