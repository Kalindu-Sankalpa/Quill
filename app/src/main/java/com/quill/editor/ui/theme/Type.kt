package com.quill.editor.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

// UI chrome uses the platform default sans; the editor uses a monospace family.
// (JetBrains Mono / Inter via downloadable fonts is a documented future enhancement.)
val QuillTypography = Typography()

/** Base text style for the code/markdown editing surface. */
val EditorBaseTextStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontSize = 14.sp,
    lineHeight = 20.sp,
)
