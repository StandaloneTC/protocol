package tech.standalonetc.protocol.packtes

/**
 * Robot packet protocol
 */
@Suppress("FunctionName", "MemberVisibilityCanBePrivate", "CanBeParameter")
object RobotPacket {

    /**
     * Device description packet
     * To let slave know what device we should use.
     */
    class DeviceDescriptionPacket(val deviceId: Byte, val deviceName: String)
        : CombinedPacket(
            BuiltinId.DeviceDescription,
            BytePacket(BuiltinId.DeviceDescription, deviceId),
            StringPacket(BuiltinId.DeviceDescription, deviceName),
            label = Label.DeviceDescriptionPacket)

    /**
     * Pwm enable packet
     * describe whether a continuous servo or normal servo enable pwm output
     */
    class PwmEnablePacket(id: Byte, val enable: Boolean)
        : BooleanPacket(id, enable, Label.PwmEnablePacket)

    /**
     * Continuous servo packet
     */
    class ContinuousServoPowerPacket(id: Byte, val power: Double)
        : DoublePacket(id, power, Label.ContinuousServoPowerPacket)

    /**
     * Encoder data packet
     */
    class EncoderDataPacket(id: Byte, val position: Int, speed: Double)
        : CombinedPacket(id,
            IntPacket(id, position),
            DoublePacket(id, speed),
            label = Label.EncoderDataPacket)

    /**
     * Motor power packet
     */
    class MotorPowerPacket(id: Byte, val power: Double)
        : DoublePacket(id, power, Label.MotorPowerPacket)

    /**
     * Servo position packet
     */
    class ServoPositionPacket(id: Byte, val degree: Double)
        : DoublePacket(id, degree, Label.ServoPositionPacket)

    /**
     * Encoder reset packet
     * reset [id]'s encoder, if it is a motor
     */
    class EncoderResetPacket(id: Byte) : BytePacket(id, 0, Label.EncoderResetPacket)

    /**
     * Gamepad data packet
     */
    class GamepadDataPacket(
            id: Byte,
            val leftBumper: Boolean,
            val rightBumper: Boolean,
            val aButton: Boolean,
            val bButton: Boolean,
            val xButton: Boolean,
            val yButton: Boolean,
            val upButton: Boolean,
            val downButton: Boolean,
            val leftButton: Boolean,
            val rightButton: Boolean,
            val leftStickX: Double,
            val leftStickY: Double,
            val leftStickButton: Boolean,
            val rightStickX: Double,
            val rightStickY: Double,
            val rightStickButton: Boolean,
            val leftTrigger: Double,
            val rightTrigger: Double
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
            //left stick part
            CombinedPacket(id,
                    DoublePacket(id, leftStickX),
                    DoublePacket(id, leftStickY),
                    BooleanPacket(id, leftStickButton)),
            //right stick part
            CombinedPacket(id,
                    DoublePacket(id, rightStickX),
                    DoublePacket(id, rightStickY),
                    BooleanPacket(id, rightStickButton)),
            //trigger part
            DoublePacket(id, leftTrigger) + DoublePacket(id, rightTrigger),
            label = Label.GamepadDataPacket
    )

    /**
     * Robot voltage packet
     */
    class VoltageDataPacket(val voltage: Double)
        : DoublePacket(BuiltinId.Voltage, voltage, Label.VoltageDataPacket)

    /**
     * Telemetry data packet
     */
    class TelemetryDataPacket(val caption: String, val string: String)
        : StringPacket(BuiltinId.Telemetry, "$caption\$\$$string", Label.TelemetryDataPacket)

    internal object Label {
        const val PwmEnablePacket: Byte = 0
        const val ContinuousServoPowerPacket: Byte = 1
        const val EncoderDataPacket: Byte = 2
        const val MotorPowerPacket: Byte = 3
        const val ServoPositionPacket: Byte = 4
        const val EncoderResetPacket: Byte = 5
        const val GamepadDataPacket: Byte = 6
        const val VoltageDataPacket: Byte = 7
        const val TelemetryDataPacket: Byte = 8
        const val DeviceDescriptionPacket: Byte = 9
    }

    /**
     * Built-in id
     * unrelated to device.
     */
    object BuiltinId {
        /**
         * Voltage packet id
         */
        const val Voltage: Byte = 126
        /**
         * Master gamepad packet id
         */
        const val GamepadMaster: Byte = 125
        /**
         * Helper gamepad packet id
         */
        const val GamepadHelper: Byte = 124
        /**
         * Telemetry data packet id
         */
        const val Telemetry: Byte = 123
        /**
         * Device description packet id
         */
        const val DeviceDescription: Byte = 122
    }

    /**
     * Wrap a primitive [Packet] to a RobotPacket
     */
    object Conversion {

        /**
         * Cast a packet data into [T]
         */
        inline fun <reified T> Packet<*>.castPacketData() = data as T

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
                        ?.run { EncoderDataPacket(id, data[0].castPacketData() as Int, data[1].castPacketData()) }

        /**
         * Try converting [DoublePacket] into [MotorPowerPacket]
         */
        fun DoublePacket.MotorPowerPacket() =
                takeIf { label == Label.MotorPowerPacket }?.run { MotorPowerPacket(id, data) }

        /**
         * Try converting [IntPacket] into [ServoPositionPacket]
         */
        fun DoublePacket.ServoPositionPacket() =
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
                    val (front, button, direction, leftStick, rightStick, trigger) = this
                    val (leftBumper, rightBumper) = front as CombinedPacket
                    val (a, b, x, y) = button as CombinedPacket
                    val (up, down, left, right) = direction as CombinedPacket
                    val (leftStickX, leftStickY, leftStickButton) = leftStick as CombinedPacket
                    val (rightStickX, rightStickY, rightStickButton) = rightStick as CombinedPacket
                    val (leftTrigger, rightTrigger) = trigger as CombinedPacket
                    GamepadDataPacket(
                            id,
                            leftBumper.castPacketData(),
                            rightBumper.castPacketData(),
                            a.castPacketData(),
                            b.castPacketData(),
                            x.castPacketData(),
                            y.castPacketData(),
                            up.castPacketData(),
                            down.castPacketData(),
                            left.castPacketData(),
                            right.castPacketData(),
                            leftStickX.castPacketData(),
                            leftStickY.castPacketData(),
                            leftStickButton.castPacketData(),
                            rightStickX.castPacketData(),
                            rightStickY.castPacketData(),
                            rightStickButton.castPacketData(),
                            leftTrigger.castPacketData(),
                            rightTrigger.castPacketData()
                    )
                }

        /**
         * Try converting [DoublePacket] into [VoltageDataPacket]
         */
        fun DoublePacket.VoltageDataPacket() =
                takeIf { label == Label.VoltageDataPacket }?.run { VoltageDataPacket(data) }

        /**
         * Try converting [StringPacket] into [TelemetryDataPacket]
         */
        fun StringPacket.TelemetryDataPacket() =
                takeIf { label == Label.TelemetryDataPacket }?.run {
                    val (caption, string) = data.split("\$\$")
                    TelemetryDataPacket(caption, string)
                }

        /**
         * Try converting [CombinedPacket] into [DeviceDescriptionPacket]
         */
        fun CombinedPacket.DeviceDescriptionPacket() =
                takeIf { label == Label.DeviceDescriptionPacket }
                        ?.run { DeviceDescriptionPacket(data[0].data as Byte, data[1].data as String) }
    }
}
