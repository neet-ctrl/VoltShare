package com.twofasapp.feature.secrets.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.twofasapp.feature.secrets.data.SecretAttachment
import com.twofasapp.feature.secrets.data.SecretEntry
import com.twofasapp.feature.secrets.data.SecretManager
import com.twofasapp.feature.secrets.data.SecretManagerType
import com.twofasapp.feature.secrets.data.SecretTrashEntry
import com.twofasapp.feature.secrets.data.SecretsRepository
import com.twofasapp.feature.secrets.data.SecretsVault
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class SecretsUiState(
    val managers: List<SecretManager> = emptyList(),
    val removedManagers: List<SecretManager> = emptyList(),
    val managerNames: Map<String, String> = emptyMap(),
    val entries: List<SecretEntry> = emptyList(),
    val trash: List<SecretTrashEntry> = emptyList(),
    val isReordering: Boolean = false,
    val hiddenUnlocked: Boolean = false,
    val hasHiddenManagers: Boolean = false,
    val hasHiddenEntries: Boolean = false,
    val hasUnlockLock: Boolean = false,
)

class SecretsViewModel(
    private val repository: SecretsRepository,
) : ViewModel() {
    private var vault = repository.read()
    private val _uiState = MutableStateFlow(vault.toUiState(hiddenUnlocked = false))
    val uiState: StateFlow<SecretsUiState> = _uiState.asStateFlow()

    fun setReordering(value: Boolean) {
        _uiState.value = _uiState.value.copy(isReordering = value)
    }

    fun moveManager(from: Int, to: Int) {
        val visible = visibleManagers()
        if (from !in visible.indices || visible.isEmpty()) return
        val target = to.coerceIn(0, visible.lastIndex)
        if (from == target) return
        val reordered = visible.toMutableList().apply { add(target, removeAt(from)) }
        val positions = reordered.mapIndexed { index, manager -> manager.id to index }.toMap()
        vault = vault.copy(
            managers = vault.managers.sortedBy { positions[it.id] ?: Int.MAX_VALUE },
        )
        updateUiState()
    }

    fun finishManagerReorder() {
        persist()
    }

    fun toggleManagerHidden(id: String) {
        vault = vault.copy(managers = vault.managers.map { if (it.id == id) it.copy(hidden = !it.hidden) else it })
        persist()
    }

    fun toggleManagerPinned(id: String) {
        vault = vault.copy(managers = vault.managers.map { if (it.id == id) it.copy(pinned = !it.pinned) else it })
        persist()
    }

    fun removeManager(id: String) {
        vault = vault.copy(managers = vault.managers.map { if (it.id == id) it.copy(removed = true) else it })
        persist()
    }

    fun restoreManager(id: String) {
        vault = vault.copy(managers = vault.managers.map { if (it.id == id) it.copy(removed = false) else it })
        persist()
    }

    fun addCustomManager(name: String) {
        if (name.isBlank()) return
        vault = vault.copy(
            managers = vault.managers + SecretManager(
                id = "custom-${UUID.randomUUID()}",
                name = name.trim(),
                type = SecretManagerType.CUSTOM,
            )
        )
        persist()
    }

    fun saveEntry(entry: SecretEntry) {
        val existing = vault.entries.any { it.id == entry.id }
        vault = vault.copy(
            entries = if (existing) vault.entries.map { if (it.id == entry.id) entry else it } else vault.entries + entry,
        )
        persist()
    }

    fun saveAttachment(uri: Uri): SecretAttachment? = repository.saveAttachment(uri)

    fun readAttachment(attachment: SecretAttachment): ByteArray? = repository.readAttachment(attachment)

    fun deleteEntry(entry: SecretEntry) {
        vault = vault.copy(
            entries = vault.entries.filterNot { it.id == entry.id },
            trash = vault.trash.filterNot { it.entry.id == entry.id } +
                SecretTrashEntry(entry = entry, deletedAt = System.currentTimeMillis()),
        )
        persist()
    }

    fun toggleEntryPinned(id: String) {
        vault = vault.copy(entries = vault.entries.map { if (it.id == id) it.copy(pinned = !it.pinned) else it })
        persist()
    }

    fun setUnlockLock(rating: Float, comment: String): Boolean {
        // Settings and the Secrets screen share the same encrypted store.
        // Re-read it so a stale screen cannot overwrite a lock configured in Settings.
        vault = repository.read()
        if (vault.unlockRating > 0f && vault.unlockComment.isNotBlank()) return false
        vault = vault.copy(unlockRating = rating, unlockComment = comment.trim())
        _uiState.value = _uiState.value.copy(hiddenUnlocked = false)
        persist()
        return true
    }

    fun changeUnlockLock(
        oldRating: Float,
        oldComment: String,
        newRating: Float,
        newComment: String,
    ): Boolean {
        vault = repository.read()
        if (vault.unlockRating <= 0f ||
            vault.unlockRating != oldRating ||
            vault.unlockComment != oldComment.trim()
        ) return false
        vault = vault.copy(unlockRating = newRating, unlockComment = newComment.trim())
        _uiState.value = _uiState.value.copy(hiddenUnlocked = false)
        persist()
        return true
    }

    fun unlockHidden(rating: Float, comment: String): Boolean {
        // Settings can update the lock while this ViewModel remains alive.
        vault = repository.read()
        val matches = vault.unlockRating > 0f &&
            vault.unlockRating == rating &&
            vault.unlockComment == comment.trim()
        if (matches) {
            updateUiState(hiddenUnlocked = true)
        }
        return matches
    }

    fun lockHidden() {
        _uiState.value = _uiState.value.copy(hiddenUnlocked = false)
    }

    fun moveEntry(managerId: String, from: Int, to: Int) {
        val ordered = vault.entries
            .filter { it.managerId == managerId && (!it.hidden || _uiState.value.hiddenUnlocked) }
            .sortedWith(compareByDescending<SecretEntry> { it.pinned }.thenBy { it.order }.thenByDescending { it.updatedAt })
        if (from !in ordered.indices || to !in ordered.indices || from == to) return
        val reordered = ordered.toMutableList().apply { add(to, removeAt(from)) }
        val orderById = reordered.mapIndexed { index, entry -> entry.id to index }
        vault = vault.copy(entries = vault.entries.map { entry ->
            orderById.firstOrNull { it.first == entry.id }?.let { entry.copy(order = it.second) } ?: entry
        })
        updateUiState()
    }

    fun finishEntryReorder() {
        persist()
    }

    fun restoreTrash(trashEntry: SecretTrashEntry) {
        if (vault.trash.none { it.entry.id == trashEntry.entry.id }) return
        vault = vault.copy(
            entries = vault.entries.filterNot { it.id == trashEntry.entry.id } + trashEntry.entry,
            trash = vault.trash.filterNot { it.entry.id == trashEntry.entry.id },
        )
        persist()
    }

    fun permanentlyDeleteTrash(trashEntry: SecretTrashEntry) {
        vault = vault.copy(trash = vault.trash.filterNot { it.entry.id == trashEntry.entry.id })
        repository.pruneUnusedAttachments(vault)
        persist()
    }

    fun reload() {
        vault = repository.read()
        _uiState.value = vault.toUiState(hiddenUnlocked = false)
    }

    fun exportEncrypted(output: java.io.OutputStream, password: String?) = repository.exportEncrypted(output, password)

    fun importEncrypted(input: java.io.InputStream, password: String?): Boolean {
        val result = repository.importEncrypted(input, password)
        if (result) reload()
        return result
    }

    private fun visibleManagers(): List<SecretManager> =
        vault.managers
            .filter { !it.removed && (!it.hidden || _uiState.value.hiddenUnlocked) }
            .sortedWith(compareByDescending<SecretManager> { it.pinned })

    private fun persist() {
        repository.pruneUnusedAttachments(vault)
        repository.write(vault)
        updateUiState()
    }

    private fun updateUiState(hiddenUnlocked: Boolean = _uiState.value.hiddenUnlocked) {
        val currentState = _uiState.value
        _uiState.value = vault.toUiState(hiddenUnlocked = hiddenUnlocked)
            .copy(isReordering = currentState.isReordering)
    }

    private fun SecretsVault.toUiState(hiddenUnlocked: Boolean) = SecretsUiState(
        managers = managers
            .filter { !it.removed && (!it.hidden || hiddenUnlocked) }
            .sortedWith(compareByDescending<SecretManager> { it.pinned }),
        removedManagers = managers.filter { it.removed },
        managerNames = managers.associate { it.id to it.name },
        entries = entries,
        trash = trash,
        hasHiddenManagers = managers.any { it.hidden && !it.removed },
        hasHiddenEntries = entries.any { it.hidden },
        hasUnlockLock = unlockRating > 0f && unlockComment.isNotBlank(),
    )
}