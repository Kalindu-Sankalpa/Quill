package com.quill.editor.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.quill.editor.QuillApplication
import com.quill.editor.di.AppContainer

/** Convenience accessor for the process-wide [AppContainer] from any composable. */
@Composable
fun appContainer(): AppContainer =
    (LocalContext.current.applicationContext as QuillApplication).container
