package tech.standalonetc.protocol

import tech.standalonetc.protocol.network.NetworkTools
import tech.standalonetc.protocol.packet.CombinedPacket
import tech.standalonetc.protocol.packet.DoublePacket
import tech.standalonetc.protocol.packet.Packet


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
        val a = NetworkTools("A", "B",
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
        val b = NetworkTools("B", "A",
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
        val e = NetworkTools("E", "F",
                onRawPacketReceive = C.rawCallback, onPacketReceive = C.callback)
        e.setPacketConversion(RobotPacket.Conversion)
        e.setTcpPacketReceiveCallback {
            println(this)
            null
        }
        while (!e.connect());
        println("Connected")
        while (readLine()!!.run { true })
            e.sendPacket(DoublePacket(0, 233.33))
    }
}

object F {
    @JvmStatic
    fun main(args: Array<String>) {
        val f = NetworkTools("F", "E",
                onRawPacketReceive = C.rawCallback, onPacketReceive = C.callback)
        f.setTcpPacketReceiveCallback {
            println(this)
            null
        }
        f.setPacketConversion(RobotPacket.Conversion)
        while (!f.isConnectedToOpposite);
        println("Connected")
        while (readLine()!!.run { true })
            f.sendPacket(DoublePacket(0, 88.88))
    }
}