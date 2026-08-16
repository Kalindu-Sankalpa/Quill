package com.quill.editor.domain.highlight

/** Categories of source tokens recognised by the highlighters. */
enum class TokenType {
    KEYWORD,
    STRING,
    COMMENT,
    NUMBER,
    ANNOTATION,
    HEADING,
    BOLD,
    ITALIC,
    CODE,
    LINK,
    PLAIN,
}

/** A half-open range [start, end) of [text] classified as [type]. */
data class TokenSpan(val start: Int, val end: Int, val type: TokenType)

/**
 * Turns raw source text into a list of styled spans. Implementations are pure (no Compose /
 * Android dependency) so they can be unit-tested on the JVM.
 */
interface SyntaxHighlighter {
    fun tokenize(text: String): List<TokenSpan>
}

/** No-op highlighter for plain text. */
object PlainHighlighter : SyntaxHighlighter {
    override fun tokenize(text: String): List<TokenSpan> = emptyList()
}
