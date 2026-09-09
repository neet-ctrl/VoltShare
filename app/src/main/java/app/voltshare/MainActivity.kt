package app.voltshare

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfRenderer
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.text.format.Formatter
import android.view.ViewGroup
import android.widget.ImageView
import android.media.MediaMetadataRetriever
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.fragment.app.FragmentActivity
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

private val VoltGreen = Color(0xFF00E676)
private val VoltBlack = Color(0xFF000000)
private val VoltSurface = Color(0xFF121212)
private val VoltSurfaceRaised = Color(0xFF1B1F1E)
private val VoltTextMuted = Color.White.copy(alpha = 0.52f)
private val VoltTeal = Color(0xFF00796B)

data class IncomingShare(
    val uris: List<Uri> = emptyList(),
    val text: String? = null,
)

data class InstalledAppChoice(
    val label: String,
    val packageName: String,
    val apkPaths: List<String>,
    val icon: Drawable? = null,
)

class MainActivity : FragmentActivity() {
    private lateinit var vault: VaultRepository
    private lateinit var lockManager: LockManager
    private lateinit var transfer: PeerTransferManager
    private var pendingIncomingShareState by mutableStateOf<IncomingShare?>(null)

    val pendingIncomingShare: IncomingShare?
        get() = pendingIncomingShareState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingIncomingShareState = intent.toIncomingShare()
        vault = VaultRepository(this)
        lockManager = LockManager(this)
        transfer = PeerTransferManager(this, vault)
        setContent {
            VoltShareTheme {
                VoltShareApp(this, vault, lockManager, transfer)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingIncomingShareState = intent.toIncomingShare()
    }

    fun consumePendingIncomingShare() {
        pendingIncomingShareState = null
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

    override fun onDestroy() {
        transfer.close()
        vault.clearViewCache()
        super.onDestroy()
    }
}

private fun Intent.toIncomingShare(): IncomingShare? {
    if (action != Intent.ACTION_SEND && action != Intent.ACTION_SEND_MULTIPLE) return null
    val uris = buildList {
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
    return IncomingShare(uris, text).takeIf { it.uris.isNotEmpty() || it.text != null }
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

@Composable
private fun VoltShareApp(
    activity: MainActivity,
    vault: VaultRepository,
    lockManager: LockManager,
    transfer: PeerTransferManager,
) {
    var configured by remember { mutableStateOf(lockManager.isConfigured()) }
    var unlocked by remember { mutableStateOf(!configured) }
    var tab by remember { mutableStateOf(AppTab.VAULT) }
    var viewerFile by remember { mutableStateOf<VaultFile?>(null) }
    var files by remember { mutableStateOf(vault.listFiles()) }
    var folders by remember { mutableStateOf(vault.listFolders()) }
    var currentFolder by remember { mutableStateOf("/") }
    var sortMode by remember { mutableStateOf(VaultSort.CUSTOM) }
    var fileToUnlock by remember { mutableStateOf<VaultFile?>(null) }
    var fileToMove by remember { mutableStateOf<VaultFile?>(null) }
    var showNewFolder by remember { mutableStateOf(false) }
    var showRenameFolder by remember { mutableStateOf(false) }
    var showSort by remember { mutableStateOf(false) }
    var importFolder by remember { mutableStateOf("/") }
    var pendingShares by remember { mutableStateOf<List<PendingShare>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val incomingShare = activity.pendingIncomingShare

    LaunchedEffect(transfer) {
        transfer.status.collect { status ->
            if (status.label.startsWith("Received and verified") || status.label.startsWith("Sent and verified")) {
                files = vault.listFiles()
                folders = vault.listFolders()
            }
        }
    }

    LaunchedEffect(configured, unlocked, incomingShare) {
        if (!configured || !unlocked || incomingShare == null) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            incomingShare.uris.forEach { uri ->
                runCatching {
                    activity.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                }
                vault.importUri(uri)
            }
            incomingShare.text?.let { text ->
                vault.createTextFile(text, "shared-text-${System.currentTimeMillis()}.txt")
            }
        }
        files = vault.listFiles()
        folders = vault.listFolders()
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
            onUnlock = { unlocked = true },
            onBiometric = { activity.authenticateWithBiometric { unlocked = true } },
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
                    viewerFile = target
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

    if (showRenameFolder && currentFolder != "/") {
        NewFolderDialog(
            title = "Rename private folder",
            confirmLabel = "Rename",
            initialName = currentFolder.substringAfterLast('/'),
            onDismiss = { showRenameFolder = false },
            onCreate = { name ->
                val renamed = vault.renameFolder(currentFolder, name)
                if (renamed != null) currentFolder = renamed
                folders = vault.listFolders()
                files = vault.listFiles()
                showRenameFolder = false
            },
        )
    }

    if (showSort) {
        SortDialog(
            selected = sortMode,
            onDismiss = { showSort = false },
            onSelected = {
                sortMode = it
                showSort = false
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
                        if (file.locked) fileToUnlock = file else viewerFile = file
                    },
                    onToggleLock = {
                        vault.toggleLocked(it)
                        files = vault.listFiles()
                    },
                    vault = vault,
                )

                AppTab.FILES -> FilesHome(
                    files = files,
                    folders = folders,
                    currentFolder = currentFolder,
                    sortMode = sortMode,
                    onImport = {
                        importFolder = currentFolder
                        picker.launch(arrayOf("*/*"))
                    },
                    onFolderSelected = { currentFolder = it },
                    onCreateFolder = { showNewFolder = true },
                    onRenameFolder = { showRenameFolder = true },
                    onDeleteFolder = { folder ->
                        vault.deleteFolder(folder)
                        folders = vault.listFolders()
                        currentFolder = "/"
                        files = vault.listFiles()
                    },
                    onSort = { showSort = true },
                    onOpen = { file ->
                        if (file.locked) fileToUnlock = file else viewerFile = file
                    },
                    onToggleLock = {
                        vault.toggleLocked(it)
                        files = vault.listFiles()
                    },
                    onMove = { fileToMove = it },
                    onReorder = { file, direction ->
                        vault.reorder(file, direction)
                        files = vault.listFiles()
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
                    onLockNow = { unlocked = false },
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
        description = "Every file is encrypted inside VoltShare’s app-private folder. Choose how you want to open the vault.",
    ) {
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
    onUnlock: () -> Unit,
    onBiometric: () -> Unit,
) {
    var secret by remember { mutableStateOf("") }
    var pattern by remember { mutableStateOf<List<Int>>(emptyList()) }
    var error by remember { mutableStateOf(false) }
    LockScaffold(
        eyebrow = "VAULT LOCKED",
        title = "Welcome back",
        description = "Your private files are still here. Unlock to continue.",
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .shadow(20.dp, CircleShape, ambientColor = VoltGreen.copy(alpha = 0.25f), spotColor = VoltGreen.copy(alpha = 0.18f))
                .background(VoltGreen.copy(alpha = 0.1f), CircleShape)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Lock, null, tint = VoltGreen, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(28.dp))
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
        AnimatedVisibility(error) {
            Text("That code does not unlock this vault.", color = Color(0xFFFF6B6B), modifier = Modifier.padding(top = 12.dp))
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
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onBiometric,
            modifier = Modifier.fillMaxWidth().height(54.dp).shadow(10.dp, RoundedCornerShape(20.dp), spotColor = VoltGreen.copy(alpha = 0.16f)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        ) {
            Icon(Icons.Default.Fingerprint, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text("Use fingerprint / device unlock")
        }
    }
}

@Composable
private fun LockScaffold(
    eyebrow: String,
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(color = VoltBlack, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bolt, null, tint = VoltGreen, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(eyebrow, color = VoltGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
            Spacer(Modifier.height(14.dp))
            Text(title, style = MaterialTheme.typography.headlineLarge, color = Color.White)
            Spacer(Modifier.height(12.dp))
            Text(description, color = VoltTextMuted, lineHeight = 22.sp)
            Spacer(Modifier.height(32.dp))
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
                    onMove = {},
                    onMoveUp = {},
                    onMoveDown = {},
                    allowReorder = false,
                    showFolderAction = false,
                )
            }
        }
        item {
            Text(
                "Files stay inside encrypted app-private storage",
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
    sortMode: VaultSort,
    onImport: () -> Unit,
    onFolderSelected: (String) -> Unit,
    onCreateFolder: () -> Unit,
    onRenameFolder: () -> Unit,
    onDeleteFolder: (String) -> Unit,
    onSort: () -> Unit,
    onOpen: (VaultFile) -> Unit,
    onToggleLock: (VaultFile) -> Unit,
    onMove: (VaultFile) -> Unit,
    onReorder: (VaultFile, Int) -> Unit,
    vault: VaultRepository,
) {
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf("All") }
    val filterOptions = listOf("All", "Sent", "Received")
    val visibleFiles = sortVaultFiles(
        files.filter {
            it.folderPath == currentFolder &&
                (searchQuery.isBlank() || it.name.contains(searchQuery.trim(), ignoreCase = true)) &&
                when (activeFilter) {
                    "Sent" -> it.transferDirection == TransferDirection.SENT
                    "Received" -> it.transferDirection == TransferDirection.RECEIVED
                    else -> true
                }
        },
        sortMode,
    )
    val childFolders = folders.filter { it.parentFolder() == currentFolder }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(VoltBlack),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("V O L T S H A R E", color = VoltGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Your private vault", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                }
                Box(
                    modifier = Modifier.size(46.dp).background(VoltGreen.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Bolt, null, tint = VoltGreen, modifier = Modifier.size(24.dp))
                }
            }
        }
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search files and folders") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = VoltGreen) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, "Clear search", tint = VoltTextMuted)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
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
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                filterOptions.forEach { option ->
                    FilterChip(
                        selected = activeFilter == option,
                        onClick = { activeFilter = option },
                        label = { Text(option) },
                        leadingIcon = if (activeFilter == option) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                        } else {
                            null
                        },
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VoltGreen,
                            selectedLabelColor = Color.Black,
                            selectedLeadingIconColor = Color.Black,
                            containerColor = VoltSurface,
                            labelColor = VoltTextMuted,
                        ),
                        border = androidx.compose.material3.FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = activeFilter == option,
                            borderColor = Color.White.copy(alpha = 0.12f),
                            selectedBorderColor = VoltGreen,
                        ),
                    )
                }
            }
        }
        item {
            VaultMetricCard(files)
        }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), accent = VoltTeal, padding = 16.dp) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    if (currentFolder != "/") {
                        IconButton(onClick = { onFolderSelected(currentFolder.parentFolder()) }) {
                            Icon(Icons.Default.ArrowBack, "Parent folder", tint = VoltGreen)
                        }
                    } else {
                        Icon(Icons.Default.Folder, null, tint = VoltGreen, modifier = Modifier.padding(12.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(if (currentFolder == "/") "All files" else currentFolder.substringAfterLast('/'), color = Color.White, fontWeight = FontWeight.Bold)
                        Text(if (currentFolder == "/") "Private root" else currentFolder, color = VoltTextMuted, fontSize = 12.sp)
                    }
                    TextButton(onClick = onCreateFolder, colors = ButtonDefaults.textButtonColors(contentColor = VoltGreen)) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Folder")
                    }
                    if (currentFolder != "/") {
                        TextButton(onClick = onRenameFolder, colors = ButtonDefaults.textButtonColors(contentColor = VoltGreen)) {
                            Text("Rename", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
        if (childFolders.isNotEmpty()) {
            item {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    childFolders.forEach { folder ->
                        Surface(
                            modifier = Modifier.width(150.dp).clickable { onFolderSelected(folder) },
                            shape = RoundedCornerShape(18.dp),
                            color = VoltSurface,
                            border = BorderStroke(1.dp, VoltGreen.copy(alpha = 0.22f)),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(14.dp)) {
                                Icon(Icons.Default.Folder, null, tint = VoltGreen, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(9.dp))
                                Text(folder.substringAfterLast('/'), color = Color.White, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                GlowButton(
                    text = "Import files",
                    icon = Icons.Default.Add,
                    onClick = onImport,
                    modifier = Modifier.weight(1f),
                )
                OutlinedButton(
                    onClick = { searchQuery = "" },
                    modifier = Modifier.weight(0.65f).height(54.dp).shadow(10.dp, RoundedCornerShape(20.dp), spotColor = VoltGreen.copy(alpha = 0.16f)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, VoltGreen.copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VoltGreen),
                ) {
                    Icon(Icons.Default.Search, null)
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Inside the vault", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${visibleFiles.size} items", color = VoltTextMuted, fontSize = 12.sp)
                    TextButton(onClick = onSort, colors = ButtonDefaults.textButtonColors(contentColor = VoltGreen)) {
                        Icon(Icons.Default.MoreVert, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(sortMode.label, fontSize = 11.sp)
                    }
                }
            }
        }
        if (visibleFiles.isEmpty()) {
            item { EmptyVaultCard(onImport) }
        } else {
            items(visibleFiles, key = { it.id }) { file ->
                FileRow(
                    file = file,
                    vault = vault,
                    onOpen = onOpen,
                    onToggleLock = onToggleLock,
                    onMove = onMove,
                    onMoveUp = { onReorder(file, -1) },
                    onMoveDown = { onReorder(file, 1) },
                    allowReorder = sortMode == VaultSort.CUSTOM,
                )
            }
        }
        if (currentFolder != "/") {
            item {
                TextButton(
                    onClick = { onDeleteFolder(currentFolder) },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF6B6B)),
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Delete this folder and move its files to root")
                }
            }
        }
        item {
            Text(
                "Stored only in app-private encrypted storage",
                color = VoltTextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
            )
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
                Text("Encrypted + hidden", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
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
    onMove: (VaultFile) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    allowReorder: Boolean,
    showFolderAction: Boolean = true,
    onDrag: (Float) -> Unit = {},
) {
    val icon = fileIcon(file)
    var dragDistance by remember(file.id) { mutableStateOf(0f) }
    var isDragging by remember(file.id) { mutableStateOf(false) }
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = if (isDragging) 1.025f else 1f
                scaleY = if (isDragging) 1.025f else 1f
                alpha = if (isDragging) 0.86f else 1f
            }
            .clickable { onOpen(file) },
        accent = if (file.locked) Color(0xFF7C4DFF) else VoltGreen,
        padding = 16.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            if (vault != null) {
                FileThumbnail(vault, file)
            } else {
                Box(
                    modifier = Modifier.size(52.dp).background(VoltGreen.copy(alpha = 0.09f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, null, tint = VoltGreen, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(file.name, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${file.transferDirection.label} • ${formatSize(file.sizeBytes)}",
                    color = VoltTextMuted,
                    fontSize = 12.sp,
                )
            }
            if (allowReorder) {
                Icon(
                    Icons.Default.DragHandle,
                    "Hold and drag to reorder",
                    tint = if (isDragging) VoltGreen else VoltTextMuted,
                    modifier = Modifier
                        .size(30.dp)
                        .pointerInput(file.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    isDragging = true
                                    dragDistance = 0f
                                },
                                onDragCancel = {
                                    isDragging = false
                                    dragDistance = 0f
                                },
                                onDragEnd = {
                                    isDragging = false
                                    dragDistance = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragDistance += dragAmount.y
                                    if (dragDistance <= -64f) {
                                        onMoveUp()
                                        onDrag(dragDistance)
                                        dragDistance = 0f
                                    } else if (dragDistance >= 64f) {
                                        onMoveDown()
                                        onDrag(dragDistance)
                                        dragDistance = 0f
                                    }
                                },
                            )
                        },
                )
            }
            if (showFolderAction) {
                IconButton(onClick = { onMove(file) }) {
                    Icon(Icons.Default.Folder, "Move to folder", tint = VoltTextMuted, modifier = Modifier.size(18.dp))
                }
            }
            IconButton(onClick = { onToggleLock(file) }) {
                Icon(if (file.locked) Icons.Default.Lock else Icons.Default.LockOpen, null, tint = if (file.locked) VoltGreen else VoltTextMuted)
            }
        }
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
    return try {
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
    } finally {
        prepared.delete()
    }
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
                        "Files sent from another VoltShare device are verified and saved to your encrypted vault only after the transfer completes.",
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
    onShowError: () -> Unit,
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
        if (status.errorLog != null) {
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
private fun SecurityHome(lockType: LockType, onLockNow: () -> Unit) {
    var fileLockDefault by remember { mutableStateOf(true) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(VoltBlack),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Text("SECURITY LAYER", color = VoltGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
            Spacer(Modifier.height(8.dp))
            Text("Control your boundary", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text("Your vault is encrypted at rest and invisible to normal file browsers.", color = VoltTextMuted, lineHeight = 21.sp)
        }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), accent = VoltGreen) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, null, tint = VoltGreen, modifier = Modifier.size(26.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("App lock", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("${lockType.label} + fingerprint available", color = VoltTextMuted, fontSize = 12.sp)
                    }
                    Icon(Icons.Default.Check, null, tint = VoltGreen)
                }
                Divider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, null, tint = VoltGreen, modifier = Modifier.size(26.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Lock each file", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Require your vault lock before viewing", color = VoltTextMuted, fontSize = 12.sp)
                    }
                    PremiumSwitch(
                        checked = fileLockDefault,
                        onCheckedChange = { fileLockDefault = it },
                    )
                }
            }
        }
        item {
            GlowButton("Lock vault now", Icons.Default.Lock, onLockNow)
        }
        item {
            Text("Security note", color = VoltTextMuted, fontSize = 12.sp)
            Spacer(Modifier.height(5.dp))
            Text("VoltShare never uploads your vault. Local discovery and transfers are initiated only when you tap Share.", color = Color.White.copy(alpha = 0.72f), fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun FileViewerScreen(activity: MainActivity, vault: VaultRepository, file: VaultFile, onBack: () -> Unit) {
    var prepared by remember(file.id) { mutableStateOf<File?>(null) }
    LaunchedEffect(file.id) {
        prepared = withContext(Dispatchers.IO) { vault.prepareViewing(file) }
    }
    Column(Modifier.fillMaxSize().background(VoltBlack)) {
        TopAppBar(
            title = { Text(file.name, maxLines = 1, color = Color.White) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = VoltBlack),
        )
        if (prepared == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Preparing a private preview…", color = VoltTextMuted)
            }
        } else {
            when {
                isImage(file) -> ImageViewer(prepared!!)
                isVideo(file) -> VideoViewer(prepared!!)
                isAudio(file) -> AudioViewer(prepared!!)
                isPdf(file) -> PdfViewer(prepared!!)
                isText(file) -> TextViewer(prepared!!)
                isInstallable(file) -> InstallerViewer(activity, prepared!!, file)
                else -> GenericViewer(activity, prepared!!, file)
            }
        }
    }
}

@Composable
private fun ImageViewer(file: File) {
    val bitmap = remember(file) { BitmapFactory.decodeFile(file.absolutePath) }
    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        bitmap?.let {
            Image(it.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxWidth(), contentScale = ContentScale.Fit)
        } ?: Text("Could not decode this image", color = VoltTextMuted)
    }
}

@Composable
private fun VideoViewer(file: File) {
    val context = LocalContext.current
    val player = remember(file) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(android.net.Uri.fromFile(file)))
            prepare()
            playWhenReady = true
        }
    }
    androidx.compose.runtime.DisposableEffect(player) {
        onDispose { player.release() }
    }
    AndroidView(
        factory = {
            PlayerView(it).apply {
                this.player = player
                useController = true
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
        },
        modifier = Modifier.fillMaxSize().background(Color.Black),
    )
}

@Composable
private fun AudioViewer(file: File) {
    val context = LocalContext.current
    val player = remember(file) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(android.net.Uri.fromFile(file)))
            prepare()
        }
    }
    androidx.compose.runtime.DisposableEffect(player) {
        onDispose { player.release() }
    }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.PlayArrow, null, tint = VoltGreen, modifier = Modifier.size(48.dp).align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(18.dp))
        Text("Audio preview", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(22.dp))
        AndroidView(
            factory = { PlayerView(it).apply { this.player = player; useController = true } },
            modifier = Modifier.fillMaxWidth().height(72.dp),
        )
    }
}

