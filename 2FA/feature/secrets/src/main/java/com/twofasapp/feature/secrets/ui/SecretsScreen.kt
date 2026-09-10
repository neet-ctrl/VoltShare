package com.twofasapp.feature.secrets.ui

import android.content.ClipData
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.view.HapticFeedbackConstants
import android.widget.ImageView
import android.widget.NumberPicker
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.designsystem.TwIcons
import com.twofasapp.designsystem.TwTheme
import com.twofasapp.designsystem.common.TwTopAppBar
import com.twofasapp.feature.secrets.data.SecretAttachment
import com.twofasapp.feature.secrets.data.SecretEntry
import com.twofasapp.feature.secrets.data.SecretField
import com.twofasapp.feature.secrets.data.SecretFieldKind
import com.twofasapp.feature.secrets.data.SecretManager
import com.twofasapp.feature.secrets.data.SecretManagerType
import com.twofasapp.feature.secrets.data.SecretTrashEntry
import com.twofasapp.feature.secrets.data.defaultFields
import java.util.UUID
import java.text.DateFormat
import java.util.Date
import java.util.Calendar
import java.util.Locale
import java.io.File
import com.twofasapp.designsystem.ktx.copyToClipboard
import com.twofasapp.feature.secrets.data.EncryptedAttachmentProvider
import org.koin.androidx.compose.koinViewModel
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.reorderable
import org.burnoutcrew.reorderable.ReorderableItem
import kotlinx.coroutines.delay

private val SecretDialogShape = RoundedCornerShape(28.dp)
private val SecretDialogColor = Color(0xFF18122E)
private val SecretDialogPanel = Color(0xFF241A42)
private val SecretDialogAccent = Color(0xFFE18CFF)
private const val VOLTSHARE_PACKAGE = "app.voltshare"
private const val VOLTSHARE_PICK_ACTION = "app.voltshare.action.PICK_VAULT_FILES"

@Composable
private fun DialogEyebrow(text: String) {
    Text(
        text = text,
        color = Color(0xFFBBA5DD),
        fontWeight = FontWeight.Bold,
        style = TwTheme.typo.caption,
    )
}

@Composable
private fun DialogTitleBlock(
    eyebrow: String,
    title: String,
    subtitle: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        DialogEyebrow(eyebrow)
        Text(title, color = TwTheme.color.onSurfacePrimary, fontWeight = FontWeight.Bold)
        Text(subtitle, color = TwTheme.color.onSurfaceSecondary, style = TwTheme.typo.caption)
    }
}

