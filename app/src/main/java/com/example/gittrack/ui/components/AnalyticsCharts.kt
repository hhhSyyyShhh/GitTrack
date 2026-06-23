package com.example.gittrack.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gittrack.data.local.ProjectStatus
import com.example.gittrack.data.local.ProjectWithProgress
import com.example.gittrack.data.local.TaskDateGroup
import com.example.gittrack.data.local.TaskEntity
import com.example.gittrack.data.local.TaskPriority
import com.example.gittrack.ui.theme.GeekPalette

private data class ChartSlice(
    val label: String,
    val value: Int,
    val color: Color
)

/**
 * “我的”页面的项目状态环形图。数据完全来自 Room 返回的项目列表。
 */
@Composable
fun ProjectStatusDonutCard(
    projects: List<ProjectWithProgress>,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val slices = listOf(
        ChartSlice("开发中", projects.count { it.project.status == ProjectStatus.IN_PROGRESS }, GeekPalette.ElectricBlue),
        ChartSlice("构思中", projects.count { it.project.status == ProjectStatus.IDEA }, GeekPalette.Violet),
        ChartSlice("已暂停", projects.count { it.project.status == ProjectStatus.PAUSED }, GeekPalette.Amber),
        ChartSlice("已完成", projects.count { it.project.status == ProjectStatus.COMPLETED }, GeekPalette.Emerald)
    )
    val total = slices.sumOf { it.value }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text("项目状态分布", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("查看当前项目所处阶段", color = scheme.onSurfaceVariant, fontSize = 13.sp)
            }

            if (total == 0) {
                Text("暂无项目数据", color = scheme.onSurfaceVariant)
            } else {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    Box(Modifier.size(138.dp), contentAlignment = Alignment.Center) {
                        Canvas(Modifier.size(128.dp)) {
                            val stroke = 20.dp.toPx()
                            val inset = stroke / 2f
                            var start = -90f
                            slices.filter { it.value > 0 }.forEach { slice ->
                                val sweep = 360f * slice.value / total.toFloat()
                                drawArc(
                                    color = slice.color,
                                    startAngle = start,
                                    sweepAngle = sweep.coerceAtLeast(2f),
                                    useCenter = false,
                                    topLeft = Offset(inset, inset),
                                    size = Size(size.width - stroke, size.height - stroke),
                                    style = Stroke(width = stroke, cap = StrokeCap.Butt)
                                )
                                start += sweep
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(total.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            Text("项目", fontSize = 12.sp, color = scheme.onSurfaceVariant)
                        }
                    }

                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        slices.forEach { slice ->
                            ChartLegendRow(slice.label, slice.value, slice.color)
                        }
                    }
                }
            }
        }
    }
}

/**
 * “我的”页面任务分布图。显示今日、即将到期、以后和无截止日期的未完成任务。
 */
@Composable
fun TaskDistributionCard(
    tasks: List<TaskEntity>,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val values = listOf(
        Triple("今天", tasks.count { !it.isCompleted && it.dateGroup == TaskDateGroup.TODAY }, GeekPalette.Rose),
        Triple("即将到期", tasks.count { !it.isCompleted && it.dateGroup == TaskDateGroup.UPCOMING }, GeekPalette.Amber),
        Triple("以后", tasks.count { !it.isCompleted && it.dateGroup == TaskDateGroup.LATER }, GeekPalette.ElectricBlue),
        Triple("无截止时间", tasks.count { !it.isCompleted && it.dateGroup == TaskDateGroup.NONE }, GeekPalette.Cyan)
    )
    val maximum = values.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Column {
                Text("待办任务分布", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("按截止时间查看当前任务压力", color = scheme.onSurfaceVariant, fontSize = 13.sp)
            }
            values.forEach { (label, count, color) ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("$count 条", fontSize = 12.sp, color = scheme.onSurfaceVariant)
                    }
                    LinearProgressIndicator(
                        progress = { count / maximum.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(9.dp),
                        color = color,
                        trackColor = color.copy(alpha = 0.14f),
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

/**
 * 项目详情页的任务结构可视化，不额外存储统计字段，避免与 Room 数据不一致。
 */
@Composable
fun ProjectTaskBreakdownCard(
    tasks: List<TaskEntity>,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val total = tasks.size.coerceAtLeast(1)
    val rows = listOf(
        Triple("已完成", tasks.count { it.isCompleted }, GeekPalette.Emerald),
        Triple("待完成", tasks.count { !it.isCompleted }, GeekPalette.ElectricBlue),
        Triple("高优先级", tasks.count { !it.isCompleted && it.priority == TaskPriority.HIGH }, GeekPalette.Rose)
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Text("任务结构", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            rows.forEach { (label, count, color) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(Modifier.size(10.dp), shape = CircleShape, color = color) {}
                    Spacer(Modifier.width(9.dp))
                    Text(label, modifier = Modifier.width(72.dp), fontSize = 13.sp)
                    LinearProgressIndicator(
                        progress = { count / total.toFloat() },
                        modifier = Modifier.weight(1f).height(8.dp),
                        color = color,
                        trackColor = color.copy(alpha = 0.14f),
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(count.toString(), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ChartLegendRow(label: String, value: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(Modifier.size(10.dp), shape = CircleShape, color = color) {}
        Spacer(Modifier.width(8.dp))
        Text(label, modifier = Modifier.weight(1f), fontSize = 13.sp)
        Text(value.toString(), fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
