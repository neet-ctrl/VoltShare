package com.twofasapp.designsystem.internal

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color


// Light theme colors
internal val seedLight = Color(0xFFED1C24)
internal val primaryLight = Color(0xFFED1C24)
internal val backgroundLight = Color(0xFFFFFFFF)
internal val backgroundSecondaryLight = Color(0xFFF5F2FF)
internal val surfaceLight = Color(0xFFF9F9F9)
internal val glassSurfaceLight = Color(0xF8FFFFFF)
internal val glassSurfaceStrongLight = Color(0xFFFFFFFF)
internal val glassOutlineLight = Color(0xFFE5DFFF)
internal val accentBlueLight = Color(0xFF3B82F6)
internal val surfaceVariantLight = Color(0xFFEEEEEE)
internal val onSurfacePrimaryLight = Color(0xFF000000)
internal val onSurfaceSecondaryLight = Color(0xFF9E9E9E)
internal val onSurfaceTertiaryLight = Color(0xFF4C4C4C)
internal val primaryIndicatorLight = Color(0xFFF8E2E3)
internal val serviceBackgroundWithGroupsLight = Color(0xFFFCFCFC)
internal val switchTrackLight = Color(0xFFEEEEEE)
internal val switchThumbLight = Color(0xFFBBBBBB)

// Dark theme colors
internal val seedDark = Color(0xFF8B5CF6)
internal val primaryDark = Color(0xFF8B5CF6)
internal val backgroundDark = Color(0xFF0A0A1A)
internal val backgroundSecondaryDark = Color(0xFF1A1040)
internal val surfaceDark = Color(0xFF15152C)
internal val glassSurfaceDark = Color(0xCC171731)
internal val glassSurfaceStrongDark = Color(0xE61D1D3D)
internal val glassOutlineDark = Color(0x3D9B7BFF)
internal val accentBlueDark = Color(0xFF3B82F6)
internal val surfaceVariantDark = Color(0xFF252044)
internal val onSurfacePrimaryDark = Color(0xFFFFFFFF)
internal val onSurfaceSecondaryDark = Color(0xFFAAA8C8)
internal val onSurfaceTertiaryDark = Color(0xFFD8D7F2)
internal val primaryIndicatorDark = Color(0xFF2C1A60)
internal val serviceBackgroundWithGroupsDark = Color(0xFF0F0F25)
internal val switchTrackDark = Color(0xFF2B2947)
internal val switchThumbDark = Color(0xFF77729B)

// Light Color Scheme
internal val OverriddenLightColors = lightColorScheme(
    primary = primaryLight,
    onPrimary = onSurfacePrimaryLight,
    background = backgroundLight,
    onBackground = onSurfacePrimaryLight,
    surface = surfaceLight,
    onSurface = onSurfacePrimaryLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfacePrimaryLight,
)

// Dark Color Scheme
internal val OverriddenDarkColors = darkColorScheme(
    primary = primaryDark,
    onPrimary = onSurfacePrimaryDark,
    background = backgroundDark,
    onBackground = onSurfacePrimaryDark,
    surface = surfaceDark,
    onSurface = onSurfacePrimaryDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfacePrimaryDark,
)
