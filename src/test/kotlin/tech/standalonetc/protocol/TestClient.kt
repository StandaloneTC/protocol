package tech.standalonetc.protocol

import tech.standalonetc.protocol.network.NetworkClient
import tech.standalonetc.protocol.packtes.DevicePacket

object A {
    @JvmStatic
    fun main(args: Array<String>) {
        val a = NetworkClient("A") {
            when (this) {
                is DevicePacket.MotorPowerPacket -> println("power: $data")
                is DevicePacket.PwmEnablePacket  -> println("pwm enable: $data")
            }
        }
        Thread.sleep(3000)
        while (true)
            a.broadcastPacket(DevicePacket.PwmEnablePacket(0, false))
    }


}

object B {
    @JvmStatic
    fun main(args: Array<String>) {
        val b = NetworkClient("B") {
            when (this) {
                is DevicePacket.MotorPowerPacket -> println("power: $data")
                is DevicePacket.PwmEnablePacket  -> println("pwm enable: $data")
            }
        }
        Thread.sleep(3000)
        while (true)
            b.broadcastPacket(DevicePacket.MotorPowerPacket(0, -0.2))
    }
}