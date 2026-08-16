package com.quill.editor.ui.editor

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quill.editor.domain.highlight.highlighterFor
import com.quill.editor.domain.highlight.rememberHighlightPalette
import com.quill.editor.domain.model.FileType
import com.quill.editor.ui.appContainer
import com.quill.editor.ui.editor.components.CodeEditor
import com.quill.editor.ui.editor.components.EditorFloatingDock
import com.quill.editor.ui.editor.components.EditorFloatingTopBar
import com.quill.editor.ui.editor.components.FindReplaceBar
import com.quill.editor.ui.editor.components.MarkdownFormatBar
import com.quill.editor.ui.editor.components.MarkdownPreview
import com.quill.editor.ui.editor.components.NameDialogRequest
import com.quill.editor.ui.editor.components.NameInputDialog
import com.quill.editor.ui.editor.components.RecoveryDialog
import kotlinx.coroutines.launch

@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateVersions: (Long) -> Unit,
    onNavigateSettings: () -> Unit,
    darkTheme: Boolean,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val container = appContainer()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val highlighter = remember(state.fileType, container.kotlinKeywords) {
        highlighterFor(state.fileType, container.kotlinKeywords)
    }
    val palette = rememberHighlightPalette(
        darkTheme = darkTheme,
        codeBackground = MaterialTheme.colorScheme.surfaceVariant,
        codeForeground = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    var nameDialog by remember { mutableStateOf<NameDialogRequest?>(null) }

    val openLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            val name = queryDisplayName(context, uri) ?: "imported.txt"
            val text = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }.getOrNull() ?: ""
            viewModel.importContent(name, text)
        }
    }

    LaunchedEffect(state.statusMessage) {
        val message = state.statusMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.consumeStatus()
        }
    }

    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val showFormatBar = state.fileType == FileType.MARKDOWN && !state.showPreview
    val contentTop = statusTop + 62.dp
    val contentBottom = navBottom +
        (if (showFormatBar) 132.dp else 88.dp) +
        (if (state.showFindBar) 84.dp else 0.dp)

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {

        // ---- Full-screen editor OR rendered preview (lifts above the keyboard) ----
        Box(modifier = Modifier.fillMaxSize().imePadding()) {
            if (state.showPreview && state.fileType == FileType.MARKDOWN) {
                MarkdownPreview(
                    markdown = state.text.text,
                    modifier = Modifier.fillMaxSize(),
                    contentTopPadding = contentTop,
                    contentBottomPadding = contentBottom,
                )
            } else {
                CodeEditor(
                    state = state,
                    highlighter = highlighter,
                    palette = palette,
                    onValueChange = viewModel::onTextChange,
                    modifier = Modifier.fillMaxSize(),
                    contentTopPadding = contentTop,
                    contentBottomPadding = contentBottom,
                )
            }
        }

        // ---- Floating circular top bar ----
        EditorFloatingTopBar(
            state = state,
            onMenu = onOpenDrawer,
            onToggleReadOnly = viewModel::toggleReadOnly,
            onSave = {
                if (viewModel.needsFileName()) {
                    nameDialog = NameDialogRequest("Save file", state.fileName, "Save")
                } else {
                    viewModel.save()
                }
            },
            onSaveAs = { nameDialog = NameDialogRequest("Save as", state.fileName, "Save") },
            onNew = viewModel::newFile,
            onOpen = { openLauncher.launch(arrayOf("text/*", "application/octet-stream")) },
            onVersions = {
                val id = state.fileId
                if (id != null) onNavigateVersions(id)
                else scope.launch { snackbarHostState.showSnackbar("Save the file first to see versions") }
            },
            onSettings = onNavigateSettings,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = statusTop),
        )

        // ---- Floating bottom stack: find bar + markdown tools + dock ----
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state.showFindBar) {
                FindReplaceBar(
                    state = state,
                    onFindChange = viewModel::setFindQuery,
                    onReplaceChange = viewModel::setReplaceQuery,
                    onNext = viewModel::findNext,
                    onReplaceAll = viewModel::replaceAll,
                    onToggleReplace = viewModel::toggleReplace,
                    onClose = viewModel::closeFind,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (showFormatBar) {
                MarkdownFormatBar(
                    onHeading = viewModel::applyHeading,
                    onBold = { viewModel.wrapSelection("**") },
                    onItalic = { viewModel.wrapSelection("*") },
                    onStrikethrough = { viewModel.wrapSelection("~~") },
                    onInlineCode = { viewModel.wrapSelection("`") },
                    onBulletList = { viewModel.toggleLinePrefix("- ") },
                    onNumberedList = { viewModel.toggleLinePrefix("1. ") },
                    onQuote = { viewModel.toggleLinePrefix("> ") },
                    onLink = viewModel::insertLink,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            EditorFloatingDock(
                state = state,
                onUndo = viewModel::undo,
                onRedo = viewModel::redo,
                onFind = viewModel::toggleFind,
                onTogglePreview = viewModel::togglePreview,
                onToggleWrap = viewModel::toggleWordWrap,
            )
        }

        // ---- Snackbar, floating just above the dock ----
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 96.dp, start = 12.dp, end = 12.dp),
        )
    }

    nameDialog?.let { request ->
        NameInputDialog(
            request = request,
            onConfirm = { name ->
                nameDialog = null
                viewModel.createAndSave(name)
            },
            onDismiss = { nameDialog = null },
        )
    }

    state.pendingRecovery?.let { recovery ->
        RecoveryDialog(
            fileName = recovery.key,
            onRestore = viewModel::restoreRecovery,
            onDiscard = viewModel::discardRecovery,
        )
    }
}

private fun queryDisplayName(context: Context, uri: Uri): String? =
    runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        }
    }.getOrNull()
