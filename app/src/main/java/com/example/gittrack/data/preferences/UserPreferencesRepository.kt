package com.example.gittrack.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.gittrack.data.local.ProjectStatus
import java.security.MessageDigest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("gittrack_preferences")

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val defaultProjectStatus: ProjectStatus = ProjectStatus.IN_PROGRESS,
    val showCompletedTasks: Boolean = true,
    val lastOpenedProjectId: Long? = null,
    val profileName: String = "",
    val profileEmail: String = "",
    val isLoggedIn: Boolean = false,
    val hasLocalAccount: Boolean = false,
    val demoDataDismissed: Boolean = false,
    val profileAvatar: String = "developer"
)

/**
 * 用户偏好与本地演示账户仓库。
 *
 * 登录/注册用于课程项目的交互演示：账户信息仅保存在当前设备的 DataStore 中，
 * 密码只保存 SHA-256 摘要，不保存明文。它不是可用于生产环境的认证方案。
 */
class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val theme = stringPreferencesKey("theme_mode")
        val defaultStatus = stringPreferencesKey("default_project_status")
        val showCompleted = booleanPreferencesKey("show_completed_tasks")
        val lastProjectId = longPreferencesKey("last_project_id")
        val profileName = stringPreferencesKey("profile_name")
        val profileEmail = stringPreferencesKey("profile_email")
        val profileAvatar = stringPreferencesKey("profile_avatar")
        val isLoggedIn = booleanPreferencesKey("is_logged_in")
        val accountName = stringPreferencesKey("account_name")
        val accountEmail = stringPreferencesKey("account_email")
        val accountPasswordHash = stringPreferencesKey("account_password_hash")
        val demoDataDismissed = booleanPreferencesKey("demo_data_dismissed")
    }

    val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val accountEmail = prefs[Keys.accountEmail].orEmpty()
        val passwordHash = prefs[Keys.accountPasswordHash].orEmpty()
        UserPreferences(
            themeMode = runCatching {
                ThemeMode.valueOf(prefs[Keys.theme] ?: ThemeMode.SYSTEM.name)
            }.getOrDefault(ThemeMode.SYSTEM),
            defaultProjectStatus = runCatching {
                ProjectStatus.valueOf(
                    prefs[Keys.defaultStatus] ?: ProjectStatus.IN_PROGRESS.name
                )
            }.getOrDefault(ProjectStatus.IN_PROGRESS),
            showCompletedTasks = prefs[Keys.showCompleted] ?: true,
            lastOpenedProjectId = prefs[Keys.lastProjectId],
            profileName = prefs[Keys.profileName] ?: prefs[Keys.accountName] ?: "test",
            profileEmail = prefs[Keys.profileEmail] ?: accountEmail.ifBlank { "test@test.com" },
            profileAvatar = prefs[Keys.profileAvatar] ?: "developer",
            isLoggedIn = prefs[Keys.isLoggedIn] ?: false,
            hasLocalAccount = accountEmail.isNotBlank() && passwordHash.isNotBlank(),
            demoDataDismissed = prefs[Keys.demoDataDismissed] ?: false
        )
    }

    suspend fun setTheme(mode: ThemeMode) = context.dataStore.edit {
        it[Keys.theme] = mode.name
    }

    suspend fun setDefaultStatus(status: ProjectStatus) = context.dataStore.edit {
        it[Keys.defaultStatus] = status.name
    }

    suspend fun setShowCompleted(show: Boolean) = context.dataStore.edit {
        it[Keys.showCompleted] = show
    }

    suspend fun setLastOpenedProjectId(id: Long) = context.dataStore.edit {
        it[Keys.lastProjectId] = id
    }

    suspend fun setProfileAvatar(avatar: String) = context.dataStore.edit {
        it[Keys.profileAvatar] = avatar
    }

    suspend fun setProfile(name: String, email: String) = context.dataStore.edit {
        it[Keys.profileName] = name.trim()
        it[Keys.profileEmail] = email.trim().lowercase()
        // 编辑个人资料时同步本地账户信息，保证下次登录使用新邮箱。
        if (!it[Keys.accountEmail].isNullOrBlank()) {
            it[Keys.accountName] = name.trim()
            it[Keys.accountEmail] = email.trim().lowercase()
        }
    }

    suspend fun register(name: String, email: String, password: String) {
        val normalizedName = name.trim()
        val normalizedEmail = email.trim().lowercase()
        context.dataStore.edit {
            it[Keys.accountName] = normalizedName
            it[Keys.accountEmail] = normalizedEmail
            it[Keys.accountPasswordHash] = hashPassword(password)
            it[Keys.profileName] = normalizedName
            it[Keys.profileEmail] = normalizedEmail
            it[Keys.isLoggedIn] = true
            it[Keys.demoDataDismissed] = true
        }
    }

    suspend fun login(email: String, password: String): Boolean {
        val prefs = context.dataStore.data.first()
        val storedEmail = prefs[Keys.accountEmail].orEmpty()
        val storedHash = prefs[Keys.accountPasswordHash].orEmpty()
        val matches = storedEmail.equals(email.trim(), ignoreCase = true) &&
            storedHash.isNotBlank() &&
            storedHash == hashPassword(password)
        if (matches) {
            context.dataStore.edit {
                it[Keys.isLoggedIn] = true
                it[Keys.demoDataDismissed] = true
            }
        }
        return matches
    }

    suspend fun markDemoDataDismissed() = context.dataStore.edit {
        it[Keys.demoDataDismissed] = true
    }

    suspend fun logout() = context.dataStore.edit {
        it[Keys.isLoggedIn] = false
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