@Composable
fun SecretsScreen(
    bottomBar: @Composable () -> Unit,
    viewModel: SecretsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedManager by remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = selectedManager != null) {
        selectedManager = null
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.lockHidden() }
    }

    if (selectedManager == null) {
        SecretsHome(
            state = state,
            bottomBar = bottomBar,
            onManagerClick = { if (!state.isReordering) selectedManager = it },
            onManagerMove = viewModel::moveManager,
            onManagerReorderFinished = viewModel::finishManagerReorder,
            onReorderToggle = { viewModel.setReordering(!state.isReordering) },
            onTogglePin = viewModel::toggleManagerPinned,
            onToggleHidden = viewModel::toggleManagerHidden,
            onRemove = viewModel::removeManager,
            onRestore = viewModel::restoreManager,
            onAddCustom = viewModel::addCustomManager,
            onRefresh = viewModel::reload,
            onUnlock = viewModel::unlockHidden,
            onRestoreTrash = viewModel::restoreTrash,
            onPermanentDeleteTrash = viewModel::permanentlyDeleteTrash,
            onReadAttachment = viewModel::readAttachment,
        )
    } else {
        val manager = state.managers.firstOrNull { it.id == selectedManager }
        if (manager == null) {
            selectedManager = null
        } else {
            SecretManagerScreen(
                manager = manager,
                entries = state.entries.filter { it.managerId == manager.id },
                trash = state.trash.filter { it.entry.managerId == manager.id },
                onBack = { selectedManager = null },
                onSave = viewModel::saveEntry,
                onDelete = viewModel::deleteEntry,
                onTogglePin = viewModel::toggleEntryPinned,
                onMoveEntry = viewModel::moveEntry,
                onEntryReorderFinished = viewModel::finishEntryReorder,
                onRefresh = viewModel::reload,
                onUnlockHidden = viewModel::unlockHidden,
                onSaveAttachment = viewModel::saveAttachment,
                onReadAttachment = viewModel::readAttachment,
                onRestoreTrash = viewModel::restoreTrash,
                onPermanentDeleteTrash = viewModel::permanentlyDeleteTrash,
                managerNames = state.managerNames,
                hasHiddenManagers = state.hasHiddenManagers,
                hasUnlockLock = state.hasUnlockLock,
                hiddenUnlocked = state.hiddenUnlocked,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SecretsHome(
    state: SecretsUiState,
    bottomBar: @Composable () -> Unit,
    onManagerClick: (String) -> Unit,
    onManagerMove: (Int, Int) -> Unit,
    onManagerReorderFinished: () -> Unit,
    onReorderToggle: () -> Unit,
    onTogglePin: (String) -> Unit,
    onToggleHidden: (String) -> Unit,
    onRemove: (String) -> Unit,
    onRestore: (String) -> Unit,
    onAddCustom: (String) -> Unit,
    onRefresh: () -> Unit,
    onUnlock: (Float, String) -> Boolean,
    onRestoreTrash: (SecretTrashEntry) -> Unit,
    onPermanentDeleteTrash: (SecretTrashEntry) -> Unit,
    onReadAttachment: (SecretAttachment) -> ByteArray?,
) {
    var selectedActionManager by remember { mutableStateOf<SecretManager?>(null) }
    var showAddManager by remember { mutableStateOf(false) }
    var showHiddenDialog by remember { mutableStateOf(false) }
    var showTrash by remember { mutableStateOf(false) }
    val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
    val reorderState = org.burnoutcrew.reorderable.rememberReorderableLazyGridState(
        gridState = gridState,
        onMove = { from, to -> onManagerMove(from.index, to.index) },
        onDragEnd = { _, _ -> onManagerReorderFinished() },
        canDragOver = { draggedOver, _ -> draggedOver.index < state.managers.size },
    )

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = bottomBar,
        topBar = {
            TwTopAppBar(
                title = { Text("Secrets", fontWeight = FontWeight.SemiBold) },
                showBackButton = false,
                actions = {
                    IconButton(onClick = onReorderToggle) {
                        Icon(
                            painter = if (state.isReordering) TwIcons.Check else TwIcons.DragHandle,
                            contentDescription = if (state.isReordering) "Done reordering" else "Reorder managers",
                        )
                    }
                    IconButton(onClick = { onRefresh(); showHiddenDialog = true }) {
                        Icon(painter = TwIcons.Comment, contentDescription = "Open feedback")
                    }
                    IconButton(onClick = { showTrash = true }) {
                        Icon(painter = TwIcons.Delete, contentDescription = "Universal trash")
                    }
                },
            )
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            TwTheme.color.background,
                            TwTheme.color.backgroundSecondary,
                            TwTheme.color.background,
                        )
                    )
                )
                .padding(padding)
            // The item gesture sends a drag event to this receiver.
            // Without it the cards can be held but never move.
            .reorderable(reorderState),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            gridItemsIndexed(
                items = state.managers,
                key = { _, manager -> manager.id },
            ) { index, manager ->
                ReorderableItem(
                    state = reorderState,
                    key = manager.id,
                ) { dragging ->
                    ManagerCard(
                        manager = manager,
                        entryCount = state.entries.count { it.managerId == manager.id },
                        isDragging = dragging,
                        isReordering = state.isReordering,
                        onClick = { onManagerClick(manager.id) },
                        onLongClick = { selectedActionManager = manager },
                        dragModifier = if (state.isReordering) {
                            Modifier.detectReorderAfterLongPress(reorderState)
                        } else {
                            Modifier
                        },
                    )
                }
            }
            item {
                AddManagerCard(onClick = { showAddManager = true })
            }
        }
    }

    selectedActionManager?.let { manager ->
        ManagerActionsDialog(
            manager = manager,
            onDismiss = { selectedActionManager = null },
            onPin = { onTogglePin(manager.id); selectedActionManager = null },
            onHide = { onToggleHidden(manager.id); selectedActionManager = null },
            onRemove = { onRemove(manager.id); selectedActionManager = null },
        )
    }
    if (showAddManager) {
        AddManagerDialog(
            removedManagers = state.removedManagers,
            onDismiss = { showAddManager = false },
            onRestore = { onRestore(it); showAddManager = false },
            onCustom = { onAddCustom(it); showAddManager = false },
        )
    }
    if (showHiddenDialog) {
        HiddenManagersDialog(
            hiddenManagers = state.managers.filter { it.hidden },
            hasHiddenManagers = state.hasHiddenManagers || state.hasHiddenEntries,
            unlocked = state.hiddenUnlocked,
            hasUnlockLock = state.hasUnlockLock,
            onDismiss = { showHiddenDialog = false },
            onSubmit = { rating, comment ->
                if (onUnlock(rating, comment)) {
                    showHiddenDialog = false
                    true
                } else if (state.hiddenUnlocked || (!state.hasHiddenManagers && !state.hasHiddenEntries)) {
                    showHiddenDialog = false
                    true
                } else {
                    false
                }
            },
        )
    }
    if (showTrash) {
        TrashDialog(
            entries = state.trash,
            managerNames = state.managerNames,
            onDismiss = { showTrash = false },
            onRestore = onRestoreTrash,
            onPermanentDelete = onPermanentDeleteTrash,
            onReadAttachment = onReadAttachment,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ManagerCard(
    manager: SecretManager,
    entryCount: Int,
    isDragging: Boolean,
    isReordering: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    dragModifier: Modifier,
) {
    val tint = managerTint(manager.type)
    val view = LocalView.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(184.dp)
            .combinedClickable(
                enabled = !isReordering,
                onClick = onClick,
                onLongClick = {
                    if (!isReordering) {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        onLongClick()
                    }
                },
            )
            .then(dragModifier),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, if (manager.pinned) tint else TwTheme.color.glassOutline),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 12.dp else 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            tint.copy(alpha = if (isDragging) .42f else .28f),
                            TwTheme.color.glassSurfaceStrong.copy(alpha = .86f),
                            TwTheme.color.backgroundSecondary.copy(alpha = .92f),
                        )
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .size(116.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 34.dp, y = (-32).dp)
                    .background(
                        Brush.radialGradient(
                            listOf(tint.copy(alpha = .48f), tint.copy(alpha = 0f))
                        ),
                        CircleShape,
                    )
            )
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-28).dp, y = 28.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(tint.copy(alpha = .22f), tint.copy(alpha = 0f))
                        ),
                        CircleShape,
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                    modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(17.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(tint.copy(alpha = .48f), tint.copy(alpha = .12f))
                                )
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(painter = managerIcon(manager.type), contentDescription = null, tint = Color.White, modifier = Modifier.size(27.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    if (manager.pinned) {
                        Icon(painter = TwIcons.Favorite, contentDescription = "Pinned", tint = tint, modifier = Modifier.size(19.dp))
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        manager.name,
                        color = TwTheme.color.onSurfacePrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                    )
                    Text(
                        "$entryCount ${if (entryCount == 1) "entry" else "entries"}",
                        color = tint,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun AddManagerCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(184.dp)
            .combinedClickable(onClick = onClick, onLongClick = {}),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, TwTheme.color.glassOutline),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            TwTheme.color.primary.copy(alpha = .22f),
                            TwTheme.color.glassSurface.copy(alpha = .82f),
                            TwTheme.color.backgroundSecondary.copy(alpha = .9f),
                        )
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(TwTheme.color.primary.copy(alpha = .2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painter = TwIcons.Add, contentDescription = "Add manager", tint = TwTheme.color.primary, modifier = Modifier.size(30.dp))
                }
                Text("Add manager", color = TwTheme.color.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ManagerActionsDialog(
    manager: SecretManager,
    onDismiss: () -> Unit,
    onPin: () -> Unit,
    onHide: () -> Unit,
    onRemove: () -> Unit,
) {
    SecretActionsDialog(
        eyebrow = "MANAGE VAULT",
        title = manager.name,
        status = if (manager.pinned) "PINNED • ENCRYPTED LOCAL VAULT" else "ENCRYPTED LOCAL VAULT",
        icon = managerIcon(manager.type),
        accent = managerTint(manager.type),
        actions = listOf(
            SecretDialogAction(
                label = if (manager.pinned) "Remove from pinned" else "Pin manager",
                icon = TwIcons.Favorite,
                onClick = onPin,
            ),
            SecretDialogAction(
                label = if (manager.hidden) "Reveal manager" else "Hide manager",
                icon = if (manager.hidden) TwIcons.Eye else TwIcons.EyeSlash,
                onClick = onHide,
            ),
            SecretDialogAction(
                label = "Remove manager",
                icon = TwIcons.Delete,
                destructive = true,
                onClick = onRemove,
            ),
        ),
        onDismiss = onDismiss,
    )
}

private data class SecretDialogAction(
    val label: String,
    val icon: Painter,
    val destructive: Boolean = false,
    val onClick: () -> Unit,
)

@Composable
private fun SecretActionsDialog(
    eyebrow: String,
    title: String,
    status: String,
    icon: Painter,
    accent: Color,
    actions: List<SecretDialogAction>,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(.82f)
                .widthIn(max = 360.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF261849),
                            Color(0xFF17112F),
                            Color(0xFF100B23),
                        )
                    )
                )
                .border(
                    BorderStroke(1.dp, accent.copy(alpha = .5f)),
                    RoundedCornerShape(26.dp),
                )
                .padding(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 52.dp, y = (-52).dp)
                    .background(
                        Brush.radialGradient(
                            listOf(accent.copy(alpha = .24f), accent.copy(alpha = 0f))
                        ),
                        CircleShape,
                    ),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(accent.copy(alpha = .85f), Color(0xFF7E56E8))
                                )
                            )
                            .border(
                                BorderStroke(1.dp, Color.White.copy(alpha = .25f)),
                                RoundedCornerShape(15.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.width(11.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = eyebrow,
                            color = accent.copy(alpha = .95f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            maxLines = 1,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = title,
                            color = TwTheme.color.onSurfacePrimary,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                painter = TwIcons.Lock,
                                contentDescription = null,
                                tint = accent.copy(alpha = .82f),
                                modifier = Modifier.size(11.dp),
                            )
                            Text(
                                text = status,
                                color = TwTheme.color.onSurfaceTertiary,
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(34.dp),
                    ) {
                        Icon(
                            painter = TwIcons.Close,
                            contentDescription = "Close",
                            tint = TwTheme.color.onSurfaceSecondary,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                actions.forEachIndexed { index, action ->
                    SecretDialogActionRow(action = action, accent = accent)
                    if (index < actions.lastIndex) {
                        Spacer(Modifier.height(7.dp))
                    }
                }
                Spacer(Modifier.height(9.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(horizontal = 0.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "Cancel",
                        color = TwTheme.color.onSurfaceSecondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun SecretDialogActionRow(
    action: SecretDialogAction,
    accent: Color,
) {
    val actionColor = if (action.destructive) TwTheme.color.error else accent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(
                if (action.destructive) {
                    TwTheme.color.error.copy(alpha = .1f)
                } else {
                    Color.White.copy(alpha = .045f)
                }
            )
            .border(
                BorderStroke(
                    1.dp,
                    if (action.destructive) {
                        TwTheme.color.error.copy(alpha = .28f)
                    } else {
                        Color.White.copy(alpha = .09f)
                    },
                ),
                RoundedCornerShape(15.dp),
            )
            .clickable(onClick = action.onClick)
            .padding(horizontal = 11.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(actionColor.copy(alpha = if (action.destructive) .16f else .13f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = action.icon,
                contentDescription = null,
                tint = actionColor,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = action.label,
                color = if (action.destructive) TwTheme.color.error else TwTheme.color.onSurfacePrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            painter = TwIcons.ChevronRight,
            contentDescription = null,
            tint = actionColor.copy(alpha = .8f),
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun AddManagerDialog(
    removedManagers: List<SecretManager>,
    onDismiss: () -> Unit,
    onRestore: (String) -> Unit,
    onCustom: (String) -> Unit,
) {
    var customName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = SecretDialogShape,
        containerColor = SecretDialogColor,
        titleContentColor = TwTheme.color.onSurfacePrimary,
        textContentColor = TwTheme.color.onSurfaceSecondary,
        icon = {
            Box(
                Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(SecretDialogAccent.copy(alpha = .55f), Color(0xFF7E56E8)))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painter = TwIcons.Add, contentDescription = null, tint = Color.White)
            }
        },
        title = { DialogTitleBlock("SECURE COLLECTION", "Add a manager", "Restore a removed vault or create a new one.") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (removedManagers.isNotEmpty()) {
                    DialogEyebrow("RESTORE REMOVED")
                    removedManagers.forEach { manager ->
                        Button(
                            onClick = { onRestore(manager.id) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = SecretDialogPanel,
                                contentColor = TwTheme.color.onSurfacePrimary,
                            ),
                        ) {
                            Icon(painter = managerIcon(manager.type), contentDescription = null)
                            Text("  ${manager.name}")
                        }
                    }
                }
                DialogEyebrow("CREATE CUSTOM")
                SecretInput(
                    value = customName,
                    onValueChange = { customName = it },
                    label = "Manager name",
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCustom(customName) },
                enabled = customName.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = SecretDialogAccent,
                    contentColor = Color(0xFF321748),
                ),
            ) { Text("Create manager", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TwTheme.color.onSurfaceSecondary) } },
    )
}

@Composable
private fun HiddenManagersDialog(
    hiddenManagers: List<SecretManager>,
    hasHiddenManagers: Boolean = hiddenManagers.isNotEmpty(),
    unlocked: Boolean,
    hasUnlockLock: Boolean = false,
    onDismiss: () -> Unit,
    onSubmit: (Float, String) -> Boolean,
) {
    FeedbackCheckInDialog(
        hasFeedback = hasUnlockLock,
        hasHiddenContent = hasHiddenManagers,
        unlocked = unlocked,
        onDismiss = onDismiss,
        onVerify = onSubmit,
    )
}

@Composable
internal fun SecretRatingPicker(
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            (1..5).forEach { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (value >= index - .5f) {
                                Color(0xFF4B315C)
                            } else {
                                Color(0xFF251B3F)
                            }
                        )
                        .border(
                            1.dp,
                            if (value >= index - .5f) {
                                Color(0xFFFFC65A).copy(alpha = .7f)
                            } else {
                                TwTheme.color.glassOutline
                            },
                            RoundedCornerShape(14.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clickable { onValueChange(index - .5f) },
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clickable { onValueChange(index.toFloat()) },
                        )
                    }
                    Text(
                        text = when {
                            value >= index -> "★"
                            value >= index - .5f -> "◐"
                            else -> "☆"
                        },
                        color = Color(0xFFFFB547),
                        fontSize = 18.sp,
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Not for me", color = TwTheme.color.onSurfaceTertiary, fontSize = 11.sp)
            Text("Exactly right", color = Color(0xFFFFC65A), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TrashDialog(
    entries: List<SecretTrashEntry>,
    managerNames: Map<String, String>,
    onDismiss: () -> Unit,
    onRestore: (SecretTrashEntry) -> Unit,
    onPermanentDelete: (SecretTrashEntry) -> Unit,
    onReadAttachment: (SecretAttachment) -> ByteArray? = { null },
) {
    val context = LocalContext.current
    var previewAttachment by remember { mutableStateOf<SecretAttachment?>(null) }
    var expandedEntryId by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            color = SecretDialogColor,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0B081A), SecretDialogColor, Color(0xFF0D091D))
                    )
                ),
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TwTopAppBar(
                        title = { Column { Text("Trash"); Text("Deleted entries", color = TwTheme.color.onSurfaceSecondary, style = TwTheme.typo.caption) } },
                        showBackButton = false,
                        actions = { IconButton(onClick = onDismiss) { Icon(painter = TwIcons.Close, contentDescription = "Close trash") } },
                    )
                },
            ) { padding ->
                LazyColumn(
                    Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (entries.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Brush.linearGradient(listOf(SecretDialogPanel, Color(0xFF1C1535))))
                                    .border(1.dp, TwTheme.color.glassOutline, RoundedCornerShape(24.dp))
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    painter = TwIcons.Delete,
                                    contentDescription = null,
                                    tint = SecretDialogAccent,
                                    modifier = Modifier.size(34.dp),
                                )
                                Text("Your trash is empty", color = TwTheme.color.onSurfacePrimary, fontWeight = FontWeight.Bold)
                                Text(
                                    "Deleted entries will stay here until restored or permanently removed.",
                                    color = TwTheme.color.onSurfaceSecondary,
                                    style = TwTheme.typo.caption,
                                )
                            }
                        }
                    }
                    items(entries, key = { it.entry.id }) { deleted ->
                        val isExpanded = expandedEntryId == deleted.entry.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                                .clickable {
                                    expandedEntryId = if (isExpanded) null else deleted.entry.id
                                },
                            colors = CardDefaults.cardColors(containerColor = SecretDialogPanel),
                            border = BorderStroke(
                                1.dp,
                                if (isExpanded) SecretDialogAccent.copy(alpha = .72f) else Color(0xFF6E4C8E),
                            ),
                            shape = RoundedCornerShape(24.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                Color(0xFF2A1C4A),
                                                SecretDialogPanel,
                                                Color(0xFF1D1537),
                                            )
                                        )
                                    )
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(15.dp))
                                            .background(SecretDialogAccent.copy(alpha = .16f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            painter = TwIcons.Delete,
                                            contentDescription = null,
                                            tint = SecretDialogAccent,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(3.dp),
                                    ) {
                                        Text(
                                            deleted.entry.title.ifBlank { "Untitled entry" },
                                            color = TwTheme.color.onSurfacePrimary,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                        )
                                        Text(
                                            "From ${managerNames[deleted.entry.managerId] ?: deleted.entry.managerId}",
                                            color = Color(0xFFE0C8FF),
                                            style = TwTheme.typo.caption,
                                            maxLines = 1,
                                        )
                                        Text(
                                            "Deleted ${DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(deleted.deletedAt))}",
                                            color = TwTheme.color.onSurfaceSecondary,
                                            style = TwTheme.typo.caption,
                                            maxLines = 1,
                                        )
                                    }
                                    Icon(
                                        painter = if (isExpanded) TwIcons.ChevronUp else TwIcons.ChevronDown,
                                        contentDescription = if (isExpanded) "Collapse entry" else "Expand entry",
                                        tint = SecretDialogAccent,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    TrashSummaryPill(
                                        text = "${deleted.entry.fields.size + if (deleted.entry.note.isNotBlank()) 1 else 0} details",
                                        modifier = Modifier.weight(1f),
                                    )
                                    TrashSummaryPill(
                                        text = if (deleted.entry.attachments.isEmpty()) {
                                            "No attachments"
                                        } else {
                                            "${deleted.entry.attachments.size} attachment${if (deleted.entry.attachments.size == 1) "" else "s"}"
                                        },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                if (isExpanded) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color(0xFF6E4C8E).copy(alpha = .65f)),
                                    )
                                    deleted.entry.fields.take(4).forEach { field ->
                                        Text(
                                            "${field.label}: ${if (field.kind == SecretFieldKind.PASSWORD) "••••••••" else field.value.ifBlank { "—" }}",
                                            color = TwTheme.color.onSurfacePrimary,
                                            maxLines = 2,
                                        )
                                    }
                                    if (deleted.entry.fields.size < 4 && deleted.entry.note.isNotBlank()) {
                                        Text(
                                            "Note: ${deleted.entry.note}",
                                            color = TwTheme.color.onSurfaceSecondary,
                                            maxLines = 3,
                                        )
                                    }
                                    deleted.entry.attachments.take(3).forEach { attachment ->
                                        Button(
                                            onClick = { previewAttachment = attachment },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF3A2B5A),
                                                contentColor = Color(0xFFEEDFFF),
                                            ),
                                        ) {
                                            Icon(painter = TwIcons.ExternalLink, contentDescription = null)
                                            Text("  Open ${attachment.name}")
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { onRestore(deleted) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF3A2B5A),
                                                contentColor = Color(0xFFEEDFFF),
                                            ),
                                        ) { Text("Restore") }
                                        TextButton(onClick = { onPermanentDelete(deleted) }, modifier = Modifier.weight(1f)) {
                                            Text("Delete forever", color = TwTheme.color.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    previewAttachment?.let { attachment ->
        AttachmentViewerDialog(
            attachment = attachment,
            bytes = onReadAttachment(attachment),
            onDismiss = { previewAttachment = null },
            onOpenExternal = {
                openAttachmentExternally(
                    context,
                    attachment,
                )
            },
        )
    }
}

@Composable
private fun TrashSummaryPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF130D27).copy(alpha = .52f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Color(0xFFBBA5DD),
            style = TwTheme.typo.caption,
            maxLines = 1,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecretManagerScreen(
    manager: SecretManager,
    entries: List<SecretEntry>,
    trash: List<SecretTrashEntry>,
    onBack: () -> Unit,
    onSave: (SecretEntry) -> Unit,
    onDelete: (SecretEntry) -> Unit,
    onTogglePin: (String) -> Unit,
    onMoveEntry: (String, Int, Int) -> Unit,
    onEntryReorderFinished: () -> Unit,
    onRefresh: () -> Unit,
    onUnlockHidden: (Float, String) -> Boolean,
    onSaveAttachment: (Uri) -> SecretAttachment?,
    onReadAttachment: (SecretAttachment) -> ByteArray?,
    onRestoreTrash: (SecretTrashEntry) -> Unit,
    onPermanentDeleteTrash: (SecretTrashEntry) -> Unit,
    managerNames: Map<String, String>,
    hasHiddenManagers: Boolean,
    hasUnlockLock: Boolean,
    hiddenUnlocked: Boolean,
) {
    val context = LocalContext.current
    var editing by remember { mutableStateOf<SecretEntry?>(null) }
    var showDetails by remember { mutableStateOf<SecretEntry?>(null) }
    var actionEntry by remember { mutableStateOf<SecretEntry?>(null) }
    var showTrash by remember { mutableStateOf(false) }
    var showWifiDiscovery by remember { mutableStateOf(false) }
    var showHiddenDialog by remember { mutableStateOf(false) }
    var previewAttachment by remember { mutableStateOf<SecretAttachment?>(null) }
    var isReorderingEntries by remember { mutableStateOf(false) }
    var showHiddenEntries by remember { mutableStateOf(hiddenUnlocked) }
    val hasHiddenEntries = entries.any { it.hidden }
    val visibleEntries = entries
        .filter { !it.hidden || showHiddenEntries }
        .sortedWith(compareByDescending<SecretEntry> { it.pinned }.thenBy { it.order }.thenByDescending { it.updatedAt })
    val fixedItemsBeforeEntries = if (manager.type == SecretManagerType.WIFI) 2 else 1
    val entryListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val entryReorderState = org.burnoutcrew.reorderable.rememberReorderableLazyListState(
        listState = entryListState,
        onMove = { from, to ->
            val fromIndex = from.index - fixedItemsBeforeEntries
            val toIndex = to.index - fixedItemsBeforeEntries
            if (fromIndex in visibleEntries.indices && toIndex in visibleEntries.indices) {
                onMoveEntry(manager.id, fromIndex, toIndex)
            }
        },
        onDragEnd = { _, _ -> onEntryReorderFinished() },
        canDragOver = { draggedOver, _ ->
            draggedOver.index in fixedItemsBeforeEntries until fixedItemsBeforeEntries + visibleEntries.size
        },
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TwTopAppBar(
                title = { Column { Text(manager.name); Text("${entries.size} entries", color = TwTheme.color.onSurfaceSecondary, style = TwTheme.typo.caption) } },
                actions = {
                    IconButton(onClick = { onRefresh(); showHiddenDialog = true }) {
                        Icon(painter = TwIcons.Comment, contentDescription = "Open feedback")
                    }
                    IconButton(onClick = { isReorderingEntries = !isReorderingEntries }) {
                        Icon(
                            painter = if (isReorderingEntries) TwIcons.Check else TwIcons.DragHandle,
                            contentDescription = if (isReorderingEntries) "Done reordering entries" else "Reorder entries",
                            tint = if (isReorderingEntries) TwTheme.color.primary else TwTheme.color.onSurfacePrimary,
                        )
                    }
                    IconButton(onClick = { showTrash = true }) { Icon(painter = TwIcons.Delete, contentDescription = "Manager trash") }
                },
                onBackClick = onBack,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            TwTheme.color.background,
                            TwTheme.color.backgroundSecondary,
                            TwTheme.color.background,
                        )
                    )
                )
                .padding(padding)
                .reorderable(entryReorderState),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (manager.type == SecretManagerType.WIFI) {
                item {
                    Button(onClick = { showWifiDiscovery = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Icon(painter = TwIcons.Refresh, contentDescription = null)
                        Text("  Discover nearby Wi-Fi")
                    }
                }
            }
            item {
                AddEntryCard(onClick = {
                    editing = SecretEntry(UUID.randomUUID().toString(), manager.id, "", manager.type.defaultFields())
                })
            }
            itemsIndexed(visibleEntries, key = { _, entry -> entry.id }) { index, entry ->
                ReorderableItem(
                    state = entryReorderState,
                    key = entry.id,
                    modifier = if (isReorderingEntries) {
                        Modifier.detectReorderAfterLongPress(entryReorderState, entry.id)
                    } else {
                        Modifier
                    },
                ) { dragging ->
                    EntryCard(
                        entry = entry,
                        managerType = manager.type,
                        isDragging = dragging,
                        isReordering = isReorderingEntries,
                        onClick = { showDetails = entry },
                        onLongClick = { actionEntry = entry },
                        onMoveUp = {
                            if (index > 0 && visibleEntries[index - 1].pinned == entry.pinned) {
                                onMoveEntry(manager.id, index, index - 1)
                            }
                        },
                        onMoveDown = {
                            if (index < visibleEntries.lastIndex && visibleEntries[index + 1].pinned == entry.pinned) {
                                onMoveEntry(manager.id, index, index + 1)
                            }
                        },
                        canMoveUp = index > 0 && visibleEntries[index - 1].pinned == entry.pinned,
                        canMoveDown = index < visibleEntries.lastIndex &&
                            visibleEntries[index + 1].pinned == entry.pinned,
                    )
                }
            }
            if (entries.isEmpty()) {
                item {
                    Text(
                        "Start with a secure entry. Every field is stored in the encrypted local vault.",
                        modifier = Modifier.padding(24.dp),
                        color = TwTheme.color.onSurfaceSecondary,
                    )
                }
            }
        }
    }

    editing?.let { entry ->
        EntryEditorDialog(
            manager = manager,
            initial = entry,
            onDismiss = { editing = null },
            onSave = { onSave(it); editing = null },
            onSaveAttachment = onSaveAttachment,
            onReadAttachment = onReadAttachment,
            onSaveCredential = { name, password ->
                val defaults = manager.type.defaultFields()
                val passwordIndex = defaults.indexOfFirst {
                    it.label.equals("Password", true) || it.kind == SecretFieldKind.PASSWORD
                }
                val credentialFields = if (passwordIndex >= 0) {
                    defaults.mapIndexed { index, field ->
                        if (index == passwordIndex) field.copy(value = password) else field
                    }
                } else {
                    defaults + SecretField(
                        id = UUID.randomUUID().toString(),
                        label = "Password",
                        value = password,
                        kind = SecretFieldKind.PASSWORD,
                    )
                }
                onSave(
                    SecretEntry(
                        id = UUID.randomUUID().toString(),
                        managerId = manager.id,
                        title = name,
                        fields = credentialFields,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                    )
                )
            },
        )
    }
    showDetails?.let { entry ->
        EntryDetailsDialog(
            entry = entry,
            onDismiss = { showDetails = null },
            onOpenAttachment = { attachment -> previewAttachment = attachment },
        )
    }
    previewAttachment?.let { attachment ->
        val bytes = onReadAttachment(attachment)
        AttachmentViewerDialog(
            attachment = attachment,
            bytes = bytes,
            onDismiss = { previewAttachment = null },
            onOpenExternal = { openAttachmentExternally(context, attachment) },
        )
    }
    actionEntry?.let { entry ->
        EntryActionsDialog(
            entry = entry,
            onDismiss = { actionEntry = null },
            onEdit = { editing = entry; actionEntry = null },
            onDelete = { onDelete(entry); actionEntry = null },
            onPin = { onTogglePin(entry.id); actionEntry = null },
            onHide = { onSave(entry.copy(hidden = !entry.hidden)); actionEntry = null },
        )
    }
    if (showTrash) {
        TrashDialog(
            entries = trash,
            managerNames = managerNames,
            onDismiss = { showTrash = false },
            onRestore = onRestoreTrash,
            onPermanentDelete = onPermanentDeleteTrash,
            onReadAttachment = onReadAttachment,
        )
    }
    if (showWifiDiscovery) {
        WifiDiscoveryDialog(
            onDismiss = { showWifiDiscovery = false },
            onSelected = { wifi ->
                onSave(
                    SecretEntry(
                        id = UUID.randomUUID().toString(),
                        managerId = manager.id,
                        title = wifi.ssid,
                        fields = manager.type.defaultFields().map { field ->
                            when (field.label) {
                                "Network name" -> field.copy(value = wifi.ssid)
                                "Password" -> field.copy(value = wifi.password)
                                "Security" -> field.copy(value = wifi.security)
                                "BSSID / MAC address" -> field.copy(value = wifi.bssid)
                                "Signal strength" -> field.copy(value = wifi.level.toString())
                                "Frequency" -> field.copy(value = wifi.frequency)
                                "Connection status" -> field.copy(value = wifi.status)
                                else -> field
                            }
                        },
                        note = "Captured from nearby Wi-Fi scan.",
                        tags = listOf("wifi", "scanned"),
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                    )
                )
                showWifiDiscovery = false
            },
        )
    }
    if (showHiddenDialog) {
        HiddenManagersDialog(
            hiddenManagers = emptyList(),
            hasHiddenManagers = hasHiddenManagers || hasHiddenEntries,
            unlocked = hiddenUnlocked,
            hasUnlockLock = hasUnlockLock,
            onDismiss = { showHiddenDialog = false },
            onSubmit = { rating, comment ->
                if (hiddenUnlocked || onUnlockHidden(rating, comment)) {
                    showHiddenEntries = true
                    showHiddenDialog = false
                    true
                } else {
                    false
                }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AddEntryCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = {}),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, TwTheme.color.primary.copy(alpha = .35f)),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            TwTheme.color.primary.copy(alpha = .28f),
                            TwTheme.color.glassSurfaceStrong.copy(alpha = .88f),
                        )
                    )
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(TwTheme.color.primary.copy(alpha = .2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painter = TwIcons.Add, contentDescription = null, tint = TwTheme.color.primary)
            }
            Column(Modifier.padding(start = 13.dp)) {
                Text("Add secure entry", color = TwTheme.color.onSurfacePrimary, fontWeight = FontWeight.Bold)
                Text("Everything is optional — add what you need", color = TwTheme.color.onSurfaceSecondary)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EntryCard(
    entry: SecretEntry,
    managerType: SecretManagerType,
    isDragging: Boolean = false,
    isReordering: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    canMoveUp: Boolean = false,
    canMoveDown: Boolean = false,
) {
    val view = LocalView.current
    val preview = entry.fields
        .firstOrNull { it.kind != SecretFieldKind.PASSWORD && it.value.isNotBlank() }
        ?.value
    val signal = if (managerType == SecretManagerType.WIFI) {
        entry.fields.firstOrNull { it.label == "Signal strength" }?.value
    } else {
        null
    }
    val connectionStatus = if (managerType == SecretManagerType.WIFI) {
        entry.fields.firstOrNull { it.label == "Connection status" }?.value
    } else {
        null
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                enabled = !isReordering,
                onClick = onClick,
                onLongClick = {
                    if (!isReordering) {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        onLongClick()
                    }
                },
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, TwTheme.color.glassOutline),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            if (isDragging) TwTheme.color.primary.copy(alpha = .38f) else TwTheme.color.glassSurfaceStrong.copy(alpha = .9f),
                            TwTheme.color.backgroundSecondary.copy(alpha = .96f),
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(TwTheme.color.primary.copy(alpha = .5f), TwTheme.color.primary.copy(alpha = .12f))
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) { Text(entry.title.take(1).uppercase().ifBlank { "+" }, color = Color.White, fontWeight = FontWeight.Bold) }
                Column(Modifier.padding(start = 13.dp).weight(1f)) {
                    Text(entry.title.ifBlank { "Untitled entry" }, color = TwTheme.color.onSurfacePrimary, fontWeight = FontWeight.Bold)
                    Text(
                        "${entry.fields.size} fields  •  ${entry.attachments.size} attachments" +
                            if (entry.tags.isNotEmpty()) "  •  ${entry.tags.take(2).joinToString(" · ")}" else "",
                        color = TwTheme.color.onSurfaceSecondary,
                    )
                    preview?.let {
                        Text(it.take(56), color = TwTheme.color.onSurfaceTertiary, maxLines = 1)
                    }
                    if (managerType == SecretManagerType.WIFI) {
                        Text(
                            listOfNotNull(signal?.takeIf(String::isNotBlank)?.let { "$it dBm" }, connectionStatus?.takeIf(String::isNotBlank))
                                .joinToString("  •  "),
                            color = TwTheme.color.onSurfaceTertiary,
                            maxLines = 1,
                        )
                    }
                }
                if (entry.pinned) Icon(painter = TwIcons.Favorite, contentDescription = "Pinned", tint = TwTheme.color.primary)
                if (isReordering) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = onMoveUp,
                            enabled = canMoveUp,
                            modifier = Modifier.size(34.dp),
                        ) {
                            Icon(
                                painter = TwIcons.ChevronUp,
                                contentDescription = "Move entry up",
                                tint = if (canMoveUp) TwTheme.color.primary else TwTheme.color.onSurfaceTertiary,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        IconButton(
                            onClick = onMoveDown,
                            enabled = canMoveDown,
                            modifier = Modifier.size(34.dp),
                        ) {
                            Icon(
                                painter = TwIcons.ChevronDown,
                                contentDescription = "Move entry down",
                                tint = if (canMoveDown) TwTheme.color.primary else TwTheme.color.onSurfaceTertiary,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                } else {
                    Icon(painter = TwIcons.ChevronRight, contentDescription = "Open", tint = TwTheme.color.onSurfaceSecondary)
                }
            }
        }
    }
}

