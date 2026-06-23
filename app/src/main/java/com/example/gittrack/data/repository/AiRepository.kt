package com.example.gittrack.data.repository

import com.example.gittrack.BuildConfig
import com.example.gittrack.data.local.TaskPriority
import com.example.gittrack.data.network.ChatMessage
import com.example.gittrack.data.network.ChatRequest
import com.example.gittrack.data.network.QwenApi
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class AiProjectPlan(
    val name: String,
    val summary: String,
    val targetUsers: String,
    val coreFeatures: List<String>,
    val techStack: String
)

data class AiGeneratedTask(
    val title: String,
    val description: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM
)

private data class ProjectPlanEnvelope(
    val name: String = "",
    val summary: String = "",
    @SerializedName("target_users") val targetUsers: String = "",
    @SerializedName("core_features") val coreFeatures: List<String> = emptyList(),
    @SerializedName("tech_stack") val techStack: String = ""
)
private data class TaskEnvelope(val tasks: List<RawTask> = emptyList())
private data class RawTask(val title: String = "", val description: String = "", val priority: String = "MEDIUM")

class AiRepository(private val api: QwenApi, private val gson: Gson = Gson()) {
    fun isConfigured(): Boolean = BuildConfig.DASHSCOPE_API_KEY.isNotBlank()

    suspend fun refineProject(idea: String): Result<AiProjectPlan> = runCatching {
        requireConfigured()
        val prompt = """
            根据下面的软件项目构思生成项目规划。只返回 JSON，不要 Markdown。
            JSON 格式：
            {"name":"项目名","summary":"一句话简介","target_users":"目标用户","core_features":["功能1","功能2"],"tech_stack":"技术栈"}
            项目构思：$idea
        """.trimIndent()
        val content = request(prompt)
        val parsed = gson.fromJson(extractJson(content), ProjectPlanEnvelope::class.java)
        AiProjectPlan(
            name = parsed.name.ifBlank { "新项目" },
            summary = parsed.summary.ifBlank { "一个由 AI 完善的开发项目" },
            targetUsers = parsed.targetUsers.ifBlank { "个人开发者" },
            coreFeatures = parsed.coreFeatures.filter { it.isNotBlank() }.take(6),
            techStack = parsed.techStack.ifBlank { "Kotlin, Jetpack Compose, Room" }
        )
    }

    suspend fun generateTasks(
        projectName: String,
        projectDescription: String,
        techStack: String,
        existingTasks: List<String>
    ): Result<List<AiGeneratedTask>> = runCatching {
        requireConfigured()
        val prompt = """
            你是软件项目规划助手。为项目生成 6 到 10 条可执行开发任务。
            只返回 JSON，不要 Markdown。
            JSON 格式：{"tasks":[{"title":"任务标题","description":"说明","priority":"HIGH|MEDIUM|LOW"}]}
            项目名称：$projectName
            项目描述：$projectDescription
            技术栈：$techStack
            已有任务：${existingTasks.joinToString("；")}
        """.trimIndent()
        val content = request(prompt)
        val parsed = gson.fromJson(extractJson(content), TaskEnvelope::class.java)
        parsed.tasks
            .filter { it.title.isNotBlank() }
            .distinctBy { it.title.trim().lowercase() }
            .take(10)
            .map {
                AiGeneratedTask(
                    title = it.title.trim().take(80),
                    description = it.description.trim().take(300),
                    priority = runCatching { TaskPriority.valueOf(it.priority.uppercase()) }.getOrDefault(TaskPriority.MEDIUM)
                )
            }
    }

    private suspend fun request(prompt: String): String {
        val response = api.chat(
            authorization = "Bearer ${BuildConfig.DASHSCOPE_API_KEY}",
            request = ChatRequest(
                model = BuildConfig.QWEN_MODEL,
                messages = listOf(
                    ChatMessage("system", "你是一名严谨的软件项目规划助手。"),
                    ChatMessage("user", prompt)
                )
            )
        )
        return response.choices.firstOrNull()?.message?.content
            ?: error("AI 返回内容为空")
    }

    private fun requireConfigured() {
        check(isConfigured()) { "未配置 DASHSCOPE_API_KEY" }
    }

    private fun extractJson(text: String): String {
        val trimmed = text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        check(start >= 0 && end > start) { "AI 返回内容无法解析" }
        return trimmed.substring(start, end + 1)
    }
}
