package com.quill.editor.ui.editor

import androidx.compose.ui.text.input.TextFieldValue
import com.quill.editor.domain.model.FileType

/** Pending crash-recovery buffer offered to the user on launch. */
data class RecoveryData(val key: String, val content: String)

/** Immutable UI state for the editor screen. */
data class EditorUiState(
    val fileId: Long? = null,
    val fileName: String = "untitled.md",
    val fileType: FileType = FileType.MARKDOWN,
    val text: TextFieldValue = TextFieldValue(""),
    val isReadOnly: Boolean = false,
    val isDirty: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val wordWrap: Boolean = true,
    val fontSize: Int = 14,
    val showLineNumbers: Boolean = true,
    val showFindBar: Boolean = false,
    val showReplace: Boolean = false,
    val showPreview: Boolean = false,
    val findQuery: String = "",
    val replaceQuery: String = "",
    val matchCount: Int = 0,
    val encoding: String = "UTF-8",
    val statusMessage: String? = null,
    val pendingRecovery: RecoveryData? = null,
)
