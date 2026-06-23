package com.example.gittrack.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gittrack.GitTrackApplication
import com.example.gittrack.data.local.ProjectEntity
import com.example.gittrack.data.preferences.ThemeMode
import com.example.gittrack.ui.screens.MeScreen
import com.example.gittrack.ui.screens.ProjectDetailScreen
import com.example.gittrack.ui.screens.ProjectFormScreen
import com.example.gittrack.ui.screens.ProjectsScreen
import com.example.gittrack.ui.screens.TaskFormScreen
import com.example.gittrack.ui.screens.TasksScreen
import com.example.gittrack.ui.theme.GitTrackTheme
import kotlinx.coroutines.flow.collectLatest

private data class TopDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val topDestinations = listOf(
    TopDestination("projects", "项目", Icons.Rounded.Folder),
    TopDestination("tasks", "任务", Icons.Rounded.TaskAlt),
    TopDestination("me", "我的", Icons.Rounded.Person)
)

@Composable
fun GitTrackApp() {
    val application = LocalContext.current.applicationContext as GitTrackApplication
    val vm: GitTrackViewModel = viewModel(
        factory = GitTrackViewModel.Factory(
            application.container.projectRepository,
            application.container.aiRepository,
            application.container.preferencesRepository
        )
    )
    val prefs by vm.preferences.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val dark = when (prefs.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    GitTrackTheme(darkTheme = dark, dynamicColor = false) {
        AppNavigation(vm)
    }
}

private fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun AppNavigation(vm: GitTrackViewModel) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStack by navController.currentBackStackEntryAsState()
    val destination = backStack?.destination
    val showBottomBar = topDestinations.any { item ->
        destination?.hierarchy?.any { node -> node.route == item.route } == true
    }

    val projectsState by vm.projectsUiState.collectAsStateWithLifecycle()
    val tasksState by vm.tasksUiState.collectAsStateWithLifecycle()
    val detailState by vm.detailUiState.collectAsStateWithLifecycle()
    val profileStats by vm.profileStats.collectAsStateWithLifecycle()
    val analyticsState by vm.analyticsUiState.collectAsStateWithLifecycle()
    val preferences by vm.preferences.collectAsStateWithLifecycle()
    val aiState by vm.aiState.collectAsStateWithLifecycle()
    val authState by vm.authState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.events.collectLatest { event ->
            when (event) {
                is UiEvent.Message -> snackbarHostState.showSnackbar(event.text)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    topDestinations.forEach { item ->
                        val selected = destination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navController.navigateTopLevel(item.route) },
                            icon = { Icon(item.icon, item.label) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { outerPadding ->
        NavHost(
            navController = navController,
            startDestination = "projects",
            modifier = Modifier.padding(outerPadding)
        ) {
            composable("projects") {
                ProjectsScreen(
                    state = projectsState,
                    onQueryChange = vm::setProjectQuery,
                    onFilterChange = vm::setProjectFilter,
                    onOpenProject = { id ->
                        vm.selectProject(id)
                        navController.navigate("project/$id")
                    },
                    onCreateProject = { navController.navigate("projectForm/0") }
                )
            }

            composable("tasks") {
                TasksScreen(
                    state = tasksState,
                    projects = projectsState.projects,
                    onQueryChange = vm::setTaskQuery,
                    onFilterChange = vm::setTaskFilter,
                    onToggle = vm::toggleTask,
                    onCreateTask = { navController.navigate("taskForm/0") }
                )
            }

            composable("me") {
                MeScreen(
                    stats = profileStats,
                    projects = analyticsState.projects,
                    tasks = analyticsState.tasks,
                    preferences = preferences,
                    authState = authState,
                    onProjects = { navController.navigateTopLevel("projects") },
                    onTasks = { navController.navigateTopLevel("tasks") },
                    onAi = {
                        val id = preferences.lastOpenedProjectId
                            ?.takeIf { candidate -> projectsState.projects.any { it.project.id == candidate } }
                            ?: projectsState.projects.firstOrNull()?.project?.id
                        if (id == null) {
                            vm.events.tryEmit(UiEvent.Message("请先创建一个项目"))
                        } else {
                            vm.selectProject(id)
                            navController.navigate("project/$id")
                        }
                    },
                    onSync = vm::syncAllGitHub,
                    onLogin = vm::login,
                    onRegister = vm::register,
                    onLogout = vm::logout,
                    onClearAuthState = vm::clearAuthState,
                    onAvatarChange = vm::updateAvatar,
                    onEditProfile = vm::updateProfile,
                    onTheme = vm::setTheme,
                    onDefaultStatus = vm::setDefaultStatus,
                    onToggleCompleted = vm::setShowCompleted,
                    onClearCache = vm::clearGitHubCache,
                    onAbout = {
                        vm.events.tryEmit(UiEvent.Message("GitTrack 1.0 · Material 3"))
                    }
                )
            }

            composable("project/{id}") { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                LaunchedEffect(id) { vm.selectProject(id) }
                ProjectDetailScreen(
                    state = detailState,
                    aiState = aiState,
                    onBack = navController::popBackStack,
                    onEdit = { navController.navigate("projectForm/$id") },
                    onAddTask = { navController.navigate("taskForm/$id") },
                    onToggleTask = vm::toggleTask,
                    onSync = vm::syncGitHub,
                    onGenerateTasks = vm::generateTasks,
                    onApplyTasks = vm::applyGeneratedTasks,
                    onClearAi = vm::clearAiState,
                    onDelete = {
                        vm.deleteSelectedProject {
                            navController.popBackStack("projects", false)
                        }
                    }
                )
            }

            composable("projectForm/{id}") { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                val existing: ProjectEntity? = if (id == 0L) {
                    null
                } else {
                    projectsState.projects.firstOrNull { it.project.id == id }?.project
                }
                ProjectFormScreen(
                    existing = existing,
                    defaultStatus = preferences.defaultProjectStatus,
                    aiState = aiState,
                    onBack = navController::popBackStack,
                    onSave = { project ->
                        vm.saveProject(project) { savedId ->
                            if (id == 0L) {
                                navController.navigate("project/$savedId") {
                                    popUpTo("projects")
                                }
                            } else {
                                navController.popBackStack()
                            }
                        }
                    },
                    onRefine = vm::refineProject,
                    onClearAi = vm::clearAiState
                )
            }

            composable("taskForm/{projectId}") { entry ->
                val projectId = entry.arguments?.getString("projectId")
                    ?.toLongOrNull()
                    ?.takeIf { it != 0L }
                TaskFormScreen(
                    projects = projectsState.projects,
                    initialProjectId = projectId,
                    onBack = navController::popBackStack,
                    onSave = { pid, title, description, priority, date ->
                        vm.addTask(pid, title, description, priority, date) {
                            navController.popBackStack()
                        }
                    }
                )
            }
        }
    }
}
