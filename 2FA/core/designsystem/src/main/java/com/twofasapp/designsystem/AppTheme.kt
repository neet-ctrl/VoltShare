package com.twofasapp.designsystem

import android.content.pm.ActivityInfo
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.window.core.layout.WindowWidthSizeClass
import com.twofasapp.designsystem.internal.OverriddenDarkColors
import com.twofasapp.designsystem.internal.OverriddenLightColors
import com.twofasapp.designsystem.internal.ThemeColors
import com.twofasapp.designsystem.internal.onSurfacePrimaryDark
import com.twofasapp.designsystem.internal.onSurfacePrimaryLight
import com.twofasapp.designsystem.internal.onSurfaceSecondaryDark
import com.twofasapp.designsystem.internal.onSurfaceSecondaryLight
import com.twofasapp.designsystem.internal.onSurfaceTertiaryDark
import com.twofasapp.designsystem.internal.onSurfaceTertiaryLight
import com.twofasapp.designsystem.internal.primaryIndicatorDark
import com.twofasapp.designsystem.internal.primaryIndicatorLight
import com.twofasapp.designsystem.internal.seedDark
import com.twofasapp.designsystem.internal.seedLight
import com.twofasapp.designsystem.internal.serviceBackgroundWithGroupsDark
import com.twofasapp.designsystem.internal.serviceBackgroundWithGroupsLight
import com.twofasapp.designsystem.internal.switchThumbDark
import com.twofasapp.designsystem.internal.switchThumbLight
import com.twofasapp.designsystem.internal.switchTrackDark
import com.twofasapp.designsystem.internal.switchTrackLight
import com.twofasapp.designsystem.internal.accentBlueDark
import com.twofasapp.designsystem.internal.accentBlueLight
import com.twofasapp.designsystem.internal.backgroundSecondaryDark
import com.twofasapp.designsystem.internal.backgroundSecondaryLight
import com.twofasapp.designsystem.internal.glassOutlineDark
import com.twofasapp.designsystem.internal.glassOutlineLight
import com.twofasapp.designsystem.internal.glassSurfaceDark
import com.twofasapp.designsystem.internal.glassSurfaceLight
import com.twofasapp.designsystem.internal.glassSurfaceStrongDark
import com.twofasapp.designsystem.internal.glassSurfaceStrongLight
import com.twofasapp.designsystem.ktx.currentActivity

val LocalAppTheme = staticCompositionLocalOf { AppTheme.Auto }
val LocalThemeColors = staticCompositionLocalOf { ThemeColors() }
val LocalDynamicColors = staticCompositionLocalOf { false }

enum class AppTheme {
    Auto, Light, Dark,
}

@Composable
fun MainAppTheme(
    content: @Composable () -> Unit
) {
    LockScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

    val isDynamicColorEnabled = LocalDynamicColors.current

    val isInDarkTheme = when (LocalAppTheme.current) {
        AppTheme.Auto -> isSystemInDarkTheme()
        AppTheme.Light -> false
        AppTheme.Dark -> true
    }

    val colorScheme: ColorScheme = when {
        isDynamicColorEnabled && isInDarkTheme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicDarkColorScheme(LocalContext.current)
        isDynamicColorEnabled && !isInDarkTheme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(LocalContext.current)
        isInDarkTheme -> OverriddenDarkColors
        else -> OverriddenLightColors
    }

    val themeColors = ThemeColors(
        seed = if (isInDarkTheme) seedDark else seedLight,
        primary = colorScheme.primary,
        background = colorScheme.background,
        backgroundSecondary = if (isInDarkTheme) backgroundSecondaryDark else backgroundSecondaryLight,
        surface = colorScheme.surface,
        glassSurface = if (isInDarkTheme) glassSurfaceDark else glassSurfaceLight,
        glassSurfaceStrong = if (isInDarkTheme) glassSurfaceStrongDark else glassSurfaceStrongLight,
        glassOutline = if (isInDarkTheme) glassOutlineDark else glassOutlineLight,
        accentBlue = if (isInDarkTheme) accentBlueDark else accentBlueLight,
        surfaceVariant = colorScheme.surfaceVariant,
        onSurfacePrimary = if (isInDarkTheme) onSurfacePrimaryDark else onSurfacePrimaryLight,
        onSurfaceSecondary = if (isInDarkTheme) onSurfaceSecondaryDark else onSurfaceSecondaryLight,
        onSurfaceTertiary = if (isInDarkTheme) onSurfaceTertiaryDark else onSurfaceTertiaryLight,
        primaryIndicator = if (isInDarkTheme) primaryIndicatorDark else primaryIndicatorLight,
        serviceBackgroundWithGroups = if (isInDarkTheme) serviceBackgroundWithGroupsDark else serviceBackgroundWithGroupsLight,
        switchTrack = if (isInDarkTheme) switchTrackDark else switchTrackLight,
        switchThumb = if (isInDarkTheme) switchThumbDark else switchThumbLight,
    )

    CompositionLocalProvider(
        LocalThemeColors provides themeColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}

@Composable
fun LockScreenOrientation(orientation: Int) {
    val activity = LocalContext.currentActivity
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isPhone = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT

    if (isPhone) {
        DisposableEffect(Unit) {
            val originalOrientation = activity.requestedOrientation
            activity.requestedOrientation = orientation
            onDispose {
                activity.requestedOrientation = originalOrientation
            }
        }
    }
}
