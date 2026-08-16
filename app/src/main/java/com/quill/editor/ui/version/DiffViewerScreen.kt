package com.quill.editor.ui.version

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quill.editor.domain.DeltaEngine
import com.quill.editor.domain.DiffLine
import com.quill.editor.domain.DiffLineType
import com.quill.editor.ui.appContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiffViewerScreen(
    fileId: Long,
    versionOld: Int,
    versionNew: Int,
    onBack: () -> Unit,
    onRestore: (Int) -> Unit,
) {
    val container = appContainer()

    val diff by produceState<List<DiffLine>?>(initialValue = null, fileId, versionOld, versionNew) {
        val entity = container.fileRepository.getFile(fileId)
        value = if (entity == null) {
            emptyList()
        } else {
            val base = container.fileRepository.readBaseContent(entity)
            val oldContent = container.versionRepository.reconstruct(fileId, versionOld, base)
            val newContent = container.versionRepository.reconstruct(fileId, versionNew, base)
            DeltaEngine.computeDiffLines(oldContent, newContent)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diff  v$versionOld → v$versionNew") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { onRestore(versionNew) }) { Text("Restore v$versionNew") }
                },
            )
        },
    ) { padding ->
        when (val lines = diff) {
            null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            else -> {
                val horizontal = rememberScrollState()
                LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                    items(lines) { line -> DiffLineRow(line, horizontal) }
                }
            }
        }
    }
}

@Composable
private fun DiffLineRow(line: DiffLine, horizontal: androidx.compose.foundation.ScrollState) {
    val dark = MaterialTheme.colorScheme.surface.luminanceIsDark()
    val (background, marker, textColor) = when (line.type) {
        DiffLineType.INSERT -> Triple(
            if (dark) Color(0xFF14351B) else Color(0xFFDDF5E1),
            "+",
            if (dark) Color(0xFFA6E9B4) else Color(0xFF1B5E20),
        )
        DiffLineType.DELETE -> Triple(
            if (dark) Color(0xFF3A1618) else Color(0xFFFBDDDD),
            "-",
            if (dark) Color(0xFFF3B6B6) else Color(0xFFB3261E),
        )
        DiffLineType.EQUAL -> Triple(
            MaterialTheme.colorScheme.surface,
            " ",
            MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    val mono = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = textColor)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = line.newLineNumber?.toString() ?: line.oldLineNumber?.toString().orEmpty(),
            style = mono.copy(color = textColor.copy(alpha = 0.5f)),
            modifier = Modifier.width(36.dp),
        )
        Text(text = marker, style = mono, modifier = Modifier.width(14.dp))
        Box(Modifier.horizontalScroll(horizontal)) {
            Text(text = line.text.ifEmpty { " " }, style = mono, maxLines = 1)
        }
    }
}

private fun Color.luminanceIsDark(): Boolean = (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f
