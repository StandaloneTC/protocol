package tech.standalonetc.protocol

import tech.standalonetc.protocol.network.NetworkClient
import tech.standalonetc.protocol.packet.CombinedPacket
import tech.standalonetc.protocol.packet.DoublePacket
import tech.standalonetc.protocol.packet.Packet


object C {
    val callback = { packet: Packet<*> ->
        println("received a: $packet")
    }
    val rawCallback = { packet: Packet<*> ->
        println("received a raw packet: $packet")
    }
}

object A {
    @JvmStatic
    fun main(args: Array<String>) {
        val a = NetworkClient("A", "B",
                onRawPacketReceive = C.rawCallback, onPacketReceive = C.callback)
        while (true) {
            a.broadcastPacket(RobotPacket.DeviceDescriptionPacket(0, "foo"))
            a.broadcastPacket(CombinedPacket(1, RobotPacket.ServoPositionPacket(1, 1.0)))
            Thread.sleep(3000)
        }
    }


}

object B {
    @JvmStatic
    fun main(args: Array<String>) {
        val b = NetworkClient("B", "A",
                onRawPacketReceive = C.rawCallback, onPacketReceive = C.callback)
        while (true) {
            b.broadcastPacket(DoublePacket(9, 233.233))
            Thread.sleep(3000)
        }
    }
}