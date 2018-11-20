package tech.standalonetc.protocol.network

import tech.standalonetc.protocol.packtes.Packet

typealias PacketCallback = Packet<*>.() -> Unit