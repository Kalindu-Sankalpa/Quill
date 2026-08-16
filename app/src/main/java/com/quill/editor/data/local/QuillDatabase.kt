package com.quill.editor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.quill.editor.data.local.dao.FileDao
import com.quill.editor.data.local.dao.VersionDao
import com.quill.editor.data.local.entity.FileEntity
import com.quill.editor.data.local.entity.VersionEntity

@Database(
    entities = [FileEntity::class, VersionEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class QuillDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
    abstract fun versionDao(): VersionDao
}
