package com.twofasapp.feature.secrets.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.twofasapp.designsystem.TwIcons
import com.twofasapp.designsystem.TwTheme
import com.twofasapp.designsystem.common.TwTopAppBar
import kotlin.math.ln
import kotlin.math.log2
import kotlin.math.max
import java.security.SecureRandom

private val passwordRandom = SecureRandom()

private enum class PasswordMode(val label: String) {
    RANDOM("Random"), MEMORABLE("Memorable"), PIN("PIN"), PASSPHRASE("Passphrase")
}

private data class PasswordOptions(
    val mode: PasswordMode = PasswordMode.RANDOM,
    val length: Int = 16,
    val uppercase: Boolean = true,
    val lowercase: Boolean = true,
    val numbers: Boolean = true,
    val symbols: Boolean = true,
    val extended: Boolean = false,
    val excludeAmbiguous: Boolean = true,
    val excludeSimilar: Boolean = false,
    val exclusions: String = "",
    val avoidRepeated: Boolean = true,
    val avoidSequential: Boolean = false,
    val minUppercase: Int = 1,
    val minLowercase: Int = 1,
    val minNumbers: Int = 1,
    val minSymbols: Int = 0,
    val words: Int = 4,
    val separator: String = "_",
    val capitalizeWords: Boolean = true,
    val includePhraseNumbers: Boolean = true,
    val includePhraseSymbols: Boolean = false,
)

