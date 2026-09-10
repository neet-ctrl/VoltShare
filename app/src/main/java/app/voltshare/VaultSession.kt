package app.voltshare

object VaultSession {
    @Volatile
    var unlocked: Boolean = false
}