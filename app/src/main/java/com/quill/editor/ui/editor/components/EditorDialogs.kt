package com.quill.editor.ui.editor.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/** Parameters for the shared "enter a file name" dialog (used by New / Save / Save As). */
data class NameDialogRequest(
    val title: String,
    val initialName: String,
    val confirmLabel: String,
)

@Composable
fun NameInputDialog(
    request: NameDialogRequest,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(request.initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(request.title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text("File name (e.g. Main.kt, notes.md)") },
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank(),
            ) { Text(request.confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
fun RecoveryDialog(
    fileName: String,
    onRestore: () -> Unit,
    onDiscard: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDiscard,
        icon = { Icon(Icons.Filled.Restore, contentDescription = null) },
        title = { Text("Unsaved changes found") },
        text = { Text("Quill recovered auto-saved changes for \"$fileName\". Restore them?") },
        confirmButton = { TextButton(onClick = onRestore) { Text("Restore") } },
        dismissButton = { TextButton(onClick = onDiscard) { Text("Discard") } },
    )
}

@Composable
fun RenameDialog(
    initialLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var label by remember { mutableStateOf(initialLabel) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename version") },
        text = {
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                singleLine = true,
                label = { Text("Label") },
            )
        },
        confirmButton = {
            TextButton(onClick = { if (label.isNotBlank()) onConfirm(label.trim()) }) { Text("Rename") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
