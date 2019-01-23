package tech.standalonetc.protocol

import tech.standalonetc.protocol.network.NetworkTools
import tech.standalonetc.protocol.packet.CombinedPacket
import tech.standalonetc.protocol.packet.DoublePacket
import tech.standalonetc.protocol.packet.Packet
import tech.standalonetc.protocol.packet.encode
import kotlin.random.Random


object C {
    private var last = 0L
    private var lastRaw = 0L
    val callback = { packet: Packet<*> ->
        if (last == 0L)
            last = System.nanoTime()
        println("received a $packet")
        println("interval ${(System.nanoTime() - last) * 1E-9}s")
        last = System.nanoTime()
    }
    val rawCallback = { packet: Packet<*> ->
        if (lastRaw == 0L)
            lastRaw = System.nanoTime()
        println("received a raw packet: $packet")
        println("interval ${(System.nanoTime() - lastRaw) * 1E-9}s")
        lastRaw = System.nanoTime()
    }
}

object A {
    @JvmStatic
    fun main(args: Array<String>) {
        val a = NetworkTools(
            "A", "B",
            onRawPacketReceive = C.rawCallback, onPacketReceive = C.callback
        )
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
        val b = NetworkTools(
            "B", "A",
            onRawPacketReceive = C.rawCallback, onPacketReceive = C.callback
        )
        b.setPacketConversion(RobotPacket.Conversion)
        while (true) {
            b.broadcastPacket(DoublePacket(9, 233.233))
            Thread.sleep(3000)
        }
    }
}

object D {
    @JvmStatic
    fun main(args: Array<String>) {
        val d = NetworkTools(
            "D", "E",
            onRawPacketReceive = C.rawCallback, onPacketReceive = C.callback
        )
        d.setPacketConversion(RobotPacket.Conversion)
        d.findOpposite()
        while (true) {
            readLine()
            d.sendPacket(
                RobotPacket.DeviceDescriptionPacket(
                    Random.nextInt(0, 127).toByte(),
                    String(Random.nextBytes(10))
                )
            ).let {
                println(it?.joinToString())
            }
        }
    }
}


object E {
    @JvmStatic
    fun main(args: Array<String>) {
        val e = NetworkTools("E", "D")
        e.setPacketConversion(RobotPacket.Conversion)
        e.setTcpPacketReceiveCallback {
            println(this)
            if (this is RobotPacket.DeviceDescriptionPacket)
                Random.nextBoolean().encode()
            else null
        }
    }
}