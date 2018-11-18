package tech.standalonetc.protocol.network

import org.mechdancer.remote.builder.remoteHub
import org.mechdancer.remote.core.RemotePlugin
import org.mechdancer.remote.core.broadcastBy
import tech.standalonetc.protocol.packtes.*
import java.io.Closeable
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

/**
 * A wrapper of [org.mechdancer.remote.core.RemoteHub]
 * Provides scheduling possibilities.
 */
class NetworkClient(name: String, private val oppositeName: String,
                    private val onPacketReceive: Packet<*>.() -> Unit,
                    private val rawPacketReceive: Packet<*>.() -> Unit) : Closeable {

    private val worker = Executors.newFixedThreadPool(3)

    private val plugin = StandalonePlugin()

    private val remoteHub = remoteHub(name) {
        plugins setup plugin
    }

    private val broadcastQueue = LinkedBlockingQueue<ByteArray>()

    init {
        worker.submit {
            while (true)
                remoteHub()
        }
        worker.submit {
            while (true)
                remoteHub.listen()
        }
        worker.submit {
            while (true)
                remoteHub.broadcastBy<StandalonePlugin>(broadcastQueue.take())
        }
    }

    /**
     * broadcast a packet
     */
    fun broadcastPacket(packet: Packet<*>) {
        broadcastQueue.offer(packet.toByteArray())
    }


    override fun close() {
        worker.shutdown()
        remoteHub.close()
    }

    private inner class StandalonePlugin : RemotePlugin('X') {

        override fun onBroadcast(sender: String, payload: ByteArray) {
            if (sender != oppositeName) return
            val packet = payload.toPrimitivePacket()
            with(DevicePacket.Conversion) {
                packet.run {
                    when (this) {
                        is DoublePacket   ->
                            MotorPowerPacket() ?: ContinuousServoPowerPacket() ?: VoltageDataPacket()

                        is BooleanPacket  ->
                            PwmEnablePacket()

                        is IntPacket      ->
                            ServoPositionPacket()

                        is CombinedPacket ->
                            EncoderDataPacket() ?: GamepadDataPacket() ?: DeviceDescriptionPacket()

                        is BytePacket     ->
                            EncoderResetPacket()

                        is StringPacket   ->
                            TelemetryDataPacket()

                        else              -> {
                            rawPacketReceive(this)
                            null
                        }
                    }
                }?.let { onPacketReceive(it) }
            }
        }

    }

}