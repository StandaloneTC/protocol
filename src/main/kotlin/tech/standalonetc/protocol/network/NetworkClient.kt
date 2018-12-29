package tech.standalonetc.protocol.network

import org.mechdancer.remote.builder.remoteHub
import org.mechdancer.remote.core.RemotePlugin
import org.mechdancer.remote.core.broadcastBy
import tech.standalonetc.protocol.packet.CombinedPacket
import tech.standalonetc.protocol.packet.Packet
import tech.standalonetc.protocol.packet.convert.PacketConversion
import tech.standalonetc.protocol.packet.toByteArray
import tech.standalonetc.protocol.packet.toPrimitivePacket
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
        var oppositeName: String,
        workers: Int = 3,
        onRawPacketReceive: PacketCallback? = null,
        onPacketReceive: PacketCallback? = null
) : Closeable {

    private val worker = Executors.newFixedThreadPool(workers * 2)

    private val plugin = StandalonePlugin()

    private val remoteHub = remoteHub(name) {
        newMemberDetected = { if (debug) log("Found $this in LAN.") }
        plugins setup plugin
    }

    private val packetReceiveCallbacks =
            ConcurrentSkipListSet<PacketCallback> { a, b -> a.hashCode().compareTo(b.hashCode()) }

    private val rawPacketReceiveCallbacks =
            ConcurrentSkipListSet<PacketCallback> { a, b -> a.hashCode().compareTo(b.hashCode()) }

    private var tcpPacketReceiveCallback: Packet<*>.() -> ByteArray = { ByteArray(0) }

    private var packetConversion: PacketConversion<*> = PacketConversion.EmptyPacketConversion

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
        repeat(workers) {
            worker.submit {
                while (!isClosed)
                    remoteHub()
            }
            worker.submit {
                while (!isClosed)
                    remoteHub.listen()
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
        log("Broadcast a $packet.")
    }

    /**
     * Send a packet.
     */
    fun sendPacket(packet: Packet<*>) {
        if (isClosed) throw IllegalStateException("NetworkClient has been closed.")
        remoteHub.call('X', oppositeName, packet.toByteArray())
        log("Send a $packet.")
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
     * Set packet received listener
     */
    fun setTcpPacketReceiveCallback(callback: Packet<*>.() -> ByteArray) {
        tcpPacketReceiveCallback = callback
    }

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
     * Set packet conversion
     */
    fun setPacketConversion(packetConversion: PacketConversion<*>) {
        this.packetConversion = packetConversion
    }

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

        fun processPacket(packet: Packet<*>) {
            packetConversion.wrap(packet)?.let { p ->
                packetReceiveCallbacks.forEach { it(p) }
            } ?: run {
                if (packet is CombinedPacket) {
                    log("Unknown combined packet. Unpacking it.")
                    packet.data.forEach { processPacket(it) }
                } else {
                    warn("Failed to wrap packet. Calling raw packet listener.")
                    rawPacketReceiveCallbacks.forEach { it(packet) }
                }
            }
        }

        override fun onBroadcast(sender: String, payload: ByteArray) {
            if (sender != oppositeName) return
            log("Received a udp packet from $sender.")
            val packet = payload.toPrimitivePacket()
            processPacket(packet)
        }

        override fun onCall(sender: String, payload: ByteArray): ByteArray {
            if (sender != oppositeName) return ByteArray(0)
            log("Received a tcp packet from $sender.")
            val packet = payload.toPrimitivePacket()
            return tcpPacketReceiveCallback(packet)
        }

    }
}