@Composable
fun PasswordGeneratorDialog(
    onDismiss: () -> Unit,
    onUsePassword: (String) -> Unit,
    onSaveCredential: (String, String) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    var options by remember { mutableStateOf(PasswordOptions()) }
    var password by remember { mutableStateOf(generate(options)) }
    var showSaveName by remember { mutableStateOf(false) }

    fun regenerate(newOptions: PasswordOptions = options) {
        options = newOptions
        password = generate(newOptions)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .focusable()
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.key == Key.Escape) {
                        onDismiss()
                        true
                    } else false
                }
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0A071A), Color(0xFF17102F), Color(0xFF090719))
                    )
                ),
        ) {
            Box(
                Modifier
                    .size(280.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        Brush.radialGradient(listOf(Color(0xFF9C5CFF).copy(alpha = .3f), Color.Transparent)),
                        CircleShape,
                    )
            )
            Column {
                TwTopAppBar(
                    title = {
                        Column {
                            Text("Password lab", color = TwTheme.color.onSurfacePrimary, fontWeight = FontWeight.Bold)
                            Text("Create something unbreakable", color = TwTheme.color.onSurfaceSecondary, fontSize = 12.sp)
                        }
                    },
                    actions = {
                        IconButton(onClick = onDismiss) {
                            Icon(painter = TwIcons.Close, contentDescription = "Close", tint = TwTheme.color.onSurfacePrimary)
                        }
                    },
                    showBackButton = false,
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 18.dp, end = 18.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        GeneratorPreview(
                            password = password,
                            level = strengthLevel(entropyBits(options, password)),
                            entropy = entropyBits(options, password),
                        )
                    }
                    item {
                        Text("PASSWORD STYLE", color = Color(0xFFB9A7D8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            PasswordMode.entries.forEach { mode ->
                                ModeChip(
                                    label = mode.label,
                                    selected = options.mode == mode,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        regenerate(
                                            options.copy(
                                                mode = mode,
                                                length = if (mode == PasswordMode.PIN) options.length.coerceIn(4, 8)
                                                else options.length.coerceIn(4, 64),
                                            )
                                        )
                                    },
                                )
                            }
                        }
                    }
                    item {
                        GeneratorSection("BASIC OPTIONS") {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Length", modifier = Modifier.width(58.dp), color = TwTheme.color.onSurfacePrimary)
                                val maximumLength = if (options.mode == PasswordMode.PIN) 8 else 64
                                Slider(
                                    value = options.length.toFloat(),
                                    onValueChange = { regenerate(options.copy(length = it.toInt().coerceIn(4, maximumLength))) },
                                    valueRange = 4f..maximumLength.toFloat(),
                                    modifier = Modifier.weight(1f),
                                )
                                Text("${options.length}", color = Color(0xFFE28CFF), fontWeight = FontWeight.Bold)
                            }
                            ToggleLine("Uppercase (A–Z)", options.uppercase) { regenerate(options.copy(uppercase = it)) }
                            ToggleLine("Lowercase (a–z)", options.lowercase) { regenerate(options.copy(lowercase = it)) }
                            ToggleLine("Numbers (0–9)", options.numbers) { regenerate(options.copy(numbers = it)) }
                            ToggleLine("Symbols (!@#$)", options.symbols) { regenerate(options.copy(symbols = it)) }
                            ToggleLine("Extended symbols", options.extended) { regenerate(options.copy(extended = it)) }
                        }
                    }
                    item {
                        GeneratorSection("ADVANCED OPTIONS") {
                            ToggleLine("Exclude ambiguous  O 0 I l 1", options.excludeAmbiguous) { regenerate(options.copy(excludeAmbiguous = it)) }
                            ToggleLine("Exclude similar  S/5 B/8 G/6", options.excludeSimilar) { regenerate(options.copy(excludeSimilar = it)) }
                            androidx.compose.material3.OutlinedTextField(
                                value = options.exclusions,
                                onValueChange = { regenerate(options.copy(exclusions = it)) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Custom exclusions") },
                                singleLine = true,
                            )
                            ToggleLine("Avoid repeated characters", options.avoidRepeated) { regenerate(options.copy(avoidRepeated = it)) }
                            ToggleLine("Avoid sequential characters", options.avoidSequential) { regenerate(options.copy(avoidSequential = it)) }
                            Text("Minimum character counts", color = TwTheme.color.onSurfaceSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                MinimumField("A–Z", options.minUppercase) { regenerate(options.copy(minUppercase = it)) }
                                MinimumField("a–z", options.minLowercase) { regenerate(options.copy(minLowercase = it)) }
                                MinimumField("0–9", options.minNumbers) { regenerate(options.copy(minNumbers = it)) }
                                MinimumField("!@#", options.minSymbols) { regenerate(options.copy(minSymbols = it)) }
                            }
                        }
                    }
                    if (options.mode == PasswordMode.MEMORABLE || options.mode == PasswordMode.PASSPHRASE) {
                        item {
                            GeneratorSection("PASSPHRASE OPTIONS") {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Words", modifier = Modifier.weight(1f), color = TwTheme.color.onSurfacePrimary)
                                    TextButton(onClick = { regenerate(options.copy(words = (options.words - 1).coerceAtLeast(2))) }) { Text("−") }
                                    Text("${options.words}", color = Color(0xFFE28CFF), fontWeight = FontWeight.Bold)
                                    TextButton(onClick = { regenerate(options.copy(words = (options.words + 1).coerceAtMost(10))) }) { Text("+") }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("-" to "Hyphens", "_" to "Underscore", "." to "Period", " " to "Space").forEach { (value, label) ->
                                        ModeChip(
                                            label = label,
                                            selected = options.separator == value,
                                            modifier = Modifier.weight(1f),
                                            onClick = { regenerate(options.copy(separator = value)) },
                                        )
                                    }
                                }
                                ToggleLine("Capitalize words", options.capitalizeWords) { regenerate(options.copy(capitalizeWords = it)) }
                                ToggleLine("Include numbers (0–9)", options.includePhraseNumbers) { regenerate(options.copy(includePhraseNumbers = it)) }
                                ToggleLine("Include symbols", options.includePhraseSymbols) { regenerate(options.copy(includePhraseSymbols = it)) }
                            }
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { context.copyToClipboard(password); onUsePassword(password) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE18CFF),
                                    contentColor = Color(0xFF321748),
                                ),
                            ) { Text("Use password and copy", fontWeight = FontWeight.Bold) }
                            Button(
                                onClick = { showSaveName = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2C2148),
                                    contentColor = Color(0xFFEEDFFF),
                                ),
                            ) { Text("Save to credentials", fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }
    }
    if (showSaveName) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSaveName = false },
            title = { Text("Save generated credential") },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Credential name") },
                    singleLine = true,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveCredential(name.trim(), password)
                        showSaveName = false
                    },
                    enabled = name.isNotBlank(),
                ) { Text("Save securely") }
            },
            dismissButton = { TextButton(onClick = { showSaveName = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun GeneratorPreview(
    password: String,
    level: String,
    entropy: Double,
) {
    val accent = strengthColor(level)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF3A1C62), Color(0xFF1C163C), Color(0xFF120D28))
                )
            )
            .border(BorderStroke(1.dp, Color(0xFFB878FF).copy(alpha = .5f)))
            .padding(19.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("GENERATED SECRET", color = Color(0xFFBA9DDC), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("Live preview", color = Color(0xFF8E7BAF), fontSize = 12.sp)
            }
        }
        Text(
            text = password,
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFE6B7FF),
            fontFamily = FontFamily.Monospace,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$level  •  ~${entropy.toInt()} bits", color = accent, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            repeat(5) { index ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(7.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (index < strengthIndex(level)) accent else Color(0xFF33284C))
                )
            }
        }
    }
}

