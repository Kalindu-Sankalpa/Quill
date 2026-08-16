package com.quill.editor.ui.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.quill.editor.data.recovery.CrashRecoveryManager
import com.quill.editor.data.repository.FileRepository
import com.quill.editor.data.repository.SettingsRepository
import com.quill.editor.data.repository.VersionRepository
import com.quill.editor.di.AppContainer
import com.quill.editor.domain.UndoRedoManager
import com.quill.editor.domain.model.FileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class EditorViewModel(
    private val fileRepository: FileRepository,
    private val versionRepository: VersionRepository,
    private val settingsRepository: SettingsRepository,
    private val crashRecoveryManager: CrashRecoveryManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    /** Last explicitly-saved content, used as the diff base for the next commit. */
    private var savedContent: String = ""
    private val undoRedo = UndoRedoManager<TextFieldValue>()

    private var autoSaveJob: Job? = null
    private var autoSaveIntervalSeconds = 10

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { s ->
                _uiState.update {
                    it.copy(
                        wordWrap = s.wordWrap,
                        fontSize = s.fontSize,
                        showLineNumbers = s.showLineNumbers,
                        encoding = if (it.fileId == null) s.encoding else it.encoding,
                    )
                }
                if (s.autoSaveIntervalSeconds != autoSaveIntervalSeconds) {
                    autoSaveIntervalSeconds = s.autoSaveIntervalSeconds
                    restartAutoSave()
                }
            }
        }
        restartAutoSave()
        checkForRecovery()
    }

    // ---- Editing ----

    fun onTextChange(new: TextFieldValue) {
        val current = _uiState.value
        if (current.isReadOnly) return
        if (new.text != current.text.text) {
            undoRedo.record(current.text)
        }
        _uiState.update {
            it.copy(
                text = new,
                isDirty = new.text != savedContent,
                canUndo = undoRedo.canUndo,
                canRedo = undoRedo.canRedo,
                matchCount = countMatches(new.text, it.findQuery),
            )
        }
    }

    fun undo() {
        val restored = undoRedo.undo(_uiState.value.text) ?: return
        _uiState.update {
            it.copy(
                text = restored,
                isDirty = restored.text != savedContent,
                canUndo = undoRedo.canUndo,
                canRedo = undoRedo.canRedo,
            )
        }
    }

    fun redo() {
        val restored = undoRedo.redo(_uiState.value.text) ?: return
        _uiState.update {
            it.copy(
                text = restored,
                isDirty = restored.text != savedContent,
                canUndo = undoRedo.canUndo,
                canRedo = undoRedo.canRedo,
            )
        }
    }

    // ---- File operations ----

    fun newFile() {
        undoRedo.clear()
        savedContent = ""
        _uiState.update {
            EditorUiState(
                fileName = "untitled.md",
                fileType = FileType.MARKDOWN,
                wordWrap = it.wordWrap,
                fontSize = it.fontSize,
                showLineNumbers = it.showLineNumbers,
                encoding = it.encoding,
                statusMessage = "New file",
            )
        }
    }

    /** True when a filename must be requested before saving (unsaved buffer). */
    fun needsFileName(): Boolean = _uiState.value.fileId == null

    /** First save of a new buffer: write base content + record version 1. */
    fun createAndSave(name: String) {
        viewModelScope.launch {
            val state = _uiState.value
            val content = state.text.text
            val entity = fileRepository.createFile(name, content, state.encoding)
            versionRepository.initBase(entity.id)
            savedContent = content
            crashRecoveryManager.clearRecovery(state.fileName)
            crashRecoveryManager.clearRecovery(entity.name)
            _uiState.update {
                it.copy(
                    fileId = entity.id,
                    fileName = entity.name,
                    fileType = FileType.fromFileName(entity.name),
                    isDirty = false,
                    statusMessage = "Saved ${entity.name} (v1)",
                )
            }
        }
    }

    /** Save an existing file: commit a new delta version. */
    fun save() {
        val state = _uiState.value
        val id = state.fileId ?: return
        viewModelScope.launch {
            val content = state.text.text
            val versionNumber = versionRepository.commit(id, savedContent, content)
            savedContent = content
            fileRepository.markOpened(id)
            crashRecoveryManager.clearRecovery(state.fileName)
            _uiState.update {
                it.copy(
                    isDirty = false,
                    statusMessage = if (versionNumber != null) "Saved v$versionNumber" else "No changes to save",
                )
            }
        }
    }

    fun openFile(id: Long) {
        viewModelScope.launch {
            val entity = fileRepository.getFile(id) ?: return@launch
            val base = fileRepository.readBaseContent(entity)
            val latest = versionRepository.latestVersionNumber(id)
            val content = versionRepository.reconstruct(id, latest, base)
            undoRedo.clear()
            savedContent = content
            fileRepository.markOpened(id)
            _uiState.update {
                it.copy(
                    fileId = id,
                    fileName = entity.name,
                    fileType = FileType.fromFileName(entity.name),
                    text = TextFieldValue(content),
                    isReadOnly = entity.isReadOnly,
                    encoding = entity.encoding,
                    isDirty = false,
                    canUndo = false,
                    canRedo = false,
                    showFindBar = false,
                    showPreview = false,
                    statusMessage = "Opened ${entity.name}",
                )
            }
        }
    }

    /** Import text picked via the system file picker into a new unsaved buffer. */
    fun importContent(name: String, content: String) {
        undoRedo.clear()
        savedContent = ""
        _uiState.update {
            it.copy(
                fileId = null,
                fileName = name,
                fileType = FileType.fromFileName(name),
                text = TextFieldValue(content),
                isReadOnly = false,
                isDirty = content.isNotEmpty(),
                canUndo = false,
                canRedo = false,
                statusMessage = "Imported $name",
            )
        }
    }

    fun toggleReadOnly() {
        val state = _uiState.value
        val newValue = !state.isReadOnly
        _uiState.update { it.copy(isReadOnly = newValue, statusMessage = if (newValue) "Read-only on" else "Read-only off") }
        state.fileId?.let { id -> viewModelScope.launch { fileRepository.setReadOnly(id, newValue) } }
    }

    // ---- Version restore (called from history/diff screens) ----

    fun restoreVersion(fileId: Long, versionNumber: Int) {
        viewModelScope.launch {
            val entity = fileRepository.getFile(fileId) ?: return@launch
            val base = fileRepository.readBaseContent(entity)
            val latest = versionRepository.latestVersionNumber(fileId)
            val latestContent = versionRepository.reconstruct(fileId, latest, base)
            val restored = versionRepository.reconstruct(fileId, versionNumber, base)
            undoRedo.clear()
            // Keep the latest as the diff base so saving records the rollback as a new version.
            savedContent = latestContent
            _uiState.update {
                it.copy(
                    fileId = fileId,
                    fileName = entity.name,
                    fileType = FileType.fromFileName(entity.name),
                    text = TextFieldValue(restored),
                    isReadOnly = entity.isReadOnly,
                    encoding = entity.encoding,
                    isDirty = restored != latestContent,
                    canUndo = false,
                    canRedo = false,
                    statusMessage = "Restored v$versionNumber (Save to keep)",
                )
            }
        }
    }

    // ---- Find & replace ----

    fun toggleFind() = _uiState.update { it.copy(showFindBar = !it.showFindBar, showReplace = false) }

    fun toggleReplace() = _uiState.update { it.copy(showFindBar = true, showReplace = !it.showReplace) }

    fun closeFind() = _uiState.update { it.copy(showFindBar = false, showReplace = false) }

    fun setFindQuery(query: String) =
        _uiState.update { it.copy(findQuery = query, matchCount = countMatches(it.text.text, query)) }

    fun setReplaceQuery(query: String) = _uiState.update { it.copy(replaceQuery = query) }

    fun findNext() {
        val state = _uiState.value
        val query = state.findQuery
        if (query.isEmpty()) return
        val text = state.text.text
        val from = state.text.selection.max
        var index = text.indexOf(query, startIndex = from.coerceIn(0, text.length))
        if (index < 0) index = text.indexOf(query, 0)
        if (index >= 0) {
            _uiState.update { it.copy(text = it.text.copy(selection = TextRange(index, index + query.length))) }
        }
    }

    fun replaceAll() {
        val state = _uiState.value
        val query = state.findQuery
        if (query.isEmpty() || state.isReadOnly) return
        val newText = state.text.text.replace(query, state.replaceQuery)
        if (newText != state.text.text) {
            undoRedo.record(state.text)
            _uiState.update {
                it.copy(
                    text = TextFieldValue(newText),
                    isDirty = newText != savedContent,
                    canUndo = undoRedo.canUndo,
                    canRedo = undoRedo.canRedo,
                    matchCount = 0,
                    statusMessage = "Replaced all",
                )
            }
        }
    }

    // ---- Markdown formatting ----

    /** Wrap the current selection (or insert at the cursor) with [prefix]…[suffix]. */
    fun wrapSelection(prefix: String, suffix: String = prefix) {
        val state = _uiState.value
        if (state.isReadOnly) return
        val tfv = state.text
        val text = tfv.text
        val start = tfv.selection.min
        val end = tfv.selection.max
        undoRedo.record(tfv)
        val newValue = if (start == end) {
            val newText = text.substring(0, start) + prefix + suffix + text.substring(end)
            TextFieldValue(newText, TextRange(start + prefix.length))
        } else {
            val selected = text.substring(start, end)
            val newText = text.substring(0, start) + prefix + selected + suffix + text.substring(end)
            TextFieldValue(newText, TextRange(start + prefix.length, start + prefix.length + selected.length))
        }
        applyEditedValue(newValue)
    }

    /** Insert [prefix] at the start of the current line (bullets, quotes, numbered lists). */
    fun toggleLinePrefix(prefix: String) {
        val state = _uiState.value
        if (state.isReadOnly) return
        val tfv = state.text
        val text = tfv.text
        val selStart = tfv.selection.min
        val selEnd = tfv.selection.max
        val lineStart = lineStartOf(text, selStart)
        undoRedo.record(tfv)
        val newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
        val delta = prefix.length
        applyEditedValue(TextFieldValue(newText, TextRange(selStart + delta, selEnd + delta)))
    }

    /** Set the current line's Markdown heading level, replacing any existing `#` markers. */
    fun applyHeading(level: Int) {
        val state = _uiState.value
        if (state.isReadOnly) return
        val tfv = state.text
        val text = tfv.text
        val cursor = tfv.selection.min
        val lineStart = lineStartOf(text, cursor)
        var afterHashes = lineStart
        while (afterHashes < text.length && text[afterHashes] == '#') afterHashes++
        if (afterHashes < text.length && text[afterHashes] == ' ') afterHashes++
        val prefix = "#".repeat(level) + " "
        undoRedo.record(tfv)
        val newText = text.substring(0, lineStart) + prefix + text.substring(afterHashes)
        val delta = prefix.length - (afterHashes - lineStart)
        val newCursor = (cursor + delta).coerceIn(0, newText.length)
        applyEditedValue(TextFieldValue(newText, TextRange(newCursor)))
    }

    /** Insert a Markdown link, using any selection as the label and selecting the placeholder URL. */
    fun insertLink() {
        val state = _uiState.value
        if (state.isReadOnly) return
        val tfv = state.text
        val text = tfv.text
        val start = tfv.selection.min
        val end = tfv.selection.max
        undoRedo.record(tfv)
        val label = if (start != end) text.substring(start, end) else "text"
        val insert = "[$label](url)"
        val newText = text.substring(0, start) + insert + text.substring(end)
        val urlStart = start + insert.indexOf("url)")
        applyEditedValue(TextFieldValue(newText, TextRange(urlStart, urlStart + 3)))
    }

    private fun lineStartOf(text: String, index: Int): Int {
        val nl = text.lastIndexOf('\n', (index - 1).coerceAtLeast(0))
        return if (nl < 0) 0 else nl + 1
    }

    private fun applyEditedValue(newValue: TextFieldValue) {
        _uiState.update {
            it.copy(
                text = newValue,
                isDirty = newValue.text != savedContent,
                canUndo = undoRedo.canUndo,
                canRedo = undoRedo.canRedo,
                matchCount = countMatches(newValue.text, it.findQuery),
            )
        }
    }

    // ---- View toggles ----

    fun togglePreview() = _uiState.update { it.copy(showPreview = !it.showPreview) }

    fun toggleWordWrap() {
        viewModelScope.launch { settingsRepository.setWordWrap(!_uiState.value.wordWrap) }
    }

    fun consumeStatus() = _uiState.update { it.copy(statusMessage = null) }

    // ---- Crash recovery ----

    private fun checkForRecovery() {
        val key = crashRecoveryManager.pendingKeys().firstOrNull() ?: return
        val content = crashRecoveryManager.readRecovery(key)
        if (!content.isNullOrEmpty()) {
            _uiState.update { it.copy(pendingRecovery = RecoveryData(key, content)) }
        }
    }

    fun restoreRecovery() {
        val recovery = _uiState.value.pendingRecovery ?: return
        undoRedo.clear()
        savedContent = ""
        _uiState.update {
            it.copy(
                fileId = null,
                fileName = recovery.key,
                fileType = FileType.fromFileName(recovery.key),
                text = TextFieldValue(recovery.content),
                isDirty = true,
                pendingRecovery = null,
                statusMessage = "Recovered unsaved changes",
            )
        }
    }

    fun discardRecovery() {
        val recovery = _uiState.value.pendingRecovery ?: return
        crashRecoveryManager.clearRecovery(recovery.key)
        _uiState.update { it.copy(pendingRecovery = null) }
    }

    // ---- Internals ----

    private fun restartAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(autoSaveIntervalSeconds * 1000L)
                val state = _uiState.value
                if (state.isDirty && !state.isReadOnly) {
                    crashRecoveryManager.saveRecovery(state.fileName, state.text.text)
                }
            }
        }
    }

    private fun countMatches(text: String, query: String): Int {
        if (query.isEmpty()) return 0
        var count = 0
        var index = text.indexOf(query)
        while (index >= 0) {
            count++
            index = text.indexOf(query, index + query.length)
        }
        return count
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                EditorViewModel(
                    container.fileRepository,
                    container.versionRepository,
                    container.settingsRepository,
                    container.crashRecoveryManager,
                )
            }
        }
    }
}
