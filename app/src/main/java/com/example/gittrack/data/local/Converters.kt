package com.example.gittrack.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter fun fromProjectStatus(value: ProjectStatus): String = value.name
    @TypeConverter fun toProjectStatus(value: String): ProjectStatus = ProjectStatus.valueOf(value)
    @TypeConverter fun fromTaskPriority(value: TaskPriority): String = value.name
    @TypeConverter fun toTaskPriority(value: String): TaskPriority = TaskPriority.valueOf(value)
    @TypeConverter fun fromTaskDateGroup(value: TaskDateGroup): String = value.name
    @TypeConverter fun toTaskDateGroup(value: String): TaskDateGroup = TaskDateGroup.valueOf(value)
}
