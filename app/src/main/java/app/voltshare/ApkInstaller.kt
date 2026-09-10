package app.voltshare

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.widget.Toast
import java.io.File
import java.util.zip.ZipFile

object ApkInstaller {
    fun install(context: Context, file: File, originalName: String = file.name): Result<Unit> = runCatching {
        check(file.isFile && file.length() > 0L) { "The installer file is empty or missing." }
        if (originalName.substringAfterLast('.', "").lowercase() == "apk") {
            installApk(context, file)
        } else {
            installContainer(context, file)
        }
    }

    private fun installApk(context: Context, file: File) {
        val installer = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
            setSize(file.length())
        }
        val sessionId = installer.createSession(params)
        try {
            installer.openSession(sessionId).use { session ->
                session.openWrite("base.apk", 0, file.length()).use { output ->
                    file.inputStream().use { input -> input.copyTo(output) }
                    session.fsync(output)
                }
                session.commit(createStatusSender(context, sessionId))
            }
        } catch (error: Throwable) {
            installer.abandonSession(sessionId)
            throw error
        }
    }

    private fun installContainer(context: Context, file: File) {
        val installer = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        val sessionId = installer.createSession(params)
        try {
            installer.openSession(sessionId).use { session ->
                var apkCount = 0
                ZipFile(file).use { zip ->
                    zip.entries().asSequence()
                        .filter { !it.isDirectory && it.name.endsWith(".apk", ignoreCase = true) }
                        .forEach { entry ->
                            apkCount += 1
                            session.openWrite(entry.name, 0, entry.size).use { output ->
                                zip.getInputStream(entry).use { input -> input.copyTo(output) }
                                session.fsync(output)
                            }
                        }
                }
                check(apkCount > 0) { "This installer package does not contain an APK." }
                session.commit(createStatusSender(context, sessionId))
            }
        } catch (error: Throwable) {
            installer.abandonSession(sessionId)
            throw error
        }
    }

    private fun createStatusSender(context: Context, sessionId: Int): android.content.IntentSender {
        val callback = Intent(context, InstallResultReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context,
            sessionId,
            callback,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_ONE_SHOT,
        )
        return pending.intentSender
    }
}

class InstallResultReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(
            PackageInstaller.EXTRA_STATUS,
            PackageInstaller.STATUS_FAILURE,
        )
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
        val text = if (status == PackageInstaller.STATUS_SUCCESS) {
            "App installed successfully"
        } else {
            "App installation failed${message?.takeIf { it.isNotBlank() }?.let { ": $it" } ?: ""}"
        }
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }
}