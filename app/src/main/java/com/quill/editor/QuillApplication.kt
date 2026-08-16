package com.quill.editor

import android.app.Application
import com.quill.editor.di.AppContainer

/**
 * Application entry point. Hosts the manual dependency-injection [AppContainer]
 * (Quill uses the AppContainer pattern instead of Hilt — see AppContainer for rationale).
 */
class QuillApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
