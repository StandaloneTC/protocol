package tech.standalonetc.protocol

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