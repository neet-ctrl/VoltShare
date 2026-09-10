package app.voltshare

import android.app.PendingIntent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfRenderer
import android.content.ClipData
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.Settings
import android.provider.OpenableColumns
import android.text.Selection
import android.text.SpannableString
import android.text.format.Formatter
import android.text.method.ScrollingMovementMethod
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.media.MediaMetadataRetriever
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WrapText
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import androidx.core.content.FileProvider
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

private val VoltGreen = Color(0xFF00E676)
private val VoltBlack = Color(0xFF000000)
private val VoltSurface = Color(0xFF121212)
private val VoltSurfaceRaised = Color(0xFF1B1F1E)
private val VoltTextMuted = Color.White.copy(alpha = 0.52f)
private val VoltTeal = Color(0xFF00796B)
private const val SCREENSHOT_UNLOCK_DURATION_MS = 5 * 60 * 1000L

data class IncomingShare(
    val uris: List<Uri> = emptyList(),
    val text: String? = null,
    val handoffAcknowledgement: PendingIntent? = null,
)

data class InstalledAppChoice(
    val label: String,
    val packageName: String,
    val apkPaths: List<String>,
    val icon: Drawable? = null,
)

private data class PendingVaultRestore(
    val uri: Uri,
    val passwordRequired: Boolean,
)

private data class FileActionTileSpec(
    val icon: ImageVector,
    val title: String,
    val onClick: () -> Unit,
    val destructive: Boolean = false,
)

open class MainActivity : FragmentActivity() {
    private lateinit var vault: VaultRepository
    private lateinit var lockManager: LockManager
    private lateinit var transfer: PeerTransferManager
    private val installScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var pendingIncomingShareState by mutableStateOf<IncomingShare?>(null)
    private var pendingBackupUriState by mutableStateOf<Uri?>(null)
    private var vaultPickerRequestedState by mutableStateOf(false)
    private var vaultPickerPurposeState by mutableStateOf(VaultShareContract.PICKER_PURPOSE_ATTACHMENTS)
    private var pendingInstallFile: File? = null
    private var pendingInstallName: String? = null
    private var installPermissionOpened = false
    private var screenshotsAllowed = false
    var diagnosticTitle by mutableStateOf<String?>(null)
        private set
    var diagnosticLog by mutableStateOf<String?>(null)
        private set

    val pendingIncomingShare: IncomingShare?
        get() = pendingIncomingShareState

    val vaultPickerRequested: Boolean
        get() = vaultPickerRequestedState

    val vaultPickerPurpose: String
        get() = vaultPickerPurposeState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingIncomingShareState = intent.toIncomingShare()
        pendingBackupUriState = intent.toVaultShareUri()
        vaultPickerRequestedState = intent.action == VaultShareContract.ACTION_PICK_VAULT_FILES
        vaultPickerPurposeState = intent.vaultPickerPurpose()
        vault = VaultRepository(this)
        lockManager = LockManager(this)
        transfer = PeerTransferManager(this, vault)
        VaultSession.unlocked = false
        setScreenshotProtection(false)
        InstallResultStore.register { log ->
            showDiagnostic("APK installation failed", log)
        }
        setContent {
            VoltShareTheme {
                VoltShareApp(this, vault, lockManager, transfer, vaultPickerRequested, vaultPickerPurpose)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingIncomingShareState = intent.toIncomingShare()
        pendingBackupUriState = intent.toVaultShareUri()
        vaultPickerRequestedState = intent.action == VaultShareContract.ACTION_PICK_VAULT_FILES
        vaultPickerPurposeState = intent.vaultPickerPurpose()
    }

    fun consumePendingIncomingShare() {
        pendingIncomingShareState = null
    }

    val pendingBackupUri: Uri?
        get() = pendingBackupUriState

    fun consumePendingBackupUri() {
        pendingBackupUriState = null
    }

    fun completeVaultPicker(files: List<VaultFile>) {
        val exportableFiles = files.filter { file ->
            !file.locked && vault.storedFile(file).isFile
        }.let { candidates ->
            if (vaultPickerPurposeState == VaultShareContract.PICKER_PURPOSE_UNIVERSAL_RESTORE) {
                candidates.take(1)
            } else {
                candidates
            }
        }
        if (exportableFiles.isEmpty()) {
            Toast.makeText(this, "Select at least one unlocked file", Toast.LENGTH_SHORT).show()
            return
        }
        val uris = exportableFiles.map { VaultShareContract.uriForFile(it.id) }
        val result = Intent().apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (uris.isNotEmpty()) {
                data = uris.first()
                clipData = ClipData.newUri(contentResolver, "VoltShare vault files", uris.first())
                uris.drop(1).forEach { uri -> clipData?.addItem(ClipData.Item(uri)) }
                callingPackage?.let { packageName ->
                    uris.forEach { uri ->
                        grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                }
            }
            type = "*/*"
            if (uris.size == 1) {
                putExtra(Intent.EXTRA_STREAM, uris.first())
            } else {
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            }
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, uris.size > 1)
        }
        setResult(RESULT_OK, result)
        finish()
    }

    fun cancelVaultPicker() {
        setResult(RESULT_CANCELED)
        finish()
    }

    fun canUseBiometric(): Boolean {
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        return BiometricManager.from(this).canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticateWithBiometric(onSuccess: () -> Unit) {
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        if (BiometricManager.from(this).canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) return
        val prompt = BiometricPrompt(
            this,
            mainExecutor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }
            },
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock VoltShare")
                .setSubtitle("Your files stay inside the private vault")
                .setAllowedAuthenticators(authenticators)
                .build(),
        )
    }

    fun setScreenshotProtection(enabled: Boolean) {
        screenshotsAllowed = enabled
        if (enabled) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    fun showDiagnostic(title: String, log: String) {
        diagnosticTitle = title
        diagnosticLog = log
    }

    fun dismissDiagnostic() {
        diagnosticTitle = null
        diagnosticLog = null
    }

    fun installVaultPackage(vault: VaultRepository, file: VaultFile) {
        installScope.launch {
            val prepared = withContext(Dispatchers.IO) { vault.prepareViewing(file) } ?: return@launch
            pendingInstallFile = prepared
            pendingInstallName = file.name
            continuePendingInstall()
        }
    }

    fun installPreparedPackage(file: File, originalName: String) {
        pendingInstallFile = file
        pendingInstallName = originalName
        continuePendingInstall()
    }

    private fun continuePendingInstall() {
        val prepared = pendingInstallFile ?: return
        val originalName = pendingInstallName ?: prepared.name
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !packageManager.canRequestPackageInstalls()) {
            if (!installPermissionOpened) {
                installPermissionOpened = true
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:$packageName"),
                    ),
                )
            }
            return
        }
        installPermissionOpened = false
        pendingInstallFile = null
        pendingInstallName = null
        installScope.launch {
            val result = withContext(Dispatchers.IO) {
                ApkInstaller.install(this@MainActivity, prepared, originalName)
            }
            result.fold(
                onSuccess = { Toast.makeText(this@MainActivity, "Android installer opened", Toast.LENGTH_SHORT).show() },
                onFailure = { error ->
                    showDiagnostic(
                        "APK installation could not start",
                        buildString {
                            appendLine("VoltShare Android installation diagnostic")
                            appendLine("Package name: $originalName")
                            appendLine("Payload path: ${prepared.absolutePath}")
                            appendLine("Payload bytes: ${prepared.length()}")
                            appendLine()
                            appendLine(error.stackTraceToString())
                        },
                    )
                },
            )
        }
    }

    override fun onResume() {
        super.onResume()
        setScreenshotProtection(screenshotsAllowed)
        InstallResultStore.consume(this)?.let {
            showDiagnostic("APK installation failed", it)
        }
        if (pendingInstallFile != null) continuePendingInstall()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) setScreenshotProtection(screenshotsAllowed)
    }

    override fun onDestroy() {
        InstallResultStore.unregister()
        installScope.cancel()
        transfer.close()
        vault.clearViewCache()
        super.onDestroy()
    }
}

private fun Intent.vaultPickerPurpose(): String =
    getStringExtra(VaultShareContract.EXTRA_PICKER_PURPOSE)
        ?.takeIf { it == VaultShareContract.PICKER_PURPOSE_UNIVERSAL_RESTORE }
        ?: VaultShareContract.PICKER_PURPOSE_ATTACHMENTS

private fun Intent.toVaultShareUri(): Uri? =
    data?.takeIf {
        action == Intent.ACTION_VIEW &&
            (it.lastPathSegment?.substringAfterLast('/')?.endsWith(".vaultshare", ignoreCase = true) == true ||
                type == VaultShareContract.MIME_VAULTSHARE)
    }

private fun Intent.toIncomingShare(): IncomingShare? {
    if (action != Intent.ACTION_SEND && action != Intent.ACTION_SEND_MULTIPLE) return null
    val uris = buildList {
        data?.let(::add)
        clipData?.let { clip ->
            for (index in 0 until clip.itemCount) {
                clip.getItemAt(index).uri?.let(::add)
            }
        }
        if (action == Intent.ACTION_SEND) {
            getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let(::add)
        } else {
            @Suppress("DEPRECATION")
            getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.forEach(::add)
        }
    }.distinct()
    val text = getStringExtra(Intent.EXTRA_TEXT)?.takeIf { it.isNotBlank() }
    @Suppress("DEPRECATION")
    val acknowledgement = getParcelableExtra<PendingIntent>("com.twofasapp.extra.VOLTSHARE_HANDOFF_ACK")
    return IncomingShare(uris, text, acknowledgement).takeIf { it.uris.isNotEmpty() || it.text != null }
}

private fun materializeIncomingUri(context: MainActivity, share: PendingShare): PendingShare? {
    val uri = (share.source as? PendingShareSource.UriSource)?.uri ?: return share
    val file = File(context.cacheDir, "voltshare-incoming-${System.currentTimeMillis()}-${UUID.randomUUID()}")
    return runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        } ?: error("Unable to open incoming share")
        share.copy(
            id = "file:${file.absolutePath}",
            sizeBytes = file.length(),
            source = PendingShareSource.FileSource(file),
        )
    }.getOrElse {
        file.delete()
        null
    }
}

@Composable
private fun VoltShareTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = androidx.compose.material3.darkColorScheme(
            background = VoltBlack,
            surface = VoltSurface,
            primary = VoltGreen,
            onPrimary = Color.Black,
            secondary = VoltTeal,
            onBackground = Color.White,
            onSurface = Color.White,
        ),
        typography = androidx.compose.material3.Typography(
            headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black, letterSpacing = (-1.2).sp),
            headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black, letterSpacing = (-0.8).sp),
            titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
            bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 15.sp, lineHeight = 22.sp),
            bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 20.sp),
            labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
        ),
        content = content,
    )
}

private enum class AppTab(val label: String, val icon: ImageVector) {
    VAULT("Vault", Icons.Default.Folder),
    FILES("Files", Icons.Default.FolderOpen),
    SHARE("Share", Icons.Default.Share),
    SECURITY("Lock", Icons.Default.Security),
}

private enum class ShareMode(val label: String) {
    SEND("Send"),
    RECEIVE("Receive"),
}

private enum class FileFilter(val label: String) {
    ALL("All"),
    SENT("Sent"),
    RECEIVED("Received"),
}

private sealed class SecurityAction {
    object ChangeMethod : SecurityAction()
    data class SetBiometric(val enabled: Boolean) : SecurityAction()
    data class SetLock(val enabled: Boolean) : SecurityAction()
    data class SetFileLockDefault(val enabled: Boolean) : SecurityAction()
    data class SetScreenshots(val enabled: Boolean) : SecurityAction()
    object LockNow : SecurityAction()
}

