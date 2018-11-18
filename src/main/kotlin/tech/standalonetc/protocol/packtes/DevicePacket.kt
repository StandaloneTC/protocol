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

    /**
     * Gamepad packet
     */
    class GamepadDataPacket(
            id: Byte,
            leftBumper: Boolean,
            rightBumper: Boolean,
            aButton: Boolean,
            bButton: Boolean,
            xButton: Boolean,
            yButton: Boolean,
            upButton: Boolean,
            downButton: Boolean,
            leftButton: Boolean,
            rightButton: Boolean,
            leftStick: Double,
            rightStick: Double,
            leftTrigger: Double,
            rightTrigger: Double
    ) : CombinedPacket(
            id,
            //front part
            BooleanPacket(id, rightBumper) + BooleanPacket(id, leftBumper),
            //button part
            CombinedPacket(id,
                    BooleanPacket(id, aButton),
                    BooleanPacket(id, bButton),
                    BooleanPacket(id, xButton),
                    BooleanPacket(id, yButton)),
            //direction key part
            CombinedPacket(id,
                    BooleanPacket(id, upButton),
                    BooleanPacket(id, downButton),
                    BooleanPacket(id, leftButton),
                    BooleanPacket(id, rightButton)),
            //stick part
            DoublePacket(id, leftStick) + DoublePacket(id, rightStick),
            //trigger part
            DoublePacket(id, leftTrigger) + DoublePacket(id, rightTrigger),
            label = Label.GamepadDataPacket
    )

    object Label {
        const val PwmEnablePacket: Byte = 0
        const val ContinuousServoPowerPacket: Byte = 1
        const val EncoderDataPacket: Byte = 2
        const val MotorPowerPacket: Byte = 3
        const val ServoPositionPacket: Byte = 4
        const val EncoderResetPacket: Byte = 5
        const val GamepadDataPacket: Byte = 6
    }

    /**
     * Wrap primitive [Packet] to DevicePacket
     */
    object Conversion {

        /**
         * Try converting [BooleanPacket] into [PwmEnablePacket]
         */
        fun BooleanPacket.PwmEnablePacket() =
                takeIf { label == Label.PwmEnablePacket }?.run { PwmEnablePacket(id, data) }

        /**
         * Try converting [DoublePacket ] into [ContinuousServoPowerPacket]
         */
        fun DoublePacket.ContinuousServoPowerPacket() =
                takeIf { label == Label.ContinuousServoPowerPacket }?.run { ContinuousServoPowerPacket(id, data) }

        /**
         * Try converting [CombinedPacket] into [EncoderDataPacket]
         */
        fun CombinedPacket.EncoderDataPacket() =
                takeIf { label == Label.EncoderDataPacket }
                        ?.run { EncoderDataPacket(id, data[0].data as Double, data[1].data as Double) }

        /**
         * Try converting [DoublePacket] into [MotorPowerPacket]
         */
        fun DoublePacket.MotorPowerPacket() =
                takeIf { label == Label.MotorPowerPacket }?.run { MotorPowerPacket(id, data) }

        /**
         * Try converting [IntPacket] into [ServoPositionPacket]
         */
        fun IntPacket.ServoPositionPacket() =
                takeIf { label == Label.ServoPositionPacket }?.run { ServoPositionPacket(id, data) }

        /**
         * Try converting [BytePacket] into [EncoderResetPacket]
         */
        fun BytePacket.EncoderResetPacket() =
                takeIf { label == Label.EncoderResetPacket }?.run { EncoderResetPacket(id) }

        /**
         * Try converting [CombinedPacket] into [GamepadDataPacket]
         */
        fun CombinedPacket.GamepadDataPacket() =
                takeIf { label == Label.GamepadDataPacket }?.run {
                    val (front, button, direction, stick, trigger) = this
                    val (leftBumper, rightBumper) = front as CombinedPacket
                    val (a, b, x, y) = button as CombinedPacket
                    val (up, down, left, right) = direction as CombinedPacket
                    val (leftStick, rightStick) = stick as CombinedPacket
                    val (leftTrigger, rightTrigger) = trigger as CombinedPacket
                    GamepadDataPacket(
                            id,
                            leftBumper.data as Boolean,
                            rightBumper.data as Boolean,
                            a.data as Boolean,
                            b.data as Boolean,
                            x.data as Boolean,
                            y.data as Boolean,
                            up.data as Boolean,
                            down.data as Boolean,
                            left.data as Boolean,
                            right.data as Boolean,
                            leftStick.data as Double,
                            rightStick.data as Double,
                            leftTrigger.data as Double,
                            rightTrigger.data as Double
                    )
                }

    }
}
