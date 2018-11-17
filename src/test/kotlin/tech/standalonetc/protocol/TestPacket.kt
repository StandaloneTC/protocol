package tech.standalonetc.protocol

import org.junit.Assert
import org.junit.Test
import tech.standalonetc.protocol.packtes.*

class TestPacket {

    @Test
    fun testEncoding() {
        val a = LongPacket(0, 8331L)
        val encoded = a.toByteArray().toPrimitivePacket()
        Assert.assertEquals(8331L, encoded.data)
    }

    @Test
    fun testCombined() {
        val a = DoublePacket(0, 8331.233)
        val b = DoublePacket(0, 233.8331)
        val encoded = (a + b).toByteArray().toPrimitivePacket() as CombinedPacket
        Assert.assertEquals(8331.233, encoded.data[0].data)
        Assert.assertEquals(233.8331, encoded.data[1].data)
    }
}