@Composable
private fun VoltShareApp(
    activity: MainActivity,
    vault: VaultRepository,
    lockManager: LockManager,
    transfer: PeerTransferManager,
    vaultPickerRequested: Boolean,
    vaultPickerPurpose: String,
) {
    var configured by remember { mutableStateOf(lockManager.isConfigured()) }
    var lockEnabled by remember { mutableStateOf(lockManager.isEnabled()) }
    var biometricEnabled by remember { mutableStateOf(lockManager.isBiometricEnabled()) }
    var fileLockDefault by remember { mutableStateOf(vault.isFileLockDefault()) }
    var screenshotEnabled by remember { mutableStateOf(false) }
    var screenshotUnlockUntil by remember { mutableStateOf<Long?>(null) }
    var unlocked by remember { mutableStateOf(!configured || !lockEnabled) }
    var tab by remember { mutableStateOf(AppTab.VAULT) }
    var viewerFile by remember { mutableStateOf<VaultFile?>(null) }
    var files by remember { mutableStateOf(vault.listFiles()) }
    var folders by remember { mutableStateOf(vault.listFolders()) }
    var primaryFolder by remember { mutableStateOf(vault.primaryFolder()) }
    var currentFolder by remember { mutableStateOf("/") }
    var fileToUnlock by remember { mutableStateOf<VaultFile?>(null) }
    var fileToMove by remember { mutableStateOf<VaultFile?>(null) }
    var showNewFolder by remember { mutableStateOf(false) }
    var importFolder by remember { mutableStateOf("/") }
    var pendingShares by remember { mutableStateOf<List<PendingShare>>(emptyList()) }
    var selectedFileIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var fileActionTarget by remember { mutableStateOf<VaultFile?>(null) }
    var exportTarget by remember { mutableStateOf<VaultFile?>(null) }
    var fileToRename by remember { mutableStateOf<VaultFile?>(null) }
    var showDeleteFilesConfirmation by remember { mutableStateOf(false) }
    var showDeleteFolderConfirmation by remember { mutableStateOf<String?>(null) }
    var showMoveSelected by remember { mutableStateOf(false) }
    var lastBackPressAt by remember { mutableStateOf(0L) }
    var securityAction by remember { mutableStateOf<SecurityAction?>(null) }
    var showChangeLock by remember { mutableStateOf(false) }
    var showBackupPasswordDialog by remember { mutableStateOf(false) }
    var backupPasswordForExport by remember { mutableStateOf<String?>(null) }
    var pendingRestore by remember { mutableStateOf<PendingVaultRestore?>(null) }
    var restorePasswordError by remember { mutableStateOf<String?>(null) }
    var restoreBusy by remember { mutableStateOf(false) }
    val transferStatus by transfer.status.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val incomingShare = activity.pendingIncomingShare

    BackHandler(enabled = configured && unlocked) {
        when {
            vaultPickerRequested -> activity.cancelVaultPicker()
            viewerFile != null -> viewerFile = null
            fileToUnlock != null -> fileToUnlock = null
            showNewFolder -> showNewFolder = false
            fileToMove != null -> fileToMove = null
            fileActionTarget != null -> fileActionTarget = null
            fileToRename != null -> fileToRename = null
            showDeleteFilesConfirmation -> showDeleteFilesConfirmation = false
            showDeleteFolderConfirmation != null -> showDeleteFolderConfirmation = null
            showMoveSelected -> showMoveSelected = false
            selectedFileIds.isNotEmpty() -> selectedFileIds = emptySet()
            tab == AppTab.FILES && currentFolder != "/" -> currentFolder = currentFolder.parentFolder()
            tab != AppTab.VAULT -> tab = AppTab.VAULT
            else -> {
                val now = System.currentTimeMillis()
                if (now - lastBackPressAt < 1800L) {
                    activity.finish()
                } else {
                    lastBackPressAt = now
                    Toast.makeText(activity, "Press back again to exit", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    BackHandler(enabled = !configured || !unlocked) {
        Toast.makeText(
            activity,
            if (!configured) "Finish setting up your vault to continue" else "Unlock VoltShare to continue",
            Toast.LENGTH_SHORT,
        ).show()
    }

    LaunchedEffect(transfer) {
        transfer.status.collect { status ->
            if (status.label.startsWith("Received and verified") || status.label.startsWith("Sent and verified")) {
                files = vault.listFiles()
                folders = vault.listFolders()
            }
        }
    }

    LaunchedEffect(lockEnabled) {
        if (!lockEnabled) {
            biometricEnabled = false
            fileLockDefault = false
            files = withContext(Dispatchers.IO) { vault.disableAllFileLocks() }
        }
    }

    LaunchedEffect(configured, unlocked) {
        VaultSession.unlocked = configured && unlocked
    }

    LaunchedEffect(screenshotEnabled) {
        activity.setScreenshotProtection(!screenshotEnabled)
    }

    LaunchedEffect(screenshotUnlockUntil) {
        val deadline = screenshotUnlockUntil ?: return@LaunchedEffect
        val remaining = deadline - System.currentTimeMillis()
        if (remaining <= 0L) {
            screenshotEnabled = false
            screenshotUnlockUntil = null
        } else {
            delay(remaining)
            screenshotEnabled = false
            screenshotUnlockUntil = null
        }
    }

    LaunchedEffect(unlocked, lockEnabled) {
        if (!unlocked || !lockEnabled) {
            screenshotEnabled = false
            screenshotUnlockUntil = null
        }
    }

    LaunchedEffect(configured, unlocked, incomingShare) {
        if (!configured || !unlocked || incomingShare == null) return@LaunchedEffect
        val picked = withContext(Dispatchers.IO) {
            val uriShares = incomingShare.uris.mapNotNull { uri ->
                pendingShareFromUri(activity, uri)?.let { share ->
                    if (incomingShare.handoffAcknowledgement != null) {
                        materializeIncomingUri(activity, share)
                    } else {
                        share
                    }
                }
            }
            val textShare = incomingShare.text?.let { createTextShare(activity, it) }
            uriShares + listOfNotNull(textShare)
        }
        if (
            incomingShare.handoffAcknowledgement != null &&
            incomingShare.uris.isNotEmpty() &&
            picked.count { it.source is PendingShareSource.FileSource } == incomingShare.uris.size
        ) {
            runCatching { incomingShare.handoffAcknowledgement.send() }
        }
        pendingShares = (pendingShares + picked).distinctBy { it.id }
        tab = AppTab.SHARE
        activity.consumePendingIncomingShare()
    }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        uris.forEach { uri ->
            runCatching {
                activity.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            vault.importUri(uri, importFolder)
        }
        files = vault.listFiles()
    }
    val shareFilePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        val picked = uris.mapNotNull { pendingShareFromUri(activity, it) }
        pendingShares = (pendingShares + picked).distinctBy { it.id }
    }
    val mediaPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        val picked = uris.mapNotNull { pendingShareFromUri(activity, it) }
        pendingShares = (pendingShares + picked).distinctBy { it.id }
    }
    val folderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val pending = withContext(Dispatchers.IO) {
                createFolderArchive(activity, uri)?.let { archive ->
                    pendingShareFromFile(archive, archive.nameWithoutExtension + ".zip", "application/zip")
                }
            }
            pending?.let { pendingShares = (pendingShares + it).distinctBy { share -> share.id } }
        }
    }
    val exportPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        val target = exportTarget
        exportTarget = null
        if (uri == null || target == null) return@rememberLauncherForActivityResult
        runCatching {
            activity.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
        }
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                copyVaultFileToTree(activity, vault, target, uri)
            }
            result.fold(
                onSuccess = { Toast.makeText(activity, "Saved ${target.name}", Toast.LENGTH_SHORT).show() },
                onFailure = {
                    Toast.makeText(
                        activity,
                        "Could not save ${target.name}: ${it.message ?: "storage error"}",
                        Toast.LENGTH_LONG,
                    ).show()
                },
            )
        }
    }
    val backupPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(VaultShareContract.MIME_VAULTSHARE),
    ) { uri ->
        val password = backupPasswordForExport
        backupPasswordForExport = null
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val temporary = File(activity.cacheDir, "vaultshare-export-${UUID.randomUUID()}.vaultshare")
                    try {
                        val summary = temporary.outputStream().use { output ->
                            vault.writeVaultShareBackup(output, password)
                        }
                        val output = runCatching {
                            activity.contentResolver.openOutputStream(uri, "wt")
                        }.getOrNull() ?: activity.contentResolver.openOutputStream(uri, "w")
                        check(output != null) { "The selected storage provider could not open the backup file." }
                        output.use { destination ->
                            temporary.inputStream().use { source -> source.copyTo(destination, 1024 * 1024) }
                        }
                        val savedSize = DocumentFile.fromSingleUri(activity, uri)?.length() ?: -1L
                        check(savedSize < 0L || savedSize == temporary.length()) {
                            "The selected storage provider saved an incomplete backup file."
                        }
                        summary
                    } finally {
                        temporary.delete()
                    }
                }
            }
            result.fold(
                onSuccess = { summary ->
                    Toast.makeText(
                        activity,
                        "Backup saved: ${summary.fileCount} file${if (summary.fileCount == 1) "" else "s"}",
                        Toast.LENGTH_LONG,
                    ).show()
                },
                onFailure = { error ->
                    activity.showDiagnostic(
                        "Backup creation failed",
                        technicalLog("VoltShare backup creation diagnostic", error),
                    )
                },
            )
        }
    }
    fun startRestore(uri: Uri, password: String?) {
        restoreBusy = true
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    activity.contentResolver.openInputStream(uri)?.use { input ->
                        vault.restoreVaultShareBackup(input, password)
                    } ?: error("The selected backup file could not be opened.")
                }
            }
            restoreBusy = false
            result.fold(
                onSuccess = { summary ->
                    pendingRestore = null
                    restorePasswordError = null
                    files = vault.listFiles()
                    folders = vault.listFolders()
                    primaryFolder = vault.primaryFolder()
                    currentFolder = "/"
                    Toast.makeText(
                        activity,
                        "Backup restored: ${summary.fileCount} file${if (summary.fileCount == 1) "" else "s"}",
                        Toast.LENGTH_LONG,
                    ).show()
                },
                onFailure = { error ->
                    if (error is VaultBackupWrongPasswordException) {
                        restorePasswordError = "Incorrect backup password. Nothing was changed."
                    } else {
                        pendingRestore = null
                        restorePasswordError = null
                        activity.showDiagnostic(
                            "Backup restore failed",
                            technicalLog("VoltShare backup restore diagnostic", error),
                        )
                    }
                },
            )
        }
    }
    fun inspectAndRestore(uri: Uri) {
        runCatching {
            activity.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        scope.launch {
            val infoResult = withContext(Dispatchers.IO) {
                runCatching {
                    activity.contentResolver.openInputStream(uri)?.use { input ->
                        vault.inspectVaultShareBackup(input)
                    } ?: error("The selected backup file could not be opened.")
                }
            }
            infoResult.fold(
                onSuccess = { info ->
                    if (info.passwordRequired) {
                        restorePasswordError = null
                        pendingRestore = PendingVaultRestore(uri, passwordRequired = true)
                    } else {
                        startRestore(uri, null)
                    }
                },
                onFailure = { error ->
                    activity.showDiagnostic(
                        "Backup inspection failed",
                        technicalLog("VoltShare backup inspection diagnostic", error),
                    )
                },
            )
        }
    }
    val restorePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        inspectAndRestore(uri)
    }

    if (!configured) {
        SetupLockScreen(
            onComplete = {
                configured = true
                unlocked = true
            },
        )
        return
    }

    if (!unlocked) {
        UnlockScreen(
            lockManager = lockManager,
            biometricEnabled = biometricEnabled && activity.canUseBiometric(),
            onUnlock = { unlocked = true },
            onBiometric = { activity.authenticateWithBiometric { unlocked = true } },
        )
        return
    }

    LaunchedEffect(activity.pendingBackupUri) {
        activity.pendingBackupUri?.let { uri ->
            activity.consumePendingBackupUri()
            inspectAndRestore(uri)
        }
    }

    activity.diagnosticLog?.let { log ->
        TechnicalErrorDialog(
            title = activity.diagnosticTitle ?: "VoltShare diagnostic",
            log = log,
            onDismiss = activity::dismissDiagnostic,
        )
    }

    if (showBackupPasswordDialog) {
        VaultSharePasswordDialog(
            title = "Create .vaultshare backup",
            description = "Choose whether this plain backup should require a password during restore. The file contents are not encrypted.",
            error = null,
            allowNoPassword = true,
            onDismiss = { showBackupPasswordDialog = false },
            onConfirm = { password ->
                showBackupPasswordDialog = false
                backupPasswordForExport = password
                backupPicker.launch("VoltShare-backup-${System.currentTimeMillis()}.vaultshare")
            },
        )
    }

    pendingRestore?.takeIf { it.passwordRequired }?.let { restore ->
        VaultSharePasswordDialog(
            title = "Unlock backup",
            description = "This backup has a password. Enter it to restore every file and folder. Nothing is changed until it is correct.",
            error = restorePasswordError,
            allowNoPassword = false,
            busy = restoreBusy,
            onDismiss = {
                if (!restoreBusy) {
                    pendingRestore = null
                    restorePasswordError = null
                }
            },
            onConfirm = { password -> startRestore(restore.uri, password.orEmpty()) },
        )
    }

    if (vaultPickerRequested) {
        FilesHome(
            files = files,
            folders = folders,
            currentFolder = currentFolder,
            primaryFolder = primaryFolder,
            selectedFileIds = selectedFileIds,
            onImport = {},
            onFolderSelected = { currentFolder = it },
            onCreateFolder = {},
            onDeleteFolder = {},
            onMakePrimary = {},
            onOpen = { file ->
                if (file.locked) {
                    Toast.makeText(activity, "Unlock this file in VoltShare before sharing it", Toast.LENGTH_SHORT).show()
                } else {
                    selectedFileIds = if (file.id in selectedFileIds) {
                        selectedFileIds - file.id
                    } else if (vaultPickerPurpose == VaultShareContract.PICKER_PURPOSE_UNIVERSAL_RESTORE) {
                        setOf(file.id)
                    } else {
                        selectedFileIds + file.id
                    }
                }
            },
            onToggleLock = {},
            onPinFile = {},
            onReorder = { _, _ -> },
            onReorderTo = { _, _ -> },
            onLongPress = {},
            onDeleteFile = {},
            onClearSelection = { selectedFileIds = emptySet() },
            onDeleteSelected = {},
            onMoveSelected = {},
            onShareSelected = { activity.completeVaultPicker(files.filter { it.id in selectedFileIds }) },
            vault = vault,
            selectionOnly = true,
            onConfirmSelection = { activity.completeVaultPicker(files.filter { it.id in selectedFileIds }) },
            pickerTitle = if (vaultPickerPurpose == VaultShareContract.PICKER_PURPOSE_UNIVERSAL_RESTORE) {
                "Restore universal backup"
            } else {
                "Choose files"
            },
            pickerDescription = if (vaultPickerPurpose == VaultShareContract.PICKER_PURPOSE_UNIVERSAL_RESTORE) {
                "Select one universal backup file to restore in 2FAS. The file is shared read-only."
            } else {
                "Select one or more files to attach to the other app"
            },
            pickerReadyText = if (vaultPickerPurpose == VaultShareContract.PICKER_PURPOSE_UNIVERSAL_RESTORE) {
                "Ready to restore"
            } else {
                "Ready to import"
            },
            pickerFooterText = if (vaultPickerPurpose == VaultShareContract.PICKER_PURPOSE_UNIVERSAL_RESTORE) {
                "The selected backup stays in VoltShare and is shared read-only with 2FAS."
            } else {
                "Selected files are shared read-only with the requesting app."
            },
            pickerConfirmLabel = if (vaultPickerPurpose == VaultShareContract.PICKER_PURPOSE_UNIVERSAL_RESTORE) {
                "Restore the file"
            } else {
                "Import selected"
            },
        )
        return
    }

    viewerFile?.let { selected ->
        FileViewerScreen(
            activity = activity,
            vault = vault,
            file = selected,
            onBack = { viewerFile = null },
        )
        return
    }

    fileToUnlock?.let { target ->
        SecretDialog(
            title = "Unlock ${target.name}",
            type = lockManager.type(),
            onDismiss = { fileToUnlock = null },
            onConfirm = { secret ->
                if (lockManager.verify(secret)) {
                    fileToUnlock = null
                    if (isInstallable(target)) {
                        activity.installVaultPackage(vault, target)
                    } else {
                        viewerFile = target
                    }
                    true
                } else {
                    false
                }
            },
        )
    }

    securityAction?.let { action ->
        LockVerificationDialog(
            title = when (action) {
                SecurityAction.ChangeMethod -> "Verify before changing your lock"
                is SecurityAction.SetBiometric -> if (action.enabled) "Verify to enable biometric unlock" else "Verify to disable biometric unlock"
                is SecurityAction.SetLock -> if (action.enabled) "Verify to enable the vault lock" else "Verify to disable the vault lock"
                is SecurityAction.SetFileLockDefault -> if (action.enabled) "Verify to lock new files by default" else "Verify to leave new files unlocked"
                is SecurityAction.SetScreenshots -> "Verify to temporarily allow screenshots"
                SecurityAction.LockNow -> "Verify before locking the vault"
            },
            type = lockManager.type(),
            onDismiss = { securityAction = null },
            onConfirm = { secret ->
                if (!lockManager.verify(secret)) {
                    false
                } else {
                    when (action) {
                        SecurityAction.ChangeMethod -> {
                            securityAction = null
                            showChangeLock = true
                        }
                        is SecurityAction.SetBiometric -> {
                            lockManager.setBiometricEnabled(action.enabled)
                            biometricEnabled = action.enabled
                            securityAction = null
                        }
                        is SecurityAction.SetLock -> {
                            lockManager.setEnabled(action.enabled)
                            lockEnabled = action.enabled
                            if (!action.enabled) {
                                biometricEnabled = false
                                fileLockDefault = false
                                fileToUnlock = null
                                screenshotEnabled = false
                                screenshotUnlockUntil = null
                                scope.launch {
                                    files = withContext(Dispatchers.IO) { vault.disableAllFileLocks() }
                                }
                            }
                            unlocked = true
                            securityAction = null
                        }
                        is SecurityAction.SetFileLockDefault -> {
                            if (action.enabled && lockEnabled) {
                                vault.setFileLockDefault(true)
                                fileLockDefault = true
                            } else {
                                fileLockDefault = false
                                scope.launch {
                                    files = withContext(Dispatchers.IO) { vault.disableAllFileLocks() }
                                }
                            }
                            securityAction = null
                        }
                        is SecurityAction.SetScreenshots -> {
                            screenshotEnabled = action.enabled
                            screenshotUnlockUntil = if (action.enabled) {
                                System.currentTimeMillis() + SCREENSHOT_UNLOCK_DURATION_MS
                            } else {
                                null
                            }
                            securityAction = null
                        }
                        SecurityAction.LockNow -> {
                            securityAction = null
                            screenshotEnabled = false
                            screenshotUnlockUntil = null
                            unlocked = false
                        }
                    }
                    true
                }
            },
        )
    }

    if (showChangeLock) {
        ChangeLockDialog(
            currentType = lockManager.type(),
            onDismiss = { showChangeLock = false },
            onChanged = { type, secret ->
                if (lockManager.configure(type, secret)) {
                    lockEnabled = true
                    unlocked = true
                    showChangeLock = false
                }
            },
        )
    }

    if (showNewFolder) {
        NewFolderDialog(
            onDismiss = { showNewFolder = false },
            onCreate = { name ->
                vault.createFolder(name, currentFolder)
                folders = vault.listFolders()
                showNewFolder = false
            },
        )
    }

    fileToMove?.let { target ->
        MoveFileDialog(
            file = target,
            folders = folders,
            onDismiss = { fileToMove = null },
            onMove = { folder ->
                vault.moveFile(target, folder)
                files = vault.listFiles()
                fileToMove = null
            },
        )
    }

    fileActionTarget?.let { target ->
        FileActionDialog(
            file = target,
            onDismiss = { fileActionTarget = null },
            onRename = {
                fileActionTarget = null
                fileToRename = target
            },
            onToggleLock = {
                if (lockEnabled) {
                    vault.toggleLocked(target)
                    files = vault.listFiles()
                }
                fileActionTarget = null
            },
            onSaveToDevice = {
                fileActionTarget = null
                exportTarget = target
                exportPicker.launch(null)
            },
            onDelete = {
                vault.deleteFile(target)
                files = vault.listFiles()
                fileActionTarget = null
            },
            onMove = {
                fileActionTarget = null
                fileToMove = target
            },
            onSelect = {
                selectedFileIds = selectedFileIds + target.id
                fileActionTarget = null
            },
            onShare = {
                fileActionTarget = null
                if (target.locked && lockEnabled) {
                    Toast.makeText(activity, "Unlock this file before sharing it", Toast.LENGTH_SHORT).show()
                } else {
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            prepareVaultShareUri(activity, vault, target)
                        }
                        result.fold(
                            onSuccess = { uri -> launchShareSheet(activity, target, uri) },
                            onFailure = {
                                Toast.makeText(
                                    activity,
                                    "Could not share ${target.name}: ${it.message ?: "storage error"}",
                                    Toast.LENGTH_LONG,
                                ).show()
                            },
                        )
                    }
                }
            },
        )
    }

    fileToRename?.let { target ->
        NewFolderDialog(
            title = "Rename file",
            confirmLabel = "Save name",
            initialName = target.name,
            onDismiss = { fileToRename = null },
            onCreate = { name ->
                vault.renameFile(target, name)
                files = vault.listFiles()
                fileToRename = null
            },
        )
    }

    showDeleteFilesConfirmation.takeIf { it }?.let {
        AlertDialog(
            onDismissRequest = { showDeleteFilesConfirmation = false },
            containerColor = VoltSurfaceRaised,
            title = { Text("Delete selected files?", color = Color.White) },
            text = {
                Text(
                    "This permanently removes ${selectedFileIds.size} private file${if (selectedFileIds.size == 1) "" else "s"} from this device.",
                    color = VoltTextMuted,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vault.deleteFiles(files.filter { it.id in selectedFileIds })
                        selectedFileIds = emptySet()
                        files = vault.listFiles()
                        showDeleteFilesConfirmation = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF8A80)),
                ) { Text("Delete permanently") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteFilesConfirmation = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = VoltTextMuted),
                ) { Text("Cancel") }
            },
        )
    }

    showDeleteFolderConfirmation?.let { folder ->
        AlertDialog(
            onDismissRequest = { showDeleteFolderConfirmation = null },
            containerColor = VoltSurfaceRaised,
            title = { Text("Delete this folder?", color = Color.White) },
            text = {
                Text(
                    "The folder and all subfolders will be removed. Files inside will return to the private root.",
                    color = VoltTextMuted,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vault.deleteFolder(folder)
                        folders = vault.listFolders()
                        primaryFolder = vault.primaryFolder()
                        currentFolder = folder.parentFolder()
                        files = vault.listFiles()
                        showDeleteFolderConfirmation = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF8A80)),
                ) { Text("Delete folder") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteFolderConfirmation = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = VoltTextMuted),
                ) { Text("Cancel") }
            },
        )
    }

    if (showMoveSelected) {
        MoveFilesDialog(
            count = selectedFileIds.size,
            folders = folders,
            onDismiss = { showMoveSelected = false },
            onMove = { folder ->
                files.filter { it.id in selectedFileIds }.forEach { vault.moveFile(it, folder) }
                files = vault.listFiles()
                selectedFileIds = emptySet()
                showMoveSelected = false
            },
        )
    }

    fun openVaultFile(file: VaultFile) {
        if (lockEnabled && file.locked) {
            fileToUnlock = file
        } else if (isInstallable(file)) {
            activity.installVaultPackage(vault, file)
        } else {
            viewerFile = file
        }
    }

    Scaffold(
        containerColor = VoltBlack,
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = VoltSurface,
                tonalElevation = 0.dp,
            ) {
                AppTab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        icon = { Icon(item.icon, item.label) },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = VoltGreen,
                            indicatorColor = VoltGreen,
                            unselectedIconColor = VoltTextMuted,
                            unselectedTextColor = VoltTextMuted,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        AnimatedContent(
            targetState = tab,
            modifier = Modifier.padding(padding),
            label = "tab-transition",
        ) { current ->
            when (current) {
                AppTab.VAULT -> VaultHome(
                    files = files,
                    onImport = {
                        importFolder = "/"
                        picker.launch(arrayOf("*/*"))
                    },
                    onOpen = { file ->
                        openVaultFile(file)
                    },
                    onToggleLock = {
                        if (lockEnabled) vault.toggleLocked(it)
                        files = vault.listFiles()
                    },
                    onPinFile = {
                        vault.togglePinnedToTop(it)
                        files = vault.listFiles()
                    },
                    vault = vault,
                )

                AppTab.FILES -> FilesHome(
                    files = files,
                    folders = folders,
                    currentFolder = currentFolder,
                    primaryFolder = primaryFolder,
                    selectedFileIds = selectedFileIds,
                    onImport = {
                        importFolder = currentFolder
                        picker.launch(arrayOf("*/*"))
                    },
                    onFolderSelected = { currentFolder = it },
                    onCreateFolder = { showNewFolder = true },
                    onDeleteFolder = { folder ->
                        showDeleteFolderConfirmation = folder
                    },
                    onMakePrimary = {
                        primaryFolder = vault.setPrimaryFolder(it)
                        folders = vault.listFolders()
                    },
                    onOpen = { file ->
                        if (selectedFileIds.isNotEmpty()) {
                            selectedFileIds = if (file.id in selectedFileIds) selectedFileIds - file.id else selectedFileIds + file.id
                        } else {
                            openVaultFile(file)
                        }
                    },
                    onToggleLock = {
                        if (lockEnabled) vault.toggleLocked(it)
                        files = vault.listFiles()
                    },
                    onPinFile = {
                        vault.togglePinnedToTop(it)
                        files = vault.listFiles()
                    },
                    onReorder = { file, direction ->
                        vault.reorder(file, direction)
                        files = vault.listFiles()
                    },
                    onReorderTo = { file, index ->
                        vault.reorderTo(file, index)
                        files = vault.listFiles()
                    },
                    onLongPress = { fileActionTarget = it },
                    onDeleteFile = {
                        vault.deleteFile(it)
                        selectedFileIds = selectedFileIds - it.id
                        files = vault.listFiles()
                    },
                    onClearSelection = { selectedFileIds = emptySet() },
                    onDeleteSelected = { showDeleteFilesConfirmation = true },
                    onMoveSelected = { showMoveSelected = true },
                    onShareSelected = {
                        val selected = files.filter { it.id in selectedFileIds }
                        val queued = selected.mapNotNull { pendingShareFromVault(vault, it) }
                        pendingShares = (pendingShares + queued).distinctBy { it.id }
                        selectedFileIds = emptySet()
                        tab = AppTab.SHARE
                    },
                    vault = vault,
                )

                AppTab.SHARE -> ShareHome(
                    transfer = transfer,
                    pendingFiles = pendingShares,
                    onPickFile = { shareFilePicker.launch(arrayOf("*/*")) },
                    onPickMedia = { mediaPicker.launch(arrayOf("image/*", "video/*")) },
                    onPickFolder = { folderPicker.launch(null) },
                    onCreateText = { text ->
                        scope.launch {
                            val created = withContext(Dispatchers.IO) {
                                createTextShare(activity, text)
                            }
                            created?.let { pendingShares = (pendingShares + it).distinctBy { share -> share.id } }
                        }
                    },
                    onPickInstalledApp = { apps ->
                        scope.launch {
                            val created = withContext(Dispatchers.IO) {
                                apps.mapNotNull { app ->
                                    createInstalledAppPackage(activity, app)?.let { packageFile ->
                                        pendingShareFromFile(
                                            packageFile,
                                            "${safeFileName(app.label)}.${if (app.apkPaths.size > 1) "apks" else "apk"}",
                                            "application/vnd.android.package-archive",
                                        )
                                    }
                                }
                            }
                            pendingShares = (pendingShares + created).distinctBy { it.id }
                        }
                    },
                    onRemovePending = { share ->
                        pendingShares = pendingShares.filterNot { it.id == share.id }
                        disposePendingShare(activity, share)
                    },
                    onClearPending = {
                        pendingShares.forEach { disposePendingShare(activity, it) }
                        pendingShares = emptyList()
                    },
                    onTransferFinished = { successful ->
                        scope.launch {
                            val committed = withContext(Dispatchers.IO) {
                                successful.filter { commitPendingShare(activity, vault, it) }.map { it.id }.toSet()
                            }
                            pendingShares = pendingShares.filterNot { it.id in committed }
                            files = vault.listFiles()
                            folders = vault.listFolders()
                        }
                    },
                )

                AppTab.SECURITY -> SecurityHome(
                    lockType = lockManager.type(),
                    lockEnabled = lockEnabled,
                    biometricEnabled = biometricEnabled,
                    fileLockDefault = fileLockDefault,
                    screenshotEnabled = screenshotEnabled,
                    transferStatus = transferStatus,
                    biometricAvailable = activity.canUseBiometric(),
                    onRequestChangeMethod = { securityAction = SecurityAction.ChangeMethod },
                    onRequestBiometric = { enabled -> securityAction = SecurityAction.SetBiometric(enabled) },
                    onRequestLock = { enabled -> securityAction = SecurityAction.SetLock(enabled) },
                    onRequestFileLockDefault = { enabled -> securityAction = SecurityAction.SetFileLockDefault(enabled) },
                    onRequestScreenshots = { enabled ->
                        if (enabled) {
                            securityAction = SecurityAction.SetScreenshots(true)
                        } else {
                            screenshotEnabled = false
                            screenshotUnlockUntil = null
                        }
                    },
                    onCreateBackup = { showBackupPasswordDialog = true },
                    onRestoreBackup = { restorePicker.launch(arrayOf("*/*")) },
                    onLockNow = { if (lockEnabled) securityAction = SecurityAction.LockNow },
                )
            }
        }
    }
}

