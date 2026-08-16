package com.quill.editor.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.quill.editor.data.local.entity.VersionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VersionDao {

    @Query("SELECT * FROM versions WHERE fileId = :fileId ORDER BY versionNumber ASC")
    fun observeForFile(fileId: Long): Flow<List<VersionEntity>>

    @Query("SELECT * FROM versions WHERE fileId = :fileId ORDER BY versionNumber ASC")
    suspend fun getForFile(fileId: Long): List<VersionEntity>

    @Query("SELECT MAX(versionNumber) FROM versions WHERE fileId = :fileId")
    suspend fun maxVersion(fileId: Long): Int?

    @Query("SELECT COUNT(*) FROM versions WHERE fileId = :fileId")
    suspend fun countForFile(fileId: Long): Int

    @Insert
    suspend fun insert(version: VersionEntity): Long

    @Update
    suspend fun update(version: VersionEntity)

    @Delete
    suspend fun delete(version: VersionEntity)
}
