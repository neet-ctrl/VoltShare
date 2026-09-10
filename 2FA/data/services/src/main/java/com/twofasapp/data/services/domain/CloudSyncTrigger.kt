package com.twofasapp.data.services.domain

enum class CloudSyncTrigger {
    FirstConnect,
    ServicesChanged,
    GroupsChanged,
    SecretsChanged,
    AppStart,
    AppBackground,
    EnterPassword,
    SetPassword,
    RemovePassword,
}