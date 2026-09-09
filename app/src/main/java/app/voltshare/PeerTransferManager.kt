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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
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
    val hosting: Boolean = false,
    val transferring: Boolean = false,
    val completedFiles: Int = 0,
    val totalFiles: Int = 0,
    val currentFile: String? = null,
    val bytesTransferred: Long = 0L,
    val totalBytes: Long = 0L,
    val errorLog: String? = null,
)

class PeerTransferManager(
    private val context: Context,
    private val vault: VaultRepository,
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val nsd = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val _peers = MutableStateFlow<List<PeerDevice>>(emptyList())
    private val _status = MutableStateFlow(TransferStatus())
    private val _vaultRevision = MutableStateFlow(0)
    val peers: StateFlow<List<PeerDevice>> = _peers
    val status: StateFlow<TransferStatus> = _status
    val vaultRevision: StateFlow<Int> = _vaultRevision

    private var server: ServerSocket? = null
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    fun startHosting() {
        if (server != null) return
        scope.launch {
            runCatching {
                server = ServerSocket(0)
                server?.reuseAddress = true
                val info = NsdServiceInfo().apply {
                    serviceName = "VoltShare-${Build.MODEL.take(16)}"
                    serviceType = SERVICE_TYPE
                    port = server!!.localPort
                }
                registrationListener = registrationListener()
                nsd.registerService(info, NsdManager.PROTOCOL_DNS_SD, registrationListener)
                _status.emit(TransferStatus("This device is visible to nearby VoltShare devices", active = true, hosting = true))
                while (true) {
                    val connection = server?.accept() ?: break
                    runCatching { handleIncoming(connection) }
                        .onFailure {
                            _status.emit(
                                TransferStatus(
                                    "A nearby transfer was rejected",
                                    hosting = true,
                                    errorLog = buildTechnicalError("Incoming transfer", it),
                                ),
                            )
                        }
                }
            }.onFailure {
                _status.emit(
                    TransferStatus(
                        "Could not open a local transfer channel",
                        errorLog = buildTechnicalError("Local transfer channel", it),
                    ),
                )
            }
        }
    }

    fun discoverPeers() {
        stopDiscovery()
        _peers.value = emptyList()
        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(serviceType: String) {
                scope.launch {
                    _status.emit(
                        TransferStatus(
                            "Looking for nearby VoltShare devices",
                            active = true,
                            hosting = server != null,
                        ),
                    )
                }
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
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                stopDiscovery()
                scope.launch {
                    _status.emit(
                        TransferStatus(
                            "Nearby-device search failed",
                            errorLog = "VoltShare discovery error\nService: $serviceType\nError code: $errorCode",
                        ),
                    )
                }
            }
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) = Unit
        }
        nsd.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    fun send(peer: PeerDevice, file: VaultFile) {
        send(peer, listOf(file))
    }

    fun send(peer: PeerDevice, files: List<VaultFile>) {
        if (files.isEmpty()) return
        scope.launch {
            val totalBytes = files.sumOf { it.sizeBytes }.coerceAtLeast(1L)
            var completedBytes = 0L
            var completedFiles = 0
            val errors = mutableListOf<String>()
            files.forEachIndexed { index, file ->
                try {
                    transferOne(
                        peer = peer,
                        file = file,
                        fileIndex = index,
                        totalFiles = files.size,
                        completedFiles = completedFiles,
                        completedBytes = completedBytes,
                        totalBytes = totalBytes,
                    )
                    completedFiles += 1
                    completedBytes += file.sizeBytes
                } catch (error: Throwable) {
                    errors += buildErrorReport(file, error)
                    _status.emit(
                        TransferStatus(
                            label = "Could not send ${file.name}",
                            progress = (completedBytes.toFloat() / totalBytes).coerceIn(0f, 1f),
                            active = true,
                            transferring = true,
                            completedFiles = completedFiles,
                            totalFiles = files.size,
                            currentFile = file.name,
                            bytesTransferred = completedBytes,
                            totalBytes = totalBytes,
                            hosting = server != null,
                            errorLog = errors.joinToString("\n\n"),
                        ),
                    )
                }
            }
            _status.emit(
                TransferStatus(
                    label = if (errors.isEmpty()) {
                        "Sent and verified ${files.size} file${if (files.size == 1) "" else "s"}"
                    } else {
                        "Finished with ${errors.size} transfer error${if (errors.size == 1) "" else "s"}"
                    },
                    progress = (completedBytes.toFloat() / totalBytes).coerceIn(0f, 1f),
                    active = false,
                    hosting = server != null,
                    transferring = false,
                    completedFiles = completedFiles,
                    totalFiles = files.size,
                    bytesTransferred = completedBytes,
                    totalBytes = totalBytes,
                    errorLog = errors.takeIf { it.isNotEmpty() }?.joinToString("\n\n"),
                ),
            )
        }
    }

    private suspend fun transferOne(
        peer: PeerDevice,
        file: VaultFile,
        fileIndex: Int,
        totalFiles: Int,
        completedFiles: Int,
        completedBytes: Long,
        totalBytes: Long,
    ) {
        _status.emit(
            TransferStatus(
                label = "Preparing ${file.name}",
                active = true,
                hosting = server != null,
                transferring = true,
                completedFiles = completedFiles,
                totalFiles = totalFiles,
                currentFile = file.name,
                bytesTransferred = completedBytes,
                totalBytes = totalBytes,
            ),
        )
        val checksum = vault.sha256(file) ?: error("Could not read encrypted vault file")
        Socket(peer.host, peer.port).use { socket ->
            socket.tcpNoDelay = true
            socket.sendBufferSize = BUFFER_SIZE
            socket.receiveBufferSize = BUFFER_SIZE
            DataOutputStream(BufferedOutputStream(socket.getOutputStream(), BUFFER_SIZE)).use { output ->
                output.writeInt(PROTOCOL_MAGIC)
                output.writeInt(PROTOCOL_VERSION)
                output.writeUTF(file.name)
                output.writeUTF(file.mimeType)
                output.writeLong(file.sizeBytes)
                output.writeUTF(checksum)
                output.writeUTF(file.folderPath)
                vault.openDecrypted(file)?.use { input ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var sent = 0L
                    var lastUpdate = 0L
                    while (sent < file.sizeBytes) {
                        val read = input.read(buffer, 0, minOf(buffer.size.toLong(), file.sizeBytes - sent).toInt())
                        check(read > 0) { "Vault file ended early" }
                        output.write(buffer, 0, read)
                        sent += read
                        if (sent == file.sizeBytes || sent - lastUpdate >= BUFFER_SIZE) {
                            output.flush()
                            lastUpdate = sent
                            val overallBytes = completedBytes + sent
                            _status.emit(
                                TransferStatus(
                                    label = "Sending ${file.name} • ${fileIndex + 1} of $totalFiles",
                                    progress = (overallBytes.toFloat() / totalBytes).coerceIn(0f, 1f),
                                    active = true,
                                    hosting = server != null,
                                    transferring = true,
                                    completedFiles = completedFiles,
                                    totalFiles = totalFiles,
                                    currentFile = file.name,
                                    bytesTransferred = overallBytes,
                                    totalBytes = totalBytes,
                                ),
                            )
                        }
                    }
                } ?: error("Could not open decrypted stream for ${file.name}")
            }
        }
        vault.markSent(file)
    }

    private fun buildErrorReport(file: VaultFile, error: Throwable): String {
        return buildString {
            appendLine("VoltShare transfer error")
            appendLine("File: ${file.name}")
            appendLine("Size: ${file.sizeBytes} bytes")
            appendLine("MIME: ${file.mimeType}")
            appendLine("Time: ${System.currentTimeMillis()}")
            appendLine("Reason: ${error.message ?: error::class.java.simpleName}")
            appendLine()
            append(error.stackTraceToString())
        }
    }

    private fun buildTechnicalError(title: String, error: Throwable): String {
        return buildString {
            appendLine("VoltShare technical error")
            appendLine("Stage: $title")
            appendLine("Time: ${System.currentTimeMillis()}")
            appendLine("Reason: ${error.message ?: error::class.java.simpleName}")
            appendLine()
            append(error.stackTraceToString())
        }
    }

    fun close() {
        stopDiscovery()
        runCatching { server?.close() }
        server = null
        registrationListener?.let { runCatching { nsd.unregisterService(it) } }
        registrationListener = null
        _status.value = TransferStatus()
    }

    private fun handleIncoming(socket: Socket) {
        socket.use {
            it.tcpNoDelay = true
            it.receiveBufferSize = BUFFER_SIZE
            val input = DataInputStream(BufferedInputStream(it.getInputStream(), BUFFER_SIZE))
            check(input.readInt() == PROTOCOL_MAGIC) { "Unknown VoltShare peer" }
            check(input.readInt() == PROTOCOL_VERSION) { "Incompatible VoltShare version" }
            val name = input.readUTF()
            val mime = input.readUTF()
            val size = input.readLong()
            val checksum = input.readUTF()
            val folderPath = input.readUTF()
            check(size in 0..MAX_FILE_SIZE_BYTES) { "Invalid transfer size" }
            _status.value = TransferStatus("Receiving $name", active = true, hosting = server != null, transferring = true)
            check(vault.importIncoming(name, mime, input, size, checksum, folderPath) != null) {
                "Received file did not verify"
            }
            _vaultRevision.update { it + 1 }
            _status.value = TransferStatus(
                "Received and verified $name in your private vault",
                1f,
                hosting = server != null,
                transferring = false,
            )
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
        private const val PROTOCOL_MAGIC = 0x56534C31
        private const val PROTOCOL_VERSION = 1
        private const val BUFFER_SIZE = 1024 * 1024
        private const val MAX_FILE_SIZE_BYTES = 50L * 1024L * 1024L * 1024L
    }
}