@Composable
private fun EntryActionsDialog(
    entry: SecretEntry,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPin: () -> Unit,
    onHide: () -> Unit,
) {
    SecretActionsDialog(
        eyebrow = "SECURE ENTRY",
        title = entry.title.ifBlank { "Untitled entry" },
        status = if (entry.pinned) "PINNED • ENCRYPTED RECORD" else "ENCRYPTED LOCAL RECORD",
        icon = TwIcons.More,
        accent = SecretDialogAccent,
        actions = listOf(
            SecretDialogAction(
                label = "Edit entry",
                icon = TwIcons.Edit,
                onClick = onEdit,
            ),
            SecretDialogAction(
                label = if (entry.pinned) "Remove from pinned" else "Pin entry",
                icon = TwIcons.Favorite,
                onClick = onPin,
            ),
            SecretDialogAction(
                label = if (entry.hidden) "Reveal entry" else "Hide entry",
                icon = if (entry.hidden) TwIcons.Eye else TwIcons.EyeSlash,
                onClick = onHide,
            ),
            SecretDialogAction(
                label = "Delete entry",
                icon = TwIcons.Delete,
                destructive = true,
                onClick = onDelete,
            ),
        ),
        onDismiss = onDismiss,
    )
}

@Composable
private fun EntryDetailsDialog(
    entry: SecretEntry,
    onDismiss: () -> Unit,
    onOpenAttachment: (SecretAttachment) -> Unit,
) {
    val context = LocalContext.current
    var revealedFields by remember { mutableStateOf(emptySet<String>()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = SecretDialogShape,
        containerColor = SecretDialogColor,
        titleContentColor = TwTheme.color.onSurfacePrimary,
        textContentColor = TwTheme.color.onSurfaceSecondary,
        icon = {
            Box(
                Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF7E56E8), SecretDialogAccent.copy(alpha = .55f)))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painter = TwIcons.Lock, contentDescription = null, tint = Color.White)
            }
        },
        title = {
            DialogTitleBlock(
                "PROTECTED ENTRY",
                entry.title.ifBlank { "Untitled entry" },
                "Reveal or copy individual values without leaving this view.",
            )
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                entry.fields.forEach { field ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SecretDialogPanel)
                            .border(1.dp, TwTheme.color.glassOutline, RoundedCornerShape(16.dp))
                            .padding(start = 12.dp, top = 10.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(field.label, color = Color(0xFFB9A7D8), style = TwTheme.typo.caption)
                            Text(
                                if (field.kind == SecretFieldKind.PASSWORD && field.id !in revealedFields) {
                                    "••••••••"
                                } else {
                                    field.value.ifBlank { "—" }
                                },
                                color = TwTheme.color.onSurfacePrimary,
                            )
                        }
                        if (field.kind == SecretFieldKind.PASSWORD) {
                            IconButton(onClick = {
                                revealedFields = if (field.id in revealedFields) {
                                    revealedFields - field.id
                                } else {
                                    revealedFields + field.id
                                }
                            }) {
                                Icon(
                                    painter = if (field.id in revealedFields) TwIcons.EyeSlash else TwIcons.Eye,
                                    contentDescription = if (field.id in revealedFields) {
                                        "Hide ${field.label}"
                                    } else {
                                        "Show ${field.label}"
                                    },
                                    tint = SecretDialogAccent,
                                )
                            }
                        }
                        IconButton(onClick = {
                            context.copySensitiveToClipboard(field.label, field.value)
                        }) { Icon(painter = TwIcons.Copy, contentDescription = "Copy ${field.label}", tint = Color(0xFFD4C1EF)) }
                    }
                }
                if (entry.tags.isNotEmpty()) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF21183A))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        DialogEyebrow("TAGS")
                        Text(entry.tags.joinToString("  •  "), color = TwTheme.color.primary)
                    }
                }
                if (entry.note.isNotBlank()) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF21183A))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        DialogEyebrow("NOTE")
                        Text(entry.note, color = TwTheme.color.onSurfacePrimary)
                    }
                }
                entry.attachments.forEach { attachment ->
                    Button(
                        onClick = { onOpenAttachment(attachment) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3A2B5A),
                            contentColor = Color(0xFFEEDFFF),
                        ),
                    ) {
                        Icon(painter = TwIcons.ExternalLink, contentDescription = null)
                        Text("  Open ${attachment.name}")
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss, shape = RoundedCornerShape(14.dp), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = SecretDialogAccent, contentColor = Color(0xFF321748))) { Text("Done", fontWeight = FontWeight.Bold) } },
    )
}

