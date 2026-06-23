package com.example.gittrack.data.repository

import androidx.room.withTransaction
import com.example.gittrack.data.local.GitHubCacheDao
import com.example.gittrack.data.local.GitHubCacheEntity
import com.example.gittrack.data.local.GitTrackDatabase
import com.example.gittrack.data.local.ProjectDao
import com.example.gittrack.data.local.ProjectEntity
import com.example.gittrack.data.local.ProjectStatus
import com.example.gittrack.data.local.ProjectWithProgress
import com.example.gittrack.data.local.TaskDao
import com.example.gittrack.data.local.TaskDateGroup
import com.example.gittrack.data.local.TaskEntity
import com.example.gittrack.data.local.TaskPriority
import com.example.gittrack.data.network.GitHubApi
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

class ProjectRepository(
    private val database: GitTrackDatabase,
    private val projectDao: ProjectDao,
    private val taskDao: TaskDao,
    private val cacheDao: GitHubCacheDao,
    private val githubApi: GitHubApi
) {
    fun observeProjects(): Flow<List<ProjectWithProgress>> = projectDao.observeProjectsWithProgress()
    fun observeProject(id: Long): Flow<ProjectEntity?> = projectDao.observeProject(id)
    fun observeTasks(): Flow<List<TaskEntity>> = taskDao.observeAll()
    fun observeTasks(projectId: Long): Flow<List<TaskEntity>> = taskDao.observeByProject(projectId)
    fun observeGitHubCache(projectId: Long): Flow<GitHubCacheEntity?> = cacheDao.observe(projectId)
    fun observeProjectCount(): Flow<Int> = projectDao.observeProjectCount()
    fun observeActiveProjectCount(): Flow<Int> = projectDao.observeActiveProjectCount()
    fun observeCompletedTaskCount(): Flow<Int> = taskDao.observeCompletedCount()

    suspend fun createProject(project: ProjectEntity): Long = projectDao.insert(project)
    suspend fun updateProject(project: ProjectEntity) = projectDao.update(project.copy(updatedAt = System.currentTimeMillis()))
    suspend fun deleteProject(project: ProjectEntity) = projectDao.delete(project)
    suspend fun getProject(id: Long): ProjectEntity? = projectDao.getProject(id)

    suspend fun addTask(task: TaskEntity): Long = taskDao.insert(task)
    suspend fun addTasks(tasks: List<TaskEntity>) = database.withTransaction { taskDao.insertAll(tasks) }
    suspend fun updateTask(task: TaskEntity) = taskDao.update(task.copy(updatedAt = System.currentTimeMillis()))
    suspend fun deleteTask(task: TaskEntity) = taskDao.delete(task)
    suspend fun getTask(id: Long): TaskEntity? = taskDao.getTask(id)

    suspend fun syncGitHub(projectId: Long, owner: String, repo: String): Result<GitHubCacheEntity> = runCatching {
        val dto = githubApi.getRepository(owner, repo)
        GitHubCacheEntity(
            projectId = projectId,
            fullName = dto.fullName,
            description = dto.description,
            language = dto.language,
            stars = dto.stars,
            forks = dto.forks,
            openIssues = dto.openIssues,
            htmlUrl = dto.htmlUrl,
            remoteUpdatedAt = dto.updatedAt
        ).also { cacheDao.upsert(it) }
    }.recoverCatching { throwable ->
        throw IllegalStateException(
            when (throwable) {
                is HttpException -> when (throwable.code()) {
                    404 -> "仓库不存在或无法访问"
                    403 -> "GitHub 请求受限，请稍后重试"
                    else -> "GitHub 请求失败（${throwable.code()}）"
                }
                else -> "网络连接失败，请稍后重试"
            }, throwable
        )
    }

    suspend fun clearGitHubCache() = cacheDao.clear()

    /**
     * 删除首次启动时插入的演示项目。
     * 只匹配名称与简介同时一致的三条内置数据，避免误删用户自己创建的普通项目。
     * tasks 和 github_cache 通过外键级联删除。
     */
    suspend fun clearDemoData() = database.withTransaction {
        projectDao.deleteDemoProjects()
    }

    /**
     * GitHub 提交版默认不插入演示数据。
     *
     * 这样其他人 clone 项目后首次运行时会得到一个空数据库，
     * 不会看到作者本地测试用的项目、任务或账号信息。
     */
    suspend fun seedIfEmpty() = Unit
}
