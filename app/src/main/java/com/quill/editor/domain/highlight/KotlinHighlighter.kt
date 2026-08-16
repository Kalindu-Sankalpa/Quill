package com.quill.editor.domain.highlight

/**
 * Kotlin syntax tokenizer.
 *
 * Uses a single master regex with ordered alternatives so that, at any position, comments and
 * strings are consumed as whole tokens *before* keyword/number matching — this prevents a keyword
 * that happens to appear inside a string or comment from being highlighted.
 */
class KotlinHighlighter(keywords: Set<String>) : SyntaxHighlighter {

    private val master: Regex = buildMasterRegex(keywords)

    override fun tokenize(text: String): List<TokenSpan> {
        val spans = ArrayList<TokenSpan>()
        for (match in master.findAll(text)) {
            val type = when {
                match.groups[G_COMMENT] != null -> TokenType.COMMENT
                match.groups[G_STRING] != null -> TokenType.STRING
                match.groups[G_ANNOTATION] != null -> TokenType.ANNOTATION
                match.groups[G_NUMBER] != null -> TokenType.NUMBER
                match.groups[G_KEYWORD] != null -> TokenType.KEYWORD
                else -> continue
            }
            spans += TokenSpan(match.range.first, match.range.last + 1, type)
        }
        return spans
    }

    private companion object {
        const val G_COMMENT = "comment"
        const val G_STRING = "string"
        const val G_ANNOTATION = "annotation"
        const val G_NUMBER = "number"
        const val G_KEYWORD = "keyword"

        fun buildMasterRegex(keywords: Set<String>): Regex {
            val kw = keywords
                .filter { it.isNotBlank() }
                .sortedByDescending { it.length }
                .joinToString("|") { Regex.escape(it) }
                .ifBlank { "\\bnever_matches_placeholder\\b" }

            val pattern = buildString {
                append("(?<$G_COMMENT>//[^\\n]*|/\\*.*?\\*/)")
                append("|(?<$G_STRING>\"\"\".*?\"\"\"|\"(?:\\\\.|[^\"\\\\\\n])*\"|'(?:\\\\.|[^'\\\\\\n])')")
                append("|(?<$G_ANNOTATION>@\\w+)")
                append("|(?<$G_NUMBER>\\b\\d[\\d_]*(?:\\.\\d+)?(?:[eE][+-]?\\d+)?[fFlLdD]?\\b)")
                append("|(?<$G_KEYWORD>\\b(?:$kw)\\b)")
            }
            return Regex(pattern, RegexOption.DOT_MATCHES_ALL)
        }
    }
}