@Composable
private fun EntryEditorDialog(
    manager: SecretManager,
    initial: SecretEntry,
    onDismiss: () -> Unit,
    onSave: (SecretEntry) -> Unit,
    onSaveAttachment: (Uri) -> SecretAttachment?,
    onReadAttachment: (SecretAttachment) -> ByteArray?,
    onSaveCredential: (String, String) -> Unit,
) {
    var title by remember { mutableStateOf(initial.title) }
    var fields by remember { mutableStateOf(initial.fields) }
    var note by remember { mutableStateOf(initial.note) }
    var tagsText by remember { mutableStateOf(initial.tags.joinToString(", ")) }
    var attachments by remember { mutableStateOf(initial.attachments) }
    var showAddField by remember { mutableStateOf(false) }
    var generatorFor by remember { mutableStateOf<String?>(null) }
    var dateFor by remember { mutableStateOf<String?>(null) }
    var fieldAction by remember { mutableStateOf<SecretField?>(null) }
    var previewAttachment by remember { mutableStateOf<SecretAttachment?>(null) }
    var showAttachmentSource by remember { mutableStateOf(false) }
    var isReorderingFields by remember { mutableStateOf(false) }
    val fieldListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val fieldReorderState = org.burnoutcrew.reorderable.rememberReorderableLazyListState(
        listState = fieldListState,
        onMove = { from, to ->
            val fromIndex = from.index - 1
            val toIndex = to.index - 1
            if (fromIndex in fields.indices && toIndex in fields.indices) {
                fields = fields.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
            }
        },
    )
    val context = LocalContext.current
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        onSaveAttachment(uri)?.let { attachments = attachments + it }
            ?: android.widget.Toast.makeText(context, "Unable to securely import attachment", android.widget.Toast.LENGTH_SHORT).show()
    }
    val voltSharePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode != android.app.Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val uris = result.data?.voltShareResultUris().orEmpty()
        if (uris.isEmpty()) return@rememberLauncherForActivityResult

        val imported = uris.mapNotNull { uri -> onSaveAttachment(uri) }
        if (imported.isNotEmpty()) {
            attachments = attachments + imported
        }
        if (imported.size != uris.size) {
            android.widget.Toast.makeText(
                context,
                "${imported.size} of ${uris.size} attachment${if (uris.size == 1) "" else "s"} imported",
                android.widget.Toast.LENGTH_SHORT,
            ).show()
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(color = TwTheme.color.background, modifier = Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TwTopAppBar(
                        title = { Column { Text("Secure entry"); Text(manager.name, color = TwTheme.color.onSurfaceSecondary, style = TwTheme.typo.caption) } },
                        actions = {
                            IconButton(onClick = { isReorderingFields = !isReorderingFields }) {
                                Icon(
                                    painter = if (isReorderingFields) TwIcons.Check else TwIcons.DragHandle,
                                    contentDescription = if (isReorderingFields) "Done reordering fields" else "Reorder fields",
                                )
                            }
                            IconButton(onClick = onDismiss) { Icon(painter = TwIcons.Close, contentDescription = "Close") }
                        },
                        showBackButton = false,
                    )
                },
                bottomBar = {
                    Button(
                        onClick = {
                            onSave(
                                initial.copy(
                                    title = title.trim(),
                                    fields = fields,
                                    note = note,
                                    tags = tagsText.split(",").map(String::trim).filter(String::isNotBlank).distinct(),
                                    attachments = attachments,
                                    createdAt = initial.createdAt.takeIf { it > 0L } ?: System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis(),
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) { Text("Save securely") }
                },
            ) { padding ->
                LazyColumn(
                    Modifier.fillMaxSize().padding(padding).reorderable(fieldReorderState),
                    state = fieldListState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        SecretInput(
                            value = title,
                            onValueChange = { title = it },
                            label = "Title (optional)",
                        )
                    }
                    itemsIndexed(fields, key = { _, field -> field.id }) { index, field ->
                        ReorderableItem(
                            state = fieldReorderState,
                            key = field.id,
                        ) { dragging ->
                            SecretFieldEditor(
                                field = field,
                                isDragging = dragging,
                                    isReordering = isReorderingFields,
                                dragModifier = if (isReorderingFields) Modifier.detectReorderAfterLongPress(fieldReorderState) else Modifier,
                                onValueChange = { value -> fields = fields.map { if (it.id == field.id) it.copy(value = value) else it } },
                                onLongPress = { fieldAction = field },
                                onGenerate = { generatorFor = field.id },
                                onPickDate = { dateFor = field.id },
                            )
                        }
                    }
                    item {
                        OutlinedButton(onClick = { showAddField = true }, modifier = Modifier.fillMaxWidth()) {
                            Icon(painter = TwIcons.Add, contentDescription = null)
                            Text("  Add more field")
                        }
                    }
                    item {
                        SecretInput(
                            value = note,
                            onValueChange = { note = it },
                            label = if (manager.type == SecretManagerType.NOTES) "Note content (optional)" else "Private note (optional)",
                            minLines = if (manager.type == SecretManagerType.NOTES) 8 else 4,
                        )
                    }
                    item {
                        SecretInput(
                            value = tagsText,
                            onValueChange = { tagsText = it },
                            label = "Tags (optional, comma separated)",
                        )
                    }
                    item {
                        OutlinedButton(onClick = { showAttachmentSource = true }, modifier = Modifier.fillMaxWidth()) {
                            Icon(painter = TwIcons.Export, contentDescription = null)
                            Text("  Attach any file")
                        }
                    }
                    items(attachments, key = { it.id }) { attachment ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { previewAttachment = attachment }
                                .background(TwTheme.color.glassSurface, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(painter = TwIcons.ExternalLink, contentDescription = null, tint = TwTheme.color.primary)
                            Text(attachment.name, Modifier.weight(1f).padding(horizontal = 10.dp), color = TwTheme.color.onSurfacePrimary)
                            IconButton(onClick = { attachments = attachments.filterNot { it.id == attachment.id } }) {
                                Icon(painter = TwIcons.Delete, contentDescription = "Remove attachment")
                            }
                        }
                    }
                }
            }
        }
    }
    if (showAttachmentSource) {
        AttachmentSourceDialog(
            onDismiss = { showAttachmentSource = false },
            onDeviceStorage = {
                showAttachmentSource = false
                filePicker.launch(arrayOf("*/*"))
            },
            onVoltShareVault = {
                showAttachmentSource = false
                try {
                    voltSharePicker.launch(
                        Intent(VOLTSHARE_PICK_ACTION).setPackage(VOLTSHARE_PACKAGE),
                    )
                } catch (_: ActivityNotFoundException) {
                    android.widget.Toast.makeText(
                        context,
                        "VoltShare is not installed on this device",
                        android.widget.Toast.LENGTH_SHORT,
                    ).show()
                } catch (_: SecurityException) {
                    android.widget.Toast.makeText(
                        context,
                        "VoltShare is not authorized for this app",
                        android.widget.Toast.LENGTH_LONG,
                    ).show()
                }
            },
        )
    }
    if (showAddField) {
        AddFieldDialog(
            manager = manager,
            existingFields = fields,
            onDismiss = { showAddField = false },
            onAdd = { field -> fields = fields + field; showAddField = false },
        )
    }
    generatorFor?.let { fieldId ->
        PasswordGeneratorDialog(
            onDismiss = { generatorFor = null },
            onUsePassword = { generated ->
                fields = fields.map { if (it.id == fieldId) it.copy(value = generated) else it }
                generatorFor = null
            },
            onSaveCredential = { name, password ->
                onSaveCredential(name, password)
                generatorFor = null
            },
        )
    }
    dateFor?.let { fieldId ->
        DateRollerDialog(
            initial = fields.firstOrNull { it.id == fieldId }?.value,
            onDismiss = { dateFor = null },
            onDateSelected = { value ->
                fields = fields.map { if (it.id == fieldId) it.copy(value = value) else it }
                dateFor = null
            },
        )
    }
    fieldAction?.let { field ->
        AlertDialog(
            onDismissRequest = { fieldAction = null },
            shape = SecretDialogShape,
            containerColor = SecretDialogColor,
            titleContentColor = TwTheme.color.onSurfacePrimary,
            textContentColor = TwTheme.color.onSurfaceSecondary,
            icon = {
                Box(
                    Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.linearGradient(listOf(SecretDialogAccent.copy(alpha = .55f), Color(0xFF7E56E8)))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painter = TwIcons.More, contentDescription = null, tint = Color.White)
                }
            },
            title = { DialogTitleBlock("FIELD ACTIONS", field.label, "Choose how to manage this optional field.") },
            text = { Text("Fields can be removed or reordered without changing the encrypted entry.") },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = {
                        fields = fields.filterNot { it.id == field.id }
                        fieldAction = null
                    }) { Text("Remove", color = TwTheme.color.error) }
                    TextButton(onClick = {
                        isReorderingFields = true
                        fieldAction = null
                    }) { Text("Reorder", color = SecretDialogAccent) }
                }
            },
            dismissButton = { TextButton(onClick = { fieldAction = null }) { Text("Cancel", color = TwTheme.color.onSurfaceSecondary) } },
        )
    }
    previewAttachment?.let { attachment ->
        val bytes = onReadAttachment(attachment)
        AttachmentViewerDialog(
            attachment = attachment,
            bytes = bytes,
            onDismiss = { previewAttachment = null },
            onOpenExternal = {
                openAttachmentExternally(context, attachment)
            },
        )
    }
}

