package com.quill.editor.domain.highlight

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HighlighterTest {

    private val kotlinKeywords = setOf("fun", "val", "var", "class", "return", "if", "else")
    private val kotlin = KotlinHighlighter(kotlinKeywords)

    private fun String.spansOf(highlighter: SyntaxHighlighter, type: TokenType): List<String> =
        highlighter.tokenize(this).filter { it.type == type }.map { substring(it.start, it.end) }

    @Test
    fun kotlin_highlightsKeywords() {
        val code = "fun add(): Int { return 1 }"
        val keywords = code.spansOf(kotlin, TokenType.KEYWORD)
        assertTrue("fun" in keywords)
        assertTrue("return" in keywords)
    }

    @Test
    fun kotlin_stringLiteralIsNotSplitIntoKeywords() {
        // "fun" inside a string must be a STRING token, never a KEYWORD.
        val code = """val s = "this fun is text""""
        val strings = code.spansOf(kotlin, TokenType.STRING)
        val keywords = code.spansOf(kotlin, TokenType.KEYWORD)

        assertTrue(strings.any { it.contains("this fun is text") })
        assertTrue("val" in keywords)
        assertFalse("the keyword inside the string must not be highlighted", keywords.count { it == "fun" } > 0)
    }

    @Test
    fun kotlin_detectsCommentsNumbersAnnotations() {
        val code = "@Composable\nval x = 42 // note"
        assertTrue(code.spansOf(kotlin, TokenType.ANNOTATION).contains("@Composable"))
        assertTrue(code.spansOf(kotlin, TokenType.NUMBER).contains("42"))
        assertTrue(code.spansOf(kotlin, TokenType.COMMENT).any { it.startsWith("// note") })
    }

    @Test
    fun markdown_detectsHeadingBoldItalicCodeLink() {
        val md = "# Title\nSome **bold** and *italic* and `code` and [label](http://x)"
        assertTrue(md.spansOf(MarkdownHighlighter, TokenType.HEADING).any { it.startsWith("# Title") })
        assertTrue(md.spansOf(MarkdownHighlighter, TokenType.BOLD).contains("**bold**"))
        assertTrue(md.spansOf(MarkdownHighlighter, TokenType.ITALIC).contains("*italic*"))
        assertTrue(md.spansOf(MarkdownHighlighter, TokenType.CODE).contains("`code`"))
        assertTrue(md.spansOf(MarkdownHighlighter, TokenType.LINK).contains("[label](http://x)"))
    }

    @Test
    fun plainHighlighter_producesNoSpans() {
        assertEquals(0, PlainHighlighter.tokenize("just plain text 123 fun").size)
    }
}