@Composable
private fun ModeChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Color(0xFFE18CFF) else Color(0xFF21183A))
            .border(
                BorderStroke(
                    1.dp,
                    if (selected) Color(0xFFF5D8FF) else Color(0xFF5C4778),
                ),
                RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (selected) Color(0xFF321748) else Color(0xFFD7C8EB),
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
        )
    }
}

@Composable
private fun GeneratorSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    var expanded by remember { mutableStateOf(title == "BASIC OPTIONS") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF241844), Color(0xFF151029))
                )
            )
            .border(BorderStroke(1.dp, Color(0xFF594473)), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, color = Color(0xFFD8C5F2), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Icon(
                painter = if (expanded) TwIcons.ChevronDown else TwIcons.ChevronRight,
                contentDescription = if (expanded) "Collapse $title" else "Expand $title",
            )
        }
        if (expanded) {
            HorizontalDivider(color = TwTheme.color.glassOutline)
            content()
        }
    }
}

@Composable
private fun ToggleLine(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(1f), color = TwTheme.color.onSurfacePrimary)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun MinimumField(label: String, value: Int, onChange: (Int) -> Unit) {
    androidx.compose.material3.OutlinedTextField(
        value = value.toString(),
        onValueChange = { onChange(it.toIntOrNull()?.coerceIn(0, 9) ?: 0) },
        label = { Text(label) },
        modifier = Modifier.width(72.dp),
        singleLine = true,
    )
}

private fun Context.copyToClipboard(value: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Generated password", value))
    Handler(Looper.getMainLooper()).postDelayed({
        val current = clipboard.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString()
        if (current == value) clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
    }, CLIPBOARD_CLEAR_DELAY_MS)
    android.widget.Toast.makeText(this, "Copied!", android.widget.Toast.LENGTH_SHORT).show()
}

private const val CLIPBOARD_CLEAR_DELAY_MS = 30_000L

private data class CharacterPools(
    val uppercase: String,
    val lowercase: String,
    val numbers: String,
    val symbols: String,
)

private fun generate(options: PasswordOptions): String {
    val pools = characterPools(options)
    return when (options.mode) {
        PasswordMode.RANDOM -> generateCharacters(options, options.length.coerceIn(4, 64), pools)
        PasswordMode.PIN -> generateCharacters(options, options.length.coerceIn(4, 8), pools)
        PasswordMode.MEMORABLE -> generateMemorable(options, pools)
        PasswordMode.PASSPHRASE -> generatePassphrase(options, pools)
    }
}

private fun characterPools(options: PasswordOptions): CharacterPools {
    fun allowed(chars: String): String = chars.filterNot { char ->
        char in options.exclusions ||
            (options.excludeAmbiguous && char in "O0Il1") ||
            (options.excludeSimilar && char in "S5B8G6")
    }

    return CharacterPools(
        uppercase = allowed("ABCDEFGHIJKLMNOPQRSTUVWXYZ"),
        lowercase = allowed("abcdefghijklmnopqrstuvwxyz"),
        numbers = allowed("0123456789"),
        symbols = allowed("!@#$%^&*()-_=+" + if (options.extended) "~|\\[]{}:;,.<>/?" else ""),
    )
}

