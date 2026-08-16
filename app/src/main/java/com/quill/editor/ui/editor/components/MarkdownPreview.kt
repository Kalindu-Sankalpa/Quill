package com.quill.editor.ui.editor.components

import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin

/** Renders Markdown to a native [TextView] via Markwon, hosted inside Compose with AndroidView. */
@Composable
fun MarkdownPreview(
    markdown: String,
    modifier: Modifier = Modifier,
    contentTopPadding: Dp = 12.dp,
    contentBottomPadding: Dp = 12.dp,
) {
    val context = LocalContext.current
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val markwon = remember(context) {
        Markwon.builder(context)
            .usePlugin(TablePlugin.create(context))
            .usePlugin(StrikethroughPlugin.create())
            .build()
    }

    val scrollState = rememberScrollState()
    AndroidView(
        factory = { ctx ->
            TextView(ctx).apply {
                setPadding(36, 28, 36, 28)
                textSize = 15f
            }
        },
        update = { textView ->
            textView.setTextColor(textColor)
            markwon.setMarkdown(textView, markdown)
        },
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(top = contentTopPadding, bottom = contentBottomPadding),
    )
}