@Composable
private fun PdfViewer(file: File) {
    var bitmap by remember(file) { mutableStateOf<android.graphics.Bitmap?>(null) }
    var pageCount by remember(file) { mutableIntStateOf(0) }
    LaunchedEffect(file) {
        withContext(Dispatchers.IO) {
            PdfRenderer(android.os.ParcelFileDescriptor.open(file, android.os.ParcelFileDescriptor.MODE_READ_ONLY)).use { renderer ->
                pageCount = renderer.pageCount
                if (renderer.pageCount > 0) {
                    renderer.openPage(0).use { page ->
                        val pageBitmap = android.graphics.Bitmap.createBitmap(page.width * 2, page.height * 2, android.graphics.Bitmap.Config.ARGB_8888)
                        page.render(pageBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        bitmap = pageBitmap
                    }
                }
            }
        }
    }
    Box(Modifier.fillMaxSize().background(Color(0xFF202020)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            bitmap?.let { Image(it.asImageBitmap(), "PDF preview", modifier = Modifier.fillMaxWidth().padding(18.dp), contentScale = ContentScale.Fit) }
            Text("$pageCount page${if (pageCount == 1) "" else "s"} • showing first page", color = VoltTextMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun TextViewer(file: File) {
    var text by remember(file) { mutableStateOf("Loading private text…") }
    LaunchedEffect(file) {
        text = withContext(Dispatchers.IO) {
            file.inputStream().bufferedReader().use { it.readText().take(200_000) }
        }
    }
    androidx.compose.foundation.layout.Column(Modifier.fillMaxSize().background(Color(0xFF0A0A0A)).verticalScroll(rememberScrollState()).padding(22.dp)) {
        Text(text, color = Color(0xFFE6F5EC), fontFamily = FontFamily.Monospace, fontSize = 13.sp, lineHeight = 21.sp)
    }
}

@Composable
private fun InstallerViewer(activity: MainActivity, file: File, vaultFile: VaultFile) {
    val installResult = remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(92.dp).background(VoltGreen.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Bolt, null, tint = VoltGreen, modifier = Modifier.size(42.dp))
        }
        Spacer(Modifier.height(22.dp))
        Text(vaultFile.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(8.dp))
        Text("Installer package stays in your private vault until you choose to install it.", color = VoltTextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(26.dp))
        GlowButton("Install package", Icons.Default.ArrowDownward, onClick = {
            installResult.value = ApkInstaller.install(activity, file).fold(
                onSuccess = { "Android installer opened" },
                onFailure = { "Could not open this package" },
            )
        })
        installResult.value?.let { Text(it, color = VoltGreen, modifier = Modifier.padding(top = 16.dp)) }
    }
}

@Composable
private fun GenericViewer(activity: MainActivity, file: File, vaultFile: VaultFile) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Description, null, tint = VoltGreen, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(18.dp))
        Text(vaultFile.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 19.sp)
        Spacer(Modifier.height(8.dp))
        Text("No specialized preview is available for this format yet.", color = VoltTextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Text("${formatSize(file.length())} stored privately", color = VoltTextMuted, fontSize = 12.sp)
    }
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
private fun SecretDialog(title: String, type: LockType, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var secret by remember { mutableStateOf("") }
    var pattern by remember { mutableStateOf<List<Int>>(emptyList()) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VoltSurfaceRaised,
        title = { Text(title, color = Color.White) },
        text = {
            if (type == LockType.PATTERN) {
                PatternPad(pattern, onChange = { pattern = it })
            } else {
                SecureField(
                    value = secret,
                    label = "Enter ${type.label.lowercase()}",
                    keyboardType = if (type == LockType.PIN) KeyboardType.NumberPassword else KeyboardType.Password,
                    onValueChange = { secret = it },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(if (type == LockType.PATTERN) pattern.joinToString("-") else secret) },
                colors = ButtonDefaults.textButtonColors(contentColor = VoltGreen),
            ) { Text("Unlock") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = VoltTextMuted)) { Text("Cancel") }
        },
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(horizontal = 12.dp),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
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
                    strokeWidth = with(density) { 5.dp.toPx() },
                )
            }
            centers.forEachIndexed { index, center ->
                val selected = index in currentPattern
                if (selected) {
                    drawCircle(
                        color = VoltGreen.copy(alpha = 0.18f),
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
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
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
        modifier = Modifier.fillMaxWidth().background(VoltSurface, RoundedCornerShape(18.dp)).padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            Surface(
                modifier = Modifier.weight(1f).clickable { onSelected(index) },
                shape = RoundedCornerShape(14.dp),
                color = if (selected) VoltGreen else Color.Transparent,
            ) {
                Text(item, color = if (selected) Color.Black else VoltTextMuted, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.padding(vertical = 12.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
            .background(if (checked) VoltGreen.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.10f))
            .border(1.dp, if (checked) VoltGreen.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.16f), RoundedCornerShape(18.dp))
            .clickable { onCheckedChange(!checked) }
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
    val imported = runCatching {
        when (val source = pending.source) {
            is PendingShareSource.UriSource -> vault.importUri(source.uri)
            is PendingShareSource.FileSource -> vault.importGeneratedFile(source.file, pending.name, pending.mimeType)
        }
    }.getOrNull() ?: return false
    vault.markSent(imported)
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
private fun isText(file: VaultFile) = file.mimeType.startsWith("text") || file.name.isMediaExtension("txt", "md", "json", "xml", "csv", "log", "kt", "java", "js", "ts", "html", "css")
private fun isInstallable(file: VaultFile) = file.name.isMediaExtension("apk", "xapk", "apks")
private fun String.isMediaExtension(vararg extensions: String) = extensions.any { endsWith(".$it", ignoreCase = true) }
private fun formatSize(bytes: Long): String = Formatter.formatFileSize(null, bytes)