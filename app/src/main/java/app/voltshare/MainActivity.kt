package app.voltshare

import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.format.Formatter
import android.view.ViewGroup
import android.widget.ImageView
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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
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
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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
            headlineLarge = TextStyle(fontWeight = FontWeight.Black, letterSpacing = (-1.2).sp),
            headlineMedium = TextStyle(fontWeight = FontWeight.Black, letterSpacing = (-0.8).sp),
            titleLarge = TextStyle(fontWeight = FontWeight.Bold),
            bodyMedium = TextStyle(fontSize = 14.sp),
        ),
        content = content,
    )
}

private enum class AppTab(val label: String, val icon: ImageVector) {
    VAULT("Vault", Icons.Default.Folder),
    SHARE("Share", Icons.Default.Share),
    SECURITY("Lock", Icons.Default.Security),
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
    var lastSharedFile by remember { mutableStateOf<VaultFile?>(null) }
    val scope = rememberCoroutineScope()
    val incomingShare = activity.pendingIncomingShare

    LaunchedEffect(transfer) {
        transfer.status.collect { status ->
            if (status.label.startsWith("Received and verified")) {
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
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val imported = withContext(Dispatchers.IO) {
                runCatching {
                    activity.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                }
                vault.importUri(uri)
            }
            imported?.let {
                files = vault.listFiles()
                lastSharedFile = it
            }
        }
    }
    val mediaPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val imported = withContext(Dispatchers.IO) { vault.importUri(uri) }
            imported?.let {
                files = vault.listFiles()
                lastSharedFile = it
            }
        }
    }
    val folderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val imported = withContext(Dispatchers.IO) {
                createFolderArchive(activity, uri)?.let { archive ->
                    try {
                        vault.importGeneratedFile(
                            source = archive,
                            displayName = "${archive.nameWithoutExtension}.zip",
                            mimeType = "application/zip",
                        )
                    } finally {
                        archive.delete()
                    }
                }
            }
            imported?.let {
                files = vault.listFiles()
                lastSharedFile = it
            }
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
                )

                AppTab.SHARE -> ShareHome(
                    files = files,
                    transfer = transfer,
                    newlyPreparedFile = lastSharedFile,
                    onPickFile = { shareFilePicker.launch(arrayOf("*/*")) },
                    onPickMedia = { mediaPicker.launch("image/*") },
                    onPickFolder = { folderPicker.launch(null) },
                    onCreateText = { text ->
                        scope.launch {
                            val created = withContext(Dispatchers.IO) {
                                vault.createTextFile(text)
                            }
                            created?.let {
                                files = vault.listFiles()
                                lastSharedFile = it
                            }
                        }
                    },
                    onPickInstalledApp = { app ->
                        scope.launch {
                            val created = withContext(Dispatchers.IO) {
                                createInstalledAppPackage(activity, app)?.let { packageFile ->
                                    try {
                                        val extension = if (app.apkPaths.size > 1) "apks" else "apk"
                                        vault.importGeneratedFile(
                                            source = packageFile,
                                            displayName = "${safeFileName(app.label)}.$extension",
                                            mimeType = "application/vnd.android.package-archive",
                                        )
                                    } finally {
                                        packageFile.delete()
                                    }
                                }
                            }
                            created?.let {
                                files = vault.listFiles()
                                lastSharedFile = it
                            }
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
    val manager = remember { LockManager(LocalContext.current) }

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
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(18.dp),
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
                .padding(horizontal = 24.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.Center,
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
) {
    var searchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val visibleFiles = sortVaultFiles(
        files.filter {
            it.folderPath == currentFolder &&
                (searchQuery.isBlank() || it.name.contains(searchQuery.trim(), ignoreCase = true))
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
        if (searchOpen) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search private files") },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = VoltGreen) },
                    trailingIcon = {
                        IconButton(onClick = { searchQuery = ""; searchOpen = false }) {
                            Icon(Icons.Default.Close, "Close search", tint = VoltTextMuted)
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
                    onClick = { searchOpen = true },
                    modifier = Modifier.weight(0.65f).height(54.dp),
                    shape = RoundedCornerShape(18.dp),
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
    onOpen: (VaultFile) -> Unit,
    onToggleLock: (VaultFile) -> Unit,
    onMove: (VaultFile) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    allowReorder: Boolean,
) {
    val icon = fileIcon(file)
    GlassCard(
        modifier = Modifier.fillMaxWidth().clickable { onOpen(file) },
        accent = if (file.locked) Color(0xFF7C4DFF) else VoltGreen,
        padding = 16.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.size(48.dp).background(VoltGreen.copy(alpha = 0.09f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = VoltGreen, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(file.name, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text("${file.mimeType.substringBefore('/')} • ${formatSize(file.sizeBytes)}", color = VoltTextMuted, fontSize = 12.sp)
            }
            if (allowReorder) {
                Column {
                    IconButton(onClick = onMoveUp, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ArrowUpward, "Move up", tint = VoltTextMuted, modifier = Modifier.size(15.dp))
                    }
                    IconButton(onClick = onMoveDown, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ArrowDownward, "Move down", tint = VoltTextMuted, modifier = Modifier.size(15.dp))
                    }
                }
            }
            IconButton(onClick = { onMove(file) }) {
                Icon(Icons.Default.Folder, "Move to folder", tint = VoltTextMuted, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = { onToggleLock(file) }) {
                Icon(if (file.locked) Icons.Default.Lock else Icons.Default.LockOpen, null, tint = if (file.locked) VoltGreen else VoltTextMuted)
            }
        }
    }
}

@Composable
private fun ShareHome(
    files: List<VaultFile>,
    transfer: PeerTransferManager,
    newlyPreparedFile: VaultFile?,
    onPickFile: () -> Unit,
    onPickMedia: () -> Unit,
    onPickFolder: () -> Unit,
    onCreateText: (String) -> Unit,
    onPickInstalledApp: (InstalledAppChoice) -> Unit,
) {
    val peers by transfer.peers.collectAsStateWithLifecycle()
    val status by transfer.status.collectAsStateWithLifecycle()
    var selectedFile by remember(files) { mutableStateOf(files.firstOrNull()) }
    var showTextEditor by remember { mutableStateOf(false) }
    var showInstalledApps by remember { mutableStateOf(false) }

    LaunchedEffect(newlyPreparedFile?.id) {
        newlyPreparedFile?.let { selectedFile = it }
    }

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
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), accent = VoltGreen) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).background(VoltGreen.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Smartphone, null, tint = VoltGreen)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("This device", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(if (status.active) "Visible to nearby devices" else "Private and ready", color = VoltTextMuted, fontSize = 12.sp)
                    }
                    Switch(
                        checked = status.active,
                        onCheckedChange = { if (it) transfer.startHosting() else transfer.close() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = VoltGreen, uncheckedThumbColor = Color.White.copy(alpha = 0.7f), uncheckedTrackColor = Color.White.copy(alpha = 0.12f)),
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                GlowButton("Find nearby", Icons.Default.Search, { transfer.discoverPeers() }, Modifier.weight(1f))
                OutlinedButton(
                    onClick = { transfer.startHosting() },
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(18.dp),
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
            Text("1. Choose a file", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                files.take(12).forEach { file ->
                    val selected = file.id == selectedFile?.id
                    Surface(
                        modifier = Modifier.width(140.dp).clickable { selectedFile = file },
                        shape = RoundedCornerShape(18.dp),
                        color = if (selected) VoltGreen.copy(alpha = 0.16f) else VoltSurface,
                        border = BorderStroke(1.dp, if (selected) VoltGreen else Color.White.copy(alpha = 0.08f)),
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Icon(fileIcon(file), null, tint = if (selected) VoltGreen else VoltTextMuted, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(file.name, color = Color.White, maxLines = 1, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        item {
            Text("2. Choose a nearby device", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        if (peers.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), accent = VoltTeal, padding = 18.dp) {
                    Text("No devices yet", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(5.dp))
                    Text("Open VoltShare on the other phone, tap Host, then tap Find nearby here.", color = VoltTextMuted, fontSize = 13.sp)
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
                            onClick = { selectedFile?.let { transfer.send(peer, it) } },
                            enabled = selectedFile != null,
                            colors = ButtonDefaults.textButtonColors(contentColor = VoltGreen),
                        ) {
                            Icon(Icons.Default.ArrowUpward, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Send")
                        }
                    }
                }
            }
        }
        item {
            AnimatedVisibility(status.active || status.progress > 0f, enter = fadeIn() + scaleIn(), exit = fadeOut()) {
                GlassCard(modifier = Modifier.fillMaxWidth(), accent = VoltGreen, padding = 18.dp) {
                    Text(status.label, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { status.progress },
                        modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                        color = VoltGreen,
                        trackColor = Color.White.copy(alpha = 0.1f),
                    )
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
                    colors = androidx.compose.material3.TextFieldDefaults.colors(
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
    onSelected: (InstalledAppChoice) -> Unit,
) {
    val context = LocalContext.current
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
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VoltSurfaceRaised,
        title = {
            Column {
                Text("Installed Android apps", color = Color.White)
                Spacer(Modifier.height(5.dp))
                Text("Only user-installed apps are shown. Split apps become an APKS package.", color = VoltTextMuted, fontSize = 12.sp)
            }
        },
        text = {
            if (apps.isEmpty()) {
                Text("No user-installed apps were found on this device.", color = VoltTextMuted)
            } else {
                Column(Modifier.height(330.dp).verticalScroll(rememberScrollState())) {
                    apps.forEach { app ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { onSelected(app) },
                            color = Color.Transparent,
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier.size(40.dp).background(VoltGreen.copy(alpha = 0.11f), CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Default.Smartphone, null, tint = VoltGreen, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(app.label, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                    Text(
                                        "${app.packageName} • ${if (app.apkPaths.size > 1) "split APKS" else "APK"}",
                                        color = VoltTextMuted,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                    )
                                }
                                Icon(Icons.Default.ArrowUpward, null, tint = VoltGreen, modifier = Modifier.size(17.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = VoltTextMuted)) {
                Text("Cancel")
            }
        },
    )
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
                    Switch(
                        checked = fileLockDefault,
                        onCheckedChange = { fileLockDefault = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = VoltGreen, uncheckedThumbColor = Color.White.copy(alpha = 0.7f), uncheckedTrackColor = Color.White.copy(alpha = 0.12f)),
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
private fun FileViewerScreen(activity: MainActivity, vault: VaultRepository, file: VaultFile, onBack: () -> Unit) {
    var prepared by remember(file.id) { mutableStateOf<File?>(null) }
    LaunchedEffect(file.id) {
        prepared = withContext(Dispatchers.IO) { vault.prepareViewing(file) }
    }
    Column(Modifier.fillMaxSize().background(VoltBlack)) {
        SmallTopAppBar(
            title = { Text(file.name, maxLines = 1, color = Color.White) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
            colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = VoltBlack),
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
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        repeat(3) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(26.dp), modifier = Modifier.padding(vertical = 9.dp)) {
                repeat(3) { column ->
                    val point = row * 3 + column
                    val selected = point in pattern
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(if (selected) 14.dp else 0.dp, CircleShape, spotColor = VoltGreen.copy(alpha = 0.65f))
                            .background(if (selected) VoltGreen else Color.White.copy(alpha = 0.1f), CircleShape)
                            .border(1.dp, if (selected) VoltGreen else Color.White.copy(alpha = 0.2f), CircleShape)
                            .clickable {
                                onChange(if (selected) pattern - point else pattern + point)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (selected) {
                            Box(Modifier.size(12.dp).background(Color.Black, CircleShape))
                        }
                    }
                }
            }
        }
    }
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
        modifier = modifier.height(54.dp).shadow(elevation, RoundedCornerShape(18.dp), spotColor = VoltGreen.copy(alpha = 0.5f)),
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
private fun GlassCard(modifier: Modifier, accent: Color, padding: androidx.compose.ui.unit.Dp = 24.dp, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .shadow(18.dp, RoundedCornerShape(26.dp), ambientColor = Color.Black, spotColor = accent.copy(alpha = 0.22f))
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