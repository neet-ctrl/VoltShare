package app.voltshare

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
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
                session.commit(createStatusSender(context, sessionId, file.name, file.length()))
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
                session.commit(createStatusSender(context, sessionId, file.name, file.length()))
            }
        } catch (error: Throwable) {
            installer.abandonSession(sessionId)
            throw error
        }
    }

    private fun createStatusSender(
        context: Context,
        sessionId: Int,
        originalName: String,
        byteCount: Long,
    ): android.content.IntentSender {
        val callback = Intent(context, InstallResultReceiver::class.java)
            .putExtra(EXTRA_ORIGINAL_NAME, originalName)
            .putExtra(EXTRA_BYTE_COUNT, byteCount)
        val pending = PendingIntent.getBroadcast(
            context,
            sessionId,
            callback,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
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
        if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
            val confirmation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_INTENT)
            }
            if (confirmation != null) {
                confirmation.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                runCatching { context.startActivity(confirmation) }.onFailure { error ->
                    publishFailure(
                        context,
                        intent,
                        "Android returned a package confirmation screen but VoltShare could not open it.",
                        error,
                    )
                }
            } else {
                publishFailure(
                    context,
                    intent,
                    "Android requested package confirmation but did not provide a confirmation intent.",
                    null,
                )
            }
            return
        }
        if (status == PackageInstaller.STATUS_SUCCESS) {
            Toast.makeText(context, "App installed successfully", Toast.LENGTH_LONG).show()
            return
        }
        publishFailure(context, intent, "Android rejected the package installation.", null, status, message)
    }

    private fun publishFailure(
        context: Context,
        intent: Intent,
        summary: String,
        error: Throwable?,
        status: Int = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE),
        message: String? = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE),
    ) {
        val log = buildString {
            appendLine("VoltShare Android installation diagnostic")
            appendLine("Time: ${System.currentTimeMillis()}")
            appendLine("Package name: ${intent.getStringExtra(EXTRA_ORIGINAL_NAME) ?: "unknown"}")
            appendLine("Payload bytes: ${intent.getLongExtra(EXTRA_BYTE_COUNT, -1L)}")
            appendLine("Session id: ${intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -1)}")
            appendLine("Android SDK: ${Build.VERSION.SDK_INT}")
            appendLine("Status code: $status")
            appendLine("Status message: ${message ?: "(none returned by Android)"}")
            appendLine("Summary: $summary")
            if (error != null) {
                appendLine()
                appendLine(error.stackTraceToString())
            }
        }
        InstallResultStore.publish(context, log)
    }
}

object InstallResultStore {
    private const val PREFS = "voltshare-install-results"
    private const val KEY_LOG = "last-log"
    private var listener: ((String) -> Unit)? = null

    fun register(onResult: (String) -> Unit) {
        listener = onResult
    }

    fun unregister() {
        listener = null
    }

    fun publish(context: Context, log: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LOG, log)
            .apply()
        listener?.invoke(log)
    }

    fun consume(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val value = prefs.getString(KEY_LOG, null)
        if (value != null) prefs.edit().remove(KEY_LOG).apply()
        return value
    }
}

private const val EXTRA_ORIGINAL_NAME = "app.voltshare.extra.INSTALL_ORIGINAL_NAME"
private const val EXTRA_BYTE_COUNT = "app.voltshare.extra.INSTALL_BYTE_COUNT"