private fun generateCharacters(
    options: PasswordOptions,
    targetLength: Int,
    pools: CharacterPools,
    includePhraseNumbers: Boolean = true,
    includePhraseSymbols: Boolean = true,
): String {
    val selected = buildList {
        if (options.uppercase && pools.uppercase.isNotEmpty()) add(pools.uppercase to options.minUppercase)
        if (options.lowercase && pools.lowercase.isNotEmpty()) add(pools.lowercase to options.minLowercase)
        if (options.numbers && includePhraseNumbers && pools.numbers.isNotEmpty()) add(pools.numbers to options.minNumbers)
        if (options.symbols && includePhraseSymbols && pools.symbols.isNotEmpty()) add(pools.symbols to options.minSymbols)
    }
    val fallback = selected.joinToString(separator = "") { it.first }
        .ifEmpty { pools.lowercase.ifEmpty { pools.numbers }.ifEmpty { "x" } }
    val output = StringBuilder()

    selected.forEach { (chars, minimum) ->
        repeat(minimum.coerceAtMost(targetLength - output.length)) {
            appendAllowed(output, chars, options)
        }
    }
    while (output.length < targetLength) {
        appendAllowed(output, fallback, options)
    }
    return output.toString()
}

private fun appendAllowed(output: StringBuilder, chars: String, options: PasswordOptions) {
    if (chars.isEmpty()) {
        output.append('x')
        return
    }
    var candidate = chars.secureRandomItem()
    var attempts = 0
    while (attempts++ < 32 && !isAllowedNext(output, candidate, options)) {
        candidate = chars.secureRandomItem()
    }
    output.append(candidate)
}

private fun isAllowedNext(output: StringBuilder, candidate: Char, options: PasswordOptions): Boolean {
    if (options.avoidRepeated && output.lastOrNull() == candidate) return false
    if (options.avoidSequential && output.length >= 2) {
        val previous = output[output.lastIndex]
        val beforePrevious = output[output.lastIndex - 1]
        val ascending = beforePrevious.code + 1 == previous.code && previous.code + 1 == candidate.code
        val descending = beforePrevious.code - 1 == previous.code && previous.code - 1 == candidate.code
        if (ascending || descending) return false
    }
    return true
}

private fun generateMemorable(options: PasswordOptions, pools: CharacterPools): String {
    val targetLength = options.length.coerceIn(4, 64)
    val numberCount = if (options.numbers && options.includePhraseNumbers && pools.numbers.isNotEmpty()) {
        max(2, options.minNumbers)
    } else {
        0
    }
    val symbolCount = if (options.symbols && options.includePhraseSymbols && pools.symbols.isNotEmpty()) {
        max(1, options.minSymbols)
    } else {
        0
    }
    val suffixLength = numberCount + symbolCount
    val baseLength = (targetLength - suffixLength).coerceAtLeast(1)
    val letters = buildString {
        if (options.uppercase) append(pools.uppercase)
        if (options.lowercase) append(pools.lowercase)
    }

    if (letters.isEmpty()) {
        return generateCharacters(
            options = options,
            targetLength = targetLength,
            pools = pools,
            includePhraseNumbers = numberCount > 0,
            includePhraseSymbols = symbolCount > 0,
        )
    }

    val syllables = listOf("ra", "vi", "lo", "ta", "ne", "ku", "mi", "zo", "pe", "sa", "di", "fo")
    val output = StringBuilder()
    var characterIndex = 0
    while (output.length < baseLength) {
        syllables.secureRandomItem().forEach { raw ->
            if (output.length < baseLength) {
                val styled = styleMemorableCharacter(raw, options, pools, characterIndex++)
                if (styled in letters) appendAllowed(output, styled.toString(), options)
            }
        }
    }
    repeat(numberCount) { appendAllowed(output, pools.numbers, options) }
    repeat(symbolCount) { appendAllowed(output, pools.symbols, options) }
    while (output.length < targetLength) appendAllowed(output, letters, options)
    return enforceMinimumCase(output.toString(), options, pools)
}

