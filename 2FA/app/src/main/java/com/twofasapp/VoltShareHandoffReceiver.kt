package com.twofasapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.io.File

class VoltShareHandoffReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_HANDOFF_ACK) return

        val fileName = intent.getStringExtra(EXTRA_CACHE_FILE_NAME) ?: return
        if (!fileName.startsWith(UNIVERSAL_BACKUP_PREFIX) || !fileName.endsWith(UNIVERSAL_BACKUP_SUFFIX)) {
            return
        }

        val cacheDir = context.cacheDir.canonicalFile
        val file = File(cacheDir, fileName).canonicalFile
        if (file.parentFile == cacheDir) {
            file.delete()
        }
    }

    companion object {
        const val ACTION_HANDOFF_ACK = "com.twofasapp.action.VOLTSHARE_HANDOFF_ACK"
        const val EXTRA_CACHE_FILE_NAME = "com.twofasapp.extra.VOLTSHARE_CACHE_FILE_NAME"
        const val UNIVERSAL_BACKUP_PREFIX = "2fas-universal-backup-"
        const val UNIVERSAL_BACKUP_SUFFIX = ".universal"
    }
}