@Composable
private fun SetupLockScreen(onComplete: () -> Unit) {
    var selected by remember { mutableStateOf(LockType.PIN) }
    var secret by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var pattern by remember { mutableStateOf<List<Int>>(emptyList()) }
    var confirmPattern by remember { mutableStateOf<List<Int>>(emptyList()) }
    var error by remember { mutableStateOf("") }
    val context = LocalContext.current
    val manager = remember { LockManager(context) }

    LockScaffold(
        eyebrow = "PRIVATE BY DESIGN",
        title = "Build your vault",
        description = "Every file stays inside VoltShare’s app-private folder. Choose the gate that feels right for you.",
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth(), accent = VoltGreen, padding = 18.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(VoltGreen.copy(alpha = 0.13f), CircleShape)
                        .border(1.dp, VoltGreen.copy(alpha = 0.38f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Security, null, tint = VoltGreen, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(13.dp))
                Column {
                    Text("ONE DEVICE. ONE PRIVATE GATE.", color = VoltGreen, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.1.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Your secret stays on this phone.", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Choose your lock method", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        FloatingSegmentedControl(
            items = LockType.entries.map { it.label },
            selectedIndex = selected.ordinal,
            onSelected = { selected = LockType.entries[it] },
        )
        Spacer(Modifier.height(28.dp))
        if (selected == LockType.PATTERN) {
            Text("Draw a 4+ point pattern", color = VoltTextMuted, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            PatternPad(pattern, onChange = { pattern = it })
            Spacer(Modifier.height(18.dp))
            Text("Draw it again to confirm", color = VoltTextMuted, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            PatternPad(confirmPattern, onChange = { confirmPattern = it })
        } else {
            SecureField(
                value = secret,
                label = "Create ${selected.label.lowercase()}",
                keyboardType = if (selected == LockType.PIN) KeyboardType.NumberPassword else KeyboardType.Password,
                onValueChange = { secret = it },
            )
            Spacer(Modifier.height(14.dp))
            SecureField(
                value = confirm,
                label = "Confirm ${selected.label.lowercase()}",
                keyboardType = if (selected == LockType.PIN) KeyboardType.NumberPassword else KeyboardType.Password,
                onValueChange = { confirm = it },
            )
        }
        AnimatedVisibility(error.isNotEmpty()) {
            Text(error, color = Color(0xFFFF6B6B), modifier = Modifier.padding(top = 12.dp))
        }
        Spacer(Modifier.height(24.dp))
        GlowButton(
            text = "Secure my vault",
            icon = Icons.Default.Lock,
            onClick = {
                error = when {
                    selected == LockType.PATTERN && pattern.size < 4 -> "Use at least 4 points."
                    selected == LockType.PATTERN && pattern != confirmPattern -> "The two patterns do not match."
                    selected != LockType.PATTERN && secret.length < 4 -> "Use at least 4 characters."
                    selected != LockType.PATTERN && secret != confirm -> "The two entries do not match."
                    !manager.configure(selected, if (selected == LockType.PATTERN) pattern.joinToString("-") else secret) -> "Could not save the lock."
                    else -> ""
                }
                if (error.isEmpty()) onComplete()
            },
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Your passcode never leaves this device.",
            color = VoltTextMuted,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

@Composable
private fun UnlockScreen(
    lockManager: LockManager,
    biometricEnabled: Boolean,
    onUnlock: () -> Unit,
    onBiometric: () -> Unit,
) {
    var secret by remember { mutableStateOf("") }
    var pattern by remember { mutableStateOf<List<Int>>(emptyList()) }
    var error by remember { mutableStateOf(false) }
    LockScaffold(
        eyebrow = "SECURITY CORE / LOCKED",
        title = "Welcome back",
        description = "Your private files are still here. Authenticate to enter your private space.",
    ) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .shadow(28.dp, CircleShape, ambientColor = VoltGreen.copy(alpha = 0.28f), spotColor = VoltGreen.copy(alpha = 0.42f))
                .background(
                    Brush.radialGradient(listOf(VoltGreen.copy(alpha = 0.28f), VoltGreen.copy(alpha = 0.04f), Color.Transparent)),
                    CircleShape,
                )
                .border(1.dp, VoltGreen.copy(alpha = 0.52f), CircleShape)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .background(Color.Black.copy(alpha = 0.42f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Lock, null, tint = VoltGreen, modifier = Modifier.size(32.dp))
            }
        }
        Spacer(Modifier.height(17.dp))
        Text(
            "${lockManager.type().label.uppercase()} GATE",
            color = VoltGreen,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 23.dp)
                .background(Color.White.copy(alpha = 0.035f), RoundedCornerShape(26.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(26.dp))
                .padding(18.dp),
        ) {
            Column {
                Text("ENTER YOUR ${lockManager.type().label.uppercase()}", color = VoltTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                Spacer(Modifier.height(13.dp))
                if (lockManager.type() == LockType.PATTERN) {
                    PatternPad(pattern, onChange = {
                        pattern = it
                        error = false
                    })
                } else {
                    SecureField(
                        value = secret,
                        label = "Enter ${lockManager.type().label.lowercase()}",
                        keyboardType = if (lockManager.type() == LockType.PIN) KeyboardType.NumberPassword else KeyboardType.Password,
                        onValueChange = {
                            secret = it
                            error = false
                        },
                    )
                }
            }
        }
        AnimatedVisibility(error) {
            Text("That code does not unlock this vault.", color = Color(0xFFFF8A80), fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp))
        }
        Spacer(Modifier.height(20.dp))
        GlowButton(
            text = "Unlock vault",
            icon = Icons.Default.LockOpen,
            onClick = {
                val attempt = if (lockManager.type() == LockType.PATTERN) pattern.joinToString("-") else secret
                if (lockManager.verify(attempt)) onUnlock() else error = true
            },
        )
        if (biometricEnabled) {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onBiometric,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = VoltGreen.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, VoltGreen.copy(alpha = 0.35f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            ) {
                Icon(Icons.Default.Fingerprint, null, tint = VoltGreen, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Use biometric / device unlock", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(
            "Stored privately • no network required",
            color = VoltTextMuted,
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

@Composable
private fun LockScaffold(
    eyebrow: String,
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF06120D), VoltBlack, VoltBlack),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 96.dp, y = (-62).dp)
                .size(250.dp)
                .background(VoltGreen.copy(alpha = 0.07f), CircleShape)
                .border(1.dp, VoltGreen.copy(alpha = 0.12f), CircleShape),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-90).dp, y = 76.dp)
                .size(220.dp)
                .background(VoltTeal.copy(alpha = 0.08f), CircleShape),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 46.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(VoltGreen.copy(alpha = 0.12f), CircleShape)
                        .border(1.dp, VoltGreen.copy(alpha = 0.38f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Bolt, null, tint = VoltGreen, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(9.dp))
                Text(eyebrow, color = VoltGreen, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp)
            }
            Spacer(Modifier.height(15.dp))
            Text(title, style = MaterialTheme.typography.headlineLarge, color = Color.White)
            Spacer(Modifier.height(10.dp))
            Text(description, color = VoltTextMuted, lineHeight = 22.sp)
            Spacer(Modifier.height(26.dp))
            Column(content = content)
        }
    }
}

@Composable
private fun VaultHome(
    files: List<VaultFile>,
    onImport: () -> Unit,
    onOpen: (VaultFile) -> Unit,
    onToggleLock: (VaultFile) -> Unit,
    onPinFile: (VaultFile) -> Unit,
    vault: VaultRepository,
) {
    val recentFiles = files.sortedByDescending { it.createdAt }.take(5)
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(VoltBlack),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    Text("V O L T S H A R E", color = VoltGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Your private vault", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Text("A calm command center for protected files", color = VoltTextMuted, fontSize = 13.sp)
                }
                Box(
                    modifier = Modifier.size(46.dp).background(VoltGreen.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Bolt, null, tint = VoltGreen, modifier = Modifier.size(24.dp))
                }
            }
        }
        item { VaultMetricCard(files) }
        item {
            GlowButton(
                text = "Import files",
                icon = Icons.Default.Add,
                onClick = onImport,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    Text("Recent files", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Open, lock, or preview what you added last", color = VoltTextMuted, fontSize = 12.sp)
                }
                Text("${files.size} total", color = VoltGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        if (recentFiles.isEmpty()) {
            item { EmptyVaultCard(onImport) }
        } else {
            items(recentFiles, key = { it.id }) { file ->
                FileRow(
                    file = file,
                    vault = vault,
                    onOpen = onOpen,
                    onToggleLock = onToggleLock,
                    onPin = { onPinFile(file) },
                    onMoveUp = {},
                    onMoveDown = {},
                    allowReorder = false,
                )
            }
        }
        item {
            Text(
                "Files stay inside private app-only storage",
                color = VoltTextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
            )
        }
    }
}

@Composable
private fun FilesHome(
    files: List<VaultFile>,
    folders: List<String>,
    currentFolder: String,
    primaryFolder: String,
    selectedFileIds: Set<String>,
    onImport: () -> Unit,
    onFolderSelected: (String) -> Unit,
    onCreateFolder: () -> Unit,
    onDeleteFolder: (String) -> Unit,
    onMakePrimary: (String) -> Unit,
    onOpen: (VaultFile) -> Unit,
    onToggleLock: (VaultFile) -> Unit,
    onPinFile: (VaultFile) -> Unit,
    onReorder: (VaultFile, Int) -> Unit,
    onReorderTo: (VaultFile, Int) -> Unit,
    onLongPress: (VaultFile) -> Unit,
    onDeleteFile: (VaultFile) -> Unit,
    onClearSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
    onMoveSelected: () -> Unit,
    onShareSelected: () -> Unit,
    vault: VaultRepository,
    selectionOnly: Boolean = false,
    onConfirmSelection: (() -> Unit)? = null,
    pickerTitle: String = "Choose files",
    pickerDescription: String = "Select one or more files to attach to the other app",
    pickerReadyText: String = "Ready to import",
    pickerFooterText: String = "Selected files are shared read-only with the requesting app.",
    pickerConfirmLabel: String = "Import selected",
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchOpen by remember { mutableStateOf(false) }
    var reorderMode by remember { mutableStateOf(false) }
    var activeFilter by remember { mutableStateOf(FileFilter.ALL) }
    var sortMode by remember { mutableStateOf(VaultSort.CUSTOM) }
    var showSortDialog by remember { mutableStateOf(false) }
    var draggedFileId by remember { mutableStateOf<String?>(null) }
    var dropTargetFileId by remember { mutableStateOf<String?>(null) }
    var dropTargetBelow by remember { mutableStateOf(false) }
    LaunchedEffect(currentFolder) {
        draggedFileId = null
        dropTargetFileId = null
        dropTargetBelow = false
    }
    val searching = searchQuery.trim().isNotEmpty()
    val filteredFiles = files.filter { file ->
        activeFilter == FileFilter.ALL ||
            (activeFilter == FileFilter.SENT && file.transferDirection == TransferDirection.SENT) ||
            (activeFilter == FileFilter.RECEIVED && file.transferDirection == TransferDirection.RECEIVED)
    }
    val childFolders = folders.filter { it.parentFolder() == currentFolder }
    val visibleFiles = sortVaultFiles(
        filteredFiles.filter {
            if (searching) {
                it.name.contains(searchQuery.trim(), ignoreCase = true) ||
                    it.folderPath.contains(searchQuery.trim(), ignoreCase = true)
            } else {
                it.folderPath == currentFolder
            }
        },
        if (reorderMode) VaultSort.CUSTOM else sortMode,
    ).let { ordered ->
        if (searching) {
            ordered
        } else {
            val (pinned, unpinned) = ordered.partition { it.pinned }
            pinned + unpinned
        }
    }
    val visibleFolders = if (searching) {
        folders.filter { folder ->
            folder != "/" &&
                folder.substringAfterLast('/').contains(searchQuery.trim(), ignoreCase = true) &&
                (activeFilter == FileFilter.ALL || filteredFiles.any {
                    it.folderPath == folder || it.folderPath.startsWith("$folder/")
                })
        }
    } else {
        childFolders.filter { folder ->
            activeFilter == FileFilter.ALL || filteredFiles.any {
                it.folderPath == folder || it.folderPath.startsWith("$folder/")
            }
        }
    }
    val directFiles = sortVaultFiles(filteredFiles.filter { it.folderPath == currentFolder }, VaultSort.CUSTOM)
    fun reorderDirectFile(file: VaultFile, delta: Int) {
        val position = directFiles.indexOfFirst { it.id == file.id }
        if (position < 0 || directFiles.isEmpty()) return
        val targetPosition = (position + delta).coerceIn(0, directFiles.lastIndex)
        dropTargetFileId = directFiles.getOrNull(targetPosition)?.id
        dropTargetBelow = delta > 0
        if (targetPosition != position) onReorderTo(file, targetPosition)
    }
    val nestedFolderCount = if (currentFolder == "/") {
        folders.count { folder ->
            folder != "/" && (activeFilter == FileFilter.ALL || filteredFiles.any {
                it.folderPath == folder || it.folderPath.startsWith("$folder/")
            })
        }
    } else {
        folders.count { folder ->
            folder != "/" &&
                folder.startsWith("$currentFolder/") &&
                (activeFilter == FileFilter.ALL || filteredFiles.any {
                    it.folderPath == folder || it.folderPath.startsWith("$folder/")
                })
        }
    }
    val nestedFileCount = if (currentFolder == "/") {
        filteredFiles.size
    } else {
        filteredFiles.count { it.folderPath == currentFolder || it.folderPath.startsWith("$currentFolder/") }
    }
    if (showSortDialog) {
        SortDialog(
            selected = sortMode,
            onDismiss = { showSortDialog = false },
            onSelected = {
                sortMode = it
                showSortDialog = false
            },
        )
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(VoltBlack),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (selectionOnly) "VOLT SHARE VAULT" else "PRIVATE LIBRARY",
                        color = VoltGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.2.sp,
                    )
                    Spacer(Modifier.height(7.dp))
                    Text(
                        if (selectionOnly) {
                            if (currentFolder == "/") pickerTitle else currentFolder.substringAfterLast('/')
                        } else if (currentFolder == "/") {
                            "Your files"
                        } else {
                            currentFolder.substringAfterLast('/')
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        if (selectionOnly) pickerDescription else "Private, organized, and only visible to you",
                        color = VoltTextMuted,
                        fontSize = 12.sp,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(16.dp, CircleShape, spotColor = VoltGreen.copy(alpha = 0.2f))
                        .background(VoltGreen.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Security, "Private library", tint = VoltGreen, modifier = Modifier.size(23.dp))
                }
            }
        }
        item {
            FilesLibrarySummary(
                files = files,
                folderCount = folders.count { it != "/" },
                visibleCount = nestedFileCount,
            )
        }
        if (!reorderMode) item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FileFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = activeFilter == filter,
                        onClick = { activeFilter = filter },
                        label = { Text(filter.label, fontSize = 12.sp) },
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VoltGreen,
                            selectedLabelColor = Color.Black,
                            containerColor = VoltSurfaceRaised,
                            labelColor = VoltTextMuted,
                        ),
                    )
                    if (filter != FileFilter.RECEIVED) Spacer(Modifier.width(6.dp))
                }
                Spacer(Modifier.weight(1f))
                Surface(
                    modifier = Modifier.size(42.dp),
                    color = VoltSurfaceRaised,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, VoltGreen.copy(alpha = 0.3f)),
                    shadowElevation = 8.dp,
                ) {
                    IconButton(onClick = { searchOpen = true }) {
                        Icon(Icons.Default.Search, "Search files and folders", tint = VoltGreen)
                    }
                }
            }
        }
        if (!reorderMode && searchOpen) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = VoltSurfaceRaised,
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    shadowElevation = 10.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(start = 5.dp, end = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = {
                                searchQuery = ""
                                searchOpen = false
                            },
                        ) {
                            Icon(Icons.Default.Close, "Close search", tint = VoltTextMuted)
                        }
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search files and folders", color = VoltTextMuted) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = VoltGreen) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, "Clear search", tint = VoltTextMuted)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = VoltGreen,
                            ),
                        )
                        IconButton(onClick = { showSortDialog = true }) {
                            Icon(Icons.Default.Sort, "Sort files", tint = VoltGreen)
                        }
                    }
                }
            }
        }
        item {
            FolderHeaderCard(
                currentFolder = currentFolder,
                primaryFolder = primaryFolder,
                fileCount = nestedFileCount,
                folderCount = nestedFolderCount,
                onBack = { onFolderSelected(currentFolder.parentFolder()) },
                onImport = onImport,
                onCreateFolder = onCreateFolder,
                onDelete = { onDeleteFolder(currentFolder) },
                onMakePrimary = { onMakePrimary(currentFolder) },
                reorderMode = reorderMode,
                draggedFileName = visibleFiles.firstOrNull { it.id == draggedFileId }?.name,
                dropTargetFileName = visibleFiles.firstOrNull { it.id == dropTargetFileId }?.name,
                dropTargetBelow = dropTargetBelow,
                onToggleReorder = {
                    searchQuery = ""
                    activeFilter = FileFilter.ALL
                    draggedFileId = null
                    dropTargetFileId = null
                    dropTargetBelow = false
                    reorderMode = !reorderMode
                },
                managementEnabled = !selectionOnly,
            )
        }
        val libraryEmpty = visibleFolders.isEmpty() && visibleFiles.isEmpty()
        if (!reorderMode && libraryEmpty) {
            item {
                FilesEmptyState(
                    searching = searching,
                    filter = activeFilter,
                    onImport = onImport,
                    onClearSearch = { searchQuery = "" },
                )
            }
        } else {
            item {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(top = 3.dp),
                ) {
                    Column {
                        Text("Library", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(
                            if (searching) "Matching folders and files" else "Everything in this location",
                            color = VoltTextMuted,
                            fontSize = 11.sp,
                        )
                    }
                    Text(
                        "${visibleFolders.size + visibleFiles.size} items",
                        color = VoltGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = VoltSurfaceRaised.copy(alpha = 0.82f),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    shadowElevation = 16.dp,
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        visibleFolders.forEach { folder ->
                            FolderCard(
                                path = folder,
                                primaryFolder = primaryFolder,
                                folderCount = folders.count { nested ->
                                    nested != folder &&
                                        nested.startsWith("$folder/") &&
                                        (activeFilter == FileFilter.ALL || filteredFiles.any {
                                            it.folderPath == nested || it.folderPath.startsWith("$nested/")
                                        })
                                },
                                fileCount = filteredFiles.count { it.folderPath == folder || it.folderPath.startsWith("$folder/") },
                                onOpen = {
                                    searchQuery = ""
                                    onFolderSelected(folder)
                                },
                                onMakePrimary = { onMakePrimary(folder) },
                                onDelete = { onDeleteFolder(folder) },
                                compact = true,
                                selectionOnly = selectionOnly,
                            )
                            if (visibleFiles.isNotEmpty() || folder != visibleFolders.last()) {
                                Divider(color = Color.White.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 14.dp))
                            }
                        }
                        visibleFiles.forEach { file ->
                            FileRow(
                                file = file,
                                vault = vault,
                                onOpen = onOpen,
                                onToggleLock = onToggleLock,
                                onPin = { onPinFile(file) },
                                onMoveUp = { reorderDirectFile(file, -1) },
                                onMoveDown = { reorderDirectFile(file, 1) },
                                allowReorder = reorderMode && !searching,
                                onReorder = { delta -> reorderDirectFile(file, delta) },
                                onDragStart = {
                                    draggedFileId = file.id
                                    dropTargetFileId = file.id
                                    dropTargetBelow = false
                                },
                                onDragEnd = {
                                    draggedFileId = null
                                    dropTargetFileId = null
                                    dropTargetBelow = false
                                },
                                onLongPress = { onLongPress(file) },
                                onDelete = { onDeleteFile(file) },
                                isSelected = file.id in selectedFileIds,
                                selectionMode = selectedFileIds.isNotEmpty(),
                                showPin = true,
                                isPinned = file.pinned,
                                reorderMode = reorderMode,
                                dragging = draggedFileId == file.id,
                                dropTarget = dropTargetFileId == file.id,
                                dropTargetBelow = dropTargetBelow,
                                compact = true,
                            )
                            if (file != visibleFiles.last()) {
                                Divider(color = Color.White.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 14.dp))
                            }
                        }
                    }
                }
            }
        }
        if (selectedFileIds.isNotEmpty()) {
            item {
                if (selectionOnly && onConfirmSelection != null) {
                    PickerSelectionBar(
                        selectedCount = selectedFileIds.size,
                        onConfirm = onConfirmSelection,
                        onClear = onClearSelection,
                            readyText = pickerReadyText,
                            confirmLabel = pickerConfirmLabel,
                    )
                } else {
                    SelectionBar(
                        selectedCount = selectedFileIds.size,
                        onShare = onShareSelected,
                        onMove = onMoveSelected,
                        onDelete = onDeleteSelected,
                        onClear = onClearSelection,
                    )
                }
            }
        }
        item {
            Text(
                if (selectionOnly) {
                    pickerFooterText
                } else {
                    "Your files stay inside VoltShare’s private app storage."
                },
                color = VoltTextMuted.copy(alpha = 0.72f),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
        }
    }
}

@Composable
private fun FilesLibrarySummary(
    files: List<VaultFile>,
    folderCount: Int,
    visibleCount: Int,
) {
    val totalBytes = files.sumOf { it.sizeBytes }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(18.dp, RoundedCornerShape(22.dp), spotColor = VoltGreen.copy(alpha = 0.18f))
            .background(
                Brush.horizontalGradient(
                    listOf(VoltGreen.copy(alpha = 0.16f), VoltSurfaceRaised, VoltSurface),
                ),
                RoundedCornerShape(22.dp),
            )
            .border(1.dp, Color.White.copy(alpha = 0.09f), RoundedCornerShape(22.dp)),
        color = Color.Transparent,
        shape = RoundedCornerShape(22.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "PROTECTED LIBRARY",
                    color = VoltGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.7.sp,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    "${files.size} protected file${if (files.size == 1) "" else "s"}",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${formatSize(totalBytes)} stored on this device",
                    color = VoltTextMuted,
                    fontSize = 12.sp,
                )
            }
            Row(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LibraryStat("$visibleCount", "view")
                LibraryStat("$folderCount", "folders")
                LibraryStat(files.count { it.locked }.toString(), "locked")
            }
        }
    }
}

@Composable
private fun LibraryStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = VoltGreen, fontWeight = FontWeight.Black, fontSize = 15.sp)
        Spacer(Modifier.height(2.dp))
        Text(label, color = VoltTextMuted, fontSize = 10.sp)
    }
}

