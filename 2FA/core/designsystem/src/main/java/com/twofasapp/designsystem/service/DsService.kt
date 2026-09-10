package com.twofasapp.designsystem.service

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twofasapp.designsystem.TwIcons
import com.twofasapp.designsystem.TwTheme
import com.twofasapp.designsystem.common.ResponsiveText
import com.twofasapp.designsystem.common.TwIconButton
import com.twofasapp.designsystem.service.atoms.NextCodeGravity
import com.twofasapp.designsystem.service.atoms.ServiceBadge
import com.twofasapp.designsystem.service.atoms.ServiceCode
import com.twofasapp.designsystem.service.atoms.ServiceDimens
import com.twofasapp.designsystem.service.atoms.ServiceDimensDefaults
import com.twofasapp.designsystem.service.atoms.ServiceHotp
import com.twofasapp.designsystem.service.atoms.ServiceImage
import com.twofasapp.designsystem.service.atoms.ServiceInfo
import com.twofasapp.designsystem.service.atoms.ServiceName
import com.twofasapp.designsystem.service.atoms.ServiceTextDefaults
import com.twofasapp.designsystem.service.atoms.ServiceTextStyle
import com.twofasapp.designsystem.service.atoms.ServiceTimer
import com.twofasapp.designsystem.service.atoms.formatCode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal const val ServiceExpireTransitionThreshold = 5

@Composable
fun animateExpireColor(timer: Int): State<Color> {
    return animateColorAsState(
        targetValue = if (timer > ServiceExpireTransitionThreshold) {
            TwTheme.color.onSurfacePrimary
        } else {
            TwTheme.color.primary
        },
        animationSpec = TweenSpec(),
        label = ""
    )
}

enum class ServiceStyle {
    Default, Compact
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DsService(
    state: ServiceState,
    modifier: Modifier = Modifier,
    style: ServiceStyle = ServiceStyle.Default,
    editMode: Boolean = false,
    showNextCode: Boolean = false,
    hideCodes: Boolean = false,
    containerColor: Color = TwTheme.color.glassSurface,
    dragHandleVisible: Boolean = true,
    dragModifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onIncrementCounterClick: (() -> Unit)? = null,
    onRevealClick: (() -> Unit)? = null,
) {
    val textStyles: ServiceTextStyle = when (style) {
        ServiceStyle.Default -> ServiceTextDefaults.default()
        ServiceStyle.Compact -> ServiceTextDefaults.compact()
    }

    val dimens: ServiceDimens = when (style) {
        ServiceStyle.Default -> ServiceDimensDefaults.default()
        ServiceStyle.Compact -> ServiceDimensDefaults.compact()
    }

    val cardShape = RoundedCornerShape(20.dp)
    val copyScope = rememberCoroutineScope()
    var copied by remember(state.name) { mutableStateOf(false) }
    val cardBorder = Brush.linearGradient(
        colors = listOf(
            TwTheme.color.accentPurple.copy(alpha = 0.72f),
            TwTheme.color.accentBlue.copy(alpha = 0.5f),
            TwTheme.color.glassOutline,
        ),
    )
    val copyBackground by animateColorAsState(
        targetValue = if (copied) {
            TwTheme.color.accentTurquoise.copy(alpha = 0.2f)
        } else {
            TwTheme.color.primary.copy(alpha = 0.12f)
        },
        label = "copy background",
    )
    val copyBorder by animateColorAsState(
        targetValue = if (copied) TwTheme.color.accentTurquoise else TwTheme.color.glassOutline,
        label = "copy border",
    )
    val copyTint by animateColorAsState(
        targetValue = if (copied) TwTheme.color.accentTurquoise else TwTheme.color.primary,
        label = "copy icon",
    )
    val copyAction: () -> Unit = {
        onClick?.invoke()
        if (onClick != null) {
            copied = true
            copyScope.launch {
                delay(1400)
                copied = false
            }
        }
    }

    if (style == ServiceStyle.Default && editMode.not()) {
        DefaultServiceCard(
            state = state,
            modifier = modifier,
            showNextCode = showNextCode,
            hideCodes = hideCodes,
            containerColor = containerColor,
            copied = copied,
            copyBackground = copyBackground,
            copyBorder = copyBorder,
            copyTint = copyTint,
            copyAction = copyAction,
            onLongClick = onLongClick,
            onIncrementCounterClick = onIncrementCounterClick,
            onRevealClick = onRevealClick,
            onClickEnabled = onClick != null,
        )
        return
    }

    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (editMode) dimens.cellHeightInEdit else dimens.cellHeight)
                .clip(cardShape)
                .background(containerColor)
                .border(BorderStroke(1.dp, cardBorder), cardShape)
                .combinedClickable(
                    enabled = onClick != null,
                    onClick = {
                        if (editMode.not()) {
                            copyAction()
                        }
                    },
                    onLongClick = {
                        onLongClick?.invoke()
                    },
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            ServiceBadge(
                color = state.badgeColor,
            )

            ServiceImage(
                type = state.imageType,
                iconLight = state.iconLight,
                iconDark = state.iconDark,
                labelText = state.labelText,
                labelColor = state.labelColor,
                textStyles = textStyles,
                dimens = dimens,
            )

            Column(modifier = Modifier.weight(1f)) {
                ServiceName(
                    text = state.name,
                    textStyles = textStyles,
                    modifier = Modifier.fillMaxWidth(),
                )

                ServiceInfo(
                    text = state.info,
                    textStyles = textStyles,
                    style = style,
                    spacer = editMode.not(),
                    modifier = Modifier.fillMaxWidth(),
                )

                if (editMode.not() && (state.revealed || hideCodes.not() || style == ServiceStyle.Default)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max)
                    ) {
                        ServiceCode(
                            code = state.code,
                            nextCode = state.nextCode,
                            timer = state.timer,
                            nextCodeVisible = state.isNextCodeEnabled(showNextCode) && editMode.not() && (state.revealed || hideCodes.not()),
                            nextCodeGravity = when (style) {
                                ServiceStyle.Default -> NextCodeGravity.Below
                                ServiceStyle.Compact -> NextCodeGravity.End
                            },
                            animateColor = state.authType == ServiceAuthType.Totp || state.authType == ServiceAuthType.Steam,
                            textStyles = textStyles,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .alpha(if (state.revealed || hideCodes.not()) 1f else 0f),
                        )

                        if (state.revealed.not() && hideCodes) {
                            HiddenDots(
                                formattedCode = state.code.formatCode(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                            )
                        }
                    }
                }
            }