@Composable
private fun AttachmentSourceDialog(
    onDismiss: () -> Unit,
    onDeviceStorage: () -> Unit,
    onVoltShareVault: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            color = SecretDialogColor,
            shape = SecretDialogShape,
            border = BorderStroke(1.dp, SecretDialogAccent.copy(alpha = .42f)),
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DialogTitleBlock(
                    eyebrow = "SECURE ATTACHMENT",
                    title = "Choose a source",
                    subtitle = "Add a file from your device or from the private VoltShare vault.",
                )
                AttachmentSourceOption(
                    icon = TwIcons.Lock,
                    title = "VoltShare Vault",
                    description = "Pick one or more unlocked files from VoltShare.",
                    accent = SecretDialogAccent,
                    onClick = onVoltShareVault,
                )
                AttachmentSourceOption(
                    icon = TwIcons.Export,
                    title = "Device storage",
                    description = "Use the existing Android storage picker.",
                    accent = TwTheme.color.primary,
                    onClick = onDeviceStorage,
                )
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("Cancel", color = TwTheme.color.onSurfaceSecondary)
                }
            }
        }
    }
}

@Composable
private fun AttachmentSourceOption(
    icon: androidx.compose.ui.graphics.painter.Painter,
    title: String,
    description: String,
    accent: Color,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = SecretDialogPanel,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = .35f)),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = .16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painter = icon, contentDescription = null, tint = accent)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 13.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(title, color = TwTheme.color.onSurfacePrimary, fontWeight = FontWeight.Bold)
                Text(description, color = TwTheme.color.onSurfaceSecondary, style = TwTheme.typo.caption)
            }
            Icon(
                painter = TwIcons.ChevronRight,
                contentDescription = null,
                tint = TwTheme.color.onSurfaceSecondary,
            )
        }
    }
}

