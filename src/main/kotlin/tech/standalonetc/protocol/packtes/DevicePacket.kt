package tech.standalonetc.protocol.packtes

/**
 * Device packet protocol
 */
object DevicePacket {

    /**
     * Pwm enable of continuous servo and normal servo
     */
    class PwmEnablePacket(id: Byte, enable: Boolean)
        : BooleanPacket(id, enable, Label.PwmEnablePacket)

    /**
     * Power of continuous servo
     */
    class ContinuousServoPowerPacket(id: Byte, power: Double)
        : DoublePacket(id, power, Label.ContinuousServoPowerPacket)

    /**
     * Encoder data
     */
    class EncoderDataPacket(id: Byte, position: Double, speed: Double)
        : CombinedPacket(id, DoublePacket(id, position), DoublePacket(id, speed),
            label = Label.EncoderDataPacket)

    /**
     * Power of continuous motor
     */
    class MotorPowerPacket(id: Byte, power: Double)
        : DoublePacket(id, power, Label.MotorPowerPacket)

    /**
     * Position of servo
     */
    class ServoPositionPacket(id: Byte, degree: Int)
        : IntPacket(id, degree, Label.ServoPositionPacket)

    /**
     * Reset a encoder
     */
    class EncoderResetPacket(id: Byte) : BytePacket(id, 8, Label.EncoderResetPacket)

    object Label {
        const val PwmEnablePacket: Byte = 0
        const val ContinuousServoPowerPacket: Byte = 1
        const val EncoderDataPacket: Byte = 2
        const val MotorPowerPacket: Byte = 3
        const val ServoPositionPacket: Byte = 4
        const val EncoderResetPacket: Byte = 5
    }

    object Conversion {

        fun BooleanPacket.PwmEnablePacket() =
                takeIf { label == Label.PwmEnablePacket }?.run { PwmEnablePacket(id, data) }

        fun DoublePacket.ContinuousServoPowerPacket() =
                takeIf { label == Label.ContinuousServoPowerPacket }?.run { ContinuousServoPowerPacket(id, data) }

        fun CombinedPacket.EncoderDataPacket() =
                takeIf { label == Label.EncoderDataPacket }
                        ?.run { EncoderDataPacket(id, data[0].data as Double, data[1].data as Double) }

        fun DoublePacket.MotorPowerPacket() =
                takeIf { label == Label.MotorPowerPacket }?.run { MotorPowerPacket(id, data) }

        fun IntPacket.ServoPositionPacket() =
                takeIf { label == Label.ServoPositionPacket }?.run { ServoPositionPacket(id, data) }

        fun BytePacket.EncoderResetPacket() =
                takeIf { label == Label.EncoderResetPacket }?.run { EncoderResetPacket(id) }
    }
}
