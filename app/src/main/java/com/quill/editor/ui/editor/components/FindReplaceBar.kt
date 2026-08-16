package com.quill.editor.ui.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.quill.editor.ui.editor.EditorUiState

/** Find / find-&-replace bar shown above the bottom toolbar. */
@Composable
fun FindReplaceBar(
    state: EditorUiState,
    onFindChange: (String) -> Unit,
    onReplaceChange: (String) -> Unit,
    onNext: () -> Unit,
    onReplaceAll: () -> Unit,
    onToggleReplace: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = state.findQuery,
                    onValueChange = onFindChange,
                    label = { Text("Find") },
                    singleLine = true,
                    trailingIcon = { Text("${state.matchCount}", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onNext) { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Find next") }
                IconButton(onClick = onToggleReplace) { Icon(Icons.Filled.FindReplace, contentDescription = "Toggle replace") }
                IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Close find") }
            }
            if (state.showReplace) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    OutlinedTextField(
                        value = state.replaceQuery,
                        onValueChange = onReplaceChange,
                        label = { Text("Replace with") },
                        singleLine = true,
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { onReplaceAll() }),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Done),
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onReplaceAll) { Text("Replace all") }
                }
            }
        }
    }
}
