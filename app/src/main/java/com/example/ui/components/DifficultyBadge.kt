package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DifficultyLevel1
import com.example.ui.theme.DifficultyLevel2
import com.example.ui.theme.DifficultyLevel3
import com.example.ui.theme.DifficultyLevel4
import com.example.ui.theme.DifficultyLevel5

@Composable
fun DifficultyBadge(
    difficulty: Int,
    modifier: Modifier = Modifier
) {
    val (label, color) = when (difficulty) {
        1 -> "مستوى ١ • سهل جدًا" to DifficultyLevel1
        2 -> "مستوى ٢ • سهل" to DifficultyLevel2
        3 -> "مستوى ٣ • متوسط" to DifficultyLevel3
        4 -> "مستوى ٤ • يحتاج التزامًا" to DifficultyLevel4
        5 -> "مستوى ٥ • متقدم" to DifficultyLevel5
        else -> "مستوى $difficulty" to DifficultyLevel3
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            color = color
        )
    }
}

