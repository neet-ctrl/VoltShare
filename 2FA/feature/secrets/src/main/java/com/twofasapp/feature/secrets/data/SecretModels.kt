package com.twofasapp.feature.secrets.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
enum class SecretManagerType {
    PASSWORDS, NOTES, CARDS, WIFI, API_KEYS, SSH_KEYS, BANK_ACCOUNTS,
    IDENTITIES, PERSONAL_TOKENS, APP_CREDENTIALS, CUSTOM
}

@Serializable
enum class SecretFieldKind { TEXT, PASSWORD, MULTILINE }

@Serializable
data class SecretField(
    val id: String,
    val label: String,
    val value: String = "",
    val kind: SecretFieldKind = SecretFieldKind.TEXT,
)

@Serializable
data class SecretAttachment(
    val id: String,
    val name: String,
    val mimeType: String = "application/octet-stream",
    val storageName: String = "",
    val size: Long = 0L,
    // Kept only so vaults created by the first Secrets pass can be migrated.
    @SerialName("uri") val legacyUri: String = "",
)

@Serializable
data class SecretEntry(
    val id: String,
    val managerId: String,
    val title: String,
    val fields: List<SecretField> = emptyList(),
    val note: String = "",
    val tags: List<String> = emptyList(),
    val attachments: List<SecretAttachment> = emptyList(),
    val pinned: Boolean = false,
    val hidden: Boolean = false,
    val order: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

@Serializable
data class SecretTrashEntry(
    val entry: SecretEntry,
    val deletedAt: Long = 0L,
)

data class SecretAttachmentContent(
    val attachment: SecretAttachment,
    val bytes: ByteArray,
)

@Serializable
data class SecretManager(
    val id: String,
    val name: String,
    val type: SecretManagerType,
    val hidden: Boolean = false,
    val pinned: Boolean = false,
    val removed: Boolean = false,
)

@Serializable
data class SecretsVault(
    val managers: List<SecretManager> = defaultSecretManagers(),
    val entries: List<SecretEntry> = emptyList(),
    val trash: List<SecretTrashEntry> = emptyList(),
    val unlockRating: Float = 0f,
    val unlockComment: String = "",
    val updatedAt: Long = 0L,
)

fun defaultSecretManagers(): List<SecretManager> = listOf(
    SecretManager("passwords", "Password Manager", SecretManagerType.PASSWORDS),
    SecretManager("notes", "Note Manager", SecretManagerType.NOTES),
    SecretManager("cards", "Debit/Credit Card Manager", SecretManagerType.CARDS),
    SecretManager("wifi", "Wi-Fi Manager", SecretManagerType.WIFI),
    SecretManager("api", "API Key Manager", SecretManagerType.API_KEYS),
    SecretManager("ssh", "SSH Key Manager", SecretManagerType.SSH_KEYS),
    SecretManager("bank", "Bank Account Manager", SecretManagerType.BANK_ACCOUNTS),
    SecretManager("identity", "Identity Manager", SecretManagerType.IDENTITIES),
    SecretManager("pat", "Personal Token Manager (P.A.T)", SecretManagerType.PERSONAL_TOKENS),
    SecretManager("app-credentials", "App Development Credential", SecretManagerType.APP_CREDENTIALS),
    SecretManager("custom", "Custom Manager", SecretManagerType.CUSTOM),
)

fun SecretManagerType.defaultFields(): List<SecretField> {
    val names = when (this) {
        SecretManagerType.PASSWORDS -> listOf("Email / username", "Password", "2FA secret / TOTP", "Website")
        SecretManagerType.NOTES -> emptyList()
        SecretManagerType.CARDS -> listOf("Cardholder name", "Card type", "Card number", "Expiry date", "CVV", "PIN")
        SecretManagerType.WIFI -> listOf(
            "Network name",
            "Password",
            "Security",
            "BSSID / MAC address",
            "Signal strength",
            "Frequency",
            "Connection status",
            "Router address",
        )
        SecretManagerType.API_KEYS -> listOf("Service / provider", "API key", "API secret", "Token", "API hash", "API ID", "Endpoint / base URL", "Environment", "Project / account ID", "Expiration date", "Permissions / scopes")
        SecretManagerType.SSH_KEYS -> listOf("Public key", "Private key", "Username", "Host")
        SecretManagerType.BANK_ACCOUNTS -> listOf("Account holder name", "Bank name", "Account number", "Account type", "IFSC code", "Branch", "Customer ID / CIF / CRN", "UPI ID", "Net banking username", "Net banking password")
        SecretManagerType.IDENTITIES -> listOf("Full name", "Mobile number", "Email address")
        SecretManagerType.PERSONAL_TOKENS -> listOf("Provider", "Token", "Description", "Expiration date", "Scopes")
        SecretManagerType.APP_CREDENTIALS -> listOf("Base64 key store", "Key store password", "Key alias", "Key alias password")
        SecretManagerType.CUSTOM -> emptyList()
    }
    return names.mapIndexed { index, name ->
        SecretField(
            id = "field-$index-${name.hashCode()}",
            label = name,
            kind = if (name.contains("password", true) || name.contains("secret", true) || name.contains("private", true) || name == "PIN" || name == "Token") SecretFieldKind.PASSWORD else SecretFieldKind.TEXT,
        )
    }
}