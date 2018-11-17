package tech.standalonetc.protocol.packtes

object DevicePacket {

    class PwmEnablePacket(id: Byte, enable: Boolean)
        : BooleanPacket(id, enable, PacketLabel.PwmEnablePacket)

    class ContinuousServoPowerPacket(id: Byte, power: Double)
        : DoublePacket(id, power, PacketLabel.ContinuousServoPowerPacket)

    class EncoderDataPacket(id: Byte, position: Double, speed: Double)
        : CombinedPacket(id, DoublePacket(id, position), DoublePacket(id, speed),
            label = PacketLabel.EncoderDataPacket)

    class MotorPowerPacket(id: Byte, power: Double)
        : DoublePacket(id, power, PacketLabel.MotorPowerPacket)

    class ServoPositionPacket(id: Byte, degree: Int)
        : IntPacket(id, degree, PacketLabel.ServoPositionPacket)

    object PacketLabel {
        const val PwmEnablePacket: Byte = 0
        const val ContinuousServoPowerPacket: Byte = 1
        const val EncoderDataPacket: Byte = 2
        const val MotorPowerPacket: Byte = 3
        const val ServoPositionPacket: Byte = 3
    }

    object PacketConverter {

        fun BooleanPacket.PwmEnablePacket() =
                takeIf { label == PacketLabel.PwmEnablePacket }?.run { PwmEnablePacket(id, data) }

        fun DoublePacket.ContinuousServoPowerPacket() =
                takeIf { label == PacketLabel.ContinuousServoPowerPacket }?.run { ContinuousServoPowerPacket(id, data) }

        fun CombinedPacket.EncoderDataPacket() =
                takeIf { label == PacketLabel.EncoderDataPacket }
                        ?.run { EncoderDataPacket(id, data[0].data as Double, data[1].data as Double) }

        fun DoublePacket.MotorPowerPacket() =
                takeIf { label == PacketLabel.MotorPowerPacket }?.run { MotorPowerPacket(id, data) }

        fun IntPacket.ServoPositionPacket() =
                takeIf { label == PacketLabel.ServoPositionPacket }?.run { ServoPositionPacket(id, data) }
    }
}
