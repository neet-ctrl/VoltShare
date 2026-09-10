package com.twofasapp.feature.secrets.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.twofasapp.designsystem.TwIcons
import com.twofasapp.designsystem.TwTheme

private val FeedbackAccent = Color(0xFFE18CFF)

internal enum class FeedbackSettingsStep {
    NONE,
    SETUP,
    ACTIONS,
    VERIFY_CHANGE,
    CHANGE,
    VERIFY_DISABLE,
}

@Composable
internal fun FeedbackCheckInDialog(
    hasFeedback: Boolean,
    hasHiddenContent: Boolean,
    unlocked: Boolean,
    onDismiss: () -> Unit,
    onVerify: (Float, String) -> Boolean,
) {
    when {
        !hasFeedback -> FeedbackModal(
            eyebrow = "FEEDBACK SPACE",
            title = "Feedback is not ready",
            icon = TwIcons.Comment,
            accent = FeedbackAccent,
            onDismiss = onDismiss,
        ) {
            FeedbackInfoCard(
                icon = TwIcons.Comment,
                text = "Your feedback check-in will appear here once it has been set up.",
                accent = FeedbackAccent,
            )
            FeedbackModalFooter(
                actionLabel = "Done",
                enabled = true,
                onAction = onDismiss,
                onDismiss = onDismiss,
            )
        }

        unlocked -> FeedbackModal(
            eyebrow = "FEEDBACK SPACE",
            title = "Thanks for checking in",
            icon = TwIcons.Comment,
            accent = FeedbackAccent,
            onDismiss = onDismiss,
        ) {
            FeedbackInfoCard(
                icon = TwIcons.CheckCircle,
                text = if (hasHiddenContent) {
                    "Your private collection is available for this session."
                } else {
                    "There is no hidden content waiting right now."
                },
                accent = Color(0xFF6FE2B0),
            )
            FeedbackModalFooter(
                actionLabel = "Done",
                enabled = true,
                onAction = onDismiss,
                onDismiss = onDismiss,
            )
        }

        else -> FeedbackFormDialog(
            eyebrow = "FEEDBACK CHECK-IN",
            title = "Share your response",
            actionLabel = "Continue",
            onDismiss = onDismiss,
            onSubmit = onVerify,
        )
    }
}

@Composable
internal fun FeedbackFormDialog(
    eyebrow: String,
    title: String,
    actionLabel: String,
    onDismiss: () -> Unit,
    onSubmit: (Float, String) -> Boolean,
) {
    var rating by remember { mutableStateOf(0f) }
    var comment by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    FeedbackModal(
        eyebrow = eyebrow,
        title = title,
        icon = TwIcons.Comment,
        accent = FeedbackAccent,
        onDismiss = onDismiss,
    ) {
        Text(
            text = "How would you rate this experience?",
            color = TwTheme.color.onSurfacePrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )
        Spacer(Modifier.height(8.dp))
        SecretRatingPicker(rating) {
            rating = it
            error = null
        }
        Text(
            text = if (rating == 0f) "Choose a rating" else "Your rating: ${rating.formatFeedbackRating()} / 5",
            color = if (rating == 0f) TwTheme.color.onSurfaceTertiary else Color(0xFFFFC65A),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Add a short comment",
            color = TwTheme.color.onSurfacePrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )
        Spacer(Modifier.height(6.dp))
        FeedbackTextField(
            value = comment,
            onValueChange = {
                comment = it
                error = null
            },
        )
        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                text = it,
                color = TwTheme.color.error,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(12.dp))
        FeedbackModalFooter(
            actionLabel = actionLabel,
            enabled = rating > 0f && comment.isNotBlank(),
            onAction = {
                if (!onSubmit(rating, comment)) {
                    error = "That response does not match. Try again."
                }
            },
            onDismiss = onDismiss,
        )
    }
}

@Composable
internal fun FeedbackControlsDialog(
    onDismiss: () -> Unit,
    onChange: () -> Unit,
    onDisable: () -> Unit,
) {
    FeedbackModal(
        eyebrow = "FEEDBACK PREFERENCES",
        title = "Manage your response",
        icon = TwIcons.Comment,
        accent = FeedbackAccent,
        onDismiss = onDismiss,
    ) {
        FeedbackInfoCard(
            icon = TwIcons.CheckCircle,
            text = "Your feedback check-in is currently active.",
            accent = Color(0xFF6FE2B0),
        )
        Spacer(Modifier.height(16.dp))
        FeedbackChoice(
            icon = TwIcons.Edit,
            title = "Change",
            accent = FeedbackAccent,
            onClick = onChange,
        )
        Spacer(Modifier.height(10.dp))
        FeedbackChoice(
            icon = TwIcons.Close,
            title = "Disable",
            accent = TwTheme.color.error,
            destructive = true,
            onClick = onDisable,
        )
    }
}

@Composable
internal fun VaultSettingsDialog(
    hasFeedback: Boolean,
    onDismiss: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onFeedback: () -> Unit,
) {
    FeedbackModal(
        eyebrow = "SECRETS VAULT",
        title = "Vault preferences",
        icon = TwIcons.Secrets,
        accent = FeedbackAccent,
        onDismiss = onDismiss,
    ) {
        FeedbackInfoCard(
            icon = TwIcons.Comment,
            text = if (hasFeedback) {
                "Feedback check-in is active"
            } else {
                "Feedback check-in is ready to set up"
            },
            accent = FeedbackAccent,
        )
        Spacer(Modifier.height(16.dp))
        FeedbackChoice(
            icon = TwIcons.Comment,
            title = "Feedback check-in",
            accent = FeedbackAccent,
            onClick = onFeedback,
        )
        Spacer(Modifier.height(10.dp))
        FeedbackChoice(
            icon = TwIcons.Export,
            title = "Create encrypted backup",
            accent = FeedbackAccent,
            onClick = onExport,
        )
        Spacer(Modifier.height(10.dp))
        FeedbackChoice(
            icon = TwIcons.Import,
            title = "Restore encrypted backup",
            accent = FeedbackAccent,
            onClick = onImport,
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Done", color = TwTheme.color.onSurfaceSecondary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun FeedbackModal(
    eyebrow: String,
    title: String,
    icon: Painter,
    accent: Color,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(.82f)
                .widthIn(max = 360.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF261849), Color(0xFF17112F), Color(0xFF100B23))
                    )
                )
                .border(BorderStroke(1.dp, accent.copy(alpha = .5f)), RoundedCornerShape(26.dp))
                .padding(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 52.dp, y = (-52).dp)
                    .background(
                        Brush.radialGradient(listOf(accent.copy(alpha = .24f), accent.copy(alpha = 0f))),
                        CircleShape,
                    ),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Brush.linearGradient(listOf(accent.copy(alpha = .9f), Color(0xFF7E56E8))))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = .26f)), RoundedCornerShape(15.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(painter = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(11.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = eyebrow,
                            color = accent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            maxLines = 1,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = title,
                            color = TwTheme.color.onSurfacePrimary,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(34.dp),
                    ) {
                        Icon(
                            painter = TwIcons.Close,
                            contentDescription = "Close",
                            tint = TwTheme.color.onSurfaceSecondary,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                content()
            }
        }
    }
}

@Composable
private fun FeedbackInfoCard(
    icon: Painter,
    text: String,
    accent: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(accent.copy(alpha = .11f))
            .border(BorderStroke(1.dp, accent.copy(alpha = .25f)), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painter = icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(9.dp))
        Text(text = text, color = TwTheme.color.onSurfaceSecondary, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun FeedbackChoice(
    icon: Painter,
    title: String,
    accent: Color,
    destructive: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(if (destructive) accent.copy(alpha = .1f) else Color.White.copy(alpha = .045f))
            .border(
                BorderStroke(1.dp, if (destructive) accent.copy(alpha = .3f) else Color.White.copy(alpha = .1f)),
                RoundedCornerShape(15.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accent.copy(alpha = .14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painter = icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            Text(
                title,
                color = if (destructive) accent else TwTheme.color.onSurfacePrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(painter = TwIcons.ChevronRight, contentDescription = null, tint = accent.copy(alpha = .8f), modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun FeedbackModalFooter(
    actionLabel: String,
    enabled: Boolean,
    onAction: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Button(
            onClick = onAction,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FeedbackAccent,
                contentColor = Color(0xFF321748),
                disabledContainerColor = FeedbackAccent.copy(alpha = .2f),
                disabledContentColor = TwTheme.color.onSurfaceTertiary,
            ),
        ) {
            Text(actionLabel, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.End),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text("Cancel", color = TwTheme.color.onSurfaceSecondary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun FeedbackTextField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Write a short response…") },
        minLines = 2,
        maxLines = 4,
        shape = RoundedCornerShape(15.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = TwTheme.color.backgroundSecondary.copy(alpha = .76f),
            unfocusedContainerColor = TwTheme.color.backgroundSecondary.copy(alpha = .52f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = FeedbackAccent,
            focusedTextColor = TwTheme.color.onSurfacePrimary,
            unfocusedTextColor = TwTheme.color.onSurfacePrimary,
            focusedPlaceholderColor = TwTheme.color.onSurfaceTertiary,
            unfocusedPlaceholderColor = TwTheme.color.onSurfaceTertiary,
        ),
    )
}

private fun Float.formatFeedbackRating(): String =
    if (this % 1f == 0f) toInt().toString() else "%.1f".format(this)