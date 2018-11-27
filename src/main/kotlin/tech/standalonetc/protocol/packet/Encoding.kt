package tech.standalonetc.protocol.packet

import java.nio.ByteBuffer

/**
 * Encode a [Byte] as a [ByteArray]
 */
fun Byte.encode() = byteArrayOf(this)

/**
 * Encode a [String] as a [ByteArray]
 */
fun String.encode() =
        toByteArray().let { src ->
            assert(src.size <= 127)
            ByteArray(1 + src.size).apply {
                src.copyInto(this, 1)
                set(0, src.size.toByte())
            }
        }

/**
 * Encode a [Boolean] as a [ByteArray]
 */
fun Boolean.encode() = (if (this) 1 else 0).toByte().encode()

/**
 * Encode using *ZigZag*
 */
fun Int.encodeZigZag(): ByteArray = toLong().encodeZigZag()


/**
 * Decode a [byteArray] into a [String]
 */
fun decodeToString(byteArray: ByteArray) =
        String(byteArray.copyOfRange(1, byteArray[0].toInt() + 1))

/**
 * Encode using *ZigZag*
 */
fun Double.encodeZigZag() = toBits().encodeZigZag()

/**
 * Encode a [Long] as a [ByteArray]
 */
fun Long.encode(): ByteArray = ByteBuffer.allocate(8).putLong(this).array()

fun ByteArray.decodeToLong() = ByteBuffer.wrap(this).long

/**
 * Encode a [Double] as a [ByteArray]
 */
fun Double.encode() = toBits().encode()

/**
 * Decode a [ByteArray] into a [Double]
 */
fun ByteArray.decodeToDouble() = Double.fromBits(decodeToLong())

/**
 * Encode a [Int] as a [ByteArray]
 */
fun Int.encode(): ByteArray = ByteBuffer.allocate(4).putInt(this).array()

/**
 * Decode a [ByteArray] into a [Int]
 */
fun ByteArray.decodeToInt() = ByteBuffer.wrap(this).int
