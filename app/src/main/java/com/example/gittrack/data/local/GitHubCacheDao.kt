package com.example.gittrack.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GitHubCacheDao {
    @Query("SELECT * FROM github_cache WHERE projectId = :projectId LIMIT 1")
    fun observe(projectId: Long): Flow<GitHubCacheEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(cache: GitHubCacheEntity)

    @Query("DELETE FROM github_cache")
    suspend fun clear()
}