@Composable
private fun FilesEmptyState(
    searching: Boolean,
    filter: FileFilter,
    onImport: () -> Unit,
    onClearSearch: () -> Unit,
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        accent = VoltTeal,
        padding = 24.dp,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(VoltGreen.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (searching) Icons.Default.Search else Icons.Default.FolderOpen,
                null,
                tint = VoltGreen,
                modifier = Modifier.size(30.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            when {
                searching -> "Nothing matched your search"
                filter != FileFilter.ALL -> "No ${filter.label.lowercase()} files here"
                else -> "This folder is empty"
            },
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Black,
        )
        Spacer(Modifier.height(7.dp))
        Text(
            if (searching) {
                "Try another name or clear the search to browse your library."
            } else {
                "Import a file here and it will stay inside the app-only vault."
            },
            color = VoltTextMuted,
            lineHeight = 20.sp,
        )
        Spacer(Modifier.height(17.dp))
        TextButton(
            onClick = if (searching) onClearSearch else onImport,
            colors = ButtonDefaults.textButtonColors(contentColor = VoltGreen),
        ) {
            Text(if (searching) "Clear search" else "Import a file", fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(5.dp))
            Icon(
                if (searching) Icons.Default.Close else Icons.Default.ArrowUpward,
                null,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun FolderHeaderCard(
    currentFolder: String,
    primaryFolder: String,
    fileCount: Int,
    folderCount: Int,
    onBack: () -> Unit,
    onImport: () -> Unit,
    onCreateFolder: () -> Unit,
    onDelete: () -> Unit,
    onMakePrimary: () -> Unit,
    reorderMode: Boolean,
    draggedFileName: String? = null,
    dropTargetFileName: String? = null,
    dropTargetBelow: Boolean = false,
    onToggleReorder: () -> Unit,
    managementEnabled: Boolean = true,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(14.dp, RoundedCornerShape(20.dp), spotColor = VoltTeal.copy(alpha = 0.18f))
            .background(
                Brush.horizontalGradient(
                    listOf(VoltTeal.copy(alpha = 0.14f), VoltSurfaceRaised, VoltSurface),
                ),
                RoundedCornerShape(20.dp),
            )
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp)),
        color = Color.Transparent,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            if (!reorderMode) {
                IconButton(onClick = onBack, enabled = currentFolder != "/") {
                    Icon(
                        if (currentFolder == "/") Icons.Default.Folder else Icons.Default.ArrowBack,
                        "Parent folder",
                        tint = if (currentFolder == "/") VoltGreen else Color.White,
                    )
                }
            } else {
                Icon(Icons.Default.DragHandle, "Reorder mode", tint = VoltGreen, modifier = Modifier.padding(horizontal = 12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(if (currentFolder == "/") "Root" else currentFolder.substringAfterLast('/'), color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    "$folderCount folder${if (folderCount == 1) "" else "s"} • $fileCount file${if (fileCount == 1) "" else "s"}",
                    color = VoltTextMuted,
                    fontSize = 11.sp,
                )
            }
            if (!reorderMode && managementEnabled) {
                IconButton(onClick = onCreateFolder) {
                    Icon(Icons.Default.Add, "Create folder", tint = VoltGreen)
                }
                IconButton(onClick = onMakePrimary) {
                    Icon(
                        if (currentFolder == primaryFolder) Icons.Default.Star else Icons.Default.StarBorder,
                        "Make primary folder",
                        tint = if (currentFolder == primaryFolder) VoltGreen else VoltTextMuted,
                    )
                }
                if (currentFolder != "/") {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "Delete folder", tint = Color(0xFFFF8A80))
                    }
                }
            }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (reorderMode && draggedFileName != null && dropTargetFileName != null) {
                        if (draggedFileName == dropTargetFileName) {
                            "Holding “$draggedFileName” • already in this position"
                        } else {
                            "Holding “$draggedFileName” • place ${if (dropTargetBelow) "after" else "before"} “$dropTargetFileName”"
                        }
                    } else if (reorderMode) {
                        "Hold a grip and drag • green line marks the new position"
                    } else {
                        if (currentFolder == "/") "Private root" else currentFolder
                    },
                    color = if (reorderMode) VoltGreen else VoltTextMuted,
                    fontSize = 11.sp,
                    maxLines = if (reorderMode) 2 else 1,
                    modifier = Modifier.weight(1f),
                )
                if (!reorderMode && managementEnabled) {
                    TextButton(
                        onClick = onImport,
                        colors = ButtonDefaults.textButtonColors(contentColor = VoltGreen),
                    ) {
                        Icon(Icons.Default.ArrowUpward, "Import files", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Import", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (managementEnabled) {
                    TextButton(
                        onClick = onToggleReorder,
                        colors = ButtonDefaults.textButtonColors(contentColor = VoltGreen),
                    ) {
                        Icon(Icons.Default.DragHandle, null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(if (reorderMode) "Done" else "Reorder", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderCard(
    path: String,
    primaryFolder: String,
    folderCount: Int,
    fileCount: Int,
    onOpen: () -> Unit,
    onMakePrimary: () -> Unit,
    onDelete: () -> Unit,
    compact: Boolean = false,
    selectionOnly: Boolean = false,
) {
    val rowContent: @Composable ColumnScope.() -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(if (compact) 44.dp else 52.dp)
                    .background(
                        if (path == primaryFolder) VoltGreen.copy(alpha = 0.16f) else VoltTeal.copy(alpha = 0.14f),
                        RoundedCornerShape(if (compact) 14.dp else 17.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Folder,
                    "Open folder",
                    tint = if (path == primaryFolder) VoltGreen else Color(0xFF65D8C8),
                    modifier = Modifier.size(if (compact) 23.dp else 27.dp),
                )
            }
            Spacer(Modifier.width(if (compact) 12.dp else 14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        path.substringAfterLast('/'),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (path == primaryFolder) {
                        Spacer(Modifier.width(7.dp))
                        Text(
                            "PRIMARY",
                            color = VoltGreen,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp,
                        )
                    }
                }
                Text(
                    "$folderCount folder${if (folderCount == 1) "" else "s"} • $fileCount file${if (fileCount == 1) "" else "s"}",
                    color = VoltTextMuted,
                    fontSize = if (compact) 10.sp else 11.sp,
                )
                if (!compact) {
                    Text(path, color = VoltTextMuted.copy(alpha = 0.7f), fontSize = 10.sp, maxLines = 1)
                }
            }
            if (!selectionOnly) {
                IconButton(
                    onClick = onMakePrimary,
                    modifier = Modifier.size(if (compact) 40.dp else 48.dp),
                ) {
                    Icon(
                        if (path == primaryFolder) Icons.Default.Star else Icons.Default.StarBorder,
                        "Make primary folder",
                        tint = if (path == primaryFolder) VoltGreen else VoltTextMuted,
                    )
                }
            }
            if (compact) {
                if (!selectionOnly) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.Delete, "Delete folder", tint = Color(0xFFFF8A80))
                    }
                }
                IconButton(onClick = onOpen, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.ArrowForward, "Open folder", tint = Color.White.copy(alpha = 0.75f))
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = onOpen) {
                        Icon(Icons.Default.ArrowForward, "Open folder", tint = Color.White.copy(alpha = 0.75f))
                    }
                    if (!selectionOnly) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, "Delete folder", tint = Color(0xFFFF8A80))
                        }
                    }
                }
            }
        }
    }
    if (compact) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            if (path == primaryFolder) VoltGreen.copy(alpha = 0.06f) else VoltTeal.copy(alpha = 0.035f),
                            Color.Transparent,
                        ),
                    ),
                )
                .padding(horizontal = 10.dp, vertical = 7.dp),
            content = rowContent,
        )
    } else {
        GlassCard(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
            accent = if (path == primaryFolder) VoltGreen else VoltTeal,
            padding = 16.dp,
            content = rowContent,
        )
    }
}

@Composable
private fun PickerSelectionBar(
    selectedCount: Int,
    onConfirm: () -> Unit,
    onClear: () -> Unit,
    readyText: String,
    confirmLabel: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = VoltSurfaceRaised,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, VoltGreen.copy(alpha = 0.35f)),
        shadowElevation = 18.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(VoltGreen.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Check, null, tint = VoltGreen, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.width(9.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("$selectedCount selected", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(readyText, color = VoltTextMuted, fontSize = 10.sp)
            }
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = VoltGreen),
            ) {
                Icon(Icons.Default.ArrowDownward, confirmLabel, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(5.dp))
                Text(confirmLabel, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            IconButton(onClick = onClear) {
                Icon(Icons.Default.Close, "Clear selection", tint = VoltTextMuted)
            }
        }
    }
}

@Composable
private fun SelectionBar(
    selectedCount: Int,
    onShare: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = VoltSurfaceRaised,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, VoltGreen.copy(alpha = 0.35f)),
        shadowElevation = 18.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(VoltGreen.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.SelectAll, null, tint = VoltGreen, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.width(9.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("$selectedCount selected", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Bulk actions", color = VoltTextMuted, fontSize = 10.sp)
            }
            IconButton(onClick = onShare) {
                Icon(Icons.Default.Share, "Share selected", tint = VoltGreen)
            }
            IconButton(onClick = onMove) {
                Icon(Icons.Default.DriveFileMove, "Move selected", tint = VoltTextMuted)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete selected", tint = Color(0xFFFF8A80))
            }
            IconButton(onClick = onClear) {
                Icon(Icons.Default.Close, "Clear selection", tint = VoltTextMuted)
            }
        }
    }
}

@Composable
private fun VaultMetricCard(files: List<VaultFile>) {
    val total = files.sumOf { it.sizeBytes }
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        accent = VoltGreen,
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("VAULT STATUS", color = VoltTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(10.dp))
                Text("Private + hidden", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(5.dp))
                Text("${files.size} protected files • ${formatSize(total)}", color = VoltTextMuted, fontSize = 13.sp)
            }
            Box(
                modifier = Modifier.size(62.dp).background(VoltGreen.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Security, null, tint = VoltGreen, modifier = Modifier.size(29.dp))
            }
        }
        Spacer(Modifier.height(22.dp))
        LinearProgressIndicator(
            progress = { (files.size / 20f).coerceIn(0.08f, 1f) },
            modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
            color = VoltGreen,
            trackColor = Color.White.copy(alpha = 0.1f),
        )
    }
}

@Composable
private fun EmptyVaultCard(onImport: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth(), accent = VoltTeal) {
        Icon(Icons.Default.Folder, null, tint = VoltGreen, modifier = Modifier.size(30.dp))
        Spacer(Modifier.height(16.dp))
        Text("Your vault is quiet", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(7.dp))
        Text("Bring in a photo, video, document, APK, or any file. It will disappear from shared storage and live here.", color = VoltTextMuted, lineHeight = 20.sp)
        Spacer(Modifier.height(18.dp))
        TextButton(onClick = onImport, colors = ButtonDefaults.textButtonColors(contentColor = VoltGreen)) {
            Text("Choose your first file")
            Spacer(Modifier.width(5.dp))
            Icon(Icons.Default.ArrowUpward, null, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun FileRow(
    file: VaultFile,
    vault: VaultRepository? = null,
    onOpen: (VaultFile) -> Unit,
    onToggleLock: (VaultFile) -> Unit,
    onPin: () -> Unit = {},
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    allowReorder: Boolean,
    onDrag: (Float) -> Unit = {},
    onReorder: (Int) -> Unit = {},
    onDragStart: () -> Unit = {},
    onDragEnd: () -> Unit = {},
    onLongPress: () -> Unit = {},
    onDelete: () -> Unit = {},
    isSelected: Boolean = false,
    selectionMode: Boolean = false,
    showPin: Boolean = true,
    isPinned: Boolean = false,
    reorderMode: Boolean = false,
    dragging: Boolean = false,
    dropTarget: Boolean = false,
    dropTargetBelow: Boolean = false,
    compact: Boolean = false,
) {
    val icon = fileIcon(file)
    var dragDistance by remember(file.id) { mutableStateOf(0f) }
    var isDragging by remember(file.id) { mutableStateOf(false) }
    val accent = if (file.locked) Color(0xFF9B7CFF) else VoltGreen
    val visuallyDragging = dragging || isDragging
    val rowContent: @Composable ColumnScope.() -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            if (vault != null) {
                FileThumbnail(vault, file)
            } else {
                Box(
                    modifier = Modifier.size(58.dp).background(accent.copy(alpha = 0.11f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, null, tint = accent, modifier = Modifier.size(25.dp))
                }
            }
            Spacer(Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        file.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (file.locked) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.Lock, "Locked file", tint = accent, modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(Modifier.height(5.dp))
                Text(
                    if (file.folderPath == "/") "Private root" else file.folderPath,
                    color = VoltTextMuted.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    maxLines = 1,
                )
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        file.transferDirection.label.uppercase(),
                        color = accent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.7.sp,
                    )
                    Text("  •  ", color = VoltTextMuted.copy(alpha = 0.5f), fontSize = 10.sp)
                    Text(formatSize(file.sizeBytes), color = VoltTextMuted, fontSize = 11.sp)
                }
            }
            if (reorderMode && allowReorder) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(42.dp)
                        .pointerInput(file.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    isDragging = true
                                    dragDistance = 0f
                                    onDragStart()
                                },
                                onDragCancel = {
                                    isDragging = false
                                    dragDistance = 0f
                                    onDragEnd()
                                },
                                onDragEnd = {
                                    isDragging = false
                                    dragDistance = 0f
                                    onDragEnd()
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragDistance += dragAmount.y
                                    if (dragDistance <= -48f) {
                                        if (reorderMode) onReorder(-1) else onMoveUp()
                                        onDrag(dragDistance)
                                        dragDistance = 0f
                                    } else if (dragDistance >= 48f) {
                                        if (reorderMode) onReorder(1) else onMoveDown()
                                        onDrag(dragDistance)
                                        dragDistance = 0f
                                    }
                                },
                            )
                        },
                ) {
                    Icon(
                        Icons.Default.DragHandle,
                        "Hold and drag to reorder",
                        tint = if (visuallyDragging) VoltGreen else VoltTextMuted,
                        modifier = Modifier.size(30.dp),
                    )
                    if (visuallyDragging) {
                        Text(
                            "MOVE",
                            color = VoltGreen,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp,
                        )
                    }
                }
            } else if (selectionMode) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(if (isSelected) VoltGreen else Color.Transparent, CircleShape)
                        .border(1.dp, if (isSelected) VoltGreen else VoltTextMuted, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                }
            } else {
                if (showPin) {
                    IconButton(onClick = onPin) {
                        Icon(
                            Icons.Default.PushPin,
                            if (isPinned) "Unpin ${file.name}" else "Pin ${file.name} to top",
                            tint = if (isPinned) VoltGreen else VoltTextMuted,
                        )
                    }
                } else {
                    IconButton(onClick = { onToggleLock(file) }) {
                        Icon(
                            if (file.locked) Icons.Default.Star else Icons.Default.StarBorder,
                            if (file.locked) "Unlock ${file.name}" else "Lock ${file.name}",
                            tint = if (file.locked) accent else VoltTextMuted,
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete ${file.name}", tint = Color(0xFFFF8A80))
                }
            }
        }
    }
    val rowModifier = Modifier
        .fillMaxWidth()
        .shadow(
            if (visuallyDragging) 22.dp else 0.dp,
            RoundedCornerShape(16.dp),
            spotColor = accent.copy(alpha = 0.42f),
        )
        .graphicsLayer {
            scaleX = if (visuallyDragging) 1.025f else 1f
            scaleY = if (visuallyDragging) 1.025f else 1f
            alpha = if (visuallyDragging) 0.9f else 1f
            rotationZ = if (visuallyDragging) -0.6f else 0f
        }
        .pointerInput(file.id, selectionMode, reorderMode) {
            detectTapGestures(
                onTap = {
                    if (!reorderMode) onOpen(file)
                },
                onLongPress = {
                    if (!reorderMode) onLongPress()
                },
            )
        }
    if (compact) {
        Column(
            modifier = rowModifier
                .border(
                    1.dp,
                    if (visuallyDragging) VoltGreen.copy(alpha = 0.9f) else Color.Transparent,
                    RoundedCornerShape(16.dp),
                )
                .background(
                    Brush.horizontalGradient(
                        listOf(accent.copy(alpha = 0.045f), Color.Transparent),
                    ),
                )
                .padding(horizontal = 10.dp, vertical = 7.dp),
        ) {
            if (dropTarget && !visuallyDragging && !dropTargetBelow) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .padding(horizontal = 8.dp)
                        .background(VoltGreen, RoundedCornerShape(3.dp)),
                )
                Spacer(Modifier.height(4.dp))
            }
            rowContent()
            if (dropTarget && !visuallyDragging && dropTargetBelow) {
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .padding(horizontal = 8.dp)
                        .background(VoltGreen, RoundedCornerShape(3.dp)),
                )
            }
        }
    } else {
        GlassCard(
            modifier = rowModifier,
            accent = accent,
            padding = 14.dp,
            content = rowContent,
        )
    }
}

@Composable
private fun FileThumbnail(vault: VaultRepository, file: VaultFile) {
    val context = LocalContext.current
    var bitmap by remember(file.id) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(file.id) {
        bitmap = withContext(Dispatchers.IO) { createFileThumbnail(context, vault, file) }
    }
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(VoltGreen.copy(alpha = 0.09f)),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "${file.name} thumbnail",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(fileIcon(file), null, tint = VoltGreen, modifier = Modifier.size(24.dp))
        }
    }
}

