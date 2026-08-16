package com.quill.editor.ui.drawer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quill.editor.data.local.entity.FileEntity

@Composable
fun QuillDrawer(
    files: List<FileEntity>,
    currentFileId: Long?,
    onNewFile: () -> Unit,
    onOpenFile: (Long) -> Unit,
    onDeleteFile: (FileEntity) -> Unit,
    onVersions: () -> Unit,
    onSettings: () -> Unit,
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 12.dp, top = 20.dp, bottom = 4.dp),
            ) {
                Text("✒ Quill", style = MaterialTheme.typography.headlineSmall)
            }
            Text(
                "A developer's mobile editor",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp, bottom = 12.dp),
            )
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            NavigationDrawerItem(
                label = { Text("New file") },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                selected = false,
                onClick = onNewFile,
            )

            Text(
                "RECENT FILES",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
            )
            if (files.isEmpty()) {
                Text(
                    "No files yet — create one and save it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                )
            } else {
                files.forEach { file ->
                    NavigationDrawerItem(
                        label = { Text(file.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        icon = { Icon(Icons.Filled.Description, contentDescription = null) },
                        badge = {
                            IconButton(onClick = { onDeleteFile(file) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete ${file.name}")
                            }
                        },
                        selected = file.id == currentFileId,
                        onClick = { onOpenFile(file.id) },
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            NavigationDrawerItem(
                label = { Text("Version history") },
                icon = { Icon(Icons.Filled.History, contentDescription = null) },
                selected = false,
                onClick = onVersions,
            )
            NavigationDrawerItem(
                label = { Text("Settings") },
                icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                selected = false,
                onClick = onSettings,
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}
