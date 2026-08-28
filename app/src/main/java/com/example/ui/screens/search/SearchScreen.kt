package com.example.ui.screens.search

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
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.HadithCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val suggestedKeywords = listOf(
        "النية", "الصلاة", "الاستغفار", "الصدقة", "الجنة", "الأخلاق", "الوالدين", "الوتر", "الذكر"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("search_screen")
    ) {
        // Search Input
        OutlinedTextField(
            value = uiState.query,
            onValueChange = { viewModel.setQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            placeholder = { Text("ابحث في 14,736 حديثاً (كلمة، رقم، راوٍ)...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (uiState.query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setQuery("") }) {
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

        // Collection Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = uiState.selectedCollection == "ALL",
                onClick = { viewModel.setCollection("ALL") },
                label = { Text("جميع الأحاديث (صحيح البخاري ومسلم)") },
                shape = RoundedCornerShape(12.dp)
            )

            FilterChip(
                selected = uiState.selectedCollection == "SAHIH_BUKHARI",
                onClick = { viewModel.setCollection("SAHIH_BUKHARI") },
                label = { Text("صحيح البخاري (7,277)") },
                shape = RoundedCornerShape(12.dp)
            )

            FilterChip(
                selected = uiState.selectedCollection == "SAHIH_MUSLIM",
                onClick = { viewModel.setCollection("SAHIH_MUSLIM") },
                label = { Text("صحيح مسلم (7,459)") },
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Suggested Keywords when query is empty
        if (uiState.query.isEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "💡 كلمات بحث شائعة:",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                suggestedKeywords.forEach { keyword ->
                    SuggestionChip(
                        onClick = { viewModel.setQuery(keyword) },
                        label = { Text(keyword) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "محرك بحث الأحاديث النبوية الموثقة",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ابحث بالكلمة أو برقم الحديث في صحيح البخاري وصحيح مسلم دون اتصال بالإنترنت.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            // Result Count
            Text(
                text = "تم العثور على ${uiState.results.size} حديث",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )

            if (uiState.results.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لم يتم العثور على نتائج للبحث «${uiState.query}»",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(uiState.results, key = { "${it.collection}_${it.id}" }) { hadith ->
                        HadithCard(hadith = hadith)
                    }
                }
            }
        }
    }
}