private fun styleMemorableCharacter(
    char: Char,
    options: PasswordOptions,
    pools: CharacterPools,
    index: Int,
): Char {
    return when {
        options.uppercase && !options.lowercase -> char.uppercaseChar().takeIf { it in pools.uppercase } ?: pools.uppercase.firstOrNull() ?: char
        !options.uppercase && options.lowercase -> char.lowercaseChar().takeIf { it in pools.lowercase } ?: pools.lowercase.firstOrNull() ?: char
        options.uppercase && options.lowercase ->
            if ((options.capitalizeWords && index == 0) || passwordRandom.nextBoolean()) {
                char.uppercaseChar().takeIf { it in pools.uppercase }
                    ?: char.lowercaseChar().takeIf { it in pools.lowercase }
                    ?: pools.uppercase.firstOrNull()
                    ?: pools.lowercase.firstOrNull()
                    ?: char
            } else {
                char.lowercaseChar().takeIf { it in pools.lowercase }
                    ?: char.uppercaseChar().takeIf { it in pools.uppercase }
                    ?: pools.lowercase.firstOrNull()
                    ?: pools.uppercase.firstOrNull()
                    ?: char
            }
        else -> char
    }
}

private fun generatePassphrase(options: PasswordOptions, pools: CharacterPools): String {
    val wordList = listOf(
        "Sunny", "Cedar", "Orbit", "Velvet", "Meadow", "Coffee",
        "Harbor", "Signal", "Amber", "Pixel", "Canyon", "Raven",
    )
    val wordCount = options.words.coerceIn(2, 10)
    val numberCount = if (options.numbers && options.includePhraseNumbers && pools.numbers.isNotEmpty()) {
        max(2, options.minNumbers)
    } else {
        0
    }
    val symbolCount = if (options.symbols && options.includePhraseSymbols && pools.symbols.isNotEmpty()) {
        max(1, options.minSymbols)
    } else {
        0
    }
    if (!options.uppercase && !options.lowercase) {
        return generateCharacters(
            options = options,
            targetLength = options.length.coerceIn(4, 64),
            pools = pools,
            includePhraseNumbers = numberCount > 0,
            includePhraseSymbols = symbolCount > 0,
        )
    }
    val separatorLength = options.separator.length
    val minimumPhraseLength = wordCount + ((wordCount - 1) * separatorLength)
    val phraseLength = (options.length - numberCount - symbolCount).coerceAtLeast(minimumPhraseLength)
    val words = (0 until wordCount).map {
        val raw = wordList.secureRandomItem()
        val filtered = raw.filter {
            it.lowercaseChar() in pools.lowercase || it.uppercaseChar() in pools.uppercase
        }
        filtered.ifBlank { pools.lowercase.ifEmpty { pools.uppercase }.ifEmpty { "x" } }
    }
    val phrase = fitWords(words, phraseLength, separatorLength).joinToString(options.separator)
    val styled = stylePassphrase(phrase, options, pools)
    val output = StringBuilder(styled)
    repeat(numberCount) { appendAllowed(output, pools.numbers, options) }
    repeat(symbolCount) { appendAllowed(output, pools.symbols, options) }
    return enforceMinimumCase(output.toString(), options, pools)
}

private fun fitWords(words: List<String>, targetLength: Int, separatorLength: Int): List<String> {
    val minimumWordsLength = words.size + ((words.size - 1) * separatorLength)
    val budget = targetLength.coerceAtLeast(minimumWordsLength)
    val availableForWords = budget - ((words.size - 1) * separatorLength)
    val baseLength = availableForWords / words.size
    var extra = availableForWords % words.size
    return words.map { word ->
        val length = baseLength + if (extra-- > 0) 1 else 0
        word.repeat((length / word.length) + 1).take(length)
    }
}

