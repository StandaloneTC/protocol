package tech.standalonetc.protocol

import tech.standalonetc.protocol.RobotPacket.Conversion.PwmEnablePacket
import tech.standalonetc.protocol.packet.*
import tech.standalonetc.protocol.packet.convert.PacketConversion
import tech.standalonetc.protocol.packet.convert.toConverter

/**
 * Robot packet protocol
 */
@Suppress("FunctionName", "MemberVisibilityCanBePrivate", "CanBeParameter")
object RobotPacket {

    /**
     * Device description packet
     * To let slave know what device we should use.
     */
    class DeviceDescriptionPacket(val deviceId: Byte, val deviceName: String) : CombinedPacket(
            BuiltinId.Environment,
            BytePacket(BuiltinId.Environment, deviceId),
            StringPacket(BuiltinId.Environment, deviceName),
            label = Label.DeviceDescriptionPacket
    )

    /**
     * Pwm enable packet
     * describe whether a continuous servo or normal servo enable pwm output
     */
    class PwmEnablePacket(id: Byte, val enable: Boolean) : BooleanPacket(id, enable, Label.PwmEnablePacket)

    /**
     * Continuous servo packet
     */
    class ContinuousServoPowerPacket(id: Byte, val power: Double) :
            DoublePacket(id, power, Label.ContinuousServoPowerPacket)

    /**
     * Encoder data packet
     */
    class EncoderDataPacket(
            id: Byte,
            val position: Int,
            val speed: Double
    ) : CombinedPacket(
            id,
            IntPacket(id, position),
            DoublePacket(id, speed),
            label = Label.EncoderDataPacket
    )

    /**
     * Motor power packet
     */
    class MotorPowerPacket(id: Byte, val power: Double) : DoublePacket(id, power, Label.MotorPowerPacket)

    /**
     * Servo position packet
     */
    class ServoPositionPacket(id: Byte, val degree: Double) : DoublePacket(id, degree, Label.ServoPositionPacket)

