package com.twofasapp.feature.secrets.navigation

import androidx.compose.runtime.Composable
import com.twofasapp.feature.secrets.ui.SecretsScreen

@Composable
fun SecretsRoute(bottomBar: @Composable () -> Unit) {
    SecretsScreen(bottomBar = bottomBar)
}