private fun stylePassphrase(value: String, options: PasswordOptions, pools: CharacterPools): String {
    return buildString(value.length) {
        var wordStart = true
        value.forEach { char ->
            if (char == options.separator.firstOrNull()) {
                append(char)
                wordStart = true
            } else {
                val styled = when {
                    options.uppercase && !options.lowercase ->
                        char.uppercaseChar().takeIf { it in pools.uppercase } ?: pools.uppercase.firstOrNull() ?: char
                    !options.uppercase && options.lowercase ->
                        char.lowercaseChar().takeIf { it in pools.lowercase } ?: pools.lowercase.firstOrNull() ?: char
                    options.uppercase && options.lowercase && options.capitalizeWords && wordStart ->
                        char.uppercaseChar().takeIf { it in pools.uppercase }
                            ?: char.lowercaseChar().takeIf { it in pools.lowercase }
                            ?: char
                    options.uppercase && options.lowercase ->
                        if (passwordRandom.nextBoolean()) {
                            char.uppercaseChar().takeIf { it in pools.uppercase }
                                ?: char.lowercaseChar().takeIf { it in pools.lowercase }
                                ?: char
                        } else {
                            char.lowercaseChar().takeIf { it in pools.lowercase }
                                ?: char.uppercaseChar().takeIf { it in pools.uppercase }
                                ?: char
                        }
                    else -> char
                }
                append(nextAllowedCharacter(this, styled, options, pools))
                wordStart = false
            }
        }
    }
}

private fun enforceMinimumCase(value: String, options: PasswordOptions, pools: CharacterPools): String {
    if (!options.uppercase && !options.lowercase) return value
    val chars = value.toCharArray()
    fun replaceUntilMinimum(
        minimum: Int,
        predicate: (Char) -> Boolean,
        replacement: String,
    ) {
        while (chars.count(predicate) < minimum && replacement.isNotEmpty()) {
            val index = chars.indexOfFirst { it.isLetter() && !predicate(it) }
            if (index < 0) return
            chars[index] = replacement.secureRandomItem()
        }
    }
    if (options.uppercase) {
        replaceUntilMinimum(options.minUppercase, Char::isUpperCase, pools.uppercase)
    }
    if (options.lowercase) {
        replaceUntilMinimum(options.minLowercase, Char::isLowerCase, pools.lowercase)
    }
    return String(chars)
}

private fun nextAllowedCharacter(
    output: StringBuilder,
    preferred: Char,
    options: PasswordOptions,
    pools: CharacterPools,
): Char {
    if (isAllowedNext(output, preferred, options)) return preferred
    val alternatives = when {
        preferred.isUpperCase() -> pools.uppercase
        preferred.isLowerCase() -> pools.lowercase
        preferred.isDigit() -> pools.numbers
        else -> pools.symbols
    }
    var candidate = alternatives.firstOrNull() ?: preferred
    repeat(32) {
        if (isAllowedNext(output, candidate, options)) return candidate
        if (alternatives.isNotEmpty()) candidate = alternatives.secureRandomItem()
    }
    return candidate
}

private fun String.secureRandomItem(): Char = this[passwordRandom.nextInt(length)]

private fun <T> List<T>.secureRandomItem(): T = this[passwordRandom.nextInt(size)]

private fun entropyBits(options: PasswordOptions, value: String): Double {
    val alphabet = when (options.mode) {
        PasswordMode.PIN -> 10
        PasswordMode.MEMORABLE, PasswordMode.PASSPHRASE -> 24
        PasswordMode.RANDOM -> listOf(options.uppercase, options.lowercase, options.numbers, options.symbols, options.extended).count { it } * 26
    }.coerceAtLeast(2)
    return max(0.0, value.length * log2(alphabet.toDouble()) - ln(value.length.toDouble()) / ln(2.0))
}

private fun strengthLevel(entropy: Double) = when {
    entropy < 35 -> "Weak"
    entropy < 55 -> "Fair"
    entropy < 75 -> "Good"
    entropy < 95 -> "Strong"
    else -> "Very Strong"
}

private fun strengthIndex(level: String) = when (level) {
    "Weak" -> 1
    "Fair" -> 2
    "Good" -> 3
    "Strong" -> 4
    else -> 5
}

private fun strengthColor(level: String) = when (level) {
    "Weak" -> Color(0xFFE45757)
    "Fair" -> Color(0xFFE59D47)
    "Good" -> Color(0xFFD4B64D)
    "Strong" -> Color(0xFF51BF78)
    else -> Color(0xFF29D391)
}