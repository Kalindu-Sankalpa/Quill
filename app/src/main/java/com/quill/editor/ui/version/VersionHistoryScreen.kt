package com.quill.editor.ui.version

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quill.editor.data.local.entity.VersionEntity
import com.quill.editor.ui.appContainer
import com.quill.editor.ui.editor.components.RenameDialog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionHistoryScreen(
    fileId: Long,
    onBack: () -> Unit,
    onViewDiff: (Int, Int) -> Unit,
    onRestore: (Int) -> Unit,
) {
    val container = appContainer()
    val versions by container.versionRepository.observeVersions(fileId)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()
    var renaming by remember { mutableStateOf<VersionEntity?>(null) }

    val newestNumber = versions.maxOfOrNull { it.versionNumber } ?: 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Version history") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (versions.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No versions yet. Save the file to create version 1.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(versions.sortedByDescending { it.versionNumber }, key = { it.id }) { version ->
                    val previous = versions
                        .filter { it.versionNumber < version.versionNumber }
                        .maxByOrNull { it.versionNumber }
                    VersionCard(
                        version = version,
                        canDiff = previous != null,
                        deletable = !version.isBase && version.versionNumber == newestNumber && newestNumber > 1,
                        onDiff = { previous?.let { onViewDiff(it.versionNumber, version.versionNumber) } },
                        onRestore = { onRestore(version.versionNumber) },
                        onRename = { renaming = version },
                        onDelete = { scope.launch { container.versionRepository.delete(version) } },
                    )
                }
            }
        }
    }

    renaming?.let { version ->
        RenameDialog(
            initialLabel = version.label ?: "v${version.versionNumber}",
            onConfirm = { label ->
                scope.launch { container.versionRepository.rename(version, label) }
                renaming = null
            },
            onDismiss = { renaming = null },
        )
    }
}

@Composable
private fun VersionCard(
    version: VersionEntity,
    canDiff: Boolean,
    deletable: Boolean,
    onDiff: () -> Unit,
    onRestore: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    Card {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "v${version.versionNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "  ${version.label ?: ""}" + if (version.isBase) "  (base)" else "",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Box(Modifier.weight(1f))
                IconButton(onClick = onRename) {
                    Icon(Icons.Filled.DriveFileRenameOutline, contentDescription = "Rename")
                }
                if (deletable) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete newest version")
                    }
                }
            }
            Text(
                text = formatTimestamp(version.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (canDiff) {
                    OutlinedButton(onClick = onDiff) {
                        Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("View diff")
                    }
                }
                FilledTonalButton(onClick = onRestore) {
                    Icon(Icons.Filled.Restore, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("Restore")
                }
            }
        }
    }
}

private val timestampFormat = SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault())

private fun formatTimestamp(millis: Long): String = timestampFormat.format(Date(millis))