private fun Intent.voltShareResultUris(): List<Uri> {
    val result = LinkedHashSet<Uri>()
    data?.let(result::add)
    clipData?.let { clip ->
        for (index in 0 until clip.itemCount) {
            clip.getItemAt(index).uri?.let(result::add)
        }
    }
    @Suppress("DEPRECATION")
    getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let(result::add)
    @Suppress("DEPRECATION")
    getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.forEach(result::add)
    return result.toList()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SecretFieldEditor(
    field: SecretField,
    isDragging: Boolean,
    isReordering: Boolean,
    dragModifier: Modifier,
    onValueChange: (String) -> Unit,
    onLongPress: () -> Unit,
    onGenerate: () -> Unit,
    onPickDate: () -> Unit,
) {
    val password = field.kind == SecretFieldKind.PASSWORD
    val date = field.label.contains("date", true) || field.label.contains("expiry", true)
    val view = LocalView.current
    Column(
        Modifier
            .fillMaxWidth()
            .combinedClickable(
                enabled = !isReordering,
                onClick = {},
                onLongClick = {
                    if (!isReordering) {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        onLongPress()
                    }
                },
            )
            .then(dragModifier)
            .background(
                Brush.linearGradient(
                    listOf(
                        if (isDragging) TwTheme.color.primary.copy(alpha = .28f) else TwTheme.color.glassSurfaceStrong.copy(alpha = .9f),
                        TwTheme.color.backgroundSecondary.copy(alpha = .72f),
                    )
                ),
                RoundedCornerShape(22.dp),
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(field.label, color = TwTheme.color.onSurfacePrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(
                if (isReordering) "Drag to move" else "Optional",
                color = TwTheme.color.onSurfaceTertiary,
                style = TwTheme.typo.caption,
            )
        }
        SecretInput(
            value = field.value,
            onValueChange = onValueChange,
            label = "Enter ${field.label.lowercase()}",
            password = password,
            minLines = if (field.kind == SecretFieldKind.MULTILINE) 3 else 1,
            onGenerate = if (password && field.label.equals("Password", true)) onGenerate else null,
            onPickDate = if (date) onPickDate else null,
        )
    }
}

@Composable
private fun SecretInput(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    password: Boolean = false,
    minLines: Int = 1,
    onGenerate: (() -> Unit)? = null,
    onPickDate: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    var revealed by remember { mutableStateOf(false) }
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label) },
        minLines = minLines,
        singleLine = minLines == 1,
        shape = RoundedCornerShape(18.dp),
        visualTransformation = if (password && !revealed) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = TwTheme.color.backgroundSecondary.copy(alpha = .76f),
            unfocusedContainerColor = TwTheme.color.backgroundSecondary.copy(alpha = .52f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = TwTheme.color.primary,
            focusedLabelColor = TwTheme.color.primary,
            unfocusedLabelColor = TwTheme.color.onSurfaceSecondary,
            focusedTextColor = TwTheme.color.onSurfacePrimary,
            unfocusedTextColor = TwTheme.color.onSurfacePrimary,
        ),
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    clipboard.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()?.let(onValueChange)
                }) {
                    Icon(painter = TwIcons.Import, contentDescription = "Paste into $label", tint = TwTheme.color.primary)
                }
                if (password) {
                    IconButton(onClick = { revealed = !revealed }) {
                        Icon(
                            painter = if (revealed) TwIcons.EyeSlash else TwIcons.Eye,
                            contentDescription = if (revealed) "Hide $label" else "Show $label",
                            tint = TwTheme.color.onSurfaceSecondary,
                        )
                    }
                }
                onGenerate?.let {
                    IconButton(onClick = it) {
                        Icon(painter = TwIcons.Refresh, contentDescription = "Generate $label", tint = TwTheme.color.primary)
                    }
                }
                onPickDate?.let {
                    IconButton(onClick = it) {
                        Icon(painter = TwIcons.Time, contentDescription = "Pick $label", tint = TwTheme.color.primary)
                    }
                }
            }
        },
    )
}

@Composable
private fun AddFieldDialog(
    manager: SecretManager,
    existingFields: List<SecretField>,
    onDismiss: () -> Unit,
    onAdd: (SecretField) -> Unit,
) {
    val additionalOptions = when (manager.type) {
        SecretManagerType.CARDS -> listOf("Bank / issuer", "Billing address", "Card nickname")
        SecretManagerType.IDENTITIES -> listOf("Driving license number", "Aadhaar number", "PAN number")
        SecretManagerType.PASSWORDS -> listOf("Recovery code", "Security question", "Passkey")
        SecretManagerType.API_KEYS -> listOf("Organization", "Region", "Webhook secret")
        else -> listOf("Recovery code", "Security question", "Passkey")
    }
    val options = (manager.type.defaultFields() + additionalOptions.map { option ->
        SecretField(
            id = "preset-${manager.type.name}-$option",
            label = option,
            kind = if (
                option.contains("code", true) ||
                option.contains("secret", true) ||
                option.contains("passkey", true)
            ) SecretFieldKind.PASSWORD else SecretFieldKind.TEXT,
        )
    }).filterNot { option ->
        existingFields.any { it.label.equals(option.label, ignoreCase = true) }
    }
    var custom by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = SecretDialogShape,
        containerColor = SecretDialogColor,
        titleContentColor = TwTheme.color.onSurfacePrimary,
        textContentColor = TwTheme.color.onSurfaceSecondary,
        icon = {
            Box(
                Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(SecretDialogAccent.copy(alpha = .55f), Color(0xFF7E56E8)))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painter = TwIcons.Add, contentDescription = null, tint = Color.White)
            }
        },
        title = { DialogTitleBlock("ENTRY STRUCTURE", "Add more field", "Choose a secure preset or create your own.") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                if (!custom) {
                    DialogEyebrow("FIELD PRESETS")
                    options.forEach { option ->
                        Button(
                            onClick = { onAdd(option.copy(id = UUID.randomUUID().toString())) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(15.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = SecretDialogPanel,
                                contentColor = TwTheme.color.onSurfacePrimary,
                            ),
                        ) {
                            Text(option.label, modifier = Modifier.fillMaxWidth())
                        }
                    }
                    Button(
                        onClick = { custom = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = SecretDialogAccent,
                            contentColor = Color(0xFF321748),
                        ),
                    ) { Text("Create custom field", fontWeight = FontWeight.Bold) }
                } else {
                    DialogEyebrow("CUSTOM FIELD")
                    SecretInput(value = name, onValueChange = { name = it }, label = "Field name")
                    SecretInput(value = value, onValueChange = { value = it }, label = "Field value", minLines = 2)
                    Button(
                        onClick = { onAdd(SecretField(UUID.randomUUID().toString(), name, value)) },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = SecretDialogAccent,
                            contentColor = Color(0xFF321748),
                        ),
                    ) { Text("Add custom field", fontWeight = FontWeight.Bold) }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TwTheme.color.onSurfaceSecondary) } },
    )
}

private data class WifiSelection(
    val ssid: String,
    val bssid: String,
    val level: Int,
    val frequency: String,
    val security: String,
    val password: String,
    val status: String,
)

private fun android.net.wifi.ScanResult.isOpenNetwork(): Boolean =
    capabilities.isBlank() || (
        !capabilities.contains("WPA") &&
            !capabilities.contains("RSN") &&
            !capabilities.contains("WEP") &&
            !capabilities.contains("EAP")
        )

private fun android.net.wifi.ScanResult.isUnsupportedNetwork(): Boolean =
    capabilities.contains("WEP") || capabilities.contains("EAP")

private fun android.net.wifi.ScanResult.isPersonalNetwork(): Boolean =
    capabilities.contains("WPA") || capabilities.contains("RSN")

private fun wifiPermissions(): Array<String> = buildList {
    add(android.Manifest.permission.ACCESS_FINE_LOCATION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(android.Manifest.permission.NEARBY_WIFI_DEVICES)
    }
}.toTypedArray()

