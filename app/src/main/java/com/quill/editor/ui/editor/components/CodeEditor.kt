package com.quill.editor.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quill.editor.domain.highlight.HighlightPalette
import com.quill.editor.domain.highlight.SyntaxHighlightTransformation
import com.quill.editor.domain.highlight.SyntaxHighlighter
import com.quill.editor.ui.editor.EditorUiState

/**
 * The code/markdown editing surface: a [BasicTextField] recolored by a
 * [SyntaxHighlightTransformation], with an optional line-number gutter and word-wrap /
 * horizontal-scroll behaviour. The gutter shares the field's vertical scroll so numbers track the
 * text (alignment is exact when word wrap is off).
 */
@Composable
fun CodeEditor(
    state: EditorUiState,
    highlighter: SyntaxHighlighter,
    palette: HighlightPalette,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    contentTopPadding: Dp = 12.dp,
    contentBottomPadding: Dp = 12.dp,
) {
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()
    val transformation = remember(highlighter, palette) { SyntaxHighlightTransformation(highlighter, palette) }
    val colors = MaterialTheme.colorScheme
    val textStyle = remember(state.fontSize, colors.onSurface) {
        TextStyle(fontFamily = FontFamily.Monospace, fontSize = state.fontSize.sp, color = colors.onSurface)
    }
    val gutterStyle = remember(state.fontSize, colors.onSurfaceVariant) {
        TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = state.fontSize.sp,
            color = colors.onSurfaceVariant.copy(alpha = 0.55f),
        )
    }

    Row(modifier = modifier.fillMaxSize().background(colors.surface)) {
        if (state.showLineNumbers) {
            val lineCount = remember(state.text.text) { state.text.text.count { it == '\n' } + 1 }
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(verticalScroll)
                    .background(colors.surfaceVariant.copy(alpha = 0.35f))
                    .padding(start = 8.dp, end = 8.dp, top = contentTopPadding, bottom = contentBottomPadding)
                    .widthIn(min = 22.dp),
            ) {
                for (i in 1..lineCount) {
                    Text(text = i.toString(), style = gutterStyle)
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(verticalScroll),
        ) {
            BasicTextField(
                value = state.text,
                onValueChange = onValueChange,
                readOnly = state.isReadOnly,
                textStyle = textStyle,
                visualTransformation = transformation,
                cursorBrush = SolidColor(colors.primary),
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (!state.wordWrap) Modifier.horizontalScroll(horizontalScroll) else Modifier)
                    .padding(start = 6.dp, end = 12.dp, top = contentTopPadding, bottom = contentBottomPadding),
            )
        }
    }
}
