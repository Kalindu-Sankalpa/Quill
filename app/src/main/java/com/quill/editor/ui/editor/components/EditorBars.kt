package com.quill.editor.ui.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WrapText
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quill.editor.ui.editor.EditorUiState

/** A round, softly-elevated icon button that floats over the editor (Telegram-style). */
@Composable
fun CircleIconButton(
    imageVector: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    size: Dp = 42.dp,
) {
    val colors = MaterialTheme.colorScheme
    val container = if (selected) colors.primaryContainer else colors.surfaceContainerHigh
    val content = when {
        selected -> colors.onPrimaryContainer
        !enabled -> colors.onSurface.copy(alpha = 0.38f)
        else -> colors.onSurface
    }
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = container.copy(alpha = 0.92f),
        contentColor = content,
        shadowElevation = 3.dp,
        tonalElevation = 3.dp,
        modifier = modifier.size(size),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(imageVector, contentDescription, modifier = Modifier.size(22.dp))
        }
    }
}

/** Floating top controls: circular menu / actions with a filename pill, editor visible behind. */
@Composable
fun EditorFloatingTopBar(
    state: EditorUiState,
    onMenu: () -> Unit,
    onToggleReadOnly: () -> Unit,
    onSave: () -> Unit,
    onSaveAs: () -> Unit,
    onNew: () -> Unit,
    onOpen: () -> Unit,
    onVersions: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CircleIconButton(Icons.Filled.Menu, "Open navigation drawer", onMenu)

        Surface(
            shape = RoundedCornerShape(50),
            color = colors.surfaceContainerHigh.copy(alpha = 0.92f),
            shadowElevation = 3.dp,
            modifier = Modifier.weight(1f),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = state.fileName + if (state.isDirty) " •" else "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = state.fileType.name.lowercase() +
                            (if (state.isReadOnly) " · read-only" else "") +
                            (if (state.showPreview) " · preview" else ""),
                        maxLines = 1,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
        }

        CircleIconButton(Icons.Filled.Save, "Save", onSave)
        CircleIconButton(
            imageVector = if (state.isReadOnly) Icons.Filled.Lock else Icons.Filled.LockOpen,
            contentDescription = "Toggle read-only",
            onClick = onToggleReadOnly,
            selected = state.isReadOnly,
        )
        Box {
            CircleIconButton(Icons.Filled.MoreVert, "More actions", { menuOpen = true })
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("New file") },
                    leadingIcon = { Icon(Icons.Filled.Add, null) },
                    onClick = { menuOpen = false; onNew() },
                )
                DropdownMenuItem(
                    text = { Text("Open…") },
                    leadingIcon = { Icon(Icons.Filled.FolderOpen, null) },
                    onClick = { menuOpen = false; onOpen() },
                )
                DropdownMenuItem(
                    text = { Text("Save as…") },
                    leadingIcon = { Icon(Icons.Filled.Save, null) },
                    onClick = { menuOpen = false; onSaveAs() },
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Version history") },
                    leadingIcon = { Icon(Icons.Filled.History, null) },
                    onClick = { menuOpen = false; onVersions() },
                )
                DropdownMenuItem(
                    text = { Text("Settings") },
                    leadingIcon = { Icon(Icons.Filled.Settings, null) },
                    onClick = { menuOpen = false; onSettings() },
                )
            }
        }
    }
}

/** Floating capsule dock with the primary editor actions. */
@Composable
fun EditorFloatingDock(
    state: EditorUiState,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onFind: () -> Unit,
    onTogglePreview: () -> Unit,
    onToggleWrap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f),
        shadowElevation = 8.dp,
        tonalElevation = 3.dp,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircleIconButton(Icons.AutoMirrored.Filled.Undo, "Undo", onUndo, enabled = state.canUndo)
            CircleIconButton(Icons.AutoMirrored.Filled.Redo, "Redo", onRedo, enabled = state.canRedo)
            CircleIconButton(Icons.Filled.Search, "Find", onFind, selected = state.showFindBar)
            CircleIconButton(Icons.Filled.WrapText, "Word wrap", onToggleWrap, selected = state.wordWrap)
            if (state.fileType.isMarkdown) {
                CircleIconButton(
                    imageVector = Icons.Filled.Visibility,
                    contentDescription = "Toggle full-screen preview",
                    onClick = onTogglePreview,
                    selected = state.showPreview,
                )
            }
        }
    }
}