@Composable
private fun WifiDiscoveryDialog(onDismiss: () -> Unit, onSelected: (WifiSelection) -> Unit) {
    val context = LocalContext.current
    var networks by remember { mutableStateOf<List<android.net.wifi.ScanResult>>(emptyList()) }
    var selected by remember { mutableStateOf<android.net.wifi.ScanResult?>(null) }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var verifying by remember { mutableStateOf(false) }
    var scanRequest by remember { mutableStateOf(0) }
    val selectedNeedsPassword = selected?.isPersonalNetwork() == true
    var needsPermission by remember {
        mutableStateOf(
            wifiPermissions().any {
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    it,
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        needsPermission = wifiPermissions().any { result[it] != true &&
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                it,
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }
    LaunchedEffect(needsPermission, scanRequest) {
        if (!needsPermission) {
            val wifi = context.applicationContext.getSystemService(android.content.Context.WIFI_SERVICE) as android.net.wifi.WifiManager
            runCatching {
                wifi.startScan()
                delay(900L)
                networks = wifi.scanResults.filter { it.SSID.isNotBlank() }.distinctBy { it.BSSID }
                error = if (networks.isEmpty()) "No visible network names found. Try scanning again." else null
            }.onFailure { error = "Unable to scan nearby networks on this device." }
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = SecretDialogShape,
        containerColor = SecretDialogColor,
        titleContentColor = TwTheme.color.onSurfacePrimary,
        textContentColor = TwTheme.color.onSurfaceSecondary,
        icon = {
            Box(
                Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF62A4FF), Color(0xFF8D7CFF)))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painter = TwIcons.Cloud, contentDescription = null, tint = Color.White)
            }
        },
        title = {
            DialogTitleBlock(
                "NETWORK VAULT",
                if (selected == null) "Nearby Wi-Fi" else "Verify ${selected?.SSID}",
                if (selected == null) "Discover a network and securely save its details." else "Confirm the connection before saving it.",
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                when {
                    needsPermission -> {
                        Text("Android requires nearby-device access to discover Wi-Fi names. Secrets only reads scan results and never changes your connection.")
                        Button(
                            onClick = { permission.launch(wifiPermissions()) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(15.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = SecretDialogAccent,
                                contentColor = Color(0xFF321748),
                            ),
                        ) { Text("Allow discovery", fontWeight = FontWeight.Bold) }
                    }
                    selected == null -> {
                        Text("Select a network to verify and save its connection details.", color = TwTheme.color.onSurfaceSecondary)
                        if (networks.isEmpty()) {
                            Button(
                                onClick = { scanRequest++ },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(15.dp),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = SecretDialogPanel,
                                    contentColor = Color(0xFFEEDFFF),
                                ),
                            ) { Text("Scan nearby networks") }
                        } else {
                            networks.forEach { network ->
                                Button(
                                    onClick = { selected = network; error = null },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(15.dp),
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                        containerColor = SecretDialogPanel,
                                        contentColor = TwTheme.color.onSurfacePrimary,
                                    ),
                                ) {
                                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                                        Text(network.SSID, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "${network.level} dBm  •  ${network.frequency} MHz  •  ${network.capabilities.ifBlank { "Open network" }}",
                                            style = TwTheme.typo.caption,
                                            color = TwTheme.color.onSurfaceSecondary,
                                            maxLines = 1,
                                        )
                                        Text("BSSID  ${network.BSSID}", style = TwTheme.typo.caption, color = Color(0xFF8E7BAF), maxLines = 1)
                                    }
                                }
                            }
                            TextButton(onClick = { scanRequest++ }) { Text("Scan again", color = SecretDialogAccent) }
                        }
                    }
                    else -> {
                        val network = selected ?: return@Column
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(SecretDialogPanel)
                                .border(1.dp, TwTheme.color.glassOutline, RoundedCornerShape(18.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Text(network.SSID, color = TwTheme.color.onSurfacePrimary, fontWeight = FontWeight.Bold)
                            Text(
                                "${network.level} dBm  •  ${network.frequency} MHz",
                                color = Color(0xFFB9A7D8),
                                style = TwTheme.typo.caption,
                            )
                            Text("BSSID  ${network.BSSID}", color = Color(0xFF8E7BAF), style = TwTheme.typo.caption)
                            Text(network.capabilities.ifBlank { "Open network" }, color = Color(0xFF8E7BAF), style = TwTheme.typo.caption, maxLines = 2)
                        }
                        when {
                            selected?.isUnsupportedNetwork() == true -> {
                                Text(
                                    "This network uses WEP or enterprise authentication, which Android does not allow this verifier to configure safely.",
                                    color = TwTheme.color.error,
                                )
                            }
                            selected?.isOpenNetwork() == true -> {
                                Text("Open network — no password is required. Continue to verify availability.", color = TwTheme.color.onSurfaceSecondary)
                            }
                            else -> {
                                Text("The password is checked against this device's Wi-Fi connection.", color = TwTheme.color.onSurfaceSecondary)
                                SecretInput(
                                    value = password,
                                    onValueChange = { password = it; error = null },
                                    label = "Wi-Fi password",
                                    password = true,
                                )
                            }
                        }
                        error?.let {
                            Text(
                                it,
                                color = TwTheme.color.error,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF4A1E3A).copy(alpha = .55f))
                                    .padding(12.dp),
                            )
                        }
                        if (verifying) {
                            Text("Verifying connection…", color = SecretDialogAccent, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selected != null) {
                Button(
                    enabled = !selectedNeedsPassword && selected?.isUnsupportedNetwork() != true && !verifying ||
                        selectedNeedsPassword && password.isNotEmpty() && !verifying,
                    onClick = {
                        val network = selected ?: return@Button
                        if (network.isUnsupportedNetwork()) {
                            error = "This Wi-Fi security type is not supported for verification."
                            return@Button
                        }
                        val enteredPassword = password
                        val selectedNetwork = network
                        verifying = true
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val connectivity = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
                            var callback: android.net.ConnectivityManager.NetworkCallback? = null
                            fun finish(status: String) {
                                Handler(Looper.getMainLooper()).post {
                                    if (!verifying) return@post
                                    verifying = false
                                    callback?.let { registeredCallback ->
                                        runCatching { connectivity.unregisterNetworkCallback(registeredCallback) }
                                    }
                                    onSelected(
                                        WifiSelection(
                                            selectedNetwork.SSID,
                                            selectedNetwork.BSSID,
                                            selectedNetwork.level,
                                            "${selectedNetwork.frequency} MHz",
                                            selectedNetwork.capabilities.ifBlank { "Open network" },
                                            enteredPassword,
                                            status,
                                        )
                                    )
                                }
                            }
                            runCatching {
                                val specifier = android.net.wifi.WifiNetworkSpecifier.Builder()
                                    .setSsid(network.SSID)
                                    .apply {
                                        if (network.isPersonalNetwork()) {
                                            setWpa2Passphrase(password)
                                        }
                                    }
                                    .build()
                                val request = android.net.NetworkRequest.Builder()
                                    .addTransportType(android.net.NetworkCapabilities.TRANSPORT_WIFI)
                                    .setNetworkSpecifier(specifier)
                                    .build()
                                callback = object : android.net.ConnectivityManager.NetworkCallback() {
                                    override fun onAvailable(networkHandle: android.net.Network) {
                                        finish("Connected and verified")
                                    }

                                    override fun onUnavailable() {
                                        finish("Saved — Android verification unavailable")
                                    }
                                }
                                connectivity.requestNetwork(request, callback!!)
                                Handler(Looper.getMainLooper()).postDelayed(
                                    { finish("Saved — verification timed out") },
                                    8_000L,
                                )
                            }.onFailure { finish("Saved — Android verification unavailable") }
                        } else {
                            verifying = false
                            onSelected(
                                WifiSelection(
                                    selectedNetwork.SSID,
                                    selectedNetwork.BSSID,
                                    selectedNetwork.level,
                                    "${selectedNetwork.frequency} MHz",
                                    selectedNetwork.capabilities.ifBlank { "Open network" },
                                    enteredPassword,
                                    "Saved — verification unavailable on this Android version",
                                )
                            )
                        }
                    },
                    shape = RoundedCornerShape(15.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = SecretDialogAccent,
                        contentColor = Color(0xFF321748),
                    ),
                ) { Text("Verify & save", fontWeight = FontWeight.Bold) }
            }
        },
        dismissButton = {
            TextButton(onClick = { if (selected != null) selected = null else onDismiss() }) {
                Text(if (selected != null) "Back" else "Cancel", color = TwTheme.color.onSurfaceSecondary)
            }
        },
    )
}

@Composable
private fun DateRollerDialog(
    initial: String?,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit,
) {
    val calendar = remember {
        Calendar.getInstance().apply {
            runCatching {
                initial?.takeIf { it.isNotBlank() }?.let {
                    time = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it) ?: time
                }
            }
        }
    }
    var year by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var month by remember { mutableStateOf(calendar.get(Calendar.MONTH) + 1) }
    var day by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    val maximumDay = remember(year, month) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    LaunchedEffect(maximumDay) {
        day = day.coerceAtMost(maximumDay)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = SecretDialogShape,
        containerColor = SecretDialogColor,
        titleContentColor = TwTheme.color.onSurfacePrimary,
        textContentColor = TwTheme.color.onSurfaceSecondary,
        icon = {
            Box(
                Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF9E54F5), Color(0xFF62A4FF)))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painter = TwIcons.Time, contentDescription = null, tint = Color.White)
            }
        },
        title = {
            DialogTitleBlock(
                "SECURITY LIFECYCLE",
                "Choose expiry date",
                "Set when this secret should be reviewed or rotated.",
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("YEAR  •  MONTH  •  DAY", color = Color(0xFFB9A7D8), fontWeight = FontWeight.Bold, style = TwTheme.typo.caption)
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SecretDialogPanel)
                        .border(1.dp, TwTheme.color.glassOutline, RoundedCornerShape(20.dp))
                        .padding(8.dp),
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Roller(
                            value = year,
                            range = 2020..2100,
                            onValueChanged = {
                                year = it
                                day = day.coerceAtMost(daysInMonth(it, month))
                            },
                            modifier = Modifier.weight(1f),
                        )
                        Roller(
                            value = month,
                            range = 1..12,
                            onValueChanged = {
                                month = it
                                day = day.coerceAtMost(daysInMonth(year, it))
                            },
                            modifier = Modifier.weight(1f),
                        )
                        Roller(
                            value = day,
                            range = 1..maximumDay,
                            onValueChanged = { day = it },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onDateSelected("%04d-%02d-%02d".format(Locale.US, year, month, day)) },
                shape = RoundedCornerShape(15.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = SecretDialogAccent,
                    contentColor = Color(0xFF321748),
                ),
            ) { Text("Set date", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TwTheme.color.onSurfaceSecondary) } },
    )
}

private fun daysInMonth(year: Int, month: Int): Int =
    Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, 1)
    }.getActualMaximum(Calendar.DAY_OF_MONTH)

@Composable
private fun Roller(
    value: Int,
    range: IntRange,
    onValueChanged: (Int) -> Unit,
    modifier: Modifier,
) {
    AndroidView(
        modifier = modifier.height(112.dp),
        factory = { context ->
            NumberPicker(context).apply {
                minValue = range.first
                maxValue = range.last
                this.value = value.coerceIn(range)
                wrapSelectorWheel = true
                setOnValueChangedListener { _, _, newValue -> onValueChanged(newValue) }
            }
        },
        update = { picker ->
            if (picker.value != value) picker.value = value.coerceIn(range)
        },
    )
}

