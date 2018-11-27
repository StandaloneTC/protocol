package tech.standalonetc.protocol.network

import tech.standalonetc.protocol.packet.Packet

typealias PacketCallback = Packet<*>.() -> Unit