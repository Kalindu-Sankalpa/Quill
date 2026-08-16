package com.quill.editor.data.recovery

import android.content.Context
import java.io.File

/**
 * Crash-recovery / auto-save engine.
 *
 * Every few seconds the editor writes the live buffer to a `.tmp` file under the app cache
 * (`cache/recovery/<key>.tmp`). On the next launch, if a `.tmp` exists we offer to restore it.
 * The `.tmp` is deleted on every successful explicit Save.
 */
class CrashRecoveryManager(context: Context) {

    private val appContext = context.applicationContext

    private val recoveryDir: File
        get() = File(appContext.cacheDir, "recovery").apply { if (!exists()) mkdirs() }

    private fun tempFile(key: String): File = File(recoveryDir, "${safeKey(key)}.tmp")

    fun saveRecovery(key: String, content: String) {
        runCatching { tempFile(key).writeText(content, Charsets.UTF_8) }
    }

    fun hasRecovery(key: String): Boolean = tempFile(key).exists()

    fun readRecovery(key: String): String? =
        tempFile(key).let { if (it.exists()) runCatching { it.readText(Charsets.UTF_8) }.getOrNull() else null }

    fun clearRecovery(key: String) {
        runCatching { tempFile(key).delete() }
    }

    /** Keys of all buffers with a pending recovery file (filename without the `.tmp`). */
    fun pendingKeys(): List<String> =
        recoveryDir.listFiles { f -> f.isFile && f.name.endsWith(".tmp") }
            ?.map { it.name.removeSuffix(".tmp") }
            ?: emptyList()

    private fun safeKey(key: String): String =
        key.replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "buffer" }
}