@Composable
private fun AttachmentViewerDialog(
    attachment: SecretAttachment,
    bytes: ByteArray?,
    onDismiss: () -> Unit,
    onOpenExternal: () -> Unit,
) {
    val context = LocalContext.current
    val mime = attachment.mimeType.lowercase(Locale.US)
    val extension = attachment.name.substringAfterLast('.', "").lowercase(Locale.US)
    val isImage = mime.startsWith("image/")
    val isPdf = mime == "application/pdf" || extension == "pdf"
    val isText = mime.startsWith("text/") ||
        mime.contains("json") ||
        mime.contains("xml") ||
        mime.contains("javascript") ||
        extension in setOf(
            "txt", "text", "log", "md", "markdown", "csv", "tsv", "json", "xml",
            "html", "htm", "css", "js", "ts", "kt", "java", "py", "rb", "go",
            "rs", "swift", "sql", "sh", "bash", "yml", "yaml", "toml", "ini",
            "conf", "properties", "env", "svg",
        )
    val textContent = bytes?.toString(Charsets.UTF_8).orEmpty()
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            color = SecretDialogColor,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0B081A), SecretDialogColor, Color(0xFF0D091D))
                    )
                ),
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TwTopAppBar(
                        title = { Text("Attachment", fontWeight = FontWeight.SemiBold) },
                        showBackButton = false,
                        actions = { IconButton(onClick = onDismiss) { Icon(painter = TwIcons.Close, contentDescription = "Close viewer") } },
                    )
                },
            ) { padding ->
                Column(
                    Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AttachmentViewerHeader(
                        attachment = attachment,
                        isText = isText,
                        isPdf = isPdf,
                        byteCount = bytes?.size?.toLong() ?: attachment.size,
                    )
                    when {
                        bytes == null -> Text(
                            "This encrypted attachment could not be opened.",
                            color = TwTheme.color.error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF4A1E3A).copy(alpha = .55f))
                                .padding(16.dp),
                        )
                        isImage -> AndroidView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(22.dp))
                                .background(SecretDialogPanel)
                                .padding(10.dp),
                            factory = { context ->
                                ImageView(context).apply {
                                    adjustViewBounds = true
                                    scaleType = ImageView.ScaleType.FIT_CENTER
                                    setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                                }
                            },
                        )
                        isPdf -> PdfAttachmentPreview(
                            bytes = bytes,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(22.dp))
                                .background(SecretDialogPanel),
                        )
                        isText -> Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(22.dp))
                                .background(SecretDialogPanel)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            DialogEyebrow("TEXT PREVIEW")
                            Text(
                                "Text is readable and can be copied securely.",
                                color = TwTheme.color.onSurfaceSecondary,
                                style = TwTheme.typo.caption,
                            )
                            Text(
                                text = textContent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState()),
                                color = TwTheme.color.onSurfacePrimary,
                            )
                        }
                        else -> Column(
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(22.dp))
                                .background(SecretDialogPanel)
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            DialogEyebrow("UNIVERSAL PREVIEW")
                            Text("This file format is safely decrypted in memory. A hexadecimal preview is shown so no unencrypted copy is stored.", color = TwTheme.color.onSurfaceSecondary)
                            Text(hexPreview(bytes), color = TwTheme.color.onSurfacePrimary)
                        }
                    }
                    if (isText && bytes != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            OutlinedButton(
                                onClick = {
                                    context.copySensitiveToClipboard(attachment.name, textContent)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(15.dp),
                                border = BorderStroke(1.dp, SecretDialogAccent.copy(alpha = .7f)),
                                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                    contentColor = SecretDialogAccent,
                                ),
                            ) {
                                Icon(painter = TwIcons.Copy, contentDescription = null)
                                Text("  Copy file")
                            }
                            Button(
                                onClick = onOpenExternal,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(15.dp),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = SecretDialogAccent,
                                    contentColor = Color(0xFF321748),
                                ),
                            ) {
                                Icon(painter = TwIcons.ExternalLink, contentDescription = null)
                                Text("  Open externally")
                            }
                        }
                    } else {
                        Button(
                            onClick = onOpenExternal,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(15.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = SecretDialogAccent,
                                contentColor = Color(0xFF321748),
                            ),
                        ) {
                            Icon(painter = TwIcons.ExternalLink, contentDescription = null)
                            Text("  Open with another app")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentViewerHeader(
    attachment: SecretAttachment,
    isText: Boolean,
    isPdf: Boolean,
    byteCount: Long,
) {
    val kind = when {
        isPdf -> "PDF DOCUMENT"
        isText -> "TEXT DOCUMENT"
        attachment.mimeType.startsWith("image/") -> "IMAGE ATTACHMENT"
        else -> "SECURE ATTACHMENT"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF2A1C4A), Color(0xFF241A42), Color(0xFF17102F))
                )
            )
            .border(1.dp, SecretDialogAccent.copy(alpha = .3f), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(
                    Brush.linearGradient(
                        listOf(SecretDialogAccent.copy(alpha = .7f), Color(0xFF9E54F5).copy(alpha = .55f))
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painter = TwIcons.Security, contentDescription = null, tint = Color.White)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 13.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            DialogEyebrow(kind)
            Text(
                attachment.name,
                color = TwTheme.color.onSurfacePrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Text(
                "${attachment.mimeType}  •  ${formatAttachmentSize(byteCount)}",
                color = TwTheme.color.onSurfaceSecondary,
                style = TwTheme.typo.caption,
                maxLines = 1,
            )
        }
    }
}

private fun formatAttachmentSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    if (bytes < 1024 * 1024) return "${bytes / 1024} KB"
    return String.format(Locale.US, "%.1f MB", bytes / (1024f * 1024f))
}

@Composable
private fun PdfAttachmentPreview(
    bytes: ByteArray,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val renderer = remember(bytes) { createPdfRenderer(context, bytes) }
    DisposableEffect(renderer) {
        onDispose {
            renderer?.close()
        }
    }

    if (renderer == null || renderer.pageCount == 0) {
        Column(
            modifier = modifier
                .clip(RoundedCornerShape(22.dp))
                .background(SecretDialogPanel)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DialogEyebrow("PDF PREVIEW")
            Text(
                "This PDF could not be rendered in the secure viewer. You can still open it with another app.",
                color = TwTheme.color.onSurfaceSecondary,
            )
        }
        return
    }

    val pageCount = renderer.pageCount
    var pageIndex by remember(bytes) { mutableStateOf(0) }
    val safePageIndex = pageIndex.coerceIn(0, pageCount - 1)

    LaunchedEffect(pageCount) {
        pageIndex = pageIndex.coerceIn(0, pageCount - 1)
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF2A1C4A), SecretDialogPanel, Color(0xFF17102F))
                )
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DialogEyebrow("PDF DOCUMENT")
            Spacer(Modifier.weight(1f))
            Text(
                "Page ${safePageIndex + 1} of $pageCount",
                color = TwTheme.color.onSurfaceSecondary,
                style = TwTheme.typo.caption,
            )
        }
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0D091D)),
            factory = { viewContext ->
                ImageView(viewContext).apply {
                    adjustViewBounds = true
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }
            },
            update = { imageView ->
                runCatching {
                    val page = renderer.openPage(safePageIndex)
                    val bitmapWidth = (page.width * 2).coerceAtMost(2400)
                    val bitmapHeight = (page.height * bitmapWidth / page.width).coerceAtLeast(1)
                    val bitmap = Bitmap.createBitmap(
                        bitmapWidth,
                        bitmapHeight,
                        Bitmap.Config.ARGB_8888,
                    )
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(
                        bitmap,
                        null,
                        null,
                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY,
                    )
                    page.close()
                    imageView.setImageBitmap(bitmap)
                }.onFailure {
                    imageView.setImageDrawable(null)
                }
            },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(
                onClick = { pageIndex = (safePageIndex - 1).coerceAtLeast(0) },
                enabled = safePageIndex > 0,
            ) {
                Icon(painter = TwIcons.ChevronLeft, contentDescription = "Previous PDF page")
            }
            Text(
                "Secure in-memory preview",
                color = TwTheme.color.onSurfaceSecondary,
                style = TwTheme.typo.caption,
            )
            IconButton(
                onClick = { pageIndex = (safePageIndex + 1).coerceAtMost(pageCount - 1) },
                enabled = safePageIndex < pageCount - 1,
            ) {
                Icon(painter = TwIcons.ChevronRight, contentDescription = "Next PDF page")
            }
        }
    }
}

private fun createPdfRenderer(context: android.content.Context, bytes: ByteArray): PdfRenderer? {
    if (bytes.isEmpty()) return null
    val temporaryFile = runCatching {
        File.createTempFile("secret-preview-", ".pdf", context.cacheDir)
    }.getOrNull() ?: return null
    return runCatching {
        temporaryFile.writeBytes(bytes)
        val descriptor = ParcelFileDescriptor.open(temporaryFile, ParcelFileDescriptor.MODE_READ_ONLY)
        temporaryFile.delete()
        runCatching { PdfRenderer(descriptor) }.getOrElse {
            descriptor.close()
            null
        }
    }.getOrElse {
        null
    }.also {
        temporaryFile.delete()
    }
}

private fun hexPreview(bytes: ByteArray): String {
    val preview = bytes.take(512)
    return preview.chunked(16).joinToString("\n") { row ->
        row.joinToString(" ") { byte -> "%02X".format(Locale.US, byte.toInt() and 0xFF) }
    } + if (bytes.size > 512) "\n… ${bytes.size - 512} more bytes" else ""
}

private fun android.content.Context.copySensitiveToClipboard(label: String, value: String) {
    copyToClipboard(
        text = value,
        label = label,
        toast = "Copied — clipboard clears in 30 seconds",
        isSensitive = true,
    )
    val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    Handler(Looper.getMainLooper()).postDelayed({
        val current = clipboard.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString()
        if (current == value) clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
    }, 30_000L)
}

private fun openAttachmentExternally(context: android.content.Context, attachment: SecretAttachment) {
    runCatching {
        val uri = EncryptedAttachmentProvider.uriFor(context, attachment)
        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, attachment.mimeType)
            clipData = ClipData.newRawUri("Secret attachment", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        })
    }.onFailure {
        android.widget.Toast.makeText(context, "No compatible viewer is installed", android.widget.Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun managerIcon(type: SecretManagerType) = when (type) {
    SecretManagerType.PASSWORDS -> TwIcons.Lock
    SecretManagerType.NOTES -> TwIcons.Write
    SecretManagerType.CARDS -> TwIcons.Passcode
    SecretManagerType.WIFI -> TwIcons.Cloud
    SecretManagerType.API_KEYS -> TwIcons.Security
    SecretManagerType.SSH_KEYS -> TwIcons.Lock
    SecretManagerType.BANK_ACCOUNTS -> TwIcons.CloudUpload
    SecretManagerType.IDENTITIES -> TwIcons.Fingerprint
    SecretManagerType.PERSONAL_TOKENS -> TwIcons.Token
    SecretManagerType.APP_CREDENTIALS -> TwIcons.Settings
    SecretManagerType.CUSTOM -> TwIcons.More
}

@Composable
private fun managerTint(type: SecretManagerType) = when (type) {
    SecretManagerType.PASSWORDS -> Color(0xFF8D7CFF)
    SecretManagerType.NOTES -> Color(0xFF4CB9A5)
    SecretManagerType.CARDS -> Color(0xFFFF9C66)
    SecretManagerType.WIFI -> Color(0xFF62A4FF)
    SecretManagerType.API_KEYS -> Color(0xFFE17BFF)
    SecretManagerType.SSH_KEYS -> Color(0xFF58D4C5)
    SecretManagerType.BANK_ACCOUNTS -> Color(0xFFFFC65A)
    SecretManagerType.IDENTITIES -> Color(0xFFFF718A)
    SecretManagerType.PERSONAL_TOKENS -> Color(0xFF9B9CFF)
    SecretManagerType.APP_CREDENTIALS -> Color(0xFF7CCBFF)
    SecretManagerType.CUSTOM -> TwTheme.color.primary
}