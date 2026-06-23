@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.gittrack.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gittrack.data.local.GitHubCacheEntity
import com.example.gittrack.data.local.ProjectEntity
import com.example.gittrack.data.local.ProjectStatus
import com.example.gittrack.data.local.ProjectWithProgress
import com.example.gittrack.data.local.TaskDateGroup
import com.example.gittrack.data.local.TaskEntity
import com.example.gittrack.data.local.TaskPriority
import com.example.gittrack.data.preferences.ThemeMode
import com.example.gittrack.data.preferences.UserPreferences
import com.example.gittrack.data.repository.AiGeneratedTask
import com.example.gittrack.data.repository.AiProjectPlan
import com.example.gittrack.ui.AiUiState
import com.example.gittrack.ui.AuthUiState
import com.example.gittrack.ui.ProfileStats
import com.example.gittrack.ui.ProjectDetailUiState
import com.example.gittrack.ui.ProjectsUiState
import com.example.gittrack.ui.TasksUiState
import com.example.gittrack.ui.components.EmptyState
import com.example.gittrack.ui.components.ProjectStatusDonutCard
import com.example.gittrack.ui.components.TaskDistributionCard
import com.example.gittrack.ui.components.ProjectTaskBreakdownCard
import com.example.gittrack.ui.components.ProjectCard
import com.example.gittrack.ui.components.ProjectStatusChip
import com.example.gittrack.ui.components.SectionHeader
import com.example.gittrack.ui.components.TaskRow
import com.example.gittrack.ui.components.statusIcon
import com.example.gittrack.ui.components.statusLabel
import com.example.gittrack.ui.theme.GeekPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    state: ProjectsUiState,
    onQueryChange: (String) -> Unit,
    onFilterChange: (ProjectStatus?) -> Unit,
    onOpenProject: (Long) -> Unit,
    onCreateProject: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            if (!state.requiresLogin) {
                FloatingActionButton(
                    onClick = onCreateProject,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Rounded.Add, "新建项目")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(Modifier.fillMaxWidth()) {
                    Text("GitTrack", style = MaterialTheme.typography.displaySmall)
                    Text(
                        "管理你的开发项目",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    placeholder = { Text("搜索项目...") }
                )
            }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.filter == null,
                        onClick = { onFilterChange(null) },
                        label = { Text("全部") }
                    )
                    ProjectStatus.entries.forEach { status ->
                        FilterChip(
                            selected = state.filter == status,
                            onClick = { onFilterChange(status) },
                            label = { Text(statusLabel(status)) },
                            leadingIcon = {
                                Icon(
                                    statusIcon(status),
                                    null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }
            if (state.requiresLogin) {
                item {
                    EmptyState(
                        icon = Icons.Rounded.Lock,
                        title = "登录后查看项目",
                        message = "退出登录后会隐藏项目和任务数据，重新登录即可继续查看。"
                    )
                }
            } else if (state.projects.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Rounded.Folder,
                        title = if (state.query.isBlank()) "还没有项目" else "没有匹配项目",
                        message = if (state.query.isBlank()) {
                            "记录你的第一个项目构思"
                        } else {
                            "尝试更换关键词或筛选条件"
                        },
                        action = { Button(onClick = onCreateProject) { Text("创建项目") } }
                    )
                }
            } else {
                ProjectStatus.entries.forEach { status ->
                    val group = state.projects.filter { it.project.status == status }
                    if (group.isNotEmpty()) {
                        item { SectionHeader(statusLabel(status), group.size, statusIcon(status)) }
                        item {
                            Card(
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.48f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                group.forEachIndexed { index, item ->
                                    ProjectCard(
                                        item,
                                        onClick = { onOpenProject(item.project.id) }
                                    )
                                    if (index != group.lastIndex) HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    state: TasksUiState,
    projects: List<ProjectWithProgress>,
    onQueryChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onToggle: (TaskEntity) -> Unit,
    onCreateTask: () -> Unit
) {
    val projectNames = remember(projects) { projects.associate { it.project.id to it.project.name } }
    Scaffold(
        floatingActionButton = {
            if (!state.requiresLogin) {
                FloatingActionButton(
                    onClick = onCreateTask,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Icon(Icons.Rounded.Add, "新增任务")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("任务", style = MaterialTheme.typography.displaySmall)
                Text("你的开发待办", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    placeholder = { Text("搜索任务...") }
                )
            }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ALL" to "全部", "TODAY" to "今天", "HIGH" to "高优先级", "DONE" to "已完成").forEach { (key, label) ->
                        FilterChip(selected = state.filter == key, onClick = { onFilterChange(key) }, label = { Text(label) })
                    }
                }
            }
            if (state.requiresLogin) {
                item {
                    EmptyState(
                        Icons.Rounded.Lock,
                        "登录后查看任务",
                        "退出登录后会隐藏上一个账号的任务数据，重新登录即可继续管理。"
                    )
                }
            } else if (state.tasks.isEmpty()) {
                item { EmptyState(Icons.Rounded.TaskAlt, "暂无待办", "点击右下角按钮添加开发任务") }
            } else {
                val groups = listOf(
                    "今天" to state.tasks.filter { !it.isCompleted && it.dateGroup == TaskDateGroup.TODAY },
                    "即将到期" to state.tasks.filter { !it.isCompleted && it.dateGroup == TaskDateGroup.UPCOMING },
                    "以后" to state.tasks.filter { !it.isCompleted && it.dateGroup == TaskDateGroup.LATER },
                    "无截止时间" to state.tasks.filter { !it.isCompleted && it.dateGroup == TaskDateGroup.NONE },
                    "已完成" to state.tasks.filter { it.isCompleted }
                )
                groups.forEach { (title, tasks) ->
                    if (tasks.isNotEmpty()) {
                        item { SectionHeader(title, tasks.size, if (title == "今天") Icons.Rounded.Today else Icons.Rounded.Checklist) }
                        item {
                            Card(
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.48f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                tasks.forEachIndexed { index, task ->
                                    TaskRow(task, projectNames[task.projectId] ?: "未知项目", onToggle = { onToggle(task) })
                                    if (index != tasks.lastIndex) HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}




@Composable
fun MeScreen(
    stats: ProfileStats,
    projects: List<ProjectWithProgress>,
    tasks: List<TaskEntity>,
    preferences: UserPreferences,
    authState: AuthUiState,
    onProjects: () -> Unit,
    onTasks: () -> Unit,
    onAi: () -> Unit,
    onSync: () -> Unit,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String, String) -> Unit,
    onLogout: () -> Unit,
    onClearAuthState: () -> Unit,
    onAvatarChange: (String) -> Unit,
    onEditProfile: (String, String) -> Unit,
    onTheme: (ThemeMode) -> Unit,
    onDefaultStatus: (ProjectStatus) -> Unit,
    onToggleCompleted: (Boolean) -> Unit,
    onClearCache: () -> Unit,
    onAbout: () -> Unit
) {
    var showProfileEditor by remember { mutableStateOf(false) }
    var showAuthSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            onAvatarChange(uri.toString())
        }
    }

    LaunchedEffect(preferences.isLoggedIn) {
        if (preferences.isLoggedIn) showAuthSheet = false
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(Modifier.fillMaxWidth()) {
                Text("我的", style = MaterialTheme.typography.displaySmall)
                Text(
                    if (preferences.isLoggedIn) "个人资料、统计与偏好" else "登录后管理你的个人资料",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val avatarUri = preferences.profileAvatar.takeIf {
                        it.startsWith("content://")
                    }

                    Surface(
                        modifier = Modifier.size(70.dp),
                        shape = CircleShape,
                        color = if (preferences.isLoggedIn) {
                            GeekPalette.ElectricBlue.copy(alpha = 0.16f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (preferences.isLoggedIn) {
                                GeekPalette.ElectricBlue.copy(alpha = 0.32f)
                            } else {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                            }
                        ),
                        onClick = {
                            if (preferences.isLoggedIn) {
                                avatarPickerLauncher.launch(arrayOf("image/*"))
                            }
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (avatarUri != null) {
                                AsyncImage(
                                    model = avatarUri,
                                    contentDescription = "头像",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = "头像",
                                    tint = if (preferences.isLoggedIn) {
                                        GeekPalette.ElectricBlue
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        }
                    }
                    Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                        Text(
                            if (preferences.isLoggedIn) preferences.profileName else "登录 GitTrack",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            if (preferences.isLoggedIn) {
                                preferences.profileEmail
                            } else if (preferences.hasLocalAccount) {
                                "使用本机账户继续"
                            } else {
                                "注册后即可编辑个人资料"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                        if (preferences.isLoggedIn) {
                            Spacer(Modifier.height(7.dp))
                            Surface(
                                shape = CircleShape,
                                color = GeekPalette.Emerald.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    "已登陆",
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                    color = GeekPalette.Emerald,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    if (preferences.isLoggedIn) {
                        OutlinedButton(onClick = { showProfileEditor = true }) {
                            Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(6.dp))
                            Text("编辑")
                        }
                    } else {
                        Button(
                            onClick = {
                                onClearAuthState()
                                showAuthSheet = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Rounded.Login, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(6.dp))
                            Text(if (preferences.hasLocalAccount) "登录" else "登录 / 注册")
                        }
                    }
                }
            }
        }

        if (preferences.isLoggedIn) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.48f)
                    )
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            stats.projects.toString(),
                            "项目总数",
                            GeekPalette.ElectricBlue
                        )
                        StatItem(
                            stats.activeProjects.toString(),
                            "开发中",
                            GeekPalette.Cyan
                        )
                        StatItem(
                            stats.completedTasks.toString(),
                            "已完成任务",
                            GeekPalette.Emerald
                        )
                    }
                }
            }

            item { SettingsGroupTitle("数据概览") }
            item { ProjectStatusDonutCard(projects) }
            item { TaskDistributionCard(tasks) }
        } else {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.55f
                        )
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(15.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "登录后查看个人数据",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = "项目统计和图表不会统计未登录状态下的演示数据",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        item { SettingsGroupTitle("快捷入口") }
        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                SettingsListItem(Icons.Rounded.Folder, "我的项目", onProjects)
                HorizontalDivider()
                SettingsListItem(Icons.Rounded.TaskAlt, "我的待办", onTasks)
                HorizontalDivider()
                SettingsListItem(Icons.Rounded.AutoAwesome, "AI 项目助手", onAi)
                HorizontalDivider()
                SettingsListItem(
                    Icons.Rounded.Sync,
                    "同步 GitHub 仓库",
                    onSync,
                    trailing = "立即同步"
                )
            }
        }

        item { SettingsGroupTitle("外观") }
        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(
                    Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "主题模式",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        ThemeMode.entries.forEachIndexed { index, mode ->
                            SegmentedButton(
                                selected = preferences.themeMode == mode,
                                onClick = { onTheme(mode) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index,
                                    ThemeMode.entries.size
                                )
                            ) {
                                Text(
                                    when (mode) {
                                        ThemeMode.SYSTEM -> "跟随系统"
                                        ThemeMode.LIGHT -> "浅色"
                                        ThemeMode.DARK -> "深色"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        item { SettingsGroupTitle("项目与任务偏好") }
        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(
                    Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("默认项目状态", fontWeight = FontWeight.Bold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProjectStatus.entries.forEach { status ->
                            FilterChip(
                                selected = preferences.defaultProjectStatus == status,
                                onClick = { onDefaultStatus(status) },
                                label = { Text(statusLabel(status)) },
                                leadingIcon = {
                                    Icon(
                                        statusIcon(status),
                                        null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
                }
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("显示已完成任务") },
                    supportingContent = { Text("关闭后，任务页隐藏已完成项目") },
                    leadingContent = {
                        Icon(
                            Icons.Rounded.Visibility,
                            null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = preferences.showCompletedTasks,
                            onCheckedChange = onToggleCompleted
                        )
                    }
                )
            }
        }

        item { SettingsGroupTitle("其他") }
        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                if (preferences.isLoggedIn) {
                    SettingsListItem(
                        Icons.Rounded.Logout,
                        "退出登录",
                        onLogout,
                        trailing = "退出"
                    )
                    HorizontalDivider()
                }
                SettingsListItem(
                    Icons.Rounded.Delete,
                    "清除 GitHub 缓存",
                    onClearCache,
                    trailing = "清除"
                )
                HorizontalDivider()
                SettingsListItem(Icons.Rounded.Info, "关于 GitTrack", onAbout)
            }
        }
    }


    if (showAuthSheet) {
        AuthSheet(
            hasLocalAccount = preferences.hasLocalAccount,
            isLoggedIn = preferences.isLoggedIn,
            state = authState,
            onDismiss = {
                onClearAuthState()
                showAuthSheet = false
            },
            onLogin = onLogin,
            onRegister = onRegister,
            onClearError = onClearAuthState
        )
    }

    if (showProfileEditor && preferences.isLoggedIn) {
        ProfileEditorSheet(
            initialName = preferences.profileName,
            initialEmail = preferences.profileEmail,
            onDismiss = { showProfileEditor = false },
            onSave = { name, email ->
                onEditProfile(name, email)
                showProfileEditor = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthSheet(
    hasLocalAccount: Boolean,
    isLoggedIn: Boolean,
    state: AuthUiState,
    onDismiss: () -> Unit,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String, String) -> Unit,
    onClearError: () -> Unit
) {
    var registerMode by rememberSaveable(hasLocalAccount) { mutableStateOf(!hasLocalAccount) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) onDismiss()
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (registerMode) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (registerMode) Icons.Rounded.PersonAdd else Icons.Rounded.Login,
                            null,
                            tint = if (registerMode) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }
                Column(Modifier.padding(start = 12.dp)) {
                    Text(
                        if (registerMode) "创建本地账户" else "登录 GitTrack",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "登录状态会保存在当前设备",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }

            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf(false, true).forEachIndexed { index, isRegister ->
                    SegmentedButton(
                        selected = registerMode == isRegister,
                        onClick = {
                            registerMode = isRegister
                            onClearError()
                        },
                        shape = SegmentedButtonDefaults.itemShape(index, 2),
                        icon = {
                            Icon(
                                if (isRegister) Icons.Rounded.PersonAdd else Icons.Rounded.Login,
                                null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    ) {
                        Text(if (isRegister) "注册" else "登录")
                    }
                }
            }

            if (registerMode) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        onClearError()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("昵称") },
                    leadingIcon = { Icon(Icons.Rounded.Person, null) },
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    onClearError()
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("邮箱") },
                leadingIcon = { Icon(Icons.Rounded.Email, null) },
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    onClearError()
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("密码") },
                leadingIcon = { Icon(Icons.Rounded.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            if (passwordVisible) "隐藏密码" else "显示密码"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                supportingText = {
                    if (registerMode) Text("密码至少 6 位")
                },
                singleLine = true
            )

            if (registerMode) {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        onClearError()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("确认密码") },
                    leadingIcon = { Icon(Icons.Rounded.Lock, null) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
            }

            state.error?.let { error ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        error,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
            ) {
                Text(
                    if (registerMode && hasLocalAccount) {
                        "提示：注册新账户会替换当前设备上原有的本地账户。此功能仅用于课程演示。"
                    } else {
                        "账户仅保存在本机，不会上传到服务器。清除应用数据后账户也会被删除。"
                    },
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            Button(
                onClick = {
                    if (registerMode) {
                        onRegister(name, email, password, confirmPassword)
                    } else {
                        onLogin(email, password)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (registerMode) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    contentColor = if (registerMode) {
                        MaterialTheme.colorScheme.onTertiary
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    }
                )
            ) {
                if (state.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = if (registerMode) {
                            MaterialTheme.colorScheme.onTertiary
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        }
                    )
                    Spacer(Modifier.size(8.dp))
                } else {
                    Icon(
                        if (registerMode) Icons.Rounded.PersonAdd else Icons.Rounded.Login,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                }
                Text(if (registerMode) "注册并登录" else "登录")
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileEditorSheet(
    initialName: String,
    initialEmail: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }
    var email by rememberSaveable(initialEmail) { mutableStateOf(initialEmail) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    val normalizedName = name.trim()
    val normalizedEmail = email.trim()
    val emailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("编辑个人资料", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(
                "昵称和登录邮箱会同步保存在本机 DataStore 中。",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("昵称") },
                leadingIcon = { Icon(Icons.Rounded.Person, null) },
                isError = submitted && normalizedName.isEmpty(),
                supportingText = {
                    if (submitted && normalizedName.isEmpty()) Text("昵称不能为空")
                },
                singleLine = true
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("邮箱") },
                leadingIcon = { Icon(Icons.Rounded.Email, null) },
                isError = submitted && !emailValid,
                supportingText = {
                    if (submitted && !emailValid) Text("请输入有效邮箱地址")
                },
                singleLine = true
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) { Text("取消") }
                Button(
                    onClick = {
                        submitted = true
                        if (normalizedName.isNotEmpty() && emailValid) {
                            onSave(normalizedName, normalizedEmail)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("保存") }
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable private fun StatItem(value: String, label: String, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = accent.copy(alpha = 0.12f)
        ) {
            Text(
                value,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
        Spacer(Modifier.height(7.dp))
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable private fun SettingsGroupTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 4.dp),
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun SettingsListItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    trailing: String? = null
) {
    val accent = when {
        title.contains("AI") -> GeekPalette.Violet
        title.contains("GitHub") || title.contains("同步") -> GeekPalette.Cyan
        title.contains("任务") || title.contains("完成") -> GeekPalette.Emerald
        title.contains("清除") || title.contains("退出") -> GeekPalette.Rose
        else -> GeekPalette.ElectricBlue
    }
    ListItem(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = { Text(title, fontWeight = FontWeight.Medium) },
        leadingContent = {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = RoundedCornerShape(12.dp),
                color = accent.copy(alpha = 0.13f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp))
                }
            }
        },
        trailingContent = {
            if (trailing != null) {
                Surface(shape = CircleShape, color = accent.copy(alpha = 0.11f)) {
                    Text(
                        trailing,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    state: ProjectDetailUiState,
    aiState: AiUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onAddTask: () -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onSync: () -> Unit,
    onGenerateTasks: () -> Unit,
    onApplyTasks: (List<AiGeneratedTask>) -> Unit,
    onClearAi: () -> Unit,
    onDelete: () -> Unit
) {
    val project = state.project
    var showDelete by remember { mutableStateOf(false) }
    var showAi by remember { mutableStateOf(false) }
    if (project == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) { Icon(Icons.Rounded.ArrowBack, "返回") }
                },
                actions = {
                    IconButton(
                        onClick = onEdit,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) { Icon(Icons.Rounded.Edit, "编辑") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(project.name, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                Text(project.summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                ProjectStatusChip(project.status)
            }
            item {
                Card(shape = RoundedCornerShape(22.dp)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        OverviewItem("${state.progressPercent}%", "项目进度")
                        OverviewItem(state.tasks.size.toString(), "任务总数")
                        OverviewItem(state.completedCount.toString(), "已完成")
                    }
                }
            }
            item { ProjectTaskBreakdownCard(state.tasks) }
            item { SectionHeader("当前任务", state.tasks.count { !it.isCompleted }, Icons.Rounded.Checklist) }
            item {
                Card(shape = RoundedCornerShape(22.dp)) {
                    if (state.tasks.isEmpty()) {
                        Text("当前项目暂无任务", modifier = Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        state.tasks.take(4).forEachIndexed { index, task ->
                            TaskRow(task, project.name, onToggle = { onToggleTask(task) })
                            if (index != state.tasks.take(4).lastIndex) HorizontalDivider()
                        }
                    }
                    TextButton(onClick = onAddTask, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Rounded.Add, null); Text("添加任务") }
                }
            }
            item { SectionHeader("项目构思", icon = Icons.Rounded.Lightbulb) }
            item { Card(shape = RoundedCornerShape(22.dp)) { Text(project.idea, modifier = Modifier.padding(18.dp), lineHeight = 23.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
            if (!project.githubOwner.isNullOrBlank() && !project.githubRepo.isNullOrBlank()) {
                item { SectionHeader("GitHub 仓库", icon = Icons.Rounded.Code) }
                item { GitHubCard(state.github, state.isSyncing, state.syncError, project.githubOwner + "/" + project.githubRepo, onSync) }
            }
            item {
                Card(shape = RoundedCornerShape(22.dp)) {
                    ListItem(
                        headlineContent = { Text("AI 拆分开发任务") },
                        leadingContent = { Icon(Icons.Rounded.AutoAwesome, null, tint = MaterialTheme.colorScheme.tertiary) },
                        trailingContent = {
                            Button(
                                onClick = { showAi = true; onGenerateTasks() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = MaterialTheme.colorScheme.onTertiary
                                )
                            ) { Text("生成") }
                        }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("删除项目", color = MaterialTheme.colorScheme.error) },
                        leadingContent = { Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error) },
                        trailingContent = { TextButton(onClick = { showDelete = true }) { Text("删除", color = MaterialTheme.colorScheme.error) } }
                    )
                }
            }
        }
    }
    if (showAi) {
        AiTasksSheet(aiState, onDismiss = { showAi = false; onClearAi() }, onApply = { onApplyTasks(it); showAi = false })
    }
    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("删除项目？") },
            text = { Text("项目及其全部任务都会被删除。") },
            confirmButton = { TextButton(onClick = { showDelete = false; onDelete() }) { Text("删除", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("取消") } }
        )
    }
}

@Composable private fun OverviewItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable private fun GitHubCard(cache: GitHubCacheEntity?, syncing: Boolean, error: String?, fallbackName: String, onSync: () -> Unit) {
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(46.dp), shape = RoundedCornerShape(14.dp), color = Color(0xFF202124)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Code, null, tint = Color.White) }
                }
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(cache?.fullName ?: fallbackName, fontWeight = FontWeight.Bold)
                    Text(cache?.description ?: error ?: "点击刷新获取仓库信息", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                IconButton(
                    onClick = onSync,
                    enabled = !syncing,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = GeekPalette.Cyan.copy(alpha = 0.13f),
                        contentColor = GeekPalette.Cyan
                    )
                ) {
                    if (syncing) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Rounded.Refresh, "刷新")
                }
            }
            if (cache != null) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text(cache.language ?: "Unknown") })
                    AssistChip(onClick = {}, label = { Text("★ ${cache.stars}") })
                    AssistChip(onClick = {}, label = { Text("Fork ${cache.forks}") })
                    AssistChip(onClick = {}, label = { Text("Issues ${cache.openIssues}") })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiTasksSheet(aiState: AiUiState, onDismiss: () -> Unit, onApply: (List<AiGeneratedTask>) -> Unit) {
    val selected = remember(aiState.generatedTasks) { mutableStateListOf<AiGeneratedTask>().apply { addAll(aiState.generatedTasks) } }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("AI 拆分任务", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            when {
                aiState.loading -> Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                aiState.error != null -> Text(aiState.error, color = MaterialTheme.colorScheme.error)
                aiState.generatedTasks.isNotEmpty() -> {
                    Text("勾选需要加入项目的任务", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    aiState.generatedTasks.forEach { task ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Checkbox(checked = task in selected, onCheckedChange = { checked -> if (checked) selected.add(task) else selected.remove(task) })
                            Column(Modifier.weight(1f)) { Text(task.title, fontWeight = FontWeight.Medium); Text(task.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }
                    Button(
                        onClick = { onApply(selected.toList()) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selected.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        )
                    ) { Text("加入 ${selected.size} 条任务") }
                }
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectFormScreen(
    existing: ProjectEntity?,
    defaultStatus: ProjectStatus,
    aiState: AiUiState,
    onBack: () -> Unit,
    onSave: (ProjectEntity) -> Unit,
    onRefine: (String) -> Unit,
    onClearAi: () -> Unit
) {
    var name by rememberSaveable(existing?.id) { mutableStateOf(existing?.name.orEmpty()) }
    var summary by rememberSaveable(existing?.id) { mutableStateOf(existing?.summary.orEmpty()) }
    var idea by rememberSaveable(existing?.id) { mutableStateOf(existing?.idea.orEmpty()) }
    var tech by rememberSaveable(existing?.id) { mutableStateOf(existing?.techStack.orEmpty()) }
    var github by rememberSaveable(existing?.id) { mutableStateOf(listOfNotNull(existing?.githubOwner, existing?.githubRepo).joinToString("/")) }
    var status by rememberSaveable(existing?.id) { mutableStateOf(existing?.status ?: defaultStatus) }
    var showAi by remember { mutableStateOf(false) }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(if (existing == null) "新建项目" else "编辑项目") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "返回") } },
            actions = { TextButton(onClick = {
                val parts = github.trim().removePrefix("https://github.com/").removeSuffix("/").removeSuffix(".git").split("/").filter { it.isNotBlank() }
                onSave(ProjectEntity(
                    id = existing?.id ?: 0,
                    name = name.trim(), summary = summary.trim(), idea = idea.trim(), status = status,
                    techStack = tech.trim(), githubOwner = parts.getOrNull(0), githubRepo = parts.getOrNull(1),
                    icon = existing?.icon ?: "folder", tone = existing?.tone ?: "blue",
                    createdAt = existing?.createdAt ?: System.currentTimeMillis()
                ))
            }) { Text("保存") } }
        )
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("项目名称") }, singleLine = true) }
            item { OutlinedTextField(summary, { summary = it }, Modifier.fillMaxWidth(), label = { Text("一句话简介") }, singleLine = true) }
            item {
                Text("项目状态", fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProjectStatus.entries.forEach { value -> ProjectStatusChip(value, selected = status == value, onClick = { status = value }) }
                }
            }
            item { OutlinedTextField(idea, { idea = it }, Modifier.fillMaxWidth(), label = { Text("项目构思") }, minLines = 5) }
            item { OutlinedTextField(tech, { tech = it }, Modifier.fillMaxWidth(), label = { Text("技术栈") }, singleLine = true) }
            item { OutlinedTextField(github, { github = it }, Modifier.fillMaxWidth(), label = { Text("GitHub 仓库（可选）") }, placeholder = { Text("owner/repository") }, singleLine = true) }
            item {
                OutlinedButton(
                    onClick = { showAi = true; onRefine(idea) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = idea.isNotBlank(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GeekPalette.Violet,
                        containerColor = GeekPalette.Violet.copy(alpha = 0.08f)
                    )
                ) {
                    Icon(Icons.Rounded.AutoAwesome, null)
                    Spacer(Modifier.size(6.dp))
                    Text("AI 帮我完善")
                }
            }
        }
    }
    if (showAi) {
        AiPlanSheet(aiState, onDismiss = { showAi = false; onClearAi() }, onApply = { plan -> name = plan.name; summary = plan.summary; tech = plan.techStack; showAi = false; onClearAi() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun AiPlanSheet(aiState: AiUiState, onDismiss: () -> Unit, onApply: (AiProjectPlan) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("AI 项目规划", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            when {
                aiState.loading -> Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                aiState.error != null -> Text(aiState.error, color = MaterialTheme.colorScheme.error)
                aiState.projectPlan != null -> {
                    val plan = aiState.projectPlan!!
                    Text(plan.name, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                    Text(plan.summary)
                    Text("目标用户：${plan.targetUsers}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    plan.coreFeatures.forEach { Text("• $it") }
                    Text("技术栈：${plan.techStack}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(
                        onClick = { onApply(plan) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        )
                    ) { Text("应用内容") }
                }
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormScreen(
    projects: List<ProjectWithProgress>,
    initialProjectId: Long?,
    onBack: () -> Unit,
    onSave: (Long, String, String, TaskPriority, TaskDateGroup) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var projectId by rememberSaveable { mutableStateOf(initialProjectId ?: projects.firstOrNull()?.project?.id ?: 0L) }
    var priority by rememberSaveable { mutableStateOf(TaskPriority.MEDIUM) }
    var date by rememberSaveable { mutableStateOf(TaskDateGroup.TODAY) }
    var projectMenu by remember { mutableStateOf(false) }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("新增任务") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "返回") } },
            actions = { TextButton(onClick = { onSave(projectId, title, description, priority, date) }) { Text("保存") } }
        )
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("任务标题") }, singleLine = true) }
            item {
                Box {
                    OutlinedButton(onClick = { projectMenu = true }, modifier = Modifier.fillMaxWidth()) { Text(projects.find { it.project.id == projectId }?.project?.name ?: "选择项目") }
                    DropdownMenu(expanded = projectMenu, onDismissRequest = { projectMenu = false }) {
                        projects.forEach { p -> DropdownMenuItem(text = { Text(p.project.name) }, onClick = { projectId = p.project.id; projectMenu = false }) }
                    }
                }
            }
            item {
                Text("优先级", fontWeight = FontWeight.Bold)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    TaskPriority.entries.forEachIndexed { index, value ->
                        SegmentedButton(selected = priority == value, onClick = { priority = value }, shape = SegmentedButtonDefaults.itemShape(index, TaskPriority.entries.size)) { Text(when(value){TaskPriority.LOW->"低";TaskPriority.MEDIUM->"中";TaskPriority.HIGH->"高"}) }
                    }
                }
            }
            item {
                Text("截止时间", fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskDateGroup.entries.forEach { value -> FilterChip(selected = date == value, onClick = { date = value }, label = { Text(when(value){TaskDateGroup.TODAY->"今天";TaskDateGroup.UPCOMING->"即将到期";TaskDateGroup.LATER->"以后";TaskDateGroup.NONE->"无截止时间"}) }) }
                }
            }
            item { OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("任务描述") }, minLines = 4) }
        }
    }
}
