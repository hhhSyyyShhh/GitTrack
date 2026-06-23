package com.example.gittrack.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gittrack.data.local.ProjectStatus
import com.example.gittrack.data.local.ProjectWithProgress
import com.example.gittrack.data.local.TaskEntity
import com.example.gittrack.data.local.TaskPriority
import com.example.gittrack.ui.theme.GeekPalette

@Composable
fun SectionHeader(
    title: String,
    count: Int? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Surface(
                modifier = Modifier.size(30.dp),
                shape = RoundedCornerShape(9.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
                }
            }
            Spacer(Modifier.width(9.dp))
        }
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        if (count != null) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f)
            ) {
                Text(
                    "$count",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ProjectStatusChip(status: ProjectStatus, selected: Boolean = true, onClick: (() -> Unit)? = null) {
    val label = statusLabel(status)
    val icon = statusIcon(status)
    if (onClick != null) {
        FilterChip(
            selected = selected,
            onClick = onClick,
            label = { Text(label, fontWeight = FontWeight.Medium) },
            leadingIcon = {
                Icon(if (selected) Icons.Rounded.Check else icon, null, modifier = Modifier.size(18.dp))
            },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surface,
                selectedContainerColor = statusContainer(status),
                selectedLabelColor = statusContent(status),
                selectedLeadingIconColor = statusContent(status)
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selected,
                borderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    } else {
        Surface(
            shape = CircleShape,
            color = statusContainer(status),
            contentColor = statusContent(status)
        ) {
            Row(
                Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(5.dp))
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ProjectCard(item: ProjectWithProgress, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val project = item.project
    val accent = projectToneContent(project.tone)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(54.dp),
            shape = RoundedCornerShape(17.dp),
            color = projectToneColor(project.tone),
            border = BorderStroke(1.dp, accent.copy(alpha = 0.16f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    projectIcon(project.icon),
                    null,
                    tint = accent,
                    modifier = Modifier.size(27.dp)
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                project.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                project.summary,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(11.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { item.progressPercent / 100f },
                    modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.13f)
                )
                Spacer(Modifier.width(9.dp))
                Text(
                    "${item.completedCount}/${item.taskCount}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        ProjectStatusChip(project.status)
    }
}

@Composable
fun TaskRow(task: TaskEntity, projectName: String, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.secondary,
                checkmarkColor = MaterialTheme.colorScheme.onSecondary,
                uncheckedColor = priorityColor(task.priority).copy(alpha = 0.78f)
            )
        )
        Column(Modifier.weight(1f).padding(horizontal = 6.dp)) {
            Text(
                task.title,
                fontWeight = FontWeight.Medium,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(projectName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        PriorityPill(if (task.isCompleted) null else task.priority)
    }
}

@Composable
private fun PriorityPill(priority: TaskPriority?) {
    val foreground = when (priority) {
        TaskPriority.HIGH -> GeekPalette.Rose
        TaskPriority.MEDIUM -> GeekPalette.Amber
        TaskPriority.LOW -> GeekPalette.Cyan
        null -> MaterialTheme.colorScheme.secondary
    }
    val text = when (priority) {
        TaskPriority.HIGH -> "高"
        TaskPriority.MEDIUM -> "中"
        TaskPriority.LOW -> "低"
        null -> "完成"
    }
    val background = foreground.copy(alpha = 0.14f)
    Surface(shape = CircleShape, color = background, contentColor = foreground) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, message: String, action: (@Composable () -> Unit)? = null) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(38.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Surface(
                modifier = Modifier.size(76.dp),
                shape = RoundedCornerShape(25.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(37.dp))
                }
            }
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
            action?.invoke()
        }
    }
}

fun statusLabel(status: ProjectStatus): String = when(status) {
    ProjectStatus.IDEA -> "构思中"
    ProjectStatus.IN_PROGRESS -> "开发中"
    ProjectStatus.PAUSED -> "已暂停"
    ProjectStatus.COMPLETED -> "已完成"
}

fun statusIcon(status: ProjectStatus): ImageVector = when(status) {
    ProjectStatus.IDEA -> Icons.Rounded.Lightbulb
    ProjectStatus.IN_PROGRESS -> Icons.Rounded.Code
    ProjectStatus.PAUSED -> Icons.Rounded.PauseCircle
    ProjectStatus.COMPLETED -> Icons.Rounded.TaskAlt
}

fun projectIcon(icon: String): ImageVector = when(icon) {
    "public" -> Icons.Rounded.Public
    "rocket_launch" -> Icons.Rounded.RocketLaunch
    "school" -> Icons.Rounded.School
    else -> Icons.Rounded.Folder
}

@Composable
private fun priorityColor(priority: TaskPriority): Color = when (priority) {
    TaskPriority.HIGH -> GeekPalette.Rose
    TaskPriority.MEDIUM -> GeekPalette.Amber
    TaskPriority.LOW -> GeekPalette.Cyan
}

@Composable
private fun statusContainer(status: ProjectStatus): Color = statusContent(status).copy(alpha = 0.14f)

@Composable
private fun statusContent(status: ProjectStatus): Color = when(status) {
    ProjectStatus.IDEA -> GeekPalette.Violet
    ProjectStatus.IN_PROGRESS -> GeekPalette.ElectricBlue
    ProjectStatus.PAUSED -> GeekPalette.Amber
    ProjectStatus.COMPLETED -> GeekPalette.Emerald
}

@Composable
private fun projectToneColor(tone: String): Color = projectToneContent(tone).copy(alpha = 0.14f)

@Composable
private fun projectToneContent(tone: String): Color = when(tone) {
    "purple" -> GeekPalette.Violet
    "orange" -> GeekPalette.Amber
    "green" -> GeekPalette.Emerald
    "cyan" -> GeekPalette.Cyan
    else -> GeekPalette.ElectricBlue
}