            if (editMode.not() && onClick != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(copyBackground)
                        .border(
                            BorderStroke(1.dp, copyBorder),
                            RoundedCornerShape(12.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    TwIconButton(
                        onClick = copyAction,
                        modifier = Modifier.size(40.dp),
                        content = {
                            Icon(
                                painter = if (copied) TwIcons.CheckCircle else TwIcons.Copy,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = copyTint,
                            )
                        },
                    )
                }
            }

            if (editMode.not()) {

                if (state.revealed || hideCodes.not() || state.authType == ServiceAuthType.Hotp) {
                    when (state.authType) {
                        ServiceAuthType.Steam,
                        ServiceAuthType.Totp -> {
                            ServiceTimer(
                                timer = state.timer,
                                progress = state.progress,
                                textStyles = textStyles,
                                dimens = dimens,
                                modifier = Modifier.padding(end = 20.dp)
                            )
                        }

                        ServiceAuthType.Hotp -> {
                            ServiceHotp(
                                enabled = state.hotpCounterEnabled,
                                onClick = { onIncrementCounterClick?.invoke() },
                                modifier = Modifier.padding(end = 7.dp)
                            )
                        }
                    }
                } else {
                    if (state.authType == ServiceAuthType.Totp || state.authType == ServiceAuthType.Steam) {
                        Box(
                            Modifier
                                .padding(end = 7.dp)
                                .size(56.dp)
                                .clip(CircleShape)
                                .clickable { onRevealClick?.invoke() }
                        ) {
                            Icon(
                                painter = TwIcons.Eye,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.Center),
                                tint = TwTheme.color.iconTint
                            )
                        }
                    }
                }
            }

            if (editMode && dragHandleVisible) {
                TwIconButton(
                    painter = TwIcons.DragHandle,
                    enabled = false,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .then(dragModifier),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DefaultServiceCard(
    state: ServiceState,
    modifier: Modifier,
    showNextCode: Boolean,
    hideCodes: Boolean,
    containerColor: Color,
    copied: Boolean,
    copyBackground: Color,
    copyBorder: Color,
    copyTint: Color,
    copyAction: () -> Unit,
    onLongClick: (() -> Unit)?,
    onIncrementCounterClick: (() -> Unit)?,
    onRevealClick: (() -> Unit)?,
    onClickEnabled: Boolean,
) {
    val cardShape = RoundedCornerShape(20.dp)
    val visibleCode = state.revealed || hideCodes.not()
    val nextCodeVisible = state.isNextCodeEnabled(showNextCode) && visibleCode
    val cardBorder = Brush.linearGradient(
        colors = listOf(
            TwTheme.color.accentPurple.copy(alpha = 0.9f),
            TwTheme.color.accentBlue.copy(alpha = 0.5f),
            TwTheme.color.glassOutline,
        ),
    )
    val cardBackground = Brush.verticalGradient(
        colors = listOf(
            TwTheme.color.primary.copy(alpha = 0.15f),
            containerColor.copy(alpha = 0.96f),
            TwTheme.color.background.copy(alpha = 0.5f),
        ),
    )

    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = cardShape,
                    clip = false,
                    ambientColor = TwTheme.color.primary.copy(alpha = 0.16f),
                    spotColor = TwTheme.color.accentPurple.copy(alpha = 0.22f),
                )
                .clip(cardShape)
                .background(containerColor)
                .background(cardBackground)
                .border(BorderStroke(1.dp, cardBorder), cardShape)
                .combinedClickable(
                    enabled = onClickEnabled,
                    onClick = { copyAction() },
                    onLongClick = { onLongClick?.invoke() },
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(3.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                TwTheme.color.accentPurple,
                                TwTheme.color.primary,
                                TwTheme.color.accentBlue.copy(alpha = 0.45f),
                            ),
                        ),
                    )
                    .align(Alignment.CenterStart),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    TwTheme.color.onSurfacePrimary.copy(alpha = 0.14f),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        TwTheme.color.primary.copy(alpha = 0.22f),
                                        TwTheme.color.surface.copy(alpha = 0.5f),
                                    ),
                                ),
                            )
                            .border(
                                BorderStroke(1.dp, TwTheme.color.primary.copy(alpha = 0.68f)),
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        ServiceImage(
                            type = state.imageType,
                            iconLight = state.iconLight,
                            iconDark = state.iconDark,
                            labelText = state.labelText,
                            labelColor = state.labelColor,
                            modifier = Modifier.size(48.dp),
                            dimens = ServiceDimensDefaults.default().copy(
                                imageSize = 48.dp,
                                labelPillWidth = 34.dp,
                                labelPillHeight = 22.dp,
                            ),
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 14.dp, top = 1.dp),
                    ) {
                        ResponsiveText(
                            text = state.name,
                            style = TwTheme.typo.body1.copy(
                                fontSize = 19.sp,
                                lineHeight = 23.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = TwTheme.color.onSurfacePrimary,
                            maxLines = 1,
                        )

                        if (state.info.isNullOrEmpty().not()) {
                            ResponsiveText(
                                text = state.info!!,
                                style = TwTheme.typo.body3.copy(
                                    fontSize = 15.sp,
                                    lineHeight = 18.sp,
                                ),
                                color = TwTheme.color.onSurfaceSecondary,
                                maxLines = 1,
                            )
                        }
                    }

                    DefaultServiceStatus(
                        state = state,
                        visibleCode = visibleCode,
                        onIncrementCounterClick = onIncrementCounterClick,
                        onRevealClick = onRevealClick,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 72.dp),
                    ) {
                        if (visibleCode) {
                            ResponsiveText(
                                text = state.code.formatCode(),
                                style = TwTheme.typo.codeLight.copy(
                                    fontSize = 48.sp,
                                    lineHeight = 50.sp,
                                ),
                                brush = Brush.linearGradient(
                                    listOf(
                                        TwTheme.color.accentLightBlue,
                                        TwTheme.color.primary,
                                        TwTheme.color.accentPurple,
                                    ),
                                ),
                                maxLines = 1,
                            )
                        } else {
                            HiddenDots(
                                formattedCode = state.code.formatCode(),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    if (onClickEnabled) {
                        DefaultCopyButton(
                            copied = copied,
                            background = copyBackground,
                            border = copyBorder,
                            tint = copyTint,
                            onClick = copyAction,
                        )
                    }
                }

                DefaultProgressBar(progress = state.progress)

                Divider(
                    color = TwTheme.color.glassOutline.copy(alpha = 0.8f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(top = 10.dp),
                )

                AnimatedVisibility(
                    visible = nextCodeVisible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 5.dp),
                    ) {
                        Text(
                            text = "Next Code :",
                            style = TwTheme.typo.caption,
                            color = TwTheme.color.onSurfaceSecondary,
                        )
                        ResponsiveText(
                            text = state.nextCode.formatCode(),
                            modifier = Modifier.padding(start = 5.dp),
                            style = TwTheme.typo.body1.copy(
                                fontSize = 18.sp,
                                lineHeight = 21.sp,
                            ),
                            brush = Brush.linearGradient(
                                listOf(
                                    TwTheme.color.accentLightBlue,
                                    TwTheme.color.primary,
                                    TwTheme.color.accentPurple,
                                ),
                            ),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DefaultServiceStatus(
    state: ServiceState,
    visibleCode: Boolean,
    onIncrementCounterClick: (() -> Unit)?,
    onRevealClick: (() -> Unit)?,
) {
    val timerColor by animateColorAsState(
        targetValue = if (state.timer <= ServiceExpireTransitionThreshold) {
            TwTheme.color.accentPink
        } else {
            TwTheme.color.primary
        },
        animationSpec = TweenSpec(),
        label = "timer pill color",
    )

    when {
        state.authType == ServiceAuthType.Hotp -> {
            ServiceHotp(
                enabled = state.hotpCounterEnabled,
                onClick = { onIncrementCounterClick?.invoke() },
                modifier = Modifier.size(44.dp),
            )
        }

        visibleCode -> {
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(18.dp),
                        clip = false,
                        ambientColor = timerColor.copy(alpha = 0.16f),
                        spotColor = timerColor.copy(alpha = 0.2f),
                    )
                    .clip(RoundedCornerShape(18.dp))
                    .background(timerColor.copy(alpha = 0.13f))
                    .border(
                        BorderStroke(1.dp, timerColor.copy(alpha = 0.28f)),
                        RoundedCornerShape(18.dp),
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "${state.timer}s",
                    style = TwTheme.typo.body2,
                    color = timerColor,
                )
            }
        }

        else -> {
            TwIconButton(
                onClick = { onRevealClick?.invoke() },
                modifier = Modifier.size(44.dp),
                content = {
                    Icon(
                        painter = TwIcons.Eye,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = TwTheme.color.iconTint,
                    )
                },
            )
        }
    }
}

@Composable
private fun DefaultCopyButton(
    copied: Boolean,
    background: Color,
    border: Color,
    tint: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(13.dp),
                clip = false,
                ambientColor = TwTheme.color.primary.copy(alpha = 0.14f),
                spotColor = TwTheme.color.primary.copy(alpha = 0.2f),
            )
            .clip(RoundedCornerShape(13.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        background.copy(alpha = (background.alpha + 0.08f).coerceAtMost(1f)),
                        background,
                    ),
                ),
            )
            .border(BorderStroke(1.dp, border), RoundedCornerShape(13.dp)),
        contentAlignment = Alignment.Center,
    ) {
        TwIconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp),
            content = {
                Icon(
                    painter = if (copied) TwIcons.CheckCircle else TwIcons.Copy,
                    contentDescription = null,
                    modifier = Modifier.size(23.dp),
                    tint = tint,
                )
            },
        )
    }
}

@Composable
private fun DefaultProgressBar(progress: Float) {
    val trackShape = RoundedCornerShape(6.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 72.dp, end = 66.dp, top = 7.dp)
            .height(6.dp)
            .clip(trackShape)
            .background(TwTheme.color.surfaceVariant.copy(alpha = 0.4f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .shadow(
                    elevation = 4.dp,
                    shape = trackShape,
                    clip = false,
                    ambientColor = TwTheme.color.primary.copy(alpha = 0.22f),
                    spotColor = TwTheme.color.primary.copy(alpha = 0.28f),
                )
                .clip(trackShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            TwTheme.color.accentPurple,
                            TwTheme.color.primary,
                            TwTheme.color.onSurfacePrimary,
                        ),
                    ),
                ),
        )
    }
}

@Composable
internal fun HiddenDots(
    modifier: Modifier = Modifier,
    formattedCode: String,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        formattedCode.map {
            if (it.isWhitespace()) {
                Spacer(modifier = Modifier.width(2.dp))
            } else {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(8.dp)
                        .background(TwTheme.color.onSurfacePrimary, CircleShape)
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewDefault() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DsService(state = ServicePreview)
        DsService(state = ServicePreview.copy(timer = 3), showNextCode = true, hideCodes = true)
        DsService(state = ServicePreview.copy(timer = 3, revealed = false), showNextCode = true, hideCodes = true)
        DsService(state = ServicePreview.copy(authType = ServiceAuthType.Hotp))
        DsService(state = ServicePreview.copy(authType = ServiceAuthType.Hotp, revealed = false), hideCodes = true)
    }
}


@Preview
@Composable
private fun PreviewCompact() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DsService(state = ServicePreview, style = ServiceStyle.Compact)
        DsService(state = ServicePreview.copy(timer = 3), style = ServiceStyle.Compact, showNextCode = true, hideCodes = true)
        DsService(state = ServicePreview.copy(revealed = false), style = ServiceStyle.Compact, hideCodes = true)
        DsService(state = ServicePreview.copy(authType = ServiceAuthType.Hotp), style = ServiceStyle.Compact)
        DsService(
            state = ServicePreview.copy(authType = ServiceAuthType.Hotp, revealed = false),
            style = ServiceStyle.Compact,
            hideCodes = true
        )
    }
}

@Preview
@Composable
private fun PreviewEdit() {
    DsService(state = ServicePreview, editMode = true)
}

internal val ServicePreview = ServiceState(
    name = "Service Name",
    info = "Additional Info",
    code = "123456",
    nextCode = "789987",
    timer = 10,
    hotpCounter = 1,
    progress = .33f,
    imageType = ServiceImageType.Label,
    authType = ServiceAuthType.Totp,
    iconLight = "",
    iconDark = "",
    labelText = "2F",
    labelColor = Color.Red,
    badgeColor = Color.Red,
    revealed = true,
)