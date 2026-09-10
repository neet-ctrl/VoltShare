package app.voltshare

import android.net.Uri

object VaultShareContract {
    const val ACTION_PICK_VAULT_FILES = "app.voltshare.action.PICK_VAULT_FILES"
    const val EXTRA_PICKER_PURPOSE = "app.voltshare.extra.PICKER_PURPOSE"
    const val PICKER_PURPOSE_ATTACHMENTS = "attachments"
    const val PICKER_PURPOSE_UNIVERSAL_RESTORE = "universal_restore"
    const val PERMISSION_ACCESS_VAULT = "app.voltshare.permission.ACCESS_VAULT"
    const val AUTHORITY = "app.voltshare.vaultprovider"
    const val PATH_FILES = "files"

    fun uriForFile(fileId: String): Uri =
        Uri.Builder()
            .scheme("content")
            .authority(AUTHORITY)
            .appendPath(PATH_FILES)
            .appendPath(fileId)
            .build()
}