private fun createFileThumbnail(context: android.content.Context, vault: VaultRepository, file: VaultFile): Bitmap? {
    val prepared = vault.prepareViewing(file) ?: return null
    return runCatching {
        when {
            isImage(file) -> {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(prepared.absolutePath, bounds)
                val sample = calculateSampleSize(bounds.outWidth, bounds.outHeight, 256, 256)
                BitmapFactory.decodeFile(
                    prepared.absolutePath,
                    BitmapFactory.Options().apply { inSampleSize = sample },
                )
            }
            isVideo(file) -> {
                val retriever = MediaMetadataRetriever()
                runCatching {
                    retriever.setDataSource(prepared.absolutePath)
                    retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                }.getOrNull().also { runCatching { retriever.release() } }
            }
            isPdf(file) -> {
                ParcelFileDescriptor.open(prepared, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                    PdfRenderer(descriptor).use { renderer ->
                        if (renderer.pageCount == 0) {
                            null
                        } else {
                            renderer.openPage(0).use { page ->
                                val scale = 256f / page.width.coerceAtLeast(1)
                                Bitmap.createBitmap(
                                    (page.width * scale).toInt().coerceAtLeast(1),
                                    (page.height * scale).toInt().coerceAtLeast(1),
                                    Bitmap.Config.ARGB_8888,
                                ).also { preview ->
                                    preview.eraseColor(Color.White.toArgb())
                                    page.render(preview, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                }
                            }
                        }
                    }
                }
            }
            isInstallable(file) -> {
                val packageInfo = context.packageManager.getPackageArchiveInfo(prepared.absolutePath, PackageManager.GET_META_DATA)
                packageInfo?.applicationInfo?.let { info ->
                    info.sourceDir = prepared.absolutePath
                    info.publicSourceDir = prepared.absolutePath
                    context.packageManager.getApplicationIcon(info).toBitmap(256, 256)
                }
            }
            else -> null
        }
    }.getOrNull()
}

private fun calculateSampleSize(width: Int, height: Int, targetWidth: Int, targetHeight: Int): Int {
    var sample = 1
    while (width / sample > targetWidth * 2 || height / sample > targetHeight * 2) sample *= 2
    return sample
}

@Composable
private fun ShareHome(
    transfer: PeerTransferManager,
    pendingFiles: List<PendingShare>,
    onPickFile: () -> Unit,
    onPickMedia: () -> Unit,
    onPickFolder: () -> Unit,
    onCreateText: (String) -> Unit,
    onPickInstalledApp: (List<InstalledAppChoice>) -> Unit,
    onRemovePending: (PendingShare) -> Unit,
    onClearPending: () -> Unit,
    onTransferFinished: (List<PendingShare>) -> Unit,
) {
    val peers by transfer.peers.collectAsStateWithLifecycle()
    val status by transfer.status.collectAsStateWithLifecycle()
    val deviceIsActive = status.active || status.hosting
    var mode by remember { mutableStateOf(ShareMode.SEND) }
    var showTextEditor by remember { mutableStateOf(false) }
    var showInstalledApps by remember { mutableStateOf(false) }
    var showErrorLog by remember { mutableStateOf(false) }

    if (showTextEditor) {
        TextComposerDialog(
            onDismiss = { showTextEditor = false },
            onCreate = {
                showTextEditor = false
                onCreateText(it)
            },
        )
    }
    if (showInstalledApps) {
        InstalledAppsDialog(
            onDismiss = { showInstalledApps = false },
            onSelected = {
                showInstalledApps = false
                onPickInstalledApp(it)
            },
        )
    }
    if (showErrorLog && status.errorLog != null) {
        TransferErrorDialog(
            errorLog = status.errorLog!!,
            onDismiss = { showErrorLog = false },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(VoltBlack),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Text("LOCAL TRANSFER", color = VoltGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
            Spacer(Modifier.height(8.dp))
            Text("Share, without a server", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text("VoltShare connects devices directly over the same local network. Nothing routes through a cloud.", color = VoltTextMuted, lineHeight = 21.sp)
        }
        item {
            ShareModeSwitch(
                mode = mode,
                onModeChange = { mode = it },
            )
        }
        if (mode == ShareMode.SEND) {
            item {
                Text("Choose what to send", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    ShareOptionTile(
                        title = "Any file",
                        subtitle = "Documents, APKs, archives",
                        icon = Icons.Default.Description,
                        onClick = onPickFile,
                        modifier = Modifier.weight(1f),
                    )
                    ShareOptionTile(
                        title = "Full folder",
                        subtitle = "Send as one ZIP",
                        icon = Icons.Default.Folder,
                        onClick = onPickFolder,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    ShareOptionTile(
                        title = "Text note",
                        subtitle = "Type or paste a .txt",
                        icon = Icons.Default.TextSnippet,
                        onClick = { showTextEditor = true },
                        modifier = Modifier.weight(1f),
                    )
                    ShareOptionTile(
                        title = "Photos",
                        subtitle = "Choose from gallery",
                        icon = Icons.Default.Image,
                        onClick = onPickMedia,
                        modifier = Modifier.weight(1f),
                    )
                }
                ShareOptionTile(
                    title = "Installed Android app",
                    subtitle = "Share a user-installed APK or split APKS package",
                    icon = Icons.Default.Smartphone,
                    onClick = { showInstalledApps = true },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            }
        }
        if (mode == ShareMode.SEND) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), accent = VoltGreen) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DeviceRadar(active = deviceIsActive)
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("This device", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            when {
                                status.label.startsWith("Looking") -> "Scanning the local network"
                                status.hosting -> "Visible to nearby devices"
                                else -> "Private and ready"
                            },
                            color = VoltTextMuted,
                            fontSize = 12.sp,
                        )
                    }
                    PremiumSwitch(
                        checked = status.hosting,
                        onCheckedChange = { if (it) transfer.startHosting() else transfer.close() },
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                GlowButton("Find nearby", Icons.Default.Search, { transfer.discoverPeers() }, Modifier.weight(1f))
                OutlinedButton(
                    onClick = { transfer.startHosting() },
                    modifier = Modifier.weight(1f).height(54.dp).shadow(10.dp, RoundedCornerShape(20.dp), spotColor = VoltGreen.copy(alpha = 0.16f)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, VoltGreen.copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VoltGreen),
                ) {
                    Icon(Icons.Default.Bolt, null)
                    Spacer(Modifier.width(7.dp))
                    Text("Host")
                }
            }
        }
        item {
            PendingShareQueue(
                pendingFiles = pendingFiles,
                onRemove = onRemovePending,
                onClear = onClearPending,
                editingEnabled = !status.transferring,
            )
        }
        item {
            Text("2. Choose a nearby device", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        if (peers.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), accent = VoltTeal, padding = 18.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DeviceRadar(active = status.label.startsWith("Looking"))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                if (status.label.startsWith("Looking")) "Scanning nearby devices…" else "No devices yet",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.height(5.dp))
                            Text(
                                if (status.label.startsWith("Looking")) {
                                    "Searching the local network for VoltShare devices."
                                } else {
                                    "Open VoltShare on the other phone, tap Host, then tap Find nearby here."
                                },
                                color = VoltTextMuted,
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
            }
        } else {
            items(peers) { peer ->
                GlassCard(modifier = Modifier.fillMaxWidth(), accent = VoltGreen, padding = 16.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Smartphone, null, tint = VoltGreen)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(peer.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text("Direct local connection", color = VoltTextMuted, fontSize = 12.sp)
                        }
                        TextButton(
                            onClick = { transfer.send(peer, pendingFiles, onTransferFinished) },
                            enabled = pendingFiles.isNotEmpty() && !status.transferring,
                            colors = ButtonDefaults.textButtonColors(contentColor = VoltGreen),
                        ) {
                            Icon(Icons.Default.ArrowUpward, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Send ${pendingFiles.size}")
                        }
                    }
                }
            }
        }
        item {
            AnimatedVisibility(
                status.active || status.progress > 0f || status.errorLog != null,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut(),
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth(), accent = VoltGreen, padding = 18.dp) {
                    Text(status.label, color = Color.White, fontWeight = FontWeight.SemiBold)
                    if (status.totalFiles > 0) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "${status.completedFiles}/${status.totalFiles} files • ${formatSize(status.bytesTransferred)} / ${formatSize(status.totalBytes)}",
                            color = VoltTextMuted,
                            fontSize = 12.sp,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { status.progress },
                        modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                        color = VoltGreen,
                        trackColor = Color.White.copy(alpha = 0.1f),
                    )
                    TransferMetrics(status)
                    if (status.errorLog != null) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { showErrorLog = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFFF6B6B).copy(alpha = 0.65f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF8A80)),
                        ) {
                            Icon(Icons.Default.Description, null, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("View detailed error report")
                        }
                    }
                }
            }
        }
        } else {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), accent = VoltGreen, padding = 20.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DeviceRadar(active = status.hosting || status.transferring)
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Receive mode", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(
                                if (status.hosting) "Listening for nearby VoltShare devices" else "Turn on receiving to become discoverable",
                                color = VoltTextMuted,
                                fontSize = 12.sp,
                            )
                        }
                        PremiumSwitch(
                            checked = status.hosting,
                            onCheckedChange = { if (it) transfer.startHosting() else transfer.close() },
                        )
                    }
                    Spacer(Modifier.height(18.dp))
                    Text(
                        "Files sent from another VoltShare device are verified and saved to your private app storage only after the transfer completes.",
                        color = Color.White.copy(alpha = 0.78f),
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                    )
                }
            }
            item {
                TransferStatusCard(
                    title = "Receiving",
                    status = status,
                    visible = status.transferring || status.label.startsWith("Received") || status.errorLog != null,
                    onShowError = { showErrorLog = true },
                )
            }
            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), accent = VoltTeal, padding = 18.dp) {
                    Text("Ready for a private handoff", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(7.dp))
                    Text(
                        "Keep receiving enabled on this screen. The other device can find you, choose its pending files, and send them directly over the local network.",
                        color = VoltTextMuted,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareModeSwitch(
    mode: ShareMode,
    onModeChange: (ShareMode) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = VoltSurface,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
    ) {
        Row(
            modifier = Modifier.padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            ShareMode.values().forEach { option ->
                val selected = option == mode
                Surface(
                    modifier = Modifier.weight(1f).clickable { onModeChange(option) },
                    color = if (selected) VoltGreen else Color.Transparent,
                    contentColor = if (selected) Color.Black else VoltTextMuted,
                    shape = RoundedCornerShape(17.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 13.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            if (option == ShareMode.SEND) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp),
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(option.label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingShareQueue(
    pendingFiles: List<PendingShare>,
    onRemove: (PendingShare) -> Unit,
    onClear: () -> Unit,
    editingEnabled: Boolean,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("1. Queue files to send", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    if (pendingFiles.isEmpty()) "Only files picked in this session appear here."
                    else "${pendingFiles.size} file${if (pendingFiles.size == 1) "" else "s"} ready",
                    color = VoltTextMuted,
                    fontSize = 12.sp,
                )
            }
            if (pendingFiles.isNotEmpty()) {
                TextButton(
                    onClick = onClear,
                    enabled = editingEnabled,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFFA49C)),
                ) {
                    Text("Clear queue")
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        if (pendingFiles.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth(), accent = VoltTeal, padding = 18.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, null, tint = VoltGreen, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Pick a file, folder, photo, note, or installed app above to start a temporary share queue.",
                        color = VoltTextMuted,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                pendingFiles.forEach { file ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = VoltSurface,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, VoltGreen.copy(alpha = 0.2f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 13.dp, top = 10.dp, bottom = 10.dp, end = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).background(VoltGreen.copy(alpha = 0.11f), RoundedCornerShape(13.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(shareIconFor(file), null, tint = VoltGreen, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(11.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(file.name, color = Color.White, maxLines = 1, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "${file.mimeType.substringAfterLast('/').replace('-', ' ')} • ${formatSize(file.sizeBytes)}",
                                    color = VoltTextMuted,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                )
                            }
                            IconButton(
                                onClick = { onRemove(file) },
                                enabled = editingEnabled,
                            ) {
                                Icon(Icons.Default.Close, "Remove ${file.name}", tint = VoltTextMuted, modifier = Modifier.size(19.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun shareIconFor(file: PendingShare): ImageVector = when {
    file.mimeType.startsWith("image/") -> Icons.Default.Image
    file.mimeType.startsWith("video/") -> Icons.Default.VideoLibrary
    file.mimeType == "application/zip" || file.name.endsWith(".apk", true) || file.name.endsWith(".apks", true) -> Icons.Default.Folder
    else -> Icons.Default.Description
}

@Composable
private fun TransferStatusCard(
    title: String,
    status: TransferStatus,
    visible: Boolean,
    onShowError: (() -> Unit)?,
) {
    if (!visible) return
    GlassCard(modifier = Modifier.fillMaxWidth(), accent = VoltGreen, padding = 18.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (status.errorLog == null) Icons.Default.Bolt else Icons.Default.Close,
                null,
                tint = if (status.errorLog == null) VoltGreen else Color(0xFFFF8A80),
            )
            Spacer(Modifier.width(10.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Text(status.label, color = VoltTextMuted, fontSize = 13.sp)
        if (status.totalFiles > 0) {
            Spacer(Modifier.height(7.dp))
            Text(
                "${status.completedFiles}/${status.totalFiles} files • ${formatSize(status.bytesTransferred)} / ${formatSize(status.totalBytes)}",
                color = VoltTextMuted,
                fontSize = 12.sp,
            )
        }
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { status.progress },
            modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
            color = VoltGreen,
            trackColor = Color.White.copy(alpha = 0.1f),
        )
        TransferMetrics(status)
        if (status.errorLog != null && onShowError != null) {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onShowError,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFFF6B6B).copy(alpha = 0.65f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF8A80)),
            ) {
                Icon(Icons.Default.Description, null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(8.dp))
                Text("View detailed error report")
            }
        }
    }
}

@Composable
private fun TransferMetrics(status: TransferStatus) {
    Spacer(Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TransferMetric(
            label = "PROGRESS",
            value = "${(status.progress.coerceIn(0f, 1f) * 100f).toInt()}%",
            modifier = Modifier.weight(1f),
        )
        TransferMetric(
            label = "SPEED",
            value = formatTransferSpeed(status.speedBytesPerSecond),
            modifier = Modifier.weight(1f),
        )
        TransferMetric(
            label = "REMAINING",
            value = formatTransferEta(status.etaSeconds),
            modifier = Modifier.weight(1f),
        )
    }
    status.currentFile?.let { currentFile ->
        Spacer(Modifier.height(10.dp))
        Text(
            "Current file · $currentFile",
            color = VoltTextMuted,
            fontSize = 11.sp,
            maxLines = 1,
        )
    }
}

@Composable
private fun TransferMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.045f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f)),
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 9.dp)) {
            Text(label, color = VoltTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.7.sp)
            Spacer(Modifier.height(3.dp))
            Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun DeviceRadar(active: Boolean) {
    val transition = rememberInfiniteTransition(label = "device-radar")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Restart),
        label = "device-pulse",
    )
    Box(
        modifier = Modifier.size(62.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (active) {
            Box(
                modifier = Modifier
                    .size(42.dp + (26.dp * pulse))
                    .border(
                        width = 1.dp,
                        color = VoltGreen.copy(alpha = (0.42f * (1f - pulse)).coerceAtLeast(0.05f)),
                        shape = CircleShape,
                    ),
            )
            Box(
                modifier = Modifier
                    .size(48.dp + (18.dp * pulse))
                    .background(VoltGreen.copy(alpha = 0.06f * (1f - pulse)), CircleShape),
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(VoltGreen.copy(alpha = if (active) 0.18f else 0.1f), CircleShape)
                .border(1.dp, VoltGreen.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Smartphone, null, tint = VoltGreen, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun TransferErrorDialog(
    errorLog: String,
    onDismiss: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            shape = RoundedCornerShape(30.dp),
            color = VoltSurfaceRaised,
            border = BorderStroke(1.dp, Color(0xFFFF6B6B).copy(alpha = 0.38f)),
            shadowElevation = 24.dp,
        ) {
            Column(Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).background(Color(0xFFFF6B6B).copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color(0xFFFF8A80), modifier = Modifier.size(25.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Transfer diagnostics", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black)
                        Text("Some files could not be sent", color = Color(0xFFFF8A80), fontSize = 12.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close error report", tint = VoltTextMuted)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "The report below contains the file, transfer stage, and technical cause. Copy it when you need to troubleshoot the two devices.",
                    color = VoltTextMuted,
                    lineHeight = 19.sp,
                    fontSize = 13.sp,
                )
                Spacer(Modifier.height(14.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().height(330.dp),
                    color = Color.Black.copy(alpha = 0.42f),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                ) {
                    Text(
                        errorLog,
                        modifier = Modifier.padding(14.dp).verticalScroll(rememberScrollState()),
                        color = Color.White.copy(alpha = 0.86f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { clipboard.setText(AnnotatedString(errorLog)) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, VoltGreen.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VoltGreen),
                    ) {
                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(7.dp))
                        Text("Copy report")
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(0.72f).height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VoltGreen, contentColor = Color.Black),
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@Composable
private fun ShareOptionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassCard(
        modifier = modifier.clickable(onClick = onClick),
        accent = VoltGreen,
        padding = 14.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).background(VoltGreen.copy(alpha = 0.11f), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = VoltGreen, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.height(3.dp))
                Text(subtitle, color = VoltTextMuted, fontSize = 10.sp, lineHeight = 13.sp)
            }
        }
    }
}

@Composable
private fun TextComposerDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VoltSurfaceRaised,
        title = {
            Column {
                Text("Create a text share", color = Color.White)
                Spacer(Modifier.height(5.dp))
                Text("Your note will be saved as a private .txt file.", color = VoltTextMuted, fontSize = 12.sp)
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth().height(190.dp),
                    placeholder = { Text("Type or paste anything…", color = VoltTextMuted) },
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    minLines = 7,
                    maxLines = 9,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = VoltSurface,
                        unfocusedContainerColor = VoltSurface,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = VoltGreen,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.14f),
                        cursorColor = VoltGreen,
                    ),
                )
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { clipboard.getText()?.text?.let { text = it } },
                    colors = ButtonDefaults.textButtonColors(contentColor = VoltGreen),
                ) {
                    Icon(Icons.Default.ContentPaste, null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("Paste from clipboard")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(text) },
                enabled = text.isNotBlank(),
                colors = ButtonDefaults.textButtonColors(contentColor = VoltGreen),
            ) {
                Icon(Icons.Default.Share, null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(6.dp))
                Text("Prepare to send")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = VoltTextMuted)) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun InstalledAppsDialog(
    onDismiss: () -> Unit,
    onSelected: (List<InstalledAppChoice>) -> Unit,
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var selectedPackages by remember { mutableStateOf<Set<String>>(emptySet()) }
    val apps = remember {
        context.packageManager
            .getInstalledApplications(PackageManager.GET_META_DATA)
            .asSequence()
            .filter { it.packageName != context.packageName }
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
            .mapNotNull { info ->
                val paths = buildList {
                    info.sourceDir?.let(::add)
                    info.splitSourceDirs?.forEach(::add)
                }.filter { File(it).isFile }
                if (paths.isEmpty()) null else InstalledAppChoice(
                    label = context.packageManager.getApplicationLabel(info).toString(),
                    packageName = info.packageName,
                    apkPaths = paths,
                    icon = runCatching { context.packageManager.getApplicationIcon(info) }.getOrNull(),
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }
    val filteredApps = apps.filter {
        query.isBlank() ||
            it.label.contains(query.trim(), ignoreCase = true) ||
            it.packageName.contains(query.trim(), ignoreCase = true)
    }
    val selectedApps = apps.filter { it.packageName in selectedPackages }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            shape = RoundedCornerShape(30.dp),
            color = VoltSurfaceRaised,
            border = BorderStroke(1.dp, VoltGreen.copy(alpha = 0.26f)),
            shadowElevation = 24.dp,
        ) {
            Column(Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).background(VoltGreen.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Smartphone, null, tint = VoltGreen, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Choose an app", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        Text("Ready to package and share", color = VoltTextMuted, fontSize = 12.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close", tint = VoltTextMuted)
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    "Select a user-installed app. Its original icon and name are shown so you can confirm exactly what will be shared.",
                    color = VoltTextMuted,
                    lineHeight = 19.sp,
                    fontSize = 13.sp,
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Search installed apps") },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = VoltGreen) },
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VoltGreen,
                        focusedLabelColor = VoltGreen,
                        cursorColor = VoltGreen,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.16f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                    ),
                    shape = RoundedCornerShape(18.dp),
                )
                Spacer(Modifier.height(14.dp))
                if (filteredApps.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                        Text(
                            if (apps.isEmpty()) "No user-installed apps were found." else "No apps match your search.",
                            color = VoltTextMuted,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.height(390.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            val selected = app.packageName in selectedPackages
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    selectedPackages = if (selected) {
                                        selectedPackages - app.packageName
                                    } else {
                                        selectedPackages + app.packageName
                                    }
                                },
                                color = if (selected) VoltGreen.copy(alpha = 0.12f) else VoltSurface,
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, if (selected) VoltGreen else Color.White.copy(alpha = 0.08f)),
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier.size(58.dp).clip(RoundedCornerShape(17.dp)).background(Color.Black),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (app.icon != null) {
                                            AndroidView(
                                                factory = { ImageView(it).apply { scaleType = ImageView.ScaleType.CENTER_INSIDE } },
                                                update = { view -> view.setImageDrawable(app.icon) },
                                                modifier = Modifier.fillMaxSize().padding(8.dp),
                                            )
                                        } else {
                                            Icon(Icons.Default.Smartphone, null, tint = VoltGreen, modifier = Modifier.size(25.dp))
                                        }
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(app.label, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Spacer(Modifier.height(3.dp))
                                        Text(app.packageName, color = VoltTextMuted, fontSize = 10.sp, maxLines = 1)
                                        Text(
                                            if (app.apkPaths.size > 1) "Split package • APKS" else "Install package • APK",
                                            color = VoltGreen,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(if (selected) VoltGreen else VoltGreen.copy(alpha = 0.12f), CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            if (selected) Icons.Default.Check else Icons.Default.ArrowUpward,
                                            if (selected) "Selected" else "Select app",
                                            tint = if (selected) Color.Black else VoltGreen,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("${selectedApps.size} selected", color = VoltGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = {
                            selectedPackages = if (selectedPackages.size == filteredApps.size) {
                                emptySet()
                            } else {
                                filteredApps.map { it.packageName }.toSet()
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = VoltGreen),
                    ) {
                        Text(if (selectedPackages.size == filteredApps.size) "Clear" else "Select all")
                    }
                    OutlinedButton(
                        onClick = { onSelected(selectedApps) },
                        enabled = selectedApps.isNotEmpty(),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VoltGreen),
                        border = BorderStroke(1.dp, VoltGreen.copy(alpha = 0.55f)),
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Prepare")
                    }
                }
            }
        }
    }
}

@Composable
private fun TechnicalErrorDialog(
    title: String,
    log: String,
    onDismiss: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            shape = RoundedCornerShape(30.dp),
            color = VoltSurfaceRaised,
            border = BorderStroke(1.dp, Color(0xFFFF6B6B).copy(alpha = 0.38f)),
            shadowElevation = 24.dp,
        ) {
            Column(Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).background(Color(0xFFFF6B6B).copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Info, null, tint = Color(0xFFFF8A80), modifier = Modifier.size(25.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("DIAGNOSTIC REPORT", color = Color(0xFFFF8A80), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(title, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close diagnostic report", tint = VoltTextMuted)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "This is the complete error returned by the operation. Copy it when reporting a problem or comparing device behavior.",
                    color = VoltTextMuted,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                )
                Spacer(Modifier.height(14.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    color = Color.Black.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                ) {
                    Text(
                        log,
                        color = Color.White.copy(alpha = 0.86f),
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(14.dp).verticalScroll(rememberScrollState()),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(log))
                            Toast.makeText(context, "Full diagnostic copied", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, VoltGreen.copy(alpha = 0.45f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VoltGreen),
                    ) {
                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(7.dp))
                        Text("Copy full log")
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(0.72f).height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VoltGreen, contentColor = Color.Black),
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultSharePasswordDialog(
    title: String,
    description: String,
    error: String?,
    allowNoPassword: Boolean,
    busy: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit,
) {
    var protectWithPassword by remember { mutableStateOf(!allowNoPassword) }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    val visibleError = error ?: localError
    Dialog(
        onDismissRequest = { if (!busy) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            shape = RoundedCornerShape(30.dp),
            color = VoltSurfaceRaised,
            border = BorderStroke(1.dp, VoltGreen.copy(alpha = 0.32f)),
            shadowElevation = 28.dp,
        ) {
            Column(Modifier.padding(22.dp).verticalScroll(rememberScrollState())) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(VoltGreen.copy(alpha = 0.13f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Security, null, tint = VoltGreen, modifier = Modifier.size(25.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("VAULTSHARE", color = VoltGreen, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    }
                    IconButton(onClick = { if (!busy) onDismiss() }) {
                        Icon(Icons.Default.Close, "Close", tint = VoltTextMuted)
                    }
                }
                Spacer(Modifier.height(13.dp))
                Text(description, color = VoltTextMuted, fontSize = 13.sp, lineHeight = 19.sp)
                if (allowNoPassword) {
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Require a password", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Without one, the backup remains plain and does not ask during restore.", color = VoltTextMuted, fontSize = 12.sp, lineHeight = 17.sp)
                        }
                        PremiumSwitch(
                            checked = protectWithPassword,
                            enabled = !busy,
                            onCheckedChange = {
                                protectWithPassword = it
                                localError = null
                            },
                        )
                    }
                }
                if (protectWithPassword) {
                    Spacer(Modifier.height(16.dp))
                    SecureField(
                        value = password,
                        label = "Backup password",
                        keyboardType = KeyboardType.Password,
                        onValueChange = {
                            password = it
                            localError = null
                        },
                    )
                    Spacer(Modifier.height(10.dp))
                    SecureField(
                        value = confirmation,
                        label = "Confirm backup password",
                        keyboardType = KeyboardType.Password,
                        onValueChange = {
                            confirmation = it
                            localError = null
                        },
                    )
                }
                visibleError?.let {
                    Text(it, color = Color(0xFFFF8A80), fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
                }
                Spacer(Modifier.height(19.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !busy,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(contentColor = VoltTextMuted),
                    ) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (protectWithPassword && password != confirmation) {
                                localError = "The passwords do not match."
                            } else if (protectWithPassword && password.length < 4) {
                                localError = "Use at least four characters."
                            } else {
                                onConfirm(if (protectWithPassword) password else null)
                            }
                        },
                        enabled = !busy && (!protectWithPassword || password.isNotEmpty()),
                        modifier = Modifier.weight(1.25f).height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VoltGreen, contentColor = Color.Black),
                    ) {
                        if (busy) {
                            CircularProgressIndicator(modifier = Modifier.size(17.dp), strokeWidth = 2.dp, color = Color.Black)
                        } else {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(7.dp))
                            Text(if (allowNoPassword) "Choose folder" else "Restore", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun technicalLog(prefix: String, error: Throwable): String = buildString {
    appendLine(prefix)
    appendLine("Time: ${System.currentTimeMillis()}")
    appendLine()
    appendLine(error.stackTraceToString())
}

@Composable
private fun SecurityHome(
    lockType: LockType,
    lockEnabled: Boolean,
    biometricEnabled: Boolean,
    fileLockDefault: Boolean,
    screenshotEnabled: Boolean,
    transferStatus: TransferStatus,
    biometricAvailable: Boolean,
    onRequestChangeMethod: () -> Unit,
    onRequestBiometric: (Boolean) -> Unit,
    onRequestLock: (Boolean) -> Unit,
    onRequestFileLockDefault: (Boolean) -> Unit,
    onRequestScreenshots: (Boolean) -> Unit,
    onCreateBackup: () -> Unit,
    onRestoreBackup: () -> Unit,
    onLockNow: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(VoltBlack),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            LockSecurityHero(
                lockType = lockType,
                lockEnabled = lockEnabled,
                biometricEnabled = biometricEnabled && lockEnabled && biometricAvailable,
                fileLockDefault = fileLockDefault,
            )
        }
        item {
            TransferStatusCard(
                title = "Share activity",
                status = transferStatus,
                visible = transferStatus.transferring || transferStatus.progress > 0f || transferStatus.errorLog != null,
                onShowError = null,
            )
        }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), accent = if (lockEnabled) VoltGreen else VoltTeal, padding = 18.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (lockEnabled) VoltGreen.copy(alpha = 0.14f) else VoltTeal.copy(alpha = 0.14f),
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            if (lockEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                            null,
                            tint = if (lockEnabled) VoltGreen else VoltTeal,
                            modifier = Modifier.size(23.dp),
                        )
                    }
                    Spacer(Modifier.width(13.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Vault lock", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            if (lockEnabled) "Protection is active whenever VoltShare opens" else "Protection is paused until you turn it back on",
                            color = VoltTextMuted,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                        )
                    }
                    PremiumSwitch(
                        checked = lockEnabled,
                        onCheckedChange = onRequestLock,
                    )
                }
            }
        }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), accent = VoltTeal, padding = 18.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(VoltTeal.copy(alpha = 0.13f), RoundedCornerShape(15.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.SwapHoriz, null, tint = Color(0xFF65D8C8), modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(13.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Unlock method", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("${lockType.label} is your current vault key", color = VoltTextMuted, fontSize = 12.sp)
                    }
                    TextButton(
                        onClick = onRequestChangeMethod,
                        enabled = lockEnabled,
                        colors = ButtonDefaults.textButtonColors(contentColor = VoltGreen),
                    ) {
                        Text("Change", fontWeight = FontWeight.Bold)
                    }
                }
                Divider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(VoltGreen.copy(alpha = 0.11f), RoundedCornerShape(15.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Security, null, tint = VoltGreen, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(13.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Lock individual files", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            if (lockEnabled) "Add a second verification gate to sensitive previews" else "Enable the vault lock to use file-level locks",
                            color = VoltTextMuted,
                            fontSize = 12.sp,
                        )
                    }
                    PremiumSwitch(
                        checked = lockEnabled && fileLockDefault,
                        enabled = lockEnabled,
                        onCheckedChange = { if (lockEnabled) onRequestFileLockDefault(it) },
                    )
                }
            }
        }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), accent = if (biometricAvailable) VoltGreen else VoltTextMuted, padding = 18.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (biometricAvailable) VoltGreen.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.07f),
                                RoundedCornerShape(15.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Fingerprint,
                            null,
                            tint = if (biometricAvailable) VoltGreen else VoltTextMuted,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Spacer(Modifier.width(13.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Biometric unlock", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            if (biometricAvailable) "Use fingerprint or device unlock at the vault gate" else "No compatible biometric or device credential is available",
                            color = VoltTextMuted,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                        )
                    }
                    PremiumSwitch(
                        checked = lockEnabled && biometricEnabled && biometricAvailable,
                        enabled = lockEnabled && biometricAvailable,
                        onCheckedChange = { if (lockEnabled) onRequestBiometric(it) },
                    )
                }
            }
        }
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                accent = if (screenshotEnabled) VoltGreen else VoltTeal,
                padding = 18.dp,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (screenshotEnabled) VoltGreen.copy(alpha = 0.12f) else VoltTeal.copy(alpha = 0.13f),
                                RoundedCornerShape(15.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Smartphone,
                            null,
                            tint = if (screenshotEnabled) VoltGreen else VoltTextMuted,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.width(13.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Allow screenshots", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            if (screenshotEnabled) "Temporarily allowed for five minutes" else "Screenshots are blocked everywhere in VoltShare",
                            color = VoltTextMuted,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                        )
                    }
                    PremiumSwitch(
                        checked = screenshotEnabled,
                        enabled = lockEnabled,
                        onCheckedChange = { if (lockEnabled) onRequestScreenshots(it) },
                    )
                }
            }
        }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), accent = VoltGreen, padding = 18.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(VoltGreen.copy(alpha = 0.13f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Folder, null, tint = VoltGreen, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(13.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Vault backup", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            "Save every file, folder, lock flag, and custom order as a .vaultshare file",
                            color = VoltTextMuted,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                        )
                    }
                }
                Spacer(Modifier.height(15.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onRestoreBackup,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, VoltGreen.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VoltGreen),
                    ) {
                        Icon(Icons.Default.Replay, null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Restore")
                    }
                    Button(
                        onClick = onCreateBackup,
                        modifier = Modifier.weight(1.15f).height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VoltGreen, contentColor = Color.Black),
                    ) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Create backup", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item {
            if (lockEnabled) {
                GlowButton(
                    text = "Lock vault now",
                    icon = Icons.Default.Lock,
                    onClick = onLockNow,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                OutlinedButton(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                ) {
                    Icon(Icons.Default.LockOpen, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Enable vault lock above to lock now")
                }
            }
        }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), accent = VoltGreen, padding = 18.dp) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Bolt, null, tint = VoltGreen, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(11.dp))
                    Column {
                        Text("Private by design", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(5.dp))
                        Text(
                            "Your lock secret is reduced to a salted device-local hash. VoltShare never uploads it, and every security change asks for the current lock first.",
                            color = VoltTextMuted,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LockSecurityHero(
    lockType: LockType,
    lockEnabled: Boolean,
    biometricEnabled: Boolean,
    fileLockDefault: Boolean,
) {
    val transition = rememberInfiniteTransition(label = "security-hero")
    val pulse by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(2200), RepeatMode.Reverse),
        label = "security-pulse",
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(218.dp)
            .shadow(24.dp, RoundedCornerShape(30.dp), spotColor = VoltGreen.copy(alpha = 0.26f))
            .background(
                Brush.linearGradient(
                    listOf(
                        VoltGreen.copy(alpha = 0.18f),
                        VoltTeal.copy(alpha = 0.13f),
                        VoltSurfaceRaised,
                    ),
                ),
                RoundedCornerShape(30.dp),
            )
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(30.dp))
            .padding(20.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size((132.dp * pulse))
                .border(1.dp, VoltGreen.copy(alpha = 0.13f), CircleShape),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(84.dp)
                .background(VoltTeal.copy(alpha = 0.08f), CircleShape),
        )
        Column(modifier = Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, null, tint = VoltGreen, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("SECURITY CORE", color = VoltGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.2.sp)
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .shadow(20.dp, CircleShape, spotColor = VoltGreen.copy(alpha = 0.5f))
                        .background(VoltGreen.copy(alpha = if (lockEnabled) 0.18f else 0.08f), CircleShape)
                        .border(1.dp, VoltGreen.copy(alpha = 0.55f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (lockEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                        null,
                        tint = if (lockEnabled) VoltGreen else VoltTextMuted,
                        modifier = Modifier.size(32.dp),
                    )
                }
                Spacer(Modifier.width(17.dp))
                Column {
                    Text(
                        if (lockEnabled) "Vault protected" else "Protection paused",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        if (biometricEnabled) "${lockType.label} + biometric ready" else "${lockType.label} only",
                        color = VoltTextMuted,
                        fontSize = 12.sp,
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LockStatusChip(if (lockEnabled) "ACTIVE" else "PAUSED", lockEnabled)
                LockStatusChip(if (biometricEnabled) "BIOMETRIC ON" else "BIOMETRIC OFF", biometricEnabled)
                LockStatusChip(
                    if (lockEnabled && fileLockDefault) "FILE LOCKS ON" else "FILE LOCKS OFF",
                    lockEnabled && fileLockDefault,
                )
            }
        }
    }
}

@Composable
private fun LockStatusChip(label: String, active: Boolean) {
    Surface(
        color = if (active) VoltGreen.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.07f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, if (active) VoltGreen.copy(alpha = 0.38f) else Color.White.copy(alpha = 0.12f)),
    ) {
        Text(
            label,
            color = if (active) VoltGreen else VoltTextMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
        )
    }
}

@Composable
private fun LockVerificationDialog(
    title: String,
    type: LockType,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Boolean,
) {
    var secret by remember { mutableStateOf("") }
    var pattern by remember { mutableStateOf<List<Int>>(emptyList()) }
    var error by remember { mutableStateOf(false) }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            shape = RoundedCornerShape(30.dp),
            color = VoltSurfaceRaised,
            border = BorderStroke(1.dp, VoltGreen.copy(alpha = 0.32f)),
            shadowElevation = 28.dp,
        ) {
            Column(Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(14.dp, CircleShape, spotColor = VoltGreen.copy(alpha = 0.35f))
                            .background(VoltGreen.copy(alpha = 0.13f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Security, null, tint = VoltGreen, modifier = Modifier.size(25.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("AUTHENTICATE", color = VoltGreen, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(title, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close", tint = VoltTextMuted)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Use your current ${type.label.lowercase()} to authorize this security change.",
                    color = VoltTextMuted,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                )
                Spacer(Modifier.height(18.dp))
                if (type == LockType.PATTERN) {
                    PatternPad(pattern, onChange = {
                        pattern = it
                        error = false
                    })
                } else {
                    SecureField(
                        value = secret,
                        label = "Current ${type.label.lowercase()}",
                        keyboardType = if (type == LockType.PIN) KeyboardType.NumberPassword else KeyboardType.Password,
                        onValueChange = {
                            secret = it
                            error = false
                        },
                    )
                }
                AnimatedVisibility(error) {
                    Text(
                        "That is not your current lock.",
                        color = Color(0xFFFF8A80),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(contentColor = VoltTextMuted),
                    ) { Text("Cancel") }
                    Button(
                        onClick = {
                            val attempt = if (type == LockType.PATTERN) pattern.joinToString("-") else secret
                            if (!onConfirm(attempt)) error = true
                        },
                        enabled = if (type == LockType.PATTERN) pattern.size >= 4 else secret.length >= 4,
                        modifier = Modifier.weight(1.25f).height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VoltGreen, contentColor = Color.Black),
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(7.dp))
                        Text("Verify", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChangeLockDialog(
    currentType: LockType,
    onDismiss: () -> Unit,
    onChanged: (LockType, String) -> Unit,
) {
    var selected by remember { mutableStateOf(currentType) }
    var secret by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var pattern by remember { mutableStateOf<List<Int>>(emptyList()) }
    var confirmPattern by remember { mutableStateOf<List<Int>>(emptyList()) }
    var error by remember { mutableStateOf("") }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            shape = RoundedCornerShape(30.dp),
            color = VoltSurfaceRaised,
            border = BorderStroke(1.dp, VoltGreen.copy(alpha = 0.32f)),
            shadowElevation = 28.dp,
        ) {
            Column(Modifier.padding(22.dp).verticalScroll(rememberScrollState())) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(14.dp, CircleShape, spotColor = VoltGreen.copy(alpha = 0.35f))
                            .background(VoltGreen.copy(alpha = 0.13f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Lock, null, tint = VoltGreen, modifier = Modifier.size(25.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("REKEY VAULT", color = VoltGreen, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Choose a new lock", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close", tint = VoltTextMuted)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "Your current lock was verified. Pick the new way you want to protect the vault.",
                    color = VoltTextMuted,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                )
                Spacer(Modifier.height(18.dp))
                FloatingSegmentedControl(
                    items = LockType.entries.map { it.label },
                    selectedIndex = selected.ordinal,
                    onSelected = {
                        selected = LockType.entries[it]
                        secret = ""
                        confirm = ""
                        pattern = emptyList()
                        confirmPattern = emptyList()
                        error = ""
                    },
                )
                Spacer(Modifier.height(18.dp))
                if (selected == LockType.PATTERN) {
                    Text("New pattern · connect at least 4 nodes", color = VoltTextMuted, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    PatternPad(pattern, onChange = {
                        pattern = it
                        error = ""
                    })
                    Spacer(Modifier.height(12.dp))
                    Text("Repeat the pattern", color = VoltTextMuted, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    PatternPad(confirmPattern, onChange = {
                        confirmPattern = it
                        error = ""
                    })
                } else {
                    SecureField(
                        value = secret,
                        label = "New ${selected.label.lowercase()}",
                        keyboardType = if (selected == LockType.PIN) KeyboardType.NumberPassword else KeyboardType.Password,
                        onValueChange = {
                            secret = it
                            error = ""
                        },
                    )
                    Spacer(Modifier.height(13.dp))
                    SecureField(
                        value = confirm,
                        label = "Repeat ${selected.label.lowercase()}",
                        keyboardType = if (selected == LockType.PIN) KeyboardType.NumberPassword else KeyboardType.Password,
                        onValueChange = {
                            confirm = it
                            error = ""
                        },
                    )
                }
                AnimatedVisibility(error.isNotEmpty()) {
                    Text(error, color = Color(0xFFFF8A80), fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
                }
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(contentColor = VoltTextMuted),
                    ) { Text("Cancel") }
                    Button(
                        onClick = {
                            error = when {
                                selected == LockType.PATTERN && pattern.size < 4 -> "Use at least 4 connected nodes."
                                selected == LockType.PATTERN && pattern != confirmPattern -> "The two patterns do not match."
                                selected != LockType.PATTERN && secret.length < 4 -> "Use at least 4 characters."
                                selected != LockType.PATTERN && secret != confirm -> "The two entries do not match."
                                else -> ""
                            }
                            if (error.isEmpty()) {
                                onChanged(selected, if (selected == LockType.PATTERN) pattern.joinToString("-") else secret)
                            }
                        },
                        modifier = Modifier.weight(1.25f).height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VoltGreen, contentColor = Color.Black),
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(7.dp))
                        Text("Save lock", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun FileViewerScreen(activity: MainActivity, vault: VaultRepository, file: VaultFile, onBack: () -> Unit) {
    val prepared = remember(file.id) { vault.prepareViewing(file) }
    var fullscreen by remember(file.id) { mutableStateOf(false) }
    var showInfo by remember(file.id) { mutableStateOf(false) }
    val insetsController = remember(activity) {
        WindowInsetsControllerCompat(activity.window, activity.window.decorView)
    }
    LaunchedEffect(fullscreen) {
        if (fullscreen) {
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }
    BackHandler(enabled = fullscreen) { fullscreen = false }
    Column(Modifier.fillMaxSize().background(VoltBlack)) {
        if (!fullscreen) {
            TopAppBar(
                title = { Text(file.name, maxLines = 1, color = Color.White) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
                actions = {
                    IconButton(onClick = { showInfo = true }) {
                        Icon(Icons.Default.Info, "File information", tint = VoltGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VoltBlack),
            )
        }
        if (prepared == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("This file could not be opened.", color = VoltTextMuted)
            }
        } else {
            when {
                isImage(file) -> ImageViewer(prepared, fullscreen) { fullscreen = !fullscreen }
                isVideo(file) -> VideoViewer(prepared, fullscreen) { fullscreen = !fullscreen }
                isAudio(file) -> AudioViewer(prepared, fullscreen) { fullscreen = !fullscreen }
                isPdf(file) -> PdfViewer(prepared, fullscreen) { fullscreen = !fullscreen }
                isText(file) -> TextViewer(prepared, fullscreen) { fullscreen = !fullscreen }
                isInstallable(file) -> InstallerViewer(activity, prepared, file, fullscreen) { fullscreen = !fullscreen }
                else -> GenericViewer(activity, prepared, file, fullscreen) { fullscreen = !fullscreen }
            }
        }
    }
    if (showInfo) {
        ViewerInfoDialog(
            file = file,
            prepared = prepared,
            onDismiss = { showInfo = false },
        )
    }
}

@Composable
private fun ImageViewer(file: File, fullscreen: Boolean, onToggleFullscreen: () -> Unit) {
    var bitmap by remember(file) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(file) {
        bitmap = withContext(Dispatchers.IO) {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            BitmapFactory.decodeFile(
                file.absolutePath,
                BitmapFactory.Options().apply {
                    inSampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight, 4096, 4096)
                },
            )
        }
    }
    bitmap?.let {
        ZoomableBitmapSurface(
            bitmap = it,
            contentDescription = "Image preview",
            fullscreen = fullscreen,
            onToggleFullscreen = onToggleFullscreen,
            toolbar = { reset, zoomIn, rotate ->
                IconButton(onClick = reset) {
                    Icon(Icons.Default.Replay, "Reset zoom", tint = VoltGreen)
                }
                IconButton(onClick = zoomIn) {
                    Icon(Icons.Default.ZoomIn, "Zoom image", tint = VoltGreen)
                }
                IconButton(onClick = rotate) {
                    Icon(Icons.Default.RotateRight, "Rotate image", tint = VoltGreen)
                }
                Text(
                    "Pinch to zoom · drag to pan",
                    color = VoltTextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f),
                )
            },
        )
    } ?: Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = VoltGreen, strokeWidth = 2.dp)
    }
}

@Composable
private fun ZoomableBitmapSurface(
    bitmap: Bitmap,
    contentDescription: String,
    fullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    toolbar: @Composable RowScope.(reset: () -> Unit, zoomIn: () -> Unit, rotate: () -> Unit) -> Unit,
) {
    var scale by remember(bitmap) { mutableFloatStateOf(1f) }
    var rotation by remember(bitmap) { mutableFloatStateOf(0f) }
    var offset by remember(bitmap) { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, panChange, rotationChange ->
        scale = (scale * zoomChange).coerceIn(1f, 7f)
        offset += panChange
        rotation += rotationChange
    }
    fun reset() {
        scale = 1f
        rotation = 0f
        offset = Offset.Zero
    }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Image(
            bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .transformable(transformState)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (scale > 1.1f) reset() else scale = 2.5f
                        },
                    )
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                    rotationZ = rotation
                },
        )
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp),
            color = VoltSurfaceRaised.copy(alpha = 0.94f),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, VoltGreen.copy(alpha = 0.24f)),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                toolbar(::reset, { scale = (scale + 0.5f).coerceAtMost(7f) }, { rotation += 90f })
                IconButton(onClick = onToggleFullscreen) {
                    Icon(
                        if (fullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        if (fullscreen) "Exit full screen" else "Full screen",
                        tint = VoltGreen,
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoViewer(file: File, fullscreen: Boolean, onToggleFullscreen: () -> Unit) {
    MediaViewer(file, fullscreen, onToggleFullscreen, isAudio = false)
}

@Composable
private fun AudioViewer(file: File, fullscreen: Boolean, onToggleFullscreen: () -> Unit) {
    MediaViewer(file, fullscreen, onToggleFullscreen, isAudio = true)
}

@Composable
private fun MediaViewer(
    file: File,
    fullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    isAudio: Boolean,
) {
    val context = LocalContext.current
    val player = remember(file) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(android.net.Uri.fromFile(file)))
            prepare()
            if (!isAudio) playWhenReady = true
        }
    }
    var isPlaying by remember(player) { mutableStateOf(player.isPlaying) }
    var muted by remember(player) { mutableStateOf(false) }
    var position by remember(player) { mutableLongStateOf(0L) }
    var duration by remember(player) { mutableLongStateOf(0L) }
    var showControls by remember(player) { mutableStateOf(true) }
    var showSpeedMenu by remember(player) { mutableStateOf(false) }
    val speeds = listOf(0.5f, 1f, 1.5f, 2f)
    androidx.compose.runtime.DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }
    LaunchedEffect(player, showControls, isPlaying) {
        while (true) {
            position = player.currentPosition.coerceAtLeast(0L)
            duration = player.duration.takeIf { it > 0L } ?: 0L
            isPlaying = player.isPlaying
            if (showControls && isPlaying) {
                delay(4500L)
                showControls = false
            } else {
                delay(250L)
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(showControls) {
                detectTapGestures(onTap = { showControls = !showControls })
            },
    ) {
        if (isAudio) {
            Column(
                modifier = Modifier.align(Alignment.Center).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(132.dp)
                        .background(VoltGreen.copy(alpha = 0.12f), CircleShape)
                        .border(1.dp, VoltGreen.copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.VideoLibrary, null, tint = VoltGreen, modifier = Modifier.size(54.dp))
                }
                Spacer(Modifier.height(18.dp))
                Text(file.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("Audio playback", color = VoltTextMuted, fontSize = 12.sp)
            }
        } else {
            AndroidView(
                factory = {
                    PlayerView(it).apply {
                        this.player = player
                        useController = false
                        resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (showControls) {
            Surface(
                modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(12.dp),
                color = VoltSurfaceRaised.copy(alpha = 0.92f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, VoltGreen.copy(alpha = 0.25f)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(if (isAudio) Icons.Default.VolumeUp else Icons.Default.PlayArrow, null, tint = VoltGreen, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(file.name, color = Color.White, maxLines = 1, modifier = Modifier.weight(1f))
                    Box {
                        IconButton(onClick = { showSpeedMenu = true }) {
                            Icon(Icons.Default.Speed, "Playback speed", tint = VoltGreen)
                        }
                        DropdownMenu(expanded = showSpeedMenu, onDismissRequest = { showSpeedMenu = false }) {
                            speeds.forEach { speed ->
                                DropdownMenuItem(
                                    text = { Text("${speed}×") },
                                    onClick = {
                                        player.setPlaybackSpeed(speed)
                                        showSpeedMenu = false
                                    },
                                )
                            }
                        }
                    }
                    IconButton(onClick = onToggleFullscreen) {
                        Icon(
                            if (fullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            if (fullscreen) "Exit full screen" else "Full screen",
                            tint = VoltGreen,
                        )
                    }
                }
            }
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(12.dp),
                color = VoltSurfaceRaised.copy(alpha = 0.95f),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, VoltGreen.copy(alpha = 0.25f)),
            ) {
                Column(Modifier.padding(horizontal = 13.dp, vertical = 8.dp)) {
                    Slider(
                        value = if (duration > 0L) position.toFloat().coerceIn(0f, duration.toFloat()) else 0f,
                        onValueChange = { position = it.toLong() },
                        onValueChangeFinished = { player.seekTo(position) },
                        valueRange = 0f..duration.coerceAtLeast(1L).toFloat(),
                        colors = androidx.compose.material3.SliderDefaults.colors(
                            thumbColor = VoltGreen,
                            activeTrackColor = VoltGreen,
                            inactiveTrackColor = Color.White.copy(alpha = 0.16f),
                        ),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(formatDuration(position), color = VoltTextMuted, fontSize = 11.sp)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { player.seekTo((position - 10_000L).coerceAtLeast(0L)) }) {
                            Icon(Icons.Default.Replay10, "Back ten seconds", tint = Color.White)
                        }
                        IconButton(onClick = {
                            if (player.isPlaying) player.pause() else player.play()
                            showControls = true
                        }) {
                            Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, if (isPlaying) "Pause" else "Play", tint = VoltGreen)
                        }
                        IconButton(onClick = {
                            player.seekTo((position + 10_000L).coerceAtMost(duration))
                        }) {
                            Icon(Icons.Default.FastForward, "Forward ten seconds", tint = Color.White)
                        }
                        IconButton(onClick = {
                            muted = !muted
                            player.volume = if (muted) 0f else 1f
                        }) {
                            Icon(if (muted) Icons.Default.VolumeOff else Icons.Default.VolumeUp, "Mute", tint = Color.White)
                        }
                        Spacer(Modifier.weight(1f))
                        Text(formatDuration(duration), color = VoltTextMuted, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfViewer(file: File, fullscreen: Boolean, onToggleFullscreen: () -> Unit) {
    var pageCount by remember(file) { mutableIntStateOf(0) }
    var currentPage by remember(file) { mutableIntStateOf(0) }
    var bitmap by remember(file) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(file) {
        pageCount = withContext(Dispatchers.IO) {
            runCatching {
                PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)).use { renderer ->
                    renderer.pageCount
                }
            }.getOrDefault(-1)
        }
    }
    LaunchedEffect(file, currentPage, pageCount) {
        if (pageCount <= 0) return@LaunchedEffect
        bitmap = null
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)).use { renderer ->
                    renderer.openPage(currentPage.coerceIn(0, renderer.pageCount - 1)).use { page ->
                        val scale = minOf(2f, 2400f / maxOf(page.width, page.height).toFloat())
                        val pageBitmap = Bitmap.createBitmap(
                            (page.width * scale).toInt().coerceAtLeast(1),
                            (page.height * scale).toInt().coerceAtLeast(1),
                            Bitmap.Config.ARGB_8888,
                        )
                        pageBitmap.eraseColor(android.graphics.Color.WHITE)
                        page.render(pageBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        pageBitmap
                    }
                }
            }.getOrNull()
        }
    }
    if (pageCount < 0) {
        Box(Modifier.fillMaxSize().background(Color(0xFF202020)), contentAlignment = Alignment.Center) {
            Text("This PDF could not be rendered.", color = VoltTextMuted)
        }
    } else bitmap?.let {
        ZoomableBitmapSurface(
            bitmap = it,
            contentDescription = "PDF page ${currentPage + 1}",
            fullscreen = fullscreen,
            onToggleFullscreen = onToggleFullscreen,
            toolbar = { reset, zoomIn, _ ->
                IconButton(onClick = { if (currentPage > 0) currentPage -= 1 }) {
                    Icon(Icons.Default.NavigateBefore, "Previous page", tint = if (currentPage > 0) VoltGreen else VoltTextMuted)
                }
                Text(
                    "${currentPage + 1} / $pageCount",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { if (currentPage < pageCount - 1) currentPage += 1 }) {
                    Icon(Icons.Default.NavigateNext, "Next page", tint = if (currentPage < pageCount - 1) VoltGreen else VoltTextMuted)
                }
                IconButton(onClick = reset) {
                    Icon(Icons.Default.Replay, "Reset page zoom", tint = VoltGreen)
                }
                IconButton(onClick = zoomIn) {
                    Icon(Icons.Default.ZoomIn, "Zoom page", tint = VoltGreen)
                }
            },
        )
    } ?: Box(Modifier.fillMaxSize().background(Color(0xFF202020)), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = VoltGreen, strokeWidth = 2.dp)
    }
}

@Composable
private fun TextViewer(file: File, fullscreen: Boolean, onToggleFullscreen: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var text by remember(file) { mutableStateOf<String?>(null) }
    var readError by remember(file) { mutableStateOf<String?>(null) }
    var searchOpen by remember(file) { mutableStateOf(false) }
    var searchQuery by remember(file) { mutableStateOf("") }
    var searchIndex by remember(file) { mutableIntStateOf(0) }
    var fontSize by remember(file) { mutableFloatStateOf(13f) }
    var wrapText by remember(file) { mutableStateOf(true) }
    LaunchedEffect(searchQuery, text) {
        searchIndex = 0
    }
    LaunchedEffect(file) {
        runCatching {
            withContext(Dispatchers.IO) {
                check(file.length() <= MAX_TEXT_VIEW_BYTES) {
                    "This text file is larger than the 8 MB interactive preview limit. Use Open with to view the complete file."
                }
                file.inputStream().bufferedReader(Charsets.UTF_8).use { it.readText() }
            }
        }.onSuccess { contents ->
            text = contents
        }.onFailure {
            readError = "This text file could not be read."
        }
    }
    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("TEXT / CODE", color = VoltGreen, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.6.sp)
            Row(
                modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { searchOpen = !searchOpen }) {
                    Icon(Icons.Default.Search, "Search text", tint = VoltGreen)
                }
                IconButton(onClick = { fontSize = (fontSize - 1f).coerceAtLeast(9f) }) {
                    Icon(Icons.Default.ZoomOut, "Decrease text size", tint = Color.White)
                }
                IconButton(onClick = { fontSize = (fontSize + 1f).coerceAtMost(24f) }) {
                    Icon(Icons.Default.ZoomIn, "Increase text size", tint = Color.White)
                }
                IconButton(onClick = { wrapText = !wrapText }) {
                    Icon(Icons.Default.WrapText, "Toggle word wrap", tint = if (wrapText) VoltGreen else VoltTextMuted)
                }
                IconButton(
                    onClick = {
                        text?.let { contents ->
                            clipboard.setText(AnnotatedString(contents))
                            Toast.makeText(context, "Copied full text", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = text != null,
                ) {
                    Icon(Icons.Default.ContentCopy, "Copy all text", tint = VoltGreen)
                }
                IconButton(onClick = { openFileWith(context, file, "text/plain") }) {
                    Icon(Icons.Default.Share, "Open text with another app", tint = VoltGreen)
                }
                IconButton(onClick = onToggleFullscreen) {
                    Icon(
                        if (fullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        "Toggle full screen",
                        tint = VoltGreen,
                    )
                }
            }
        }
        if (searchOpen) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                singleLine = true,
                placeholder = { Text("Find in file", color = VoltTextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = VoltGreen) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, "Clear search", tint = VoltTextMuted) }
                    }
                },
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VoltGreen,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = VoltGreen,
                ),
            )
            if (searchQuery.isNotBlank() && text != null) {
                val matches = findTextMatches(text!!, searchQuery)
                val matchCount = matches.size
                Text(
                    if (matchCount == 0) "No matches" else "$matchCount match${if (matchCount == 1) "" else "es"}",
                    color = if (matchCount == 0) Color(0xFFFF8A80) else VoltGreen,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                )
                if (matchCount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        IconButton(onClick = { searchIndex = (searchIndex - 1 + matchCount) % matchCount }) {
                            Icon(Icons.Default.NavigateBefore, "Previous match", tint = VoltGreen)
                        }
                        Text("${searchIndex + 1} / $matchCount", color = VoltTextMuted, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterVertically))
                        IconButton(onClick = { searchIndex = (searchIndex + 1) % matchCount }) {
                            Icon(Icons.Default.NavigateNext, "Next match", tint = VoltGreen)
                        }
                    }
                }
            }
        }
        when {
            readError != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(readError!!, color = VoltTextMuted)
                }
            }
            text == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VoltGreen, strokeWidth = 2.dp)
                }
            }
            else -> {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 22.dp),
                    factory = { viewContext ->
                        TextView(viewContext).apply {
                            setTextColor(android.graphics.Color.rgb(230, 245, 236))
                            setBackgroundColor(android.graphics.Color.rgb(10, 10, 10))
                            typeface = android.graphics.Typeface.MONOSPACE
                            textSize = fontSize
                            setLineSpacing(0f, 1.62f)
                            setPadding(0, 0, 0, (22 * resources.displayMetrics.density).toInt())
                            gravity = android.view.Gravity.TOP or android.view.Gravity.START
                            isVerticalScrollBarEnabled = true
                            isFocusableInTouchMode = true
                            movementMethod = ScrollingMovementMethod.getInstance()
                            setTextIsSelectable(true)
                            setHorizontallyScrolling(!wrapText)
                            setCustomSelectionActionModeCallback(
                                createUrlSelectionActionModeCallback(viewContext, this),
                            )
                        }
                    },
                    update = { textView ->
                        textView.textSize = fontSize
                        textView.setHorizontallyScrolling(!wrapText)
                        if (textView.text.toString() != text!!) {
                            textView.text = SpannableString(text!!)
                        }
                        val matches = findTextMatches(text!!, searchQuery)
                        val matchStart = matches.getOrNull(searchIndex)
                        matchStart?.let { start ->
                            textView.post {
                                textView.requestFocus()
                                (textView.text as? android.text.Spannable)?.let { spannable ->
                                    Selection.setSelection(spannable, start, start + searchQuery.length)
                                }
                                textView.layout?.let { layout ->
                                    textView.scrollTo(0, layout.getLineTop(layout.getLineForOffset(start)))
                                }
                            }
                        }
                    },
                )
            }
        }
    }
}

