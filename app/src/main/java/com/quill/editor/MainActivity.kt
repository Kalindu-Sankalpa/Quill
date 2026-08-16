package com.quill.editor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.quill.editor.ui.QuillApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Draw edge-to-edge and let the app's own background show through the gesture/nav bar
        // instead of the system's translucent contrast scrim. (API 29+, matches minSdk.)
        window.isNavigationBarContrastEnforced = false
        setContent { QuillApp() }
    }
}
