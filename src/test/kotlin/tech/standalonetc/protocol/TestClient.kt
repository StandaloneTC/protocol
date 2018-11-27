package tech.standalonetc.protocol

import tech.standalonetc.protocol.network.NetworkClient
import tech.standalonetc.protocol.packet.CombinedPacket
import tech.standalonetc.protocol.packet.DoublePacket
import tech.standalonetc.protocol.packet.Packet


object C {
    val callback = { packet: Packet<*> ->
        println("received a $packet")
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
        a.setPacketConversion(RobotPacket.Conversion)
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
        b.setPacketConversion(RobotPacket.Conversion)
        while (true) {
            b.broadcastPacket(DoublePacket(9, 233.233))
            Thread.sleep(3000)
        }
    }
}

object E {
    @JvmStatic
    fun main(args: Array<String>) {
        val e = NetworkClient("E", "F",
                onRawPacketReceive = C.rawCallback, onPacketReceive = C.callback)
        e.setPacketConversion(RobotPacket.Conversion)
        while (true) {
            e.broadcastPacket(RobotPacket.OpModeInfoPacket("sb", 0))
            Thread.sleep(3000)
        }
    }
}

object F {
    @JvmStatic
    fun main(args: Array<String>) {
        val f = NetworkClient("F", "E",
                onRawPacketReceive = C.rawCallback, onPacketReceive = C.callback)
        f.debug = true
        f.setPacketConversion(RobotPacket.Conversion)
        while (true) {
        }
    }
}