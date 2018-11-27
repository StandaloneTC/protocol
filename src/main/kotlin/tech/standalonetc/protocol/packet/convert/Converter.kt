package tech.standalonetc.protocol.packet.convert

import tech.standalonetc.protocol.packet.Packet

interface Converter<in T : Packet<*>, out R> {

    fun T.wrap(): R?

}