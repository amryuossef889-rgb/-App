package com.example.ui.screens.welcome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun WelcomeScreen(onContinue: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                            )
                        )
                    )
                    .then(
                        Modifier.background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.10f),
                            RoundedCornerShape(28.dp)
                        )
                    )
                    .padding(26.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "شعار سُنّة",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(68.dp)
                )
            }

            Spacer(Modifier.height(22.dp))
            Text(
                text = "أهلاً بك في سُنّة",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "رفيقك اليومي للتعرّف على السنن النبوية ومتابعة تطبيقها بخطوات بسيطة.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(26.dp))
            FeatureCard(
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                title = "سنن موثقة وتفاصيلها",
                description = "تصفّح السنن والأحاديث المرتبطة بها."
            )
            Spacer(Modifier.height(12.dp))
            FeatureCard(
                icon = { Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                title = "متابعة وتذكيرات",
                description = "تابع إنجازك واضبط التذكيرات من الإعدادات."
            )
            Spacer(Modifier.height(26.dp))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("ابدأ رحلتك", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
private fun FeatureCard(
    icon: @Composable () -> Unit,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.32f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .padding(10.dp)
            ) { icon() }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
