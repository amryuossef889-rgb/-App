package com.example.ui.screens.favorites

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.repository.SettingsRepository
import com.example.data.repository.SunnahRepository
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(
    settingsRepository: SettingsRepository,
    sunnahRepository: SunnahRepository,
    onOpenSunnah: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val favoriteIds by settingsRepository.favoriteSunnahIdsFlow.collectAsState(initial = emptySet())
    val allSunnahs by sunnahRepository.getAllSunnahsWithHadith().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val favorites = allSunnahs.filter { it.sunnah.id in favoriteIds }

    if (favorites.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("☆", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(12.dp))
                Text("مفضلتك تبدأ من هنا", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "افتح أي سُنّة واضغط «إضافة إلى المفضلة» لتجدها هنا.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "السنن المحفوظة (${favorites.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        items(favorites, key = { it.sunnah.id }) { saved ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onOpenSunnah(saved.sunnah.id) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = saved.sunnah.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = saved.sunnah.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                scope.launch { settingsRepository.toggleFavoriteSunnah(saved.sunnah.id) }
                            }
                        ) {
                            Text("إزالة من المفضلة")
                        }
                    }
                }
            }
        }
    }
}
