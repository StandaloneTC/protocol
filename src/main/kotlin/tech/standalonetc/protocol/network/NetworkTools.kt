package tech.standalonetc.protocol.network

import org.mechdancer.remote.modules.multicast.MulticastListener
import org.mechdancer.remote.modules.tcpconnection.LongConnectionServer
import org.mechdancer.remote.presets.RemoteDsl.Companion.remoteHub
import org.mechdancer.remote.protocol.RemotePacket
import org.mechdancer.remote.resources.Command
import tech.standalonetc.protocol.packet.CombinedPacket
import tech.standalonetc.protocol.packet.Packet
import tech.standalonetc.protocol.packet.convert.PacketConversion
import tech.standalonetc.protocol.packet.toByteArray
import tech.standalonetc.protocol.packet.toPrimitivePacket
import java.io.Closeable
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.Executors
import java.util.logging.Logger
import kotlin.concurrent.thread

/**
 * A wrapper of [org.mechdancer.remote.presets.RemoteHub]
 * Provides scheduling possibilities.
 */
class NetworkTools(
        name: String,
        var oppositeName: String,
        udpWorkers: Int = 3,
        tcpWorkers: Int = 5,
        onRawPacketReceive: PacketCallback? = null,
        onPacketReceive: PacketCallback? = null
) : Closeable {

    private val worker = Executors.newFixedThreadPool(udpWorkers + tcpWorkers)

    private val longConnectionServer = LongConnectionServer {
        if (it.isEmpty()) null
        else
            processPacket(
                    it.toPrimitivePacket(),
                    tcpPacketReceiveCallback,
                    tcpPacketReceiveCallback
            ).first


    }
    private val remoteHub = remoteHub(name) {
        newMemberDetected { log("Found $it in LAN.") }
        inAddition { longConnectionServer }
        inAddition { MulticastProcessor() }
    }

    private val packetReceiveCallbacks =
            ConcurrentSkipListSet<PacketCallback> { a, b -> a.hashCode().compareTo(b.hashCode()) }

    private val rawPacketReceiveCallbacks =
            ConcurrentSkipListSet<PacketCallback> { a, b -> a.hashCode().compareTo(b.hashCode()) }

    private var tcpPacketReceiveCallback: Packet<*>.() -> ByteArray? = { null }

    private var packetConversion: PacketConversion<*> = PacketConversion.EmptyPacketConversion

    private val logger = Logger.getLogger(javaClass.name)

    private var isClosed = false

    var debug = false

    val isConnectedToOpposite
        get() = longConnectionServer.client == oppositeName

    private fun log(message: String) {
        if (debug)
            logger.info(message)
    }

    private fun warn(message: String) {
        if (debug)
            logger.warning(message)
    }

    init {
        thread {
            remoteHub.openAllNetworks()
            remoteHub.askEveryone()
        }
        repeat(udpWorkers) {
            worker.submit {
                while (!isClosed)
                    remoteHub()
            }
        }
        repeat(tcpWorkers) {
            worker.submit {
                while (!isClosed)
                    remoteHub.accept()
            }
        }
        onPacketReceive?.let { packetReceiveCallbacks.add(it) }
        onRawPacketReceive?.let { rawPacketReceiveCallbacks.add(it) }
    }

    /**
     * Broadcast a packet.
     */
    fun broadcastPacket(packet: Packet<*>) {
        if (isClosed) throw IllegalStateException("NetworkTools has been closed.")
        remoteHub.broadcast(Cmd, packet.toByteArray())
        log("Broadcast a $packet.")
    }

    /**
     * Send a packet.
     */
    fun sendPacket(packet: Packet<*>) {
        if (isClosed) throw IllegalStateException("NetworkTools has been closed.")
        if (!isConnectedToOpposite) throw IllegalStateException("Not yet connected to opposite.")
        longConnectionServer.call(packet.toByteArray())
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
    fun setTcpPacketReceiveCallback(callback: Packet<*>.() -> ByteArray?) {
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
     * Connect to opposite
     */
    fun connect(): Boolean {
        if (isConnectedToOpposite) throw java.lang.IllegalStateException("Already connected to opposite.")
        longConnectionServer.connect(oppositeName)
        return isConnectedToOpposite
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
        worker.shutdownNow()
        logger.info("NetworkTools closed.")
    }

    private fun <R1, R2> processPacket(
            packet: Packet<*>, raw: Packet<*>.() -> R1,
            listener: Packet<*>.() -> R2
    ): Pair<R1?, R2?> =
            packetConversion.wrap(packet)?.let { p ->
                null to listener(p)
            } ?: run {
                if (packet is CombinedPacket) {
                    log("Unknown combined packet. Unpacking it.")
                    packet.data.map { processPacket(it, raw, listener) }
                    null to null
                } else {
                    warn("Failed to wrap packet. Calling raw packet listener.")
                    raw(packet) to null
                }
            }


    private object Cmd : Command {
        override val id: Byte = 108
    }

    private inner class MulticastProcessor : MulticastListener {
        override val interest: Set<Command> = setOf(Cmd)
        override fun equals(other: Any?): Boolean = false
        override fun hashCode(): Int = 0

        override fun process(remotePacket: RemotePacket) {
            val (sender, command, payload) = remotePacket
            if (command != Cmd.id) return
            if (sender != oppositeName) return
            log("Received a udp packet from $sender.")
            val packet = payload.toPrimitivePacket()
            processPacket(packet, {
                rawPacketReceiveCallbacks.forEach { it(this) }
            }) {

                packetReceiveCallbacks.forEach { it(this) }
            }
        }

    }
}