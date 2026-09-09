package app.voltshare

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.zip.ZipFile

object ApkInstaller {
    fun install(context: Context, file: File): Result<Unit> = runCatching {
        if (file.extension.lowercase() == "apk") {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } else {
            installContainer(context, file)
        }
    }

    private fun installContainer(context: Context, file: File) {
        val installer = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        val sessionId = installer.createSession(params)
        installer.openSession(sessionId).use { session ->
            ZipFile(file).use { zip ->
                zip.entries().asSequence()
                    .filter { !it.isDirectory && it.name.endsWith(".apk", ignoreCase = true) }
                    .forEach { entry ->
                        session.openWrite(entry.name, 0, entry.size).use { output ->
                            zip.getInputStream(entry).use { input -> input.copyTo(output) }
                            session.fsync(output)
                        }
                    }
            }
            val callback = Intent(context, InstallResultReceiver::class.java)
            val pending = PendingIntent.getBroadcast(
                context,
                sessionId,
                callback,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
            )
            session.commit(pending.intentSender)
        }
    }
}

class InstallResultReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Unit
}

private fun Uri.isApkLike(): Boolean = toString().endsWith(".apk", ignoreCase = true)