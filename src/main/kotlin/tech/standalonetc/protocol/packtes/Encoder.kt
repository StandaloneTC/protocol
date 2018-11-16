package tech.standalonetc.protocol.packtes

import tech.standalonetc.protocol.CombinedPacket
import tech.standalonetc.protocol.DoublePacket
import tech.standalonetc.protocol.StringPacket

typealias EncoderPosition = DoublePacket

typealias EncoderAngle = DoublePacket

typealias EncoderReset = StringPacket

class EncoderResetPacket(id: Byte) : StringPacket(id, "reset")

class EncoderData(
    id: Byte,
    val position: Double,
    val angle: Double
) : CombinedPacket(id, EncoderPosition(id, position), EncoderAngle(id, angle))