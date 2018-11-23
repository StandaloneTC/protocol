package tech.standalonetc.protocol.network

import org.mechdancer.remote.builder.remoteHub
import org.mechdancer.remote.core.RemotePlugin
import org.mechdancer.remote.core.broadcastBy
import tech.standalonetc.protocol.packtes.*
import java.io.Closeable
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.Executors
import java.util.logging.Logger

/**
 * A wrapper of [org.mechdancer.remote.core.RemoteHub]
 * Provides scheduling possibilities.
 */
class NetworkClient(
    name: String,
    private val oppositeName: String,
    workers: Int = 3,
    onRawPacketReceive: PacketCallback? = null,
    onPacketReceive: PacketCallback? = null
) : Closeable {

    private val worker = Executors.newFixedThreadPool(workers + 1)

    private val plugin = StandalonePlugin()

    private val remoteHub = remoteHub(name) {
        plugins setup plugin
    }

    private val packetReceiveCallbacks =
        ConcurrentSkipListSet<PacketCallback> { a, b -> a.hashCode().compareTo(b.hashCode()) }

    private val rawPacketReceiveCallbacks =
        ConcurrentSkipListSet<PacketCallback> { a, b -> a.hashCode().compareTo(b.hashCode()) }

    private val logger = Logger.getLogger(javaClass.name)

    private var isClosed = false

    var debug = false

    private fun log(message: String) {
        if (debug)
            logger.info(message)
    }

    private fun warn(message: String) {
        if (debug)
            logger.warning(message)
    }

    init {
        worker.submit {
            while (!isClosed)
                remoteHub.listen()
        }
        repeat(workers) {
            worker.submit {
                while (!isClosed)
                    remoteHub()
            }
        }
        onPacketReceive?.let { packetReceiveCallbacks.add(it) }
        onRawPacketReceive?.let { rawPacketReceiveCallbacks.add(it) }
    }

    /**
     * Broadcast a packet.
     */
    fun broadcastPacket(packet: Packet<*>) {
        if (isClosed) throw IllegalStateException("NetworkClient has been closed.")
        remoteHub.broadcastBy<StandalonePlugin>(packet.toByteArray())
        log("Broadcast a ${packet.javaClass.simpleName}.")
    }


    /**
     * Add a packet callback.
     */
    fun addPacketCallBack(callback: PacketCallback) = packetReceiveCallbacks.add(callback)

    /**
     * Remove a packet callback.
     */
    fun removePacketCallback(callback: PacketCallback) = packetReceiveCallbacks.remove(callback)

    /**
     * Add a raw packet callback.
     * A raw packet callback will be invoked only a packet
     * failed to be wrapped.
     */
    fun addRawPacketCallback(callback: PacketCallback) = rawPacketReceiveCallbacks.add(callback)

    /**
     * Remove a raw packet callback.
     */
    fun removeRawPacketCallback(callback: PacketCallback) = rawPacketReceiveCallbacks.remove(callback)

    /**
     * Shutdown this client.
     * No side effects produced calling repeatedly.
     */
    override fun close() {
        if (isClosed) return
        isClosed = true
        packetReceiveCallbacks.clear()
        rawPacketReceiveCallbacks.clear()
        worker.shutdown()
        remoteHub.close()
        logger.info("NetworkClient closed.")
    }

    private inner class StandalonePlugin : RemotePlugin('X') {

        private fun processPacket(packet: Packet<*>) {
            with(RobotPacket.Conversion) {
                packet.run {
                    when (this) {
                        is DoublePacket ->
                            MotorPowerPacket()
                                ?: ContinuousServoPowerPacket()
                                ?: VoltageDataPacket()
                                ?: ServoPositionPacket()

                        is BooleanPacket ->
                            PwmEnablePacket()

                        is CombinedPacket ->
                            EncoderDataPacket()
                                ?: GamepadDataPacket()
                                ?: DeviceDescriptionPacket()
                                ?: OpModeInfoPacket()

                        is BytePacket ->
                            EncoderResetPacket() ?: TelemetryClearPacket()

                        is StringPacket ->
                            TelemetryDataPacket()

                        is IntPacket ->
                            OperationPeriodPacket()

                        else -> null

                    } ?: run {
                        if (packet is CombinedPacket) {
                            log("Unknown combined packet. Unpacking it.")
                            (this as CombinedPacket).data.forEach { processPacket(it) }
                            null
                        } else {
                            warn(
                                "Failed to wrap packet. " +
                                        "Calling raw packet listener."
                            )
                            rawPacketReceiveCallbacks.forEach { it(this) }
                            null
                        }
                    }
                }?.let { packet -> packetReceiveCallbacks.forEach { it(packet) } }
            }
        }

        override fun onBroadcast(sender: String, payload: ByteArray) {
            log("Received a packet from $sender.")
            if (sender != oppositeName) return
            val packet = payload.toPrimitivePacket()
            processPacket(packet)
        }

    }

}