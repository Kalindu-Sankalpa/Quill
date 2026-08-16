package com.quill.editor.ui.settings

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quill.editor.data.repository.QuillSettings
import com.quill.editor.data.repository.ThemeMode
import com.quill.editor.ui.appContainer
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val container = appContainer()
    val settings by container.settingsRepository.settings.collectAsStateWithLifecycle(initialValue = QuillSettings())
    val repo = container.settingsRepository
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
        ) {
            SectionTitle("Appearance")
            Text(
                "Theme",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )
            ThemeMode.entries.forEach { mode ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(selected = settings.themeMode == mode, onClick = {
                            scope.launch { repo.setThemeMode(mode) }
                        })
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                ) {
                    RadioButton(selected = settings.themeMode == mode, onClick = null)
                    Text(
                        text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }
            }

            SwitchRow(
                title = "Dynamic color",
                subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    "Use wallpaper-based Material You colors"
                } else {
                    "Requires Android 12+ — this device uses the static palette"
                },
                checked = settings.dynamicColor,
                onCheckedChange = { scope.launch { repo.setDynamicColor(it) } },
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            SectionTitle("Editor")

            FontSizeRow(
                fontSize = settings.fontSize,
                onCommit = { scope.launch { repo.setFontSize(it) } },
            )
            SwitchRow(
                title = "Word wrap",
                subtitle = "Wrap long lines instead of scrolling horizontally",
                checked = settings.wordWrap,
                onCheckedChange = { scope.launch { repo.setWordWrap(it) } },
            )
            SwitchRow(
                title = "Show line numbers",
                subtitle = "Display a line-number gutter",
                checked = settings.showLineNumbers,
                onCheckedChange = { scope.launch { repo.setShowLineNumbers(it) } },
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            SectionTitle("Files")

            DropdownRow(
                title = "Auto-save interval",
                current = "${settings.autoSaveIntervalSeconds}s",
                options = listOf(5, 10, 15, 30, 60),
                optionLabel = { "${it}s" },
                onSelect = { scope.launch { repo.setAutoSaveInterval(it) } },
            )
            DropdownRow(
                title = "File encoding",
                current = settings.encoding,
                options = listOf("UTF-8", "ISO-8859-1", "US-ASCII"),
                optionLabel = { it },
                onSelect = { scope.launch { repo.setEncoding(it) } },
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
    )
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun FontSizeRow(fontSize: Int, onCommit: (Int) -> Unit) {
    var sliderValue by remember(fontSize) { mutableFloatStateOf(fontSize.toFloat()) }
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text("Editor font size: ${sliderValue.roundToInt()}sp", style = MaterialTheme.typography.bodyLarge)
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onCommit(sliderValue.roundToInt()) },
            valueRange = 10f..24f,
            steps = 13,
        )
    }
}

@Composable
private fun <T> DropdownRow(
    title: String,
    current: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(current)
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(optionLabel(option)) },
                        onClick = {
                            expanded = false
                            onSelect(option)
                        },
                    )
                }
            }
        }
    }
}
