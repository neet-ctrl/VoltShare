package app.voltshare

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

data class PeerDevice(
    val name: String,
    val host: InetAddress,
    val port: Int,
)

data class TransferStatus(
    val label: String = "Ready for a local transfer",
    val progress: Float = 0f,
    val active: Boolean = false,
)

class PeerTransferManager(
    private val context: Context,
    private val vault: VaultRepository,
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val nsd = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val _peers = MutableStateFlow<List<PeerDevice>>(emptyList())
    private val _status = MutableStateFlow(TransferStatus())
    val peers: StateFlow<List<PeerDevice>> = _peers
    val status: StateFlow<TransferStatus> = _status

    private var server: ServerSocket? = null
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    fun startHosting() {
        if (server != null) return
        scope.launch {
            runCatching {
                server = ServerSocket(0)
                val info = NsdServiceInfo().apply {
                    serviceName = "VoltShare-${Build.MODEL.take(16)}"
                    serviceType = SERVICE_TYPE
                    port = server!!.localPort
                }
                registrationListener = registrationListener()
                nsd.registerService(info, NsdManager.PROTOCOL_DNS_SD, registrationListener)
                _status.emit(TransferStatus("This device is visible to nearby VoltShare devices", active = true))
                while (true) {
                    val connection = server?.accept() ?: break
                    handleIncoming(connection)
                }
            }.onFailure {
                _status.emit(TransferStatus("Could not open a local transfer channel"))
            }
        }
    }

    fun discoverPeers() {
        stopDiscovery()
        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(serviceType: String) {
                scope.launch { _status.emit(TransferStatus("Looking for nearby VoltShare devices", active = true)) }
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                nsd.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                    override fun onResolveFailed(info: NsdServiceInfo, errorCode: Int) = Unit
                    override fun onServiceResolved(info: NsdServiceInfo) {
                        val peer = PeerDevice(info.serviceName, info.host, info.port)
                        scope.launch {
                            _peers.emit((_peers.value + peer).distinctBy { "${it.host.hostAddress}:${it.port}" })
                        }
                    }
                })
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                scope.launch { _peers.emit(_peers.value.filterNot { it.name == serviceInfo.serviceName }) }
            }

            override fun onDiscoveryStopped(serviceType: String) = Unit
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) = stopDiscovery()
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) = Unit
        }
        nsd.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    fun send(peer: PeerDevice, file: VaultFile) {
        scope.launch {
            val source = vault.prepareViewing(file) ?: return@launch
            runCatching {
                _status.emit(TransferStatus("Sending ${file.name}", active = true))
                Socket(peer.host, peer.port).use { socket ->
                    DataOutputStream(socket.getOutputStream()).use { output ->
                        output.writeUTF(file.name)
                        output.writeUTF(file.mimeType)
                        output.writeLong(source.length())
                        File(source.absolutePath).inputStream().use { input ->
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            var sent = 0L
                            while (true) {
                                val read = input.read(buffer)
                                if (read <= 0) break
                                output.write(buffer, 0, read)
                                sent += read
                                _status.emit(
                                    TransferStatus(
                                        "Sending ${file.name}",
                                        (sent.toFloat() / source.length()).coerceIn(0f, 1f),
                                        true,
                                    ),
                                )
                            }
                        }
                    }
                }
                _status.emit(TransferStatus("Sent securely over the local network"))
            }.onFailure {
                _status.emit(TransferStatus("Transfer failed. Keep both devices on the same Wi‑Fi"))
            }
        }
    }

    fun close() {
        stopDiscovery()
        runCatching { server?.close() }
        server = null
        registrationListener?.let { runCatching { nsd.unregisterService(it) } }
        registrationListener = null
    }

    private fun handleIncoming(socket: Socket) {
        socket.use {
            val input = DataInputStream(it.getInputStream())
            val name = input.readUTF()
            val mime = input.readUTF()
            val size = input.readLong()
            _status.value = TransferStatus("Receiving $name", active = true)
            vault.importIncoming(name, mime, input, size)
            _status.value = TransferStatus("Received $name into your private vault")
        }
    }

    private fun registrationListener() = object : NsdManager.RegistrationListener {
        override fun onServiceRegistered(serviceInfo: NsdServiceInfo) = Unit
        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) = Unit
        override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) = Unit
        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) = Unit
    }

    private fun stopDiscovery() {
        discoveryListener?.let { runCatching { nsd.stopServiceDiscovery(it) } }
        discoveryListener = null
    }

    companion object {
        private const val SERVICE_TYPE = "_voltshare._tcp."
    }
}