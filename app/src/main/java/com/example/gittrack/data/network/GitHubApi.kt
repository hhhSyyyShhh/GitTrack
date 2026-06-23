package com.example.gittrack.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubApi {
    @GET("repos/{owner}/{repo}")
    suspend fun getRepository(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): GitHubRepoDto
}

data class GitHubRepoDto(
    @SerializedName("full_name") val fullName: String,
    val description: String?,
    val language: String?,
    @SerializedName("stargazers_count") val stars: Int,
    @SerializedName("forks_count") val forks: Int,
    @SerializedName("open_issues_count") val openIssues: Int,
    @SerializedName("html_url") val htmlUrl: String,
    @SerializedName("updated_at") val updatedAt: String?
)
