package com.twofasapp.feature.home.ui.services.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.twofasapp.designsystem.TwIcons
import com.twofasapp.designsystem.TwTheme
import com.twofasapp.locale.TwLocale

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun ServicesFab(
    isVisible: Boolean,
    isExtendedVisible: Boolean,
    isNormalVisible: Boolean,
    onClick: () -> Unit,
) {
    val fabShape = RoundedCornerShape(18.dp)
    val fabBorder = Brush.linearGradient(
        colors = listOf(
            TwTheme.color.accentLightBlue.copy(alpha = 0.9f),
            TwTheme.color.accentPurple.copy(alpha = 0.85f),
        ),
    )

    if (isVisible) {
        if (isExtendedVisible) {
            ExtendedFloatingActionButton(
                onClick = onClick,
                modifier = Modifier.border(BorderStroke(1.dp, fabBorder), fabShape),
                shape = fabShape,
                icon = { Icon(TwIcons.Add, null) },
                text = { Text(text = TwLocale.strings.servicesEmptyPairServiceCta) },
                containerColor = TwTheme.color.primary,
                contentColor = Color.White,
            )
        } else {
            AnimatedVisibility(
                visible = isNormalVisible,
                enter = scaleIn(tween(150)),
                exit = scaleOut(tween(150)),
            ) {
                FloatingActionButton(
                    onClick = onClick,
                    modifier = Modifier.border(BorderStroke(1.dp, fabBorder), fabShape),
                    shape = fabShape,
                    content = { Icon(TwIcons.Add, null) },
                    containerColor = TwTheme.color.primary,
                    contentColor = Color.White,
                )
            }
        }
    }
}