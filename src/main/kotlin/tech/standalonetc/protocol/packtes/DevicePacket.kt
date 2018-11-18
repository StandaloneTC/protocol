package tech.standalonetc.protocol.packtes

/**
 * Device packet protocol
 */
@Suppress("FunctionName", "MemberVisibilityCanBePrivate", "CanBeParameter")
object DevicePacket {

    /**
     * Device description
     * To let slave know what device we should use.
     */
    class DeviceDescriptionPacket(val deviceId: Byte, val deviceName: String)
        : CombinedPacket(
            BuiltinId.DeviceDescriptionPacket,
            BytePacket(BuiltinId.DeviceDescriptionPacket, deviceId),
            StringPacket(BuiltinId.DeviceDescriptionPacket, deviceName),
            label = Label.DeviceDescriptionPacket)

    /**
     * Pwm enable of continuous servo and normal servo
     */
    class PwmEnablePacket(id: Byte, val enable: Boolean)
        : BooleanPacket(id, enable, Label.PwmEnablePacket)

    /**
     * Power of continuous servo
     */
    class ContinuousServoPowerPacket(id: Byte, val power: Double)
        : DoublePacket(id, power, Label.ContinuousServoPowerPacket)

    /**
     * Encoder data
     */
    class EncoderDataPacket(id: Byte, val position: Int, speed: Double)
        : CombinedPacket(id,
            IntPacket(id, position),
            DoublePacket(id, speed),
            label = Label.EncoderDataPacket)

    /**
     * Power of continuous motor
     */
    class MotorPowerPacket(id: Byte, val power: Double)
        : DoublePacket(id, power, Label.MotorPowerPacket)

    /**
     * Position of servo
     */
    class ServoPositionPacket(id: Byte, val degree: Double)
        : DoublePacket(id, degree, Label.ServoPositionPacket)

    /**
     * Reset a encoder
     */
    class EncoderResetPacket(id: Byte) : BytePacket(id, 8, Label.EncoderResetPacket)

    /**
     * Gamepad packet
     */
    sealed class GamepadDataPacket(
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
            val leftStick: Double,
            val rightStick: Double,
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
            //stick part
            DoublePacket(id, leftStick) + DoublePacket(id, rightStick),
            //trigger part
            DoublePacket(id, leftTrigger) + DoublePacket(id, rightTrigger),
            label = Label.GamepadDataPacket
    ) {
        /**
         * Master gamepad data
         */
        class Master(
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
        ) : GamepadDataPacket(125,
                leftBumper,
                rightBumper,
                aButton,
                bButton,
                xButton,
                yButton,
                upButton,
                downButton,
                leftButton,
                rightButton,
                leftStick,
                rightStick,
                leftTrigger,
                rightTrigger)

        /**
         * Helper gamepad data
         */
        class Helper(
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
        ) : GamepadDataPacket(124,
                leftBumper,
                rightBumper,
                aButton,
                bButton,
                xButton,
                yButton,
                upButton,
                downButton,
                leftButton,
                rightButton,
                leftStick,
                rightStick,
                leftTrigger,
                rightTrigger)
    }

    /**
     * Voltage of robot
     */
    class VoltageDataPacket(val voltage: Double)
        : DoublePacket(BuiltinId.Voltage, voltage, Label.VoltageDataPacket)

    /**
     * Telemetry data
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
        const val Voltage: Byte = 126
        const val GamepadMaster: Byte = 125
        const val GamepadHelper: Byte = 124
        const val Telemetry: Byte = 123
        const val DeviceDescriptionPacket: Byte = 122
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
                        ?.run { EncoderDataPacket(id, data[0].data as Int, data[1].data as Double) }

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
                    val (front, button, direction, stick, trigger) = this
                    val (leftBumper, rightBumper) = front as CombinedPacket
                    val (a, b, x, y) = button as CombinedPacket
                    val (up, down, left, right) = direction as CombinedPacket
                    val (leftStick, rightStick) = stick as CombinedPacket
                    val (leftTrigger, rightTrigger) = trigger as CombinedPacket
                    when (id) {
                        BuiltinId.GamepadMaster -> GamepadDataPacket.Master(
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
                        BuiltinId.GamepadHelper -> GamepadDataPacket.Helper(
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
                        else                    -> null
                    }
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

        fun CombinedPacket.DeviceDescriptionPacket() =
                takeIf { label == Label.DeviceDescriptionPacket }
                        ?.run { DeviceDescriptionPacket(data[0].data as Byte, data[1].data as String) }
    }
}
