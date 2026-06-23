package com.example.gittrack.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("""
        SELECT p.*,
               COUNT(t.id) AS taskCount,
               COALESCE(SUM(CASE WHEN t.isCompleted = 1 THEN 1 ELSE 0 END), 0) AS completedCount
        FROM projects p
        LEFT JOIN tasks t ON p.id = t.projectId
        GROUP BY p.id
        ORDER BY p.updatedAt DESC
    """)
    fun observeProjectsWithProgress(): Flow<List<ProjectWithProgress>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    fun observeProject(id: Long): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProject(id: Long): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: ProjectEntity): Long

    @Update suspend fun update(project: ProjectEntity)
    @Delete suspend fun delete(project: ProjectEntity)

    @Query("SELECT COUNT(*) FROM projects")
    suspend fun count(): Int

    @Query("""
        DELETE FROM projects
        WHERE (name = 'GitTrack' AND summary = '简洁高效的开发项目管理工具')
           OR (name = 'WordOrbit' AND summary = '智能单词记忆与学习平台')
           OR (name = 'SpaceLog' AND summary = '太空探索与任务记录应用')
    """)
    suspend fun deleteDemoProjects()

    @Query("SELECT COUNT(*) FROM projects")
    fun observeProjectCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM projects WHERE status = 'IN_PROGRESS'")
    fun observeActiveProjectCount(): Flow<Int>
}
