package com.quill.editor.domain.highlight

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import com.quill.editor.domain.model.FileType
import com.quill.editor.ui.theme.SyntaxColors

/** Resolved (light- or dark-aware) colors used to turn [TokenType]s into [SpanStyle]s. */
data class HighlightPalette(
    val keyword: Color,
    val string: Color,
    val comment: Color,
    val number: Color,
    val annotation: Color,
    val heading: Color,
    val link: Color,
    val codeForeground: Color,
    val codeBackground: Color,
) {
    fun styleFor(type: TokenType): SpanStyle? = when (type) {
        TokenType.KEYWORD -> SpanStyle(color = keyword, fontWeight = FontWeight.Bold)
        TokenType.STRING -> SpanStyle(color = string)
        TokenType.COMMENT -> SpanStyle(color = comment, fontStyle = FontStyle.Italic)
        TokenType.NUMBER -> SpanStyle(color = number)
        TokenType.ANNOTATION -> SpanStyle(color = annotation)
        TokenType.HEADING -> SpanStyle(color = heading, fontWeight = FontWeight.Bold)
        TokenType.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
        TokenType.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
        TokenType.CODE -> SpanStyle(
            fontFamily = FontFamily.Monospace,
            color = codeForeground,
            background = codeBackground,
        )

        TokenType.LINK -> SpanStyle(color = link, textDecoration = TextDecoration.Underline)
        TokenType.PLAIN -> null
    }
}

@Composable
fun rememberHighlightPalette(darkTheme: Boolean, codeBackground: Color, codeForeground: Color): HighlightPalette =
    remember(darkTheme, codeBackground, codeForeground) {
        if (darkTheme) {
            HighlightPalette(
                keyword = SyntaxColors.keywordDark,
                string = SyntaxColors.stringDark,
                comment = SyntaxColors.commentDark,
                number = SyntaxColors.numberDark,
                annotation = SyntaxColors.annotationDark,
                heading = SyntaxColors.headingDark,
                link = SyntaxColors.linkDark,
                codeForeground = codeForeground,
                codeBackground = codeBackground,
            )
        } else {
            HighlightPalette(
                keyword = SyntaxColors.keyword,
                string = SyntaxColors.string,
                comment = SyntaxColors.comment,
                number = SyntaxColors.number,
                annotation = SyntaxColors.annotation,
                heading = SyntaxColors.heading,
                link = SyntaxColors.link,
                codeForeground = codeForeground,
                codeBackground = codeBackground,
            )
        }
    }

/** Apply [tokens] over [text] to produce a styled [AnnotatedString]. */
fun buildHighlightedString(
    text: String,
    tokens: List<TokenSpan>,
    palette: HighlightPalette,
): AnnotatedString = buildAnnotatedString {
    append(text)
    val length = text.length
    for (token in tokens) {
        val style = palette.styleFor(token.type) ?: continue
        val start = token.start.coerceIn(0, length)
        val end = token.end.coerceIn(start, length)
        if (end > start) addStyle(style, start, end)
    }
}

/**
 * A [VisualTransformation] that recolors the editor buffer without changing its length, so
 * [OffsetMapping.Identity] is valid (cursor/selection offsets are preserved 1:1).
 */
class SyntaxHighlightTransformation(
    private val highlighter: SyntaxHighlighter,
    private val palette: HighlightPalette,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val tokens = highlighter.tokenize(text.text)
        val annotated = buildHighlightedString(text.text, tokens, palette)
        return TransformedText(annotated, OffsetMapping.Identity)
    }
}

/** Pick the right highlighter for a file type. Kotlin needs the keyword set. */
fun highlighterFor(fileType: FileType, kotlinKeywords: Set<String>): SyntaxHighlighter = when (fileType) {
    FileType.KOTLIN -> KotlinHighlighter(kotlinKeywords)
    FileType.MARKDOWN -> MarkdownHighlighter
    FileType.PLAIN -> PlainHighlighter
}
