package com.example.ui.screens.sunnahs

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CategoryChip
import com.example.ui.components.SunnahCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SunnahListScreen(
    viewModel: SunnahListViewModel,
    onNavigateToSunnahDetail: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val difficultyTabs = listOf(
        0 to "الكل (100)",
        1 to "مستوى 1: سهل جداً",
        2 to "مستوى 2: سهل",
        3 to "مستوى 3: متوسط",
        4 to "مستوى 4: التزام",
        5 to "مستوى 5: مجاهدة"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("sunnah_list_screen")
    ) {
        // Search Input
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            placeholder = { Text("ابحث في قائمة السنن الـ 100...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "مسح")
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
        )

        // Difficulty Tabs
        ScrollableTabRow(
            selectedTabIndex = difficultyTabs.indexOfFirst { it.first == uiState.selectedDifficulty }.coerceAtLeast(0),
            edgePadding = 16.dp,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                val index = difficultyTabs.indexOfFirst { it.first == uiState.selectedDifficulty }.coerceAtLeast(0)
                if (index < tabPositions.size) {
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                }
            },
            divider = {}
        ) {
            difficultyTabs.forEach { (diff, label) ->
                val isSelected = uiState.selectedDifficulty == diff
                Tab(
                    selected = isSelected,
                    onClick = { viewModel.setDifficulty(diff) },
                    text = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Status filter & Category chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Chips
            FilterChip(
                selected = uiState.selectedStatus == StatusFilter.ALL,
                onClick = { viewModel.setStatus(StatusFilter.ALL) },
                label = { Text("جميع الحالات") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(end = 6.dp)
            )
            FilterChip(
                selected = uiState.selectedStatus == StatusFilter.UNCOMPLETED,
                onClick = { viewModel.setStatus(StatusFilter.UNCOMPLETED) },
                label = { Text("غير المنجز") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(end = 6.dp)
            )
            FilterChip(
                selected = uiState.selectedStatus == StatusFilter.COMPLETED,
                onClick = { viewModel.setStatus(StatusFilter.COMPLETED) },
                label = { Text("المنجز") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(end = 12.dp)
            )

            // Category Chips
            uiState.categories.forEach { category ->
                CategoryChip(
                    category = category,
                    isSelected = uiState.selectedCategory == category,
                    onSelected = { viewModel.setCategory(it) }
                )
            }
        }

        // Result Count
        Text(
            text = "عرض ${uiState.sunnahs.size} سُنّة",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        // Sunnah List
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.sunnahs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لا توجد سنن مطابقة للتصفية",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.sunnahs, key = { it.sunnah.id }) { item ->
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
}
