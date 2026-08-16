package com.quill.editor.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One saved version of a file. Version 1 is the base (full content lives on disk, [patchText] null).
 * Later versions store only the unified-diff [patchText] from the previous version.
 */
@Entity(
    tableName = "versions",
    foreignKeys = [
        ForeignKey(
            entity = FileEntity::class,
            parentColumns = ["id"],
            childColumns = ["fileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("fileId")],
)
data class VersionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileId: Long,
    val versionNumber: Int,
    val label: String? = null,
    val patchText: String? = null,
    val isBase: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