    /**
     * Device reset packet
     * try resetting [id]
     */
    class DeviceResetPacket(id: Byte) : BytePacket(id, 0, Label.DeviceResetPacket)

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
            CombinedPacket(
                    id,
                    BooleanPacket(id, aButton),
                    BooleanPacket(id, bButton),
                    BooleanPacket(id, xButton),
                    BooleanPacket(id, yButton)
            ),
            //direction key part
            CombinedPacket(
                    id,
                    BooleanPacket(id, upButton),
                    BooleanPacket(id, downButton),
                    BooleanPacket(id, leftButton),
                    BooleanPacket(id, rightButton)
            ),
            //left stick part
            CombinedPacket(
                    id,
                    DoublePacket(id, leftStickX),
                    DoublePacket(id, leftStickY),
                    BooleanPacket(id, leftStickButton)
            ),
            //right stick part
            CombinedPacket(
                    id,
                    DoublePacket(id, rightStickX),
                    DoublePacket(id, rightStickY),
                    BooleanPacket(id, rightStickButton)
            ),
            //trigger part
            DoublePacket(id, leftTrigger) + DoublePacket(id, rightTrigger),
            label = Label.GamepadDataPacket
    )

    /**
     * Robot voltage packet
     */
    class VoltageDataPacket(val voltage: Double) : DoublePacket(BuiltinId.Environment, voltage, Label.VoltageDataPacket)

    /**
     * Telemetry data packet
     */
    class TelemetryDataPacket(val caption: String, val string: String) :
            StringPacket(BuiltinId.Environment, "$caption\$\$$string", Label.TelemetryDataPacket)

    /**
     * Telemetry clear packet
     * clear all telemetry messages.
     */
    object TelemetryClearPacket : BytePacket(BuiltinId.Environment, 0, label = Label.TelemetryClearPacket)

    /**
     * OpMode info packet
     */
    class OpModeInfoPacket(
            val opModeName: String,
            val state: Byte
    ) : CombinedPacket(
            BuiltinId.Environment,
            StringPacket(BuiltinId.Environment, opModeName),
            BytePacket(BuiltinId.Environment, state),
            label = Label.OpModeInfoPacket
    ) {
        companion object {
            const val INIT: Byte = 0
            const val START: Byte = 1
            const val STOP: Byte = 2
        }
    }

    /**
     * Operation period packet
     */
    class OperationPeriodPacket(val period: Int) :
            IntPacket(BuiltinId.Environment, period, Label.OperationPeriodPacket)


    /**
     * Touch sensor data packet
     */
    class TouchSensorDataPacket(id: Byte,
                                val bePressed: Boolean
    ) : BooleanPacket(id, bePressed, Label.TouchSensorDataPacket)

    /**
     * Gyro data packet
     */
    class GyroDataPacket(
            id: Byte,
            val pitchRate: Double,
            val yawRate: Double,
            val rollRate: Double
    ) : CombinedPacket(
            id,
            DoublePacket(id, pitchRate),
            DoublePacket(id, yawRate),
            DoublePacket(id, rollRate),
            label = Label.GyroDataPacket
    )

    /**
     * Color sensor data packet
     */
    class ColorSensorDataPacket(
            id: Byte,
            val r: Double,
            val g: Double,
            val b: Double,
            val a: Double
    ) : CombinedPacket(
            id,
            DoublePacket(id, r),
            DoublePacket(id, g),
            DoublePacket(id, b),
            DoublePacket(id, a),
            label = Label.ColorSensorDataPacket
    )

    /**
     * Color sensor led packet
     */
    class ColorSensorLedPacket(
            id: Byte,
            enableLed: Boolean
    ) : BooleanPacket(
            id,
            enableLed,
            Label.ColorSensorLedPacket
    )

    internal object Label {
        const val PwmEnablePacket: Byte = 0
        const val ContinuousServoPowerPacket: Byte = 1
        const val EncoderDataPacket: Byte = 2
        const val MotorPowerPacket: Byte = 3
        const val ServoPositionPacket: Byte = 4
        const val DeviceResetPacket: Byte = 5
        const val GamepadDataPacket: Byte = 6
        const val VoltageDataPacket: Byte = 7
        const val TelemetryDataPacket: Byte = 8
        const val DeviceDescriptionPacket: Byte = 9
        const val TelemetryClearPacket: Byte = 10
        const val OpModeInfoPacket: Byte = 11
        const val OperationPeriodPacket: Byte = 12
        const val TouchSensorDataPacket: Byte = 13
        const val GyroDataPacket: Byte = 14
        const val ColorSensorDataPacket: Byte = 15
        const val ColorSensorLedPacket: Byte = 16
    }

    /**
     * Built-in id
     * unrelated to device.
     */
    object BuiltinId {
        /**
         * Environment id
         */
        const val Environment: Byte = 126

        /**
         * Master gamepad id
         */
        const val GamepadMaster: Byte = 125

        /**
         * Helper gamepad id
         */
        const val GamepadHelper: Byte = 124
    }

    /**
     * Wrap a primitive [Packet] to a RobotPacket
     */
    object Conversion : PacketConversion<Packet<*>>() {

        init {
            booleanConverters.apply {
                add({ packet: BooleanPacket -> packet.PwmEnablePacket() }.toConverter())
                add({ packet: BooleanPacket -> packet.TouchSensorDataPacket() }.toConverter())
                add({ packet: BooleanPacket -> packet.ColorSensorLedPacket() }.toConverter())
            }
            doubleConverters.apply {
                add({ packet: DoublePacket -> packet.ContinuousServoPowerPacket() }.toConverter())
                add({ packet: DoublePacket -> packet.MotorPowerPacket() }.toConverter())
                add({ packet: DoublePacket -> packet.ServoPositionPacket() }.toConverter())
                add({ packet: DoublePacket -> packet.VoltageDataPacket() }.toConverter())
            }
            intConverters.apply {
                add({ packet: IntPacket -> packet.OperationPeriodPacket() }.toConverter())
            }
            stringConverters.apply {
                add({ packet: StringPacket -> packet.TelemetryDataPacket() }.toConverter())
            }
            byteConverters.apply {
                add({ packet: BytePacket -> packet.TelemetryClearPacket() }.toConverter())
                add({ packet: BytePacket -> packet.EncoderResetPacket() }.toConverter())
            }
            combinedConverters.apply {
                add({ packet: CombinedPacket -> packet.DeviceDescriptionPacket() }.toConverter())
                add({ packet: CombinedPacket -> packet.EncoderDataPacket() }.toConverter())
                add({ packet: CombinedPacket -> packet.OpModeInfoPacket() }.toConverter())
                add({ packet: CombinedPacket -> packet.GamepadDataPacket() }.toConverter())
                add({ packet: CombinedPacket -> packet.GyroDataPacket() }.toConverter())
                add({ packet: CombinedPacket -> packet.ColorSensorDataPacket() }.toConverter())
            }
        }


        /**
         * Try converting [BooleanPacket] into [PwmEnablePacket]
         */
        fun BooleanPacket.PwmEnablePacket() =
                takeIf { label == Label.PwmEnablePacket }?.run { PwmEnablePacket(id, data) }

        /**
         * Try converting [DoublePacket] into [ContinuousServoPowerPacket]
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
         * Try converting [BytePacket] into [DeviceResetPacket]
         */
        fun BytePacket.EncoderResetPacket() =
                takeIf { label == Label.DeviceResetPacket }?.run { DeviceResetPacket(id) }

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
                        ?.run { DeviceDescriptionPacket(data[0].castPacketData(), data[1].castPacketData()) }

        /**
         * Try converting [BytePacket] into [TelemetryClearPacket]
         */
        fun BytePacket.TelemetryClearPacket() =
                takeIf { label == Label.TelemetryClearPacket }
                        ?.run { TelemetryClearPacket }

        /**
         * Try converting [CombinedPacket] into [OpModeInfoPacket]
         */
        fun CombinedPacket.OpModeInfoPacket() =
                takeIf { label == Label.OpModeInfoPacket }
                        ?.run { OpModeInfoPacket(data[0].castPacketData(), data[1].castPacketData()) }

        /**
         * Try converting [IntPacket] into [OperationPeriodPacket]
         */
        fun IntPacket.OperationPeriodPacket() =
                takeIf { label == Label.OperationPeriodPacket }
                        ?.run { OperationPeriodPacket(data) }

        /**
         * Try converting [BooleanPacket] into [TouchSensorDataPacket]
         */
        fun BooleanPacket.TouchSensorDataPacket() =
                takeIf { label == Label.TouchSensorDataPacket }
                        ?.run { TouchSensorDataPacket(id, data) }

        /**
         * Try converting [CombinedPacket] into [GyroDataPacket]
         */
        fun CombinedPacket.GyroDataPacket() =
                takeIf { label == Label.GyroDataPacket }
                        ?.run {
                            GyroDataPacket(id,
                                    data[0].castPacketData(),
                                    data[1].castPacketData(),
                                    data[2].castPacketData())
                        }

        /**
         * Try converting [CombinedPacket] into [ColorSensorDataPacket]
         */
        fun CombinedPacket.ColorSensorDataPacket() =
                takeIf { label == Label.ColorSensorLedPacket }
                        ?.run {
                            ColorSensorDataPacket(id,
                                    data[0].castPacketData(),
                                    data[1].castPacketData(),
                                    data[2].castPacketData(),
                                    data[3].castPacketData())
                        }

        /**
         * Try converting [BooleanPacket] into [ColorSensorLedPacket]
         */
        fun BooleanPacket.ColorSensorLedPacket() =
                takeIf { label == Label.ColorSensorLedPacket }
                        ?.run { ColorSensorLedPacket(id, data) }
    }
}
