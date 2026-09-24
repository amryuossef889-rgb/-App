package com.example.ui.screens.progress

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.UserProgress
import com.example.data.model.SunnahWithHadith

data class Achievement(
    val title: String,
    val description: String,
    val target: Int,
    val current: Int
) {
    val unlocked: Boolean get() = current >= target
}

@Composable
fun ProgressScreen(
    progress: UserProgress?,
    sunnahs: List<SunnahWithHadith>,
    modifier: Modifier = Modifier
) {
    val completed = com.example.data.repository.SunnahRepository
        .parseCompletedSunnahIds(progress?.completedSunnahs ?: "[]")
    val total = sunnahs.size.coerceAtLeast(1)
    val completedCount = completed.size
    val percentage = (completedCount.toFloat() / total.toFloat()).coerceIn(0f, 1f)

    val achievements = listOf(
        Achievement("بداية مباركة", "أنجز أول سُنّة في مسارك", 1, completedCount),
        Achievement("عشر سنن", "أنجز عشر سنن موثقة", 10, completedCount),
        Achievement("خمسون", "أنجز خمسين سُنّة", 50, completedCount),
        Achievement("مئة", "أنجز مئة سُنّة", 100, completedCount),
        Achievement("خمسمئة", "أنجز خمسمئة سُنّة", 500, completedCount),
        Achievement("ألف سُنّة", "أنجز ألف سُنّة موثقة", 1000, completedCount),
        Achievement("ثبات أسبوع", "حافظ على سلسلة سبعة أيام", 7, progress?.longestStreak ?: 0),
        Achievement("ثبات شهر", "حافظ على سلسلة ثلاثين يوماً", 30, progress?.longestStreak ?: 0)
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("رحلتك مع السُّنّة", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("${completedCount} من ${sunnahs.size} سُنّة موثقة", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(progress = { percentage }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Text("السلسلة الحالية: ${progress?.currentStreak ?: 0} يوم • أطول سلسلة: ${progress?.longestStreak ?: 0} يوم",
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Text("الإنجازات", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        items(achievements) { achievement ->
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (achievement.unlocked)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(achievement.title, fontWeight = FontWeight.Bold)
                        Text(if (achievement.unlocked) "مكتمل ✓" else "${achievement.current}/${achievement.target}",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(5.dp))
                    Text(achievement.description, style = MaterialTheme.typography.bodyMedium)
                    if (!achievement.unlocked) {
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (achievement.current.toFloat() / achievement.target).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
