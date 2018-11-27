package tech.standalonetc.protocol

import org.junit.Assert
import org.junit.Test
import tech.standalonetc.protocol.packet.*

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

        val (a2, b2) = encoded
        Assert.assertEquals(8331.233, a2.data)
        Assert.assertEquals(233.8331, b2.data)
    }

    @Test
    fun testGamepad() {
        val a = RobotPacket.GamepadDataPacket(
                RobotPacket.BuiltinId.GamepadMaster,
                false,
                false,
                true,
                false,
                false,
                false,
                true,
                true,
                false,
                true,
                .7,
                .5,
                false,
                .3,
                .2,
                false,
                .1,
                .2
        )
        val encoded = a.toByteArray()
        val a1 = encoded.toPrimitivePacket() as CombinedPacket
        val (_, _, _, _, _, trigger) = a1
        Assert.assertEquals(.1, (trigger as CombinedPacket).data[0].data)
    }
}