private const val ChromeStablePackage = "com.android.chrome"
private const val ChromeIncognitoExtra =
    "com.google.android.apps.chrome.EXTRA_OPEN_NEW_INCOGNITO_TAB"
private const val OpenInChromeIncognitoActionId = 0x564f4c54

private fun createUrlSelectionActionModeCallback(
    context: android.content.Context,
    textView: TextView,
): ActionMode.Callback =
    object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            ensureIncognitoMenuItem(menu, textView)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            ensureIncognitoMenuItem(menu, textView)
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            if (item.itemId != OpenInChromeIncognitoActionId) return false

            val url = textView.selectedHttpUrl() ?: return false
            openInChromeIncognito(context, url)
            mode.finish()
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) = Unit
    }

private fun ensureIncognitoMenuItem(menu: Menu, textView: TextView) {
    val item = menu.findItem(OpenInChromeIncognitoActionId)
        ?: menu.add(Menu.NONE, OpenInChromeIncognitoActionId, Menu.NONE, "Open in Chrome Incognito").apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }
    item.isVisible = textView.selectedHttpUrl() != null
}

private fun TextView.selectedHttpUrl(): String? {
    val start = selectionStart
    val end = selectionEnd
    if (start < 0 || end <= start || end > text.length) return null

    val selected = text.subSequence(start, end).toString()
    if (selected.isEmpty() || selected != selected.trim()) return null
    if (selected.any { it.isWhitespace() || it.isISOControl() }) return null

    val uri = runCatching { Uri.parse(selected) }.getOrNull() ?: return null
    val scheme = uri.scheme?.lowercase() ?: return null
    if (scheme != "http" && scheme != "https") return null
    if (uri.host.isNullOrBlank()) return null
    return selected
}

