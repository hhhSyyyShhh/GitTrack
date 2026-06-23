package com.example.gittrack.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gittrack.data.local.GitHubCacheEntity
import com.example.gittrack.data.local.ProjectEntity
import com.example.gittrack.data.local.ProjectStatus
import com.example.gittrack.data.local.ProjectWithProgress
import com.example.gittrack.data.local.TaskDateGroup
import com.example.gittrack.data.local.TaskEntity
import com.example.gittrack.data.local.TaskPriority
import com.example.gittrack.data.preferences.ThemeMode
import com.example.gittrack.data.preferences.UserPreferences
import com.example.gittrack.data.preferences.UserPreferencesRepository
import com.example.gittrack.data.repository.AiGeneratedTask
import com.example.gittrack.data.repository.AiProjectPlan
import com.example.gittrack.data.repository.AiRepository
import com.example.gittrack.data.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


data class ProjectsUiState(
    val projects: List<ProjectWithProgress> = emptyList(),
    val query: String = "",
    val filter: ProjectStatus? = null,
    val requiresLogin: Boolean = false
)

data class TasksUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val query: String = "",
    val filter: String = "ALL",
    val showCompleted: Boolean = true,
    val requiresLogin: Boolean = false
)

data class ProjectDetailUiState(
    val project: ProjectEntity? = null,
    val tasks: List<TaskEntity> = emptyList(),
    val github: GitHubCacheEntity? = null,
    val isSyncing: Boolean = false,
    val syncError: String? = null
) {
    val completedCount: Int get() = tasks.count { it.isCompleted }
    val progressPercent: Int get() = if (tasks.isEmpty()) 0 else completedCount * 100 / tasks.size
}

private data class ProjectDetailRawState(
    val project: ProjectEntity?,
    val tasks: List<TaskEntity>,
    val github: GitHubCacheEntity?,
    val isSyncing: Boolean,
    val syncError: String?
)

data class ProfileStats(val projects: Int = 0, val activeProjects: Int = 0, val completedTasks: Int = 0)

data class AnalyticsUiState(
    val projects: List<ProjectWithProgress> = emptyList(),
    val tasks: List<TaskEntity> = emptyList()
)

data class AiUiState(
    val loading: Boolean = false,
    val projectPlan: AiProjectPlan? = null,
    val generatedTasks: List<AiGeneratedTask> = emptyList(),
    val error: String? = null
)

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null
)

sealed interface UiEvent {
    data class Message(val text: String) : UiEvent

}

class GitTrackViewModel(
    private val repository: ProjectRepository,
    private val aiRepository: AiRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val projectQuery = MutableStateFlow("")
    private val projectFilter = MutableStateFlow<ProjectStatus?>(null)
    private val taskQuery = MutableStateFlow("")
    private val taskFilter = MutableStateFlow("ALL")
    private val selectedProjectId = MutableStateFlow<Long?>(null)
    private val githubSyncing = MutableStateFlow(false)
    private val githubError = MutableStateFlow<String?>(null)

    val events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 8)
    val aiState = MutableStateFlow(AiUiState())
    val authState = MutableStateFlow(AuthUiState())

    val preferences: StateFlow<UserPreferences> = preferencesRepository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())

    val projectsUiState: StateFlow<ProjectsUiState> = combine(
        repository.observeProjects(),
        projectQuery,
        projectFilter,
        preferences
    ) { projects, query, filter, prefs ->
        val canShowProjectData = prefs.isLoggedIn ||
            (!prefs.hasLocalAccount && !prefs.demoDataDismissed)

        if (!canShowProjectData) {
            ProjectsUiState(
                query = query,
                filter = filter,
                requiresLogin = true
            )
        } else {
            val normalized = query.trim().lowercase()
            ProjectsUiState(
                projects = projects.filter {
                    (filter == null || it.project.status == filter) &&
                        (normalized.isBlank() || listOf(
                            it.project.name,
                            it.project.summary,
                            it.project.techStack
                        ).any { text -> text.lowercase().contains(normalized) })
                },
                query = query,
                filter = filter,
                requiresLogin = false
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ProjectsUiState()
    )

    val tasksUiState: StateFlow<TasksUiState> = combine(
        repository.observeTasks(),
        taskQuery,
        taskFilter,
        preferences
    ) { tasks, query, filter, prefs ->
        val canShowTaskData = prefs.isLoggedIn ||
            (!prefs.hasLocalAccount && !prefs.demoDataDismissed)

        if (!canShowTaskData) {
            TasksUiState(
                query = query,
                filter = filter,
                showCompleted = prefs.showCompletedTasks,
                requiresLogin = true
            )
        } else {
            val normalized = query.trim().lowercase()
            val filtered = tasks.filter { task ->
                val byCompleted = prefs.showCompletedTasks || !task.isCompleted
                val byFilter = when (filter) {
                    "TODAY" -> !task.isCompleted && task.dateGroup == TaskDateGroup.TODAY
                    "HIGH" -> !task.isCompleted && task.priority == TaskPriority.HIGH
                    "DONE" -> task.isCompleted
                    else -> true
                }
                val byQuery = normalized.isBlank() || task.title.lowercase().contains(normalized)
                byCompleted && byFilter && byQuery
            }
            TasksUiState(
                tasks = filtered,
                query = query,
                filter = filter,
                showCompleted = prefs.showCompletedTasks,
                requiresLogin = false
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        TasksUiState()
    )

    val detailUiState: StateFlow<ProjectDetailUiState> = selectedProjectId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(ProjectDetailUiState())
            } else {
                val rawDetailFlow = combine(
                    repository.observeProject(id),
                    repository.observeTasks(id),
                    repository.observeGitHubCache(id),
                    githubSyncing,
                    githubError
                ) { project, tasks, github, isSyncing, syncError ->
                    ProjectDetailRawState(
                        project = project,
                        tasks = tasks,
                        github = github,
                        isSyncing = isSyncing,
                        syncError = syncError
                    )
                }

                combine(
                    rawDetailFlow,
                    preferences
                ) { raw, prefs ->
                    val canShowDetail = prefs.isLoggedIn ||
                        (!prefs.hasLocalAccount && !prefs.demoDataDismissed)

                    if (canShowDetail) {
                        ProjectDetailUiState(
                            project = raw.project,
                            tasks = raw.tasks,
                            github = raw.github,
                            isSyncing = raw.isSyncing,
                            syncError = raw.syncError
                        )
                    } else {
                        ProjectDetailUiState()
                    }
                }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ProjectDetailUiState()
        )

    /**
     * “我的”页面属于登录后的个人空间。
     *
     * Room 中可能存在未登录演示数据，因此统计结果必须同时结合登录状态。
     * 未登录时统一返回 0，避免把演示项目误显示成用户个人数据。
     */
    val profileStats: StateFlow<ProfileStats> = combine(
        repository.observeProjectCount(),
        repository.observeActiveProjectCount(),
        repository.observeCompletedTaskCount(),
        preferences
    ) { total, active, completed, prefs ->
        if (prefs.isLoggedIn) {
            ProfileStats(total, active, completed)
        } else {
            ProfileStats()
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ProfileStats()
    )

    /**
     * 图表数据同样受登录状态控制。
     *
     * 未登录时项目页仍可展示演示数据，但“我的”页不能把演示数据作为个人统计。
     */
    val analyticsUiState: StateFlow<AnalyticsUiState> = combine(
        repository.observeProjects(),
        repository.observeTasks(),
        preferences
    ) { projects, tasks, prefs ->
        if (prefs.isLoggedIn) {
            AnalyticsUiState(projects, tasks)
        } else {
            AnalyticsUiState()
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AnalyticsUiState()
    )

    init {
        viewModelScope.launch {
            // GitHub 提交版默认使用空数据库，不再自动插入任何演示项目或任务。
            // 用户首次运行后可自行注册、登录并创建自己的项目数据。
            val initialPreferences = preferencesRepository.preferences.first()
            if (initialPreferences.isLoggedIn || initialPreferences.demoDataDismissed) {
                repository.clearDemoData()
                preferencesRepository.markDemoDataDismissed()
            }
        }
    }

    fun setProjectQuery(value: String) { projectQuery.value = value }
    fun setProjectFilter(value: ProjectStatus?) { projectFilter.value = value }
    fun setTaskQuery(value: String) { taskQuery.value = value }
    fun setTaskFilter(value: String) { taskFilter.value = value }
    fun selectProject(id: Long) {
        selectedProjectId.value = id
        viewModelScope.launch { preferencesRepository.setLastOpenedProjectId(id) }
    }

    suspend fun loadProject(id: Long): ProjectEntity? = repository.getProject(id)

    fun saveProject(project: ProjectEntity, onSaved: (Long) -> Unit) = viewModelScope.launch {
        runCatching {
            require(project.name.isNotBlank()) { "项目名称不能为空" }
            require(project.summary.isNotBlank()) { "项目简介不能为空" }
            if (project.id == 0L) repository.createProject(project) else {
                repository.updateProject(project)
                project.id
            }
        }.onSuccess {
            selectProject(it)
            events.tryEmit(UiEvent.Message(if (project.id == 0L) "项目已创建" else "项目已更新"))
            onSaved(it)
        }.onFailure { events.tryEmit(UiEvent.Message(it.message ?: "保存失败")) }
    }

    fun deleteSelectedProject(onDeleted: () -> Unit) = viewModelScope.launch {
        val project = detailUiState.value.project ?: return@launch
        runCatching { repository.deleteProject(project) }
            .onSuccess { events.tryEmit(UiEvent.Message("项目已删除")); onDeleted() }
            .onFailure { events.tryEmit(UiEvent.Message("删除失败")) }
    }

    fun addTask(
        projectId: Long,
        title: String,
        description: String,
        priority: TaskPriority,
        dateGroup: TaskDateGroup,
        onSaved: () -> Unit
    ) = viewModelScope.launch {
        runCatching {
            require(title.isNotBlank()) { "任务标题不能为空" }
            repository.addTask(TaskEntity(
                projectId = projectId,
                title = title.trim(),
                description = description.trim(),
                priority = priority,
                dateGroup = dateGroup
            ))
        }.onSuccess { events.tryEmit(UiEvent.Message("任务已添加")); onSaved() }
            .onFailure { events.tryEmit(UiEvent.Message(it.message ?: "添加失败")) }
    }

    fun toggleTask(task: TaskEntity) = viewModelScope.launch {
        repository.updateTask(task.copy(isCompleted = !task.isCompleted))
    }

    fun deleteTask(task: TaskEntity) = viewModelScope.launch {
        repository.deleteTask(task)
        events.tryEmit(UiEvent.Message("任务已删除"))
    }

    fun syncGitHub() = viewModelScope.launch {
        val project = detailUiState.value.project ?: return@launch
        val owner = project.githubOwner
        val repo = project.githubRepo
        if (owner.isNullOrBlank() || repo.isNullOrBlank()) {
            githubError.value = "尚未关联 GitHub 仓库"
            return@launch
        }
        githubSyncing.value = true
        githubError.value = null
        repository.syncGitHub(project.id, owner, repo)
            .onSuccess { events.tryEmit(UiEvent.Message("GitHub 仓库已同步")) }
            .onFailure { githubError.value = it.message }
        githubSyncing.value = false
    }

    fun refineProject(idea: String) = viewModelScope.launch {
        aiState.value = AiUiState(loading = true)
        aiRepository.refineProject(idea)
            .onSuccess { aiState.value = AiUiState(projectPlan = it) }
            .onFailure { aiState.value = AiUiState(error = it.message ?: "AI 请求失败") }
    }

    fun generateTasks() = viewModelScope.launch {
        val detail = detailUiState.value
        val project = detail.project ?: return@launch
        aiState.value = AiUiState(loading = true)
        aiRepository.generateTasks(
            projectName = project.name,
            projectDescription = project.idea,
            techStack = project.techStack,
            existingTasks = detail.tasks.map { it.title }
        ).onSuccess { aiState.value = AiUiState(generatedTasks = it) }
            .onFailure { aiState.value = AiUiState(error = it.message ?: "AI 请求失败") }
    }

    fun applyGeneratedTasks(tasks: List<AiGeneratedTask>) = viewModelScope.launch {
        val projectId = detailUiState.value.project?.id ?: return@launch
        repository.addTasks(tasks.map {
            TaskEntity(
                projectId = projectId,
                title = it.title,
                description = it.description,
                priority = it.priority,
                dateGroup = TaskDateGroup.UPCOMING
            )
        })
        aiState.value = AiUiState()
        events.tryEmit(UiEvent.Message("已加入 ${tasks.size} 条任务"))
    }


    fun login(email: String, password: String) = viewModelScope.launch {
        authState.value = AuthUiState(loading = true)
        runCatching {
            require(preferences.value.hasLocalAccount) { "本机还没有账户，请先注册" }
            require(email.trim().isNotEmpty()) { "请输入邮箱" }
            require(password.isNotEmpty()) { "请输入密码" }
            require(preferencesRepository.login(email, password)) { "邮箱或密码不正确" }
            repository.clearDemoData()
            preferencesRepository.markDemoDataDismissed()
        }.onSuccess {
            authState.value = AuthUiState()
            events.tryEmit(UiEvent.Message("登录成功"))
        }.onFailure {
            authState.value = AuthUiState(error = it.message ?: "登录失败")
        }
    }

    fun register(name: String, email: String, password: String, confirmPassword: String) =
        viewModelScope.launch {
            authState.value = AuthUiState(loading = true)
            runCatching {
                val normalizedName = name.trim()
                val normalizedEmail = email.trim().lowercase()
                require(normalizedName.isNotEmpty()) { "请输入昵称" }
                require(android.util.Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
                    "请输入有效邮箱地址"
                }
                require(password.length >= 6) { "密码至少需要 6 位" }
                require(password == confirmPassword) { "两次输入的密码不一致" }
                preferencesRepository.register(normalizedName, normalizedEmail, password)
                repository.clearDemoData()
                preferencesRepository.markDemoDataDismissed()
            }.onSuccess {
                authState.value = AuthUiState()
                events.tryEmit(UiEvent.Message("注册成功，已自动登录"))
            }.onFailure {
                authState.value = AuthUiState(error = it.message ?: "注册失败")
            }
        }


    fun updateAvatar(avatar: String) = viewModelScope.launch {
        runCatching {
            preferencesRepository.setProfileAvatar(avatar)
        }.onSuccess {
            events.tryEmit(UiEvent.Message("头像已更新"))
        }.onFailure {
            events.tryEmit(
                UiEvent.Message(
                    it.message ?: "头像更新失败"
                )
            )
        }
    }

    fun logout() = viewModelScope.launch {
        preferencesRepository.logout()
        authState.value = AuthUiState()
        events.tryEmit(UiEvent.Message("已退出登录"))
    }

    fun clearAuthState() {
        authState.value = AuthUiState()
    }

    fun updateProfile(name: String, email: String) = viewModelScope.launch {
        runCatching {
            require(preferences.value.isLoggedIn) { "请先登录" }
            require(name.trim().isNotEmpty()) { "昵称不能为空" }
            require(email.trim().isNotEmpty()) { "邮箱不能为空" }
            require(android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) { "邮箱格式不正确" }
            preferencesRepository.setProfile(name.trim(), email.trim())
        }.onSuccess {
            events.tryEmit(UiEvent.Message("个人资料已更新"))
        }.onFailure {
            events.tryEmit(UiEvent.Message(it.message ?: "资料保存失败"))
        }
    }

    fun syncAllGitHub() = viewModelScope.launch {
        val linkedProjects = projectsUiState.value.projects.map { it.project }
            .filter { !it.githubOwner.isNullOrBlank() && !it.githubRepo.isNullOrBlank() }
        if (linkedProjects.isEmpty()) {
            events.tryEmit(UiEvent.Message("暂无已关联 GitHub 仓库的项目"))
            return@launch
        }
        var successCount = 0
        linkedProjects.forEach { project ->
            repository.syncGitHub(project.id, project.githubOwner!!, project.githubRepo!!)
                .onSuccess { successCount++ }
        }
        events.tryEmit(UiEvent.Message("已同步 $successCount/${linkedProjects.size} 个仓库"))
    }
    fun clearAiState() { aiState.value = AiUiState() }
    fun setTheme(mode: ThemeMode) = viewModelScope.launch { preferencesRepository.setTheme(mode) }
    fun setDefaultStatus(status: ProjectStatus) = viewModelScope.launch { preferencesRepository.setDefaultStatus(status) }
    fun setShowCompleted(show: Boolean) = viewModelScope.launch { preferencesRepository.setShowCompleted(show) }
    fun clearGitHubCache() = viewModelScope.launch { repository.clearGitHubCache(); events.tryEmit(UiEvent.Message("缓存已清除")) }

    class Factory(
        private val repository: ProjectRepository,
        private val aiRepository: AiRepository,
        private val preferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            GitTrackViewModel(repository, aiRepository, preferencesRepository) as T
    }
}
