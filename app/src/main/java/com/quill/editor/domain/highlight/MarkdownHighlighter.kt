package com.quill.editor.domain.highlight

/**
 * Markdown syntax tokenizer. Single master regex, ordered so fenced code blocks and headings win
 * over inline emphasis. Bold is matched before italic so `**x**` isn't split into two italics.
 */
object MarkdownHighlighter : SyntaxHighlighter {

    private const val G_CODEBLOCK = "codeblock"
    private const val G_HEADING = "heading"
    private const val G_CODE = "code"
    private const val G_LINK = "link"
    private const val G_BOLD = "bold"
    private const val G_ITALIC = "italic"

    private val master: Regex = run {
        val pattern = buildString {
            append("(?<$G_CODEBLOCK>```.*?```)")
            append("|(?<$G_HEADING>^#{1,6}[^\\n]*)")
            append("|(?<$G_CODE>`[^`\\n]+`)")
            append("|(?<$G_LINK>\\[[^\\]\\n]*]\\([^)\\n]*\\))")
            append("|(?<$G_BOLD>\\*\\*[^\\n]+?\\*\\*|__[^\\n]+?__)")
            append("|(?<$G_ITALIC>\\*[^*\\n]+?\\*|_[^_\\n]+?_)")
        }
        Regex(pattern, setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE))
    }

    override fun tokenize(text: String): List<TokenSpan> {
        val spans = ArrayList<TokenSpan>()
        for (match in master.findAll(text)) {
            val type = when {
                match.groups[G_CODEBLOCK] != null -> TokenType.CODE
                match.groups[G_HEADING] != null -> TokenType.HEADING
                match.groups[G_CODE] != null -> TokenType.CODE
                match.groups[G_LINK] != null -> TokenType.LINK
                match.groups[G_BOLD] != null -> TokenType.BOLD
                match.groups[G_ITALIC] != null -> TokenType.ITALIC
                else -> continue
            }
            spans += TokenSpan(match.range.first, match.range.last + 1, type)
        }
        return spans
    }
}
