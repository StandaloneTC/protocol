package tech.standalonetc.protocol

import tech.standalonetc.protocol.network.NetworkClient
import tech.standalonetc.protocol.packtes.DevicePacket
import tech.standalonetc.protocol.packtes.DoublePacket
import tech.standalonetc.protocol.packtes.Packet


object C {
    val callback = { packet: Packet<*> ->
        println("receive $packet")
    }
}

object A {
    @JvmStatic
    fun main(args: Array<String>) {
        val a = NetworkClient("A", "B", onRawPacketReceive = {
            println("raw: ${toString()}")
        }, onPacketReceive = C.callback)
        while (true) {
            a.broadcastPacket(DevicePacket.DeviceDescriptionPacket(0, "foo"))
            Thread.sleep(3000)
        }
    }


}

object B {
    @JvmStatic
    fun main(args: Array<String>) {
        val b = NetworkClient("B", "A", onPacketReceive = C.callback)
        while (true) {
            b.broadcastPacket(DoublePacket(9, 233.233))
            Thread.sleep(3000)
        }
    }
}