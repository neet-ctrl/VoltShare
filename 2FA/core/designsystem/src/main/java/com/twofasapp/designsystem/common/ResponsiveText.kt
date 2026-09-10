package com.twofasapp.designsystem.common

import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.twofasapp.designsystem.TwTheme

private const val TEXT_SCALE_REDUCTION_INTERVAL = 0.9f

@Composable
fun ResponsiveText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    brush: Brush? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = 1,
    style: TextStyle = LocalTextStyle.current,
    targetTextSizeHeight: TextUnit = style.fontSize,
) {
    var textSize by remember(text, targetTextSizeHeight) {
        mutableStateOf(targetTextSizeHeight)
    }

    Text(
        modifier = modifier,
        text = text,
        color = color,
        style = style.copy(fontSize = textSize, brush = brush),
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { textLayoutResult ->
            val maxCurrentLineIndex: Int = textLayoutResult.lineCount - 1

            if (textLayoutResult.isLineEllipsized(maxCurrentLineIndex)) {
                textSize = textSize.times(TEXT_SCALE_REDUCTION_INTERVAL)
            }
        },
    )
}