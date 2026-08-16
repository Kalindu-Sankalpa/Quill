package com.quill.editor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Metadata for a file managed by Quill. Actual content lives on internal storage at [path]. */
@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val path: String,
    val encoding: String = "UTF-8",
    val isReadOnly: Boolean = false,
    val lastOpened: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
