package tech.standalonetc.protocol.packtes

import java.nio.ByteBuffer

fun Byte.encode() = byteArrayOf(this)

fun String.encode() =
        toByteArray().let { src ->
            assert(src.size <= 127)
            ByteArray(1 + src.size).apply {
                src.copyInto(this, 1)
                set(0, src.size.toByte())
            }
        }

fun Boolean.encode() = (if (this) 1 else 0).toByte().encode()

fun Int.encodeZigZag(): ByteArray = toLong().encodeZigZag()


fun decodeToString(byteArray: ByteArray) =
        String(byteArray.copyOfRange(1, byteArray[0].toInt() + 1))


fun Double.encodeZigZag() = toBits().encodeZigZag()


fun Long.encode(): ByteArray = ByteBuffer.allocate(8).putLong(this).array()
fun ByteArray.decodeToLong() = ByteBuffer.wrap(this).long

fun Double.encode() = toBits().encode()
fun ByteArray.decodeToDouble() = Double.fromBits(decodeToLong())

fun Int.encode(): ByteArray = ByteBuffer.allocate(4).putInt(this).array()
fun ByteArray.decodeToInt() = ByteBuffer.wrap(this).int
