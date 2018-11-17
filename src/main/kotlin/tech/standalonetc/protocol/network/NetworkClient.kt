package tech.standalonetc.protocol.network

import org.mechdancer.remote.builder.remoteHub
import org.mechdancer.remote.core.RemotePlugin
import org.mechdancer.remote.core.broadcastBy
import tech.standalonetc.protocol.packtes.*
import java.io.Closeable
import java.util.concurrent.Executors

/**
 * A wrapper of [org.mechdancer.remote.core.RemoteHub]
 * Provides scheduling possibilities.
 */
class NetworkClient(name: String, private val onPacketReceive: Packet<*>.() -> Unit) : Closeable {

    private val worker = Executors.newFixedThreadPool(3)

    private val plugin = StandalonePlugin()

    private val remoteHub = remoteHub(name) {
        plugins setup plugin
    }

    init {
        worker.submit {
            while (true)
                remoteHub()
        }
        worker.submit {
            while (true)
                remoteHub.listen()
        }
    }

    /**
     * broadcast a packet
     */
    fun broadcastPacket(packet: Packet<*>) {
        worker.submit { remoteHub.broadcastBy<StandalonePlugin>(packet.toByteArray()) }
    }


    override fun close() {
        worker.shutdown()
        remoteHub.close()
    }

    private inner class StandalonePlugin : RemotePlugin('X') {

        override fun onBroadcast(sender: String, payload: ByteArray) {
            val packet = payload.toPrimitivePacket()
            with(DevicePacket.Conversion) {
                packet.run {
                    when (this) {
                        is DoublePacket   ->
                            MotorPowerPacket() ?: ContinuousServoPowerPacket()

                        is BooleanPacket  ->
                            PwmEnablePacket()

                        is IntPacket      ->
                            ServoPositionPacket()

                        is CombinedPacket ->
                            EncoderDataPacket()

                        is BytePacket     ->
                            EncoderResetPacket()

                        else              -> null
                    }
                }?.let { onPacketReceive(it) }
            }
        }

    }

}