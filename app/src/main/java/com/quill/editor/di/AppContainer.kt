package com.quill.editor.di

import android.content.Context
import androidx.room.Room
import com.quill.editor.data.local.QuillDatabase
import com.quill.editor.data.recovery.CrashRecoveryManager
import com.quill.editor.data.repository.FileRepository
import com.quill.editor.data.repository.SettingsRepository
import com.quill.editor.data.repository.VersionRepository

/**
 * Lightweight manual dependency container (Google's "AppContainer" pattern) — used instead of Hilt
 * to keep the build simple on the AGP 9 / Kotlin 2.2 toolchain. Everything is lazily constructed
 * and process-scoped.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val database: QuillDatabase by lazy {
        Room.databaseBuilder(appContext, QuillDatabase::class.java, "quill.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    val fileRepository: FileRepository by lazy { FileRepository(appContext, database.fileDao()) }
    val versionRepository: VersionRepository by lazy { VersionRepository(database.versionDao()) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(appContext) }
    val crashRecoveryManager: CrashRecoveryManager by lazy { CrashRecoveryManager(appContext) }

    val kotlinKeywords: Set<String> by lazy { loadKotlinKeywords(appContext) }
}

private fun loadKotlinKeywords(context: Context): Set<String> =
    runCatching {
        context.assets.open("kotlin_keywords.txt").bufferedReader().useLines { lines ->
            lines.map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }
                .toSet()
        }
    }.getOrDefault(DEFAULT_KOTLIN_KEYWORDS)

private val DEFAULT_KOTLIN_KEYWORDS = setOf(
    "as", "break", "class", "continue", "do", "else", "false", "for", "fun", "if", "in",
    "interface", "is", "null", "object", "package", "return", "super", "this", "throw", "true",
    "try", "typealias", "typeof", "val", "var", "when", "while", "by", "catch", "constructor",
    "delegate", "dynamic", "field", "file", "finally", "get", "import", "init", "param", "property",
    "receiver", "set", "setparam", "value", "where", "abstract", "actual", "annotation", "companion",
    "const", "crossinline", "data", "enum", "expect", "external", "final", "infix", "inline", "inner",
    "internal", "lateinit", "noinline", "open", "operator", "out", "override", "private", "protected",
    "public", "reified", "sealed", "suspend", "tailrec", "vararg",
)
