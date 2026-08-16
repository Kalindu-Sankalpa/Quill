package com.quill.editor.data.repository

import android.content.Context
import com.quill.editor.data.local.dao.FileDao
import com.quill.editor.data.local.entity.FileEntity
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.nio.charset.Charset

/**
 * Owns file metadata (Room) and the on-disk *base* content in app-internal storage.
 * Base content is written once when a file is created; later edits are stored as version patches
 * by [VersionRepository], so the database never duplicates full-file content.
 */
class FileRepository(context: Context, private val fileDao: FileDao) {

    private val appContext = context.applicationContext

    private val documentsDir: File
        get() = File(appContext.filesDir, "documents").apply { if (!exists()) mkdirs() }

    fun observeFiles(): Flow<List<FileEntity>> = fileDao.observeAll()

    suspend fun getFile(id: Long): FileEntity? = fileDao.getById(id)

    /** Read the immutable base (version 1) content for a file. */
    fun readBaseContent(entity: FileEntity): String {
        val file = File(entity.path)
        return if (file.exists()) file.readText(charsetOf(entity.encoding)) else ""
    }

    /** Create a new managed file, writing [content] as its base version on disk. */
    suspend fun createFile(name: String, content: String, encoding: String = "UTF-8"): FileEntity {
        val fileName = ensureUnique(sanitize(name))
        val file = File(documentsDir, fileName)
        file.writeText(content, charsetOf(encoding))
        val now = System.currentTimeMillis()
        val entity = FileEntity(
            name = fileName,
            path = file.absolutePath,
            encoding = encoding,
            lastOpened = now,
            createdAt = now,
        )
        return entity.copy(id = fileDao.insert(entity))
    }

    suspend fun markOpened(id: Long) {
        fileDao.getById(id)?.let { fileDao.update(it.copy(lastOpened = System.currentTimeMillis())) }
    }

    suspend fun setReadOnly(id: Long, readOnly: Boolean): FileEntity? {
        val entity = fileDao.getById(id) ?: return null
        val updated = entity.copy(isReadOnly = readOnly)
        fileDao.update(updated)
        return updated
    }

    suspend fun deleteFile(entity: FileEntity) {
        runCatching { File(entity.path).delete() }
        fileDao.delete(entity)
    }

    private fun charsetOf(name: String): Charset =
        runCatching { Charset.forName(name) }.getOrDefault(Charsets.UTF_8)

    private fun sanitize(name: String): String =
        name.trim().replace(Regex("[^A-Za-z0-9._ -]"), "_").ifBlank { "untitled.txt" }

    private fun ensureUnique(name: String): String {
        if (!File(documentsDir, name).exists()) return name
        val dot = name.lastIndexOf('.')
        val base = if (dot > 0) name.substring(0, dot) else name
        val ext = if (dot > 0) name.substring(dot) else ""
        var i = 1
        while (true) {
            val candidate = "$base($i)$ext"
            if (!File(documentsDir, candidate).exists()) return candidate
            i++
        }
    }
}
