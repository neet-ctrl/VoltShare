package com.twofasapp.designsystem.common

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.twofasapp.designsystem.TwTheme

@Composable
fun TwNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val glassSurfaceStrong = TwTheme.color.glassSurfaceStrong
    val glassOutline = TwTheme.color.glassOutline

    NavigationBar(
        tonalElevation = 0.dp,
        modifier = modifier.drawBehind {
            drawRect(glassSurfaceStrong)
            drawLine(
                color = glassOutline,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 1.dp.toPx(),
            )
        },
        content = content,
        containerColor = Color.Transparent,
    )
}

@Composable
fun RowScope.TwNavigationBarItem(
    text: String,
    icon: Painter,
    selected: Boolean,
    showBadge: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primary = TwTheme.color.primary

    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        label = { Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        icon = {
            BadgedBox(badge = {
                if (showBadge) {
                    Badge(containerColor = TwTheme.color.primary)
                }
            }) {
                Icon(painter = icon, contentDescription = null)
            }

        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = primary,
            selectedTextColor = primary,
            indicatorColor = TwTheme.color.primaryIndicator.copy(alpha = 0.72f),
            unselectedIconColor = TwTheme.color.onSurfaceSecondary,
            unselectedTextColor = TwTheme.color.onSurfaceSecondary,
        ),
        modifier = modifier.drawBehind {
            if (selected) {
                drawLine(
                    color = primary,
                    start = Offset(size.width * 0.3f, size.height - 2.dp.toPx()),
                    end = Offset(size.width * 0.7f, size.height - 2.dp.toPx()),
                    strokeWidth = 2.dp.toPx(),
                )
            }
        },
    )
}