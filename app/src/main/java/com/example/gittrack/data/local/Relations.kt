package com.example.gittrack.data.local

import androidx.room.Embedded

data class ProjectWithProgress(
    @Embedded val project: ProjectEntity,
    val taskCount: Long,
    val completedCount: Long
) {
    val progressPercent: Int
        get() = if (taskCount == 0L) 0 else ((completedCount * 100) / taskCount).toInt().coerceIn(0, 100)
}
