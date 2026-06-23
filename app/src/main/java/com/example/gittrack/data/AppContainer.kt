package com.example.gittrack.data

import android.content.Context
import androidx.room.Room
import com.example.gittrack.BuildConfig
import com.example.gittrack.data.local.GitTrackDatabase
import com.example.gittrack.data.network.GitHubApi
import com.example.gittrack.data.network.QwenApi
import com.example.gittrack.data.preferences.UserPreferencesRepository
import com.example.gittrack.data.repository.AiRepository
import com.example.gittrack.data.repository.ProjectRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(
        context.applicationContext,
        GitTrackDatabase::class.java,
        "gittrack.db"
    ).fallbackToDestructiveMigration().build()

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    private fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val githubApi = retrofit("https://api.github.com/").create(GitHubApi::class.java)
    private val qwenApi = retrofit(BuildConfig.DASHSCOPE_BASE_URL).create(QwenApi::class.java)

    val projectRepository = ProjectRepository(
        database = database,
        projectDao = database.projectDao(),
        taskDao = database.taskDao(),
        cacheDao = database.githubCacheDao(),
        githubApi = githubApi
    )
    val aiRepository = AiRepository(qwenApi)
    val preferencesRepository = UserPreferencesRepository(context.applicationContext)
}
