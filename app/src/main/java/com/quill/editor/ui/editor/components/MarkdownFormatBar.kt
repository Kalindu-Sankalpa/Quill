package com.quill.editor.ui.editor.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Horizontally-scrollable Markdown formatting strip (a floating capsule).
 * Buttons wrap the selection / prefix the line with the right Markdown syntax.
 */
@Composable
fun MarkdownFormatBar(
    onHeading: (Int) -> Unit,
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onStrikethrough: () -> Unit,
    onInlineCode: () -> Unit,
    onBulletList: () -> Unit,
    onNumberedList: () -> Unit,
    onQuote: () -> Unit,
    onLink: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f),
        shadowElevation = 6.dp,
        tonalElevation = 3.dp,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextChip("H1") { onHeading(1) }
            TextChip("H2") { onHeading(2) }
            TextChip("H3") { onHeading(3) }
            ChipDivider()
            IconChip(Icons.Filled.FormatBold, "Bold", onBold)
            IconChip(Icons.Filled.FormatItalic, "Italic", onItalic)
            IconChip(Icons.Filled.FormatStrikethrough, "Strikethrough", onStrikethrough)
            IconChip(Icons.Filled.Code, "Inline code", onInlineCode)
            ChipDivider()
            IconChip(Icons.AutoMirrored.Filled.FormatListBulleted, "Bulleted list", onBulletList)
            IconChip(Icons.Filled.FormatListNumbered, "Numbered list", onNumberedList)
            IconChip(Icons.Filled.FormatQuote, "Quote", onQuote)
            IconChip(Icons.Filled.Link, "Link", onLink)
        }
    }
}

@Composable
private fun IconChip(imageVector: ImageVector, contentDescription: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier.size(38.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector, contentDescription, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun TextChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier.size(width = 44.dp, height = 38.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ChipDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(22.dp)
            .padding(vertical = 1.dp),
    ) {
        Surface(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.fillMaxHeight().width(1.dp)) {}
    }
}
