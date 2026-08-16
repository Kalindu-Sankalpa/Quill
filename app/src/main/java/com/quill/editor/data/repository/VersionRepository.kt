package com.quill.editor.data.repository

import com.quill.editor.data.local.dao.VersionDao
import com.quill.editor.data.local.entity.VersionEntity
import com.quill.editor.domain.DeltaEngine
import kotlinx.coroutines.flow.Flow

/**
 * Delta-based version control. Version 1 is the base (recorded by [initBase]); each [commit] stores
 * only the unified-diff patch from the previous saved content. [reconstruct] chain-applies patches.
 */
class VersionRepository(private val versionDao: VersionDao) {

    fun observeVersions(fileId: Long): Flow<List<VersionEntity>> = versionDao.observeForFile(fileId)

    suspend fun getVersions(fileId: Long): List<VersionEntity> = versionDao.getForFile(fileId)

    suspend fun latestVersionNumber(fileId: Long): Int = versionDao.maxVersion(fileId) ?: 1

    /** Record version 1 (base). Call once, right after the base content is written to disk. */
    suspend fun initBase(fileId: Long, label: String = "Initial") {
        versionDao.insert(
            VersionEntity(
                fileId = fileId,
                versionNumber = 1,
                label = label,
                patchText = null,
                isBase = true,
            ),
        )
    }

    /**
     * Commit a new version as a forward patch from [previousContent] to [newContent].
     * Returns the new version number, or null when nothing changed.
     */
    suspend fun commit(
        fileId: Long,
        previousContent: String,
        newContent: String,
        label: String? = null,
    ): Int? {
        if (previousContent == newContent) return null
        val next = (versionDao.maxVersion(fileId) ?: 0) + 1
        val patch = DeltaEngine.createPatch(previousContent, newContent)
        versionDao.insert(
            VersionEntity(
                fileId = fileId,
                versionNumber = next,
                label = label ?: "v$next",
                patchText = patch,
                isBase = false,
            ),
        )
        return next
    }

    /** Reconstruct the content saved at [targetVersion] from [baseContent] + chained patches. */
    suspend fun reconstruct(fileId: Long, targetVersion: Int, baseContent: String): String {
        val patches = versionDao.getForFile(fileId)
            .filter { !it.isBase && it.versionNumber in 2..targetVersion }
            .sortedBy { it.versionNumber }
            .mapNotNull { it.patchText }
        return DeltaEngine.reconstruct(baseContent, patches)
    }

    suspend fun rename(version: VersionEntity, label: String) {
        versionDao.update(version.copy(label = label))
    }

    /** Delete a version. Only the newest non-base version is safe to delete (keeps the chain intact). */
    suspend fun delete(version: VersionEntity) {
        versionDao.delete(version)
    }
}
