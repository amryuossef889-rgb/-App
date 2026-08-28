package com.example.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DifficultyBadge
import com.example.ui.components.HadithCard
import com.example.ui.components.StreakCard
import com.example.ui.components.SunnahCard
import com.example.ui.theme.MinimalEmeraldAccent
import com.example.ui.theme.MinimalEmeraldBorder
import com.example.ui.theme.MinimalEmeraldLight
import com.example.ui.theme.MinimalEmeraldPrimary
import com.example.ui.theme.StreakOrangeBg
import com.example.ui.theme.StreakOrangeText

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSunnahDetail: (Int) -> Unit,
    onNavigateToSunnahList: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val currentOrder = uiState.currentSunnahWithHadith?.sunnah?.orderIndex ?: 1
    val currentStreak = uiState.userProgress?.currentStreak ?: 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Clean Minimalism Header with Day Number and Streak Pill
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "اليوم $currentOrder",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "سُنّة اليوم 🌿",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }

                // Minimal Streak Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(StreakOrangeBg)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🔥 $currentStreak أيام",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = StreakOrangeText
                        )
                    )
                }
            }
        }

        // Stats Grid: 2 Clean Minimalist Cards (Completed & Rate)
        item {
            StreakCard(
                currentStreak = currentStreak,
                longestStreak = uiState.userProgress?.longestStreak ?: 0,
                completedCount = uiState.completedSunnahIds.size,
                totalCount = uiState.totalSunnahsCount
            )
        }

        // Today's Sunnah Featured Card (Clean Minimalism Aesthetic)
        val todaySunnah = uiState.currentSunnahWithHadith
        if (todaySunnah != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .clickable { onNavigateToSunnahDetail(todaySunnah.sunnah.id) },
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Top accent stripe in Emerald
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(MinimalEmeraldAccent)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp)
                        ) {
                            // Header: Level Badge & Order #
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DifficultyBadge(difficulty = todaySunnah.sunnah.difficulty)

                                Text(
                                    text = String.format("#%03d", todaySunnah.sunnah.orderIndex),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Sunnah Title
                            Text(
                                text = todaySunnah.sunnah.title,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 30.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Sunnah Description
                            Text(
                                text = todaySunnah.sunnah.description,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 22.sp
                                )
                            )

                            // Authentic Hadith Callout Box with right emerald border
                            if (todaySunnah.hadith != null) {
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    // Right accent stripe
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .background(MinimalEmeraldAccent)
                                    )

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = "« ${todaySunnah.hadith.arabicText} »",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = FontFamily.Serif,
                                                fontWeight = FontWeight.Medium,
                                                lineHeight = 26.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = todaySunnah.hadith.narrator.ifEmpty { "حديث شريف" },
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                                )
                                            )

                                            Text(
                                                text = if (todaySunnah.hadith.collection == "SAHIH_BUKHARI") "صحيح البخاري" else "صحيح مسلم",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Action completion buttons (Full width, 54dp height, 16dp radius)
                            if (uiState.isTodayCompleted) {
                                OutlinedButton(
                                    onClick = { viewModel.toggleCurrentSunnah() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .testTag("completed_button"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "أنجزت السنة بنجاح ✅",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.toggleCurrentSunnah() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .testTag("mark_done_button"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "أنجزت السنة اليوم",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Next Sunnah Banner
        val upcomingFirst = uiState.upcomingSunnahs.firstOrNull()
        if (upcomingFirst != null) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clickable { onNavigateToSunnahDetail(upcomingFirst.sunnah.id) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "السنة التالية: ${upcomingFirst.sunnah.title}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "غداً بإذن الله",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }

        // Upcoming Next Sunnahs Section
        if (uiState.upcomingSunnahs.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📌 السنن القادمة في مسارك",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )

                    Text(
                        text = "عرض الكل",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable { onNavigateToSunnahList() }
                    )
                }
            }

            items(uiState.upcomingSunnahs, key = { it.sunnah.id }) { item ->
                SunnahCard(
                    sunnahWithHadith = item,
                    isCompleted = uiState.completedSunnahIds.contains(item.sunnah.id),
                    onToggleCompleted = { viewModel.toggleSunnah(item.sunnah.id) },
                    onClick = { onNavigateToSunnahDetail(item.sunnah.id) }
                )
            }
        }
    }
}

