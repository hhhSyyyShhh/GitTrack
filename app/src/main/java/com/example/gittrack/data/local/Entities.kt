package com.example.gittrack.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ProjectStatus { IDEA, IN_PROGRESS, PAUSED, COMPLETED }
enum class TaskPriority { LOW, MEDIUM, HIGH }
enum class TaskDateGroup { TODAY, UPCOMING, LATER, NONE }

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val summary: String,
    val idea: String,
    val status: ProjectStatus,
    val techStack: String,
    val githubOwner: String? = null,
    val githubRepo: String? = null,
    val icon: String = "folder",
    val tone: String = "blue",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tasks",
    foreignKeys = [ForeignKey(
        entity = ProjectEntity::class,
        parentColumns = ["id"],
        childColumns = ["projectId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("projectId")]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val title: String,
    val description: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val dateGroup: TaskDateGroup = TaskDateGroup.NONE,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "github_cache",
    foreignKeys = [ForeignKey(
        entity = ProjectEntity::class,
        parentColumns = ["id"],
        childColumns = ["projectId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("projectId")]
)
data class GitHubCacheEntity(
    @PrimaryKey val projectId: Long,
    val fullName: String,
    val description: String?,
    val language: String?,
    val stars: Int,
    val forks: Int,
    val openIssues: Int,
    val htmlUrl: String,
    val remoteUpdatedAt: String?,
    val syncedAt: Long = System.currentTimeMillis()
)
