package com.twofasapp.feature.home.ui.bottombar

import androidx.compose.runtime.Composable
import com.twofasapp.android.navigation.Screen
import com.twofasapp.designsystem.TwIcons
import com.twofasapp.designsystem.common.TwNavigationBar
import com.twofasapp.designsystem.common.TwNavigationBarItem
import com.twofasapp.locale.TwLocale

private val bottomNavItems
    @Composable
    get() = listOf(
        BottomNavItem(
            title = TwLocale.strings.bottomBarTokens,
            icon = TwIcons.Token,
            iconSelected = TwIcons.Token,
            route = Screen.Services.route,
        ),
        BottomNavItem(
            title = "Secrets",
            icon = TwIcons.Secrets,
            iconSelected = TwIcons.Secrets,
            route = Screen.Secrets.route,
        ),
        BottomNavItem(
            title = TwLocale.strings.bottomBarSettings,
            icon = TwIcons.Settings,
            iconSelected = TwIcons.SettingsFilled,
            route = Screen.Settings.route,
        ),
    )

interface BottomBarListener {
    fun openHome()
    fun openSecrets()
    fun openSettings()
}

@Composable
internal fun BottomBar(
    selectedIndex: Int,
    listener: BottomBarListener,
    onItemClick: () -> Unit = {},
) {
    TwNavigationBar {
        bottomNavItems.forEachIndexed { index, item ->
            TwNavigationBarItem(
                text = item.title,
                icon = if (index == selectedIndex) item.iconSelected else item.icon,
                selected = index == selectedIndex,
                showBadge = false,
                onClick = {
                    when {
                        index == 0 && selectedIndex != 0 -> {
                            onItemClick()
                            listener.openHome()
                        }
                        index == 1 && selectedIndex != 1 -> {
                            onItemClick()
                            listener.openSecrets()
                        }
                        index == 2 && selectedIndex != 2 -> {
                            onItemClick()
                            listener.openSettings()
                        }
                    }
                }
            )
        }
    }
}
