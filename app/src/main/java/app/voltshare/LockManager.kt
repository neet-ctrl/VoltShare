package app.voltshare

import android.content.Context
import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

enum class LockType(val label: String) {
    PIN("PIN"),
    PATTERN("Pattern"),
    PASSWORD("Password"),
}

class LockManager(context: Context) {
    private val prefs = context.getSharedPreferences("voltshare-lock", Context.MODE_PRIVATE)
    private val random = SecureRandom()

    fun isConfigured(): Boolean = prefs.contains(KEY_HASH)

    fun isEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, true)

    fun setEnabled(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_ENABLED, enabled)
            .putBoolean(KEY_BIOMETRIC_ENABLED, if (enabled) isBiometricEnabled() else false)
            .apply()
    }

    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun type(): LockType = runCatching {
        LockType.valueOf(prefs.getString(KEY_TYPE, LockType.PIN.name) ?: LockType.PIN.name)
    }.getOrDefault(LockType.PIN)

    fun configure(type: LockType, secret: String): Boolean {
        if (secret.length < 4) return false
        val salt = ByteArray(16).also(random::nextBytes)
        prefs.edit()
            .putString(KEY_TYPE, type.name)
            .putString(KEY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(KEY_HASH, hash(secret, salt))
            .putBoolean(KEY_ENABLED, true)
            .apply()
        return true
    }

    fun verify(secret: String): Boolean {
        val encodedSalt = prefs.getString(KEY_SALT, null) ?: return false
        val expected = prefs.getString(KEY_HASH, null) ?: return false
        return constantTimeEquals(expected, hash(secret, Base64.decode(encodedSalt, Base64.NO_WRAP)))
    }

    private fun hash(secret: String, salt: ByteArray): String {
        val spec = PBEKeySpec(secret.toCharArray(), salt, 120_000, 256)
        val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun constantTimeEquals(first: String, second: String): Boolean {
        val left = first.toByteArray()
        val right = second.toByteArray()
        if (left.size != right.size) return false
        var result = 0
        left.indices.forEach { result = result or (left[it].toInt() xor right[it].toInt()) }
        return result == 0
    }

    companion object {
        private const val KEY_TYPE = "type"
        private const val KEY_SALT = "salt"
        private const val KEY_HASH = "hash"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_BIOMETRIC_ENABLED = "biometric-enabled"
    }
}