private fun openInChromeIncognito(context: android.content.Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(url)
        setPackage(ChromeStablePackage)
        putExtra(ChromeIncognitoExtra, true)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    if (intent.resolveActivity(context.packageManager) == null) {
        Toast.makeText(context, "Google Chrome is not installed", Toast.LENGTH_SHORT).show()
        return
    }
    runCatching {
        context.startActivity(intent)
    }.onFailure {
        Toast.makeText(context, "Could not open Google Chrome", Toast.LENGTH_SHORT).show()
    }
}

private fun openFileWith(context: android.content.Context, file: File, mimeType: String) {
    val uri = runCatching {
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }.getOrNull() ?: run {
        Toast.makeText(context, "This file could not be shared", Toast.LENGTH_SHORT).show()
        return
    }
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType.ifBlank { "application/octet-stream" })
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching {
        context.startActivity(Intent.createChooser(intent, "Open with"))
    }.onFailure {
        Toast.makeText(context, "No compatible app was found", Toast.LENGTH_SHORT).show()
    }
}

private fun prepareVaultShareUri(
    context: MainActivity,
    vault: VaultRepository,
    file: VaultFile,
): Result<Uri> = runCatching {
    val source = vault.storedFile(file)
    check(source.isFile) { "The vault file is no longer available." }
    val shareDirectory = File(context.cacheDir, "voltshare-share-${UUID.randomUUID()}").apply {
        check(mkdirs() || isDirectory) { "Could not prepare the share file." }
    }
    val shareFile = File(shareDirectory, safeFileName(file.name))
    source.inputStream().use { input ->
        shareFile.outputStream().use { output -> input.copyTo(output, 1024 * 1024) }
    }
    check(shareFile.length() == source.length()) { "The share copy was incomplete." }
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", shareFile)
}

private fun launchShareSheet(context: MainActivity, file: VaultFile, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = file.mimeType.ifBlank { shareMimeFromName(file.name) }
        putExtra(Intent.EXTRA_STREAM, uri)
        clipData = ClipData.newRawUri(file.name, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching {
        context.startActivity(Intent.createChooser(intent, "Share ${file.name}"))
    }.onFailure {
        Toast.makeText(context, "No compatible app was found", Toast.LENGTH_SHORT).show()
    }
}

private const val MAX_TEXT_VIEW_BYTES = 8 * 1024 * 1024

private fun findTextMatches(text: String, query: String): List<Int> {
    if (query.isBlank()) return emptyList()
    val matches = mutableListOf<Int>()
    var cursor = 0
    while (cursor <= text.length - query.length) {
        val match = text.indexOf(query, startIndex = cursor, ignoreCase = true)
        if (match < 0) break
        matches += match
        cursor = (match + query.length).coerceAtLeast(match + 1)
    }
    return matches
}

@Composable
private fun InstallerViewer(
    activity: MainActivity,
    file: File,
    vaultFile: VaultFile,
    fullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
) {
    val installResult = remember { mutableStateOf<String?>(null) }
    Box(Modifier.fillMaxSize().background(VoltBlack)) {
        Column(
            Modifier.align(Alignment.Center).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier.size(112.dp).background(VoltGreen.copy(alpha = 0.12f), CircleShape)
                    .border(1.dp, VoltGreen.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Bolt, null, tint = VoltGreen, modifier = Modifier.size(52.dp))
            }
            Spacer(Modifier.height(22.dp))
            Text("INSTALLER PACKAGE", color = VoltGreen, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp)
            Spacer(Modifier.height(8.dp))
            Text(vaultFile.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(
                "The package stays private until you approve the Android installation.",
                color = VoltTextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(Modifier.height(26.dp))
            GlowButton("Install package", Icons.Default.ArrowDownward, onClick = {
                activity.installPreparedPackage(file, vaultFile.name)
                installResult.value = "Installation started"
            })
            installResult.value?.let { Text(it, color = VoltGreen, modifier = Modifier.padding(top = 16.dp)) }
        }
        IconButton(
            onClick = onToggleFullscreen,
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
        ) {
            Icon(
                if (fullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                "Toggle full screen",
                tint = VoltGreen,
            )
        }
    }
}

@Composable
private fun GenericViewer(
    activity: MainActivity,
    file: File,
    vaultFile: VaultFile,
    fullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
) {
    val context = LocalContext.current
    val mimeType = vaultFile.mimeType.ifBlank { "application/octet-stream" }
    var header by remember(file) { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(file) {
        header = withContext(Dispatchers.IO) {
            runCatching {
                file.inputStream().use { input ->
                    val bytes = ByteArray(32)
                    val count = input.read(bytes)
                    if (count <= 0) null else bytes.copyOf(count)
                }
            }.getOrNull()
        }
    }
    Box(Modifier.fillMaxSize().background(VoltBlack)) {
        Column(
            Modifier.fillMaxWidth().align(Alignment.Center).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier.size(112.dp).background(VoltGreen.copy(alpha = 0.1f), CircleShape)
                    .border(1.dp, VoltGreen.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Description, null, tint = VoltGreen, modifier = Modifier.size(52.dp))
            }
            Spacer(Modifier.height(22.dp))
            Text("UNIVERSAL FILE VIEWER", color = VoltGreen, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp)
            Spacer(Modifier.height(8.dp))
            Text(vaultFile.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.height(18.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = VoltSurfaceRaised,
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ViewerMetadataRow("Type", mimeType)
                    ViewerMetadataRow("Size", formatReadableFileSize(file.length()))
                    ViewerMetadataRow("Extension", vaultFile.name.substringAfterLast('.', "unknown").uppercase(Locale.US))
                    ViewerMetadataRow("Storage", "Private VoltShare vault")
                    ViewerMetadataRow("Access", "Read-only preview")
                }
            }
            header?.let { bytes ->
                Spacer(Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black.copy(alpha = 0.32f),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text(
                        "Header preview\n${bytes.joinToString(" ") { byte -> "%02X".format(Locale.US, byte) }}",
                        color = VoltTextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { openFileWith(context, file, mimeType) },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VoltGreen),
                    border = BorderStroke(1.dp, VoltGreen.copy(alpha = 0.55f)),
                ) {
                    Icon(Icons.Default.Share, "Open with", modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("Open with")
                }
                IconButton(onClick = onToggleFullscreen) {
                    Icon(
                        if (fullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        "Toggle full screen",
                        tint = VoltGreen,
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewerMetadataRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = VoltTextMuted, fontSize = 12.sp)
        Spacer(Modifier.width(12.dp))
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 2)
    }
}

@Composable
private fun ViewerInfoDialog(
    file: VaultFile,
    prepared: File?,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VoltSurfaceRaised,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = VoltGreen, modifier = Modifier.size(21.dp))
                Spacer(Modifier.width(9.dp))
                Text("File information", color = Color.White)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(file.name, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 2)
                ViewerMetadataRow("Type", file.mimeType.ifBlank { "Unknown" })
                ViewerMetadataRow("Size", formatReadableFileSize(file.sizeBytes))
                ViewerMetadataRow("Location", file.folderPath)
                ViewerMetadataRow("Preview", if (prepared?.isFile == true) "Ready" else "Unavailable")
                ViewerMetadataRow("Access", if (file.locked) "Lock protected" else "Unlocked")
                file.contentHash?.let { ViewerMetadataRow("SHA-256", it.take(16) + "…") }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = VoltGreen)) {
                Text("Done")
            }
        },
    )
}

@Composable
private fun NewFolderDialog(
    title: String = "Create private folder",
    confirmLabel: String = "Create",
    initialName: String = "",
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VoltSurfaceRaised,
        title = { Text(title, color = Color.White) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Folder name") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VoltGreen,
                    focusedLabelColor = VoltGreen,
                    cursorColor = VoltGreen,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.16f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                ),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name) },
                enabled = name.trim().isNotEmpty(),
                colors = ButtonDefaults.textButtonColors(contentColor = VoltGreen),
            ) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = VoltTextMuted)) { Text("Cancel") }
        },
    )
}

@Composable
private fun SortDialog(selected: VaultSort, onDismiss: () -> Unit, onSelected: (VaultSort) -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VoltSurfaceRaised,
        title = { Text("Sort this folder", color = Color.White) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                VaultSort.entries.forEach { option ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onSelected(option) },
                        color = if (option == selected) VoltGreen.copy(alpha = 0.14f) else Color.Transparent,
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp)) {
                            Box(
                                modifier = Modifier.size(18.dp).border(1.dp, if (option == selected) VoltGreen else VoltTextMuted, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (option == selected) Box(Modifier.size(8.dp).background(VoltGreen, CircleShape))
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(option.label, color = if (option == selected) VoltGreen else Color.White)
                        }
                    }
                }
            }
        },
        confirmButton = {},
    )
}

@Composable
private fun MoveFileDialog(
    file: VaultFile,
    folders: List<String>,
    onDismiss: () -> Unit,
    onMove: (String) -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VoltSurfaceRaised,
        title = { Text("Move ${file.name}", color = Color.White, maxLines = 1) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                folders.forEach { folder ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onMove(folder) },
                        color = if (folder == file.folderPath) VoltGreen.copy(alpha = 0.14f) else Color.Transparent,
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                            Icon(Icons.Default.Folder, null, tint = if (folder == file.folderPath) VoltGreen else VoltTextMuted, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(if (folder == "/") "All files" else folder, color = Color.White)
                            if (folder == file.folderPath) {
                                Spacer(Modifier.weight(1f))
                                Icon(Icons.Default.Check, null, tint = VoltGreen, modifier = Modifier.size(17.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
    )
}

@Composable
private fun MoveFilesDialog(
    count: Int,
    folders: List<String>,
    onDismiss: () -> Unit,
    onMove: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VoltSurfaceRaised,
        title = { Text("Move $count selected", color = Color.White) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                folders.forEach { folder ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onMove(folder) },
                        color = Color.Transparent,
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 13.dp),
                        ) {
                            Icon(Icons.Default.Folder, null, tint = VoltGreen, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(if (folder == "/") "Root" else folder, color = Color.White)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = VoltTextMuted)) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun FileActionDialog(
    file: VaultFile,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onToggleLock: () -> Unit,
    onSaveToDevice: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit,
    onSelect: () -> Unit,
    onShare: () -> Unit,
) {
    val actions = listOf(
        FileActionTileSpec(Icons.Default.Edit, "Rename", onRename),
        FileActionTileSpec(
            if (file.locked) Icons.Default.LockOpen else Icons.Default.Lock,
            if (file.locked) "Unlock" else "Lock",
            onToggleLock,
        ),
        FileActionTileSpec(Icons.Default.DriveFileMove, "Move", onMove),
        FileActionTileSpec(Icons.Default.FolderOpen, "Save", onSaveToDevice),
        FileActionTileSpec(Icons.Default.SelectAll, "Select", onSelect),
        FileActionTileSpec(Icons.Default.Share, "Share", onShare),
        FileActionTileSpec(Icons.Default.Delete, "Delete", onDelete, destructive = true),
    )
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF151B19),
            border = BorderStroke(1.dp, VoltGreen.copy(alpha = 0.38f)),
            shadowElevation = 30.dp,
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 15.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.size(50.dp).background(VoltGreen.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(fileIcon(file), null, tint = VoltGreen, modifier = Modifier.size(25.dp))
                    }
                    Spacer(Modifier.width(13.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("FILE ACTIONS", color = VoltGreen, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.7.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(file.name, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black, maxLines = 1)
                        Text(file.folderPath, color = VoltTextMuted, fontSize = 10.sp, maxLines = 1)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Close", tint = VoltTextMuted) }
                }
                Spacer(Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = VoltGreen.copy(alpha = 0.085f),
                    border = BorderStroke(1.dp, VoltGreen.copy(alpha = 0.24f)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = VoltGreen,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(9.dp))
                        Column {
                            Text(
                                "SIZE",
                                color = VoltTextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.3.sp,
                            )
                            Text(
                                formatReadableFileSize(file.sizeBytes),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(13.dp))
                actions.chunked(3).forEachIndexed { rowIndex, row ->
                    if (rowIndex > 0) Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        row.forEach { action ->
                            FileActionTile(action, Modifier.weight(1f))
                        }
                        repeat(3 - row.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FileActionTile(
    action: FileActionTileSpec,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(72.dp).clickable(onClick = action.onClick),
        color = if (action.destructive) Color(0xFFFF6B6B).copy(alpha = 0.08f) else Color.White.copy(alpha = 0.045f),
        shape = RoundedCornerShape(17.dp),
        border = BorderStroke(
            1.dp,
            if (action.destructive) Color(0xFFFF8A80).copy(alpha = 0.28f) else Color.White.copy(alpha = 0.07f),
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 5.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    action.icon,
                    contentDescription = action.title,
                    tint = if (action.destructive) Color(0xFFFF8A80) else VoltGreen,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    action.title,
                    color = if (action.destructive) Color(0xFFFF8A80) else Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun SecretDialog(
    title: String,
    type: LockType,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Boolean,
) {
    LockVerificationDialog(
        title = title,
        type = type,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@Composable
private fun PatternPad(pattern: List<Int>, onChange: (List<Int>) -> Unit) {
    val density = LocalDensity.current
    val nodeRadius = with(density) { 25.dp.toPx() }
    val hitRadius = with(density) { 38.dp.toPx() }
    val currentPattern by rememberUpdatedState(pattern)
    val currentOnChange by rememberUpdatedState(onChange)
    var activeLineEnd by remember { mutableStateOf<Offset?>(null) }
    var isDrawing by remember { mutableStateOf(false) }
    val transition = rememberInfiniteTransition(label = "pattern-pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "pattern-pulse-scale",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(horizontal = 12.dp),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .shadow(18.dp, RoundedCornerShape(28.dp), spotColor = VoltGreen.copy(alpha = 0.22f))
                .background(
                    Brush.radialGradient(
                        listOf(VoltGreen.copy(alpha = 0.12f), Color.White.copy(alpha = 0.025f), Color.Transparent),
                    ),
                    RoundedCornerShape(28.dp),
                )
                .border(1.dp, VoltGreen.copy(alpha = 0.24f), RoundedCornerShape(28.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { position ->
                            val point = patternPointAt(
                                position,
                                androidx.compose.ui.geometry.Size(size.width.toFloat(), size.height.toFloat()),
                                hitRadius,
                            )
                            if (point != null) {
                                isDrawing = true
                                activeLineEnd = position
                                currentOnChange(listOf(point))
                            }
                        },
                        onDragCancel = {
                            isDrawing = false
                            activeLineEnd = null
                        },
                        onDragEnd = {
                            isDrawing = false
                            activeLineEnd = null
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            if (!isDrawing) return@detectDragGestures
                            activeLineEnd = change.position
                            val point = patternPointAt(
                                change.position,
                                androidx.compose.ui.geometry.Size(size.width.toFloat(), size.height.toFloat()),
                                hitRadius,
                            ) ?: return@detectDragGestures
                            val next = appendPatternPoint(currentPattern, point)
                            if (next != currentPattern) currentOnChange(next)
                        },
                    )
                },
        ) {
            val centers = patternCenters(size.width, size.height)
            val selectedCenters = currentPattern.mapNotNull { centers.getOrNull(it) }
            selectedCenters.zipWithNext().forEach { (start, end) ->
                drawLine(
                    color = VoltGreen.copy(alpha = 0.18f),
                    start = start,
                    end = end,
                    strokeWidth = with(density) { 18.dp.toPx() },
                )
                drawLine(
                    color = VoltGreen,
                    start = start,
                    end = end,
                    strokeWidth = with(density) { 7.dp.toPx() },
                )
            }
            if (isDrawing && selectedCenters.isNotEmpty() && activeLineEnd != null) {
                drawLine(
                    color = VoltGreen.copy(alpha = 0.6f),
                    start = selectedCenters.last(),
                    end = activeLineEnd!!,
                    strokeWidth = with(density) { 6.dp.toPx() },
                )
            }
            centers.forEachIndexed { index, center ->
                val selected = index in currentPattern
                if (selected) {
                    drawCircle(
                        color = VoltGreen.copy(alpha = 0.12f),
                        radius = with(density) { 43.dp.toPx() } * pulse,
                        center = center,
                    )
                    drawCircle(
                        color = VoltGreen.copy(alpha = 0.22f),
                        radius = with(density) { 34.dp.toPx() },
                        center = center,
                    )
                    drawCircle(color = VoltGreen, radius = nodeRadius, center = center)
                    drawCircle(color = Color.Black, radius = with(density) { 8.dp.toPx() }, center = center)
                } else {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        radius = nodeRadius,
                        center = center,
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.24f),
                        radius = nodeRadius,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(with(density) { 1.dp.toPx() }),
                    )
                    drawCircle(
                        color = VoltGreen.copy(alpha = 0.24f),
                        radius = with(density) { 5.dp.toPx() },
                        center = center,
                    )
                }
            }
        }
    }
}

private fun patternCenters(width: Float, height: Float): List<Offset> {
    val side = minOf(width, height)
    val inset = side * 0.19f
    val gap = (side - inset * 2f) / 2f
    val startX = (width - side) / 2f + inset
    val startY = (height - side) / 2f + inset
    return buildList {
        repeat(3) { row ->
            repeat(3) { column ->
                add(Offset(startX + gap * column, startY + gap * row))
            }
        }
    }
}

private fun patternPointAt(position: Offset, size: androidx.compose.ui.geometry.Size, hitRadius: Float): Int? {
    return patternCenters(size.width, size.height)
        .mapIndexed { index, center -> index to (center - position).getDistance() }
        .minByOrNull { it.second }
        ?.takeIf { it.second <= hitRadius }
        ?.first
}

private fun appendPatternPoint(pattern: List<Int>, point: Int): List<Int> {
    if (point in pattern) return pattern
    val last = pattern.lastOrNull() ?: return listOf(point)
    val middle = when (setOf(last, point)) {
        setOf(0, 2) -> 1
        setOf(0, 6) -> 3
        setOf(2, 8) -> 5
        setOf(6, 8) -> 7
        setOf(0, 8) -> 4
        setOf(2, 6) -> 4
        else -> null
    }
    return if (middle != null && middle !in pattern) pattern + middle + point else pattern + point
}

@Composable
private fun SecureField(value: String, label: String, keyboardType: KeyboardType, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = PasswordVisualTransformation(),
        shape = RoundedCornerShape(18.dp),
        leadingIcon = { Icon(Icons.Default.Lock, null, tint = VoltGreen, modifier = Modifier.size(19.dp)) },
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Black.copy(alpha = 0.26f),
            unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
            focusedBorderColor = VoltGreen,
            focusedLabelColor = VoltGreen,
            cursorColor = VoltGreen,
            unfocusedBorderColor = Color.White.copy(alpha = 0.16f),
            unfocusedLabelColor = VoltTextMuted,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
        ),
    )
}

@Composable
private fun FloatingSegmentedControl(items: List<String>, selectedIndex: Int, onSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.045f), RoundedCornerShape(20.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .shadow(if (selected) 10.dp else 0.dp, RoundedCornerShape(15.dp), spotColor = VoltGreen.copy(alpha = 0.4f))
                    .clickable { onSelected(index) },
                shape = RoundedCornerShape(15.dp),
                color = if (selected) VoltGreen else Color.Transparent,
                border = if (selected) BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)) else null,
            ) {
                Text(
                    item,
                    color = if (selected) Color.Black else VoltTextMuted,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(vertical = 12.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun GlowButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val elevation by androidx.compose.animation.core.animateDpAsState(if (text.isNotEmpty()) 12.dp else 0.dp, label = "button-glow")
    Button(
        onClick = onClick,
        modifier = modifier
            .height(54.dp)
            .shadow(elevation, RoundedCornerShape(20.dp), ambientColor = VoltGreen.copy(alpha = 0.18f), spotColor = VoltGreen.copy(alpha = 0.55f))
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = VoltGreen, contentColor = Color.Black),
        contentPadding = PaddingValues(horizontal = 18.dp),
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PremiumSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    val thumbOffset by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (checked) 27.dp else 4.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "premium-switch-thumb",
    )
    Box(
        modifier = Modifier
            .width(58.dp)
            .height(34.dp)
            .clip(RoundedCornerShape(18.dp))
            .graphicsLayer { alpha = if (enabled) 1f else 0.42f }
            .background(if (checked) VoltGreen.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.10f))
            .border(1.dp, if (checked) VoltGreen.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.16f), RoundedCornerShape(18.dp))
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(26.dp)
                .shadow(if (checked) 12.dp else 2.dp, CircleShape, spotColor = VoltGreen.copy(alpha = 0.58f))
                .background(if (checked) VoltGreen else Color.White.copy(alpha = 0.72f), CircleShape),
        )
    }
}

@Composable
private fun GlassCard(modifier: Modifier, accent: Color, padding: androidx.compose.ui.unit.Dp = 24.dp, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .shadow(34.dp, RoundedCornerShape(26.dp), ambientColor = Color.Black.copy(alpha = 0.92f), spotColor = Color.Black.copy(alpha = 0.9f))
            .shadow(13.dp, RoundedCornerShape(26.dp), ambientColor = accent.copy(alpha = 0.08f), spotColor = accent.copy(alpha = 0.26f))
            .background(
                Brush.verticalGradient(
                    listOf(accent.copy(alpha = 0.13f), VoltSurface, VoltSurface),
                ),
                RoundedCornerShape(26.dp),
            )
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(26.dp))
            .padding(padding),
        content = content,
    )
}

private fun pendingShareFromUri(context: MainActivity, uri: Uri): PendingShare? {
    var displayName: String? = null
    var sizeBytes: Long? = null
    runCatching {
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                displayName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                sizeBytes = cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE)).takeIf { it >= 0L }
            }
        }
    }
    val name = safeFileName(displayName ?: uri.lastPathSegment?.substringAfterLast('/') ?: "shared-file")
    val descriptorLength = runCatching {
        context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
    }.getOrDefault(-1L)
    val size = sizeBytes ?: descriptorLength.takeIf { it >= 0L } ?: 0L
    runCatching {
        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return PendingShare(
        id = "uri:$uri",
        name = name,
        mimeType = context.contentResolver.getType(uri) ?: shareMimeFromName(name),
        sizeBytes = size,
        source = PendingShareSource.UriSource(uri),
    )
}

private fun pendingShareFromFile(file: File, displayName: String = file.name, mimeType: String = shareMimeFromName(displayName)): PendingShare? {
    if (!file.isFile) return null
    return PendingShare(
        id = "file:${file.absolutePath}",
        name = safeFileName(displayName),
        mimeType = mimeType,
        sizeBytes = file.length(),
        source = PendingShareSource.FileSource(file),
    )
}

private fun pendingShareFromVault(vault: VaultRepository, file: VaultFile): PendingShare? {
    if (vault.storedFile(file).isFile.not()) return null
    return PendingShare(
        id = "vault:${file.id}",
        name = file.name,
        mimeType = file.mimeType,
        sizeBytes = file.sizeBytes,
        source = PendingShareSource.VaultSource(file),
    )
}

private fun copyVaultFileToTree(
    context: MainActivity,
    vault: VaultRepository,
    file: VaultFile,
    treeUri: Uri,
): Result<Unit> = runCatching {
    val source = vault.storedFile(file)
    check(source.isFile) { "The vault file is no longer available." }
    val tree = DocumentFile.fromTreeUri(context, treeUri)
    check(tree?.isDirectory == true) { "The selected storage folder is not available." }

    val existing = tree.findFile(file.name)?.takeUnless { it.isDirectory }
    val destination = existing ?: tree.createFile(
        file.mimeType.ifBlank { "application/octet-stream" },
        file.name,
    )
    check(destination != null) { "The selected storage provider could not create the file." }
    val output = runCatching {
        context.contentResolver.openOutputStream(destination.uri, "wt")
    }.getOrNull() ?: context.contentResolver.openOutputStream(destination.uri, "w")
    check(output != null) { "The selected storage provider could not open the file." }
    output.use { destinationStream ->
        source.inputStream().use { sourceStream -> sourceStream.copyTo(destinationStream) }
    }
}

private fun createTextShare(context: MainActivity, text: String): PendingShare? {
    val file = File(context.cacheDir, "voltshare-note-${System.currentTimeMillis()}.txt")
    return runCatching {
        file.writeText(text, Charsets.UTF_8)
        pendingShareFromFile(file, file.name, "text/plain")
    }.getOrElse {
        file.delete()
        null
    }
}

private fun commitPendingShare(context: MainActivity, vault: VaultRepository, pending: PendingShare): Boolean {
    val sentFile = runCatching {
        when (val source = pending.source) {
            is PendingShareSource.UriSource -> vault.importUri(source.uri, vault.primaryFolder())
            is PendingShareSource.FileSource -> vault.importGeneratedFile(source.file, pending.name, pending.mimeType, vault.primaryFolder())
            is PendingShareSource.VaultSource -> source.file
        }
    }.getOrNull() ?: return false
    vault.markSent(sentFile)
    disposePendingShare(context, pending)
    return true
}

private fun disposePendingShare(context: MainActivity, pending: PendingShare) {
    when (val source = pending.source) {
        is PendingShareSource.UriSource -> runCatching {
            context.contentResolver.releasePersistableUriPermission(
                source.uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
        is PendingShareSource.FileSource -> source.file.delete()
        is PendingShareSource.VaultSource -> Unit
    }
}

private fun shareMimeFromName(name: String): String = when (name.substringAfterLast('.', "").lowercase()) {
    "jpg", "jpeg", "png", "webp", "gif", "heic" -> "image/*"
    "mp4", "mkv", "webm", "mov", "avi" -> "video/*"
    "mp3", "wav", "m4a", "flac" -> "audio/*"
    "pdf" -> "application/pdf"
    "apk", "xapk", "apks" -> "application/vnd.android.package-archive"
    "zip" -> "application/zip"
    "txt", "md", "json", "xml", "csv", "log", "kt", "java", "js", "ts", "html", "css" -> "text/plain"
    else -> "application/octet-stream"
}

private fun createFolderArchive(context: MainActivity, treeUri: Uri): File? {
    val root = DocumentFile.fromTreeUri(context, treeUri) ?: return null
    val rootName = safeFileName(root.name ?: "shared-folder")
    val output = File(context.cacheDir, "$rootName-${System.currentTimeMillis()}.zip")
    return runCatching {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(output))).use { zip ->
            root.listFiles().forEach { child ->
                addDocumentToZip(context, child, "$rootName/${safeFileName(child.name ?: "item")}", zip)
            }
        }
        output
    }.getOrElse {
        output.delete()
        null
    }
}

private fun addDocumentToZip(
    context: MainActivity,
    document: DocumentFile,
    path: String,
    zip: ZipOutputStream,
) {
    if (document.isDirectory) {
        zip.putNextEntry(ZipEntry("$path/"))
        zip.closeEntry()
        document.listFiles().forEach { child ->
            addDocumentToZip(context, child, "$path/${safeFileName(child.name ?: "item")}", zip)
        }
    } else {
        zip.putNextEntry(ZipEntry(path))
        context.contentResolver.openInputStream(document.uri)?.use { input ->
            input.copyTo(zip, 1024 * 1024)
        } ?: error("Could not read ${document.name}")
        zip.closeEntry()
    }
}

private fun createInstalledAppPackage(context: MainActivity, app: InstalledAppChoice): File? {
    val extension = if (app.apkPaths.size > 1) "apks" else "apk"
    val output = File(context.cacheDir, "${safeFileName(app.label)}-${System.currentTimeMillis()}.$extension")
    return runCatching {
        if (app.apkPaths.size == 1) {
            File(app.apkPaths.first()).inputStream().use { input ->
                output.outputStream().use { out -> input.copyTo(out, 1024 * 1024) }
            }
        } else {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(output))).use { zip ->
                app.apkPaths.forEachIndexed { index, sourcePath ->
                    val source = File(sourcePath)
                    zip.putNextEntry(ZipEntry("${index.toString().padStart(2, '0')}-${source.name}"))
                    source.inputStream().use { input -> input.copyTo(zip, 1024 * 1024) }
                    zip.closeEntry()
                }
            }
        }
        output
    }.getOrElse {
        output.delete()
        null
    }
}

private fun safeFileName(value: String): String =
    value.replace(Regex("[^A-Za-z0-9._ -]"), "_").trim().take(80).ifBlank { "shared-item" }

private fun fileIcon(file: VaultFile): ImageVector = when {
    isImage(file) -> Icons.Default.Image
    isVideo(file) -> Icons.Default.VideoLibrary
    isText(file) -> Icons.Default.TextSnippet
    isInstallable(file) -> Icons.Default.Bolt
    else -> Icons.Default.Description
}

private fun sortVaultFiles(files: List<VaultFile>, sort: VaultSort): List<VaultFile> = when (sort) {
    VaultSort.CUSTOM -> files.sortedBy { it.order }
    VaultSort.NAME_ASC -> files.sortedBy { it.name.lowercase() }
    VaultSort.NAME_DESC -> files.sortedByDescending { it.name.lowercase() }
    VaultSort.NEWEST -> files.sortedByDescending { it.createdAt }
    VaultSort.OLDEST -> files.sortedBy { it.createdAt }
    VaultSort.LARGEST -> files.sortedByDescending { it.sizeBytes }
    VaultSort.SMALLEST -> files.sortedBy { it.sizeBytes }
    VaultSort.TYPE -> files.sortedWith(compareBy({ it.mimeType }, { it.name.lowercase() }))
}

private fun String.parentFolder(): String {
    if (this == "/") return "/"
    val parent = substringBeforeLast('/', "")
    return if (parent.isBlank()) "/" else parent
}

private fun isImage(file: VaultFile) = file.mimeType.startsWith("image") || file.name.isMediaExtension("jpg", "jpeg", "png", "webp", "gif", "heic")
private fun isVideo(file: VaultFile) = file.mimeType.startsWith("video") || file.name.isMediaExtension("mp4", "mkv", "webm", "mov", "avi")
private fun isAudio(file: VaultFile) = file.mimeType.startsWith("audio") || file.name.isMediaExtension("mp3", "wav", "m4a", "flac", "aac", "ogg")
private fun isPdf(file: VaultFile) = file.mimeType == "application/pdf" || file.name.endsWith(".pdf", true)
private fun isText(file: VaultFile): Boolean {
    val mimeType = file.mimeType.lowercase()
    return mimeType.startsWith("text/") ||
        mimeType in setOf(
            "application/json",
            "application/javascript",
            "application/ld+json",
            "application/xml",
            "application/rtf",
            "application/sql",
            "application/x-javascript",
            "application/x-sh",
            "application/x-yaml",
            "application/yaml",
            "image/svg+xml",
        ) ||
        file.name.isMediaExtension(
            "txt", "text", "md", "markdown", "json", "jsonl", "ndjson", "xml", "xsl", "xslt",
            "csv", "tsv", "log", "kt", "kts", "java", "js", "jsx", "mjs", "ts", "tsx",
            "html", "htm", "css", "scss", "sass", "less", "svg", "yaml", "yml", "toml",
            "ini", "conf", "config", "properties", "env", "sql", "sh", "bash", "zsh",
            "fish", "gradle", "groovy", "diff", "patch", "srt", "vtt", "tex", "graphql",
            "gql", "c", "h", "cc", "cpp", "cxx", "hpp", "cs", "swift", "go", "rs", "py",
            "rb", "php", "vue", "webmanifest",
        )
}
private fun isInstallable(file: VaultFile) = file.name.isMediaExtension("apk", "xapk", "apks")
private fun String.isMediaExtension(vararg extensions: String) = extensions.any { endsWith(".$it", ignoreCase = true) }
private fun formatSize(bytes: Long): String = Formatter.formatFileSize(null, bytes)

private fun formatDuration(milliseconds: Long): String {
    val totalSeconds = (milliseconds.coerceAtLeast(0L) / 1000L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

private fun formatReadableFileSize(bytes: Long): String {
    val safeBytes = bytes.coerceAtLeast(0L)
    val kilobyte = 1024.0
    val megabyte = kilobyte * 1024.0
    val gigabyte = megabyte * 1024.0
    val (value, unit) = when {
        safeBytes >= gigabyte -> safeBytes.toDouble() / gigabyte to "GB"
        safeBytes >= megabyte -> safeBytes.toDouble() / megabyte to "MB"
        else -> safeBytes.toDouble() / kilobyte to "KB"
    }
    val formatted = String.format(Locale.US, "%.2f", value)
        .trimEnd('0')
        .trimEnd('.')
    return "$formatted $unit"
}

private fun formatTransferSpeed(bytesPerSecond: Long): String =
    if (bytesPerSecond > 0L) "${formatSize(bytesPerSecond)}/s" else "Calculating…"

private fun formatTransferEta(seconds: Long?): String {
    if (seconds == null) return "Calculating…"
    if (seconds <= 0L) return "Complete"
    val hours = seconds / 3600L
    val minutes = (seconds % 3600L) / 60L
    val remainingSeconds = seconds % 60L
    return when {
        hours > 0L -> "${hours}h ${minutes}m"
        minutes > 0L -> "${minutes}m ${remainingSeconds}s"
        else -> "${remainingSeconds}s"
    }
}