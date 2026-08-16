package com.quill.editor.domain.model

/** The kind of file being edited, used to pick a syntax highlighter and preview mode. */
enum class FileType {
    KOTLIN,
    MARKDOWN,
    PLAIN;

    val isMarkdown: Boolean get() = this == MARKDOWN

    companion object {
        fun fromFileName(name: String): FileType = when (name.substringAfterLast('.', "").lowercase()) {
            "kt", "kts" -> KOTLIN
            "md", "markdown" -> MARKDOWN
            else -> PLAIN
        }
    }
}
