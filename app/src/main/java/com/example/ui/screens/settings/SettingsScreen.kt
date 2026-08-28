package com.example.ui.screens.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AppFontSize
import com.example.data.repository.BackgroundMode
import com.example.data.repository.BackgroundScale
import com.example.data.repository.ThemeMode
import java.io.File
import java.io.FileOutputStream

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()

    var showTimeDialog by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableIntStateOf(settings.reminderHour) }
    var selectedMinute by remember { mutableIntStateOf(settings.reminderMinute) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bgFile = File(context.filesDir, "custom_bg.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(bgFile).use { output ->
                        input.copyTo(output)
                    }
                }
                viewModel.setCustomBackgroundPath(bgFile.absolutePath)
                Toast.makeText(context, "تم تعيين الخلفية المخصصة بنجاح", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "تعذر حفظ الصورة", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Theme & Mode
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "المظهر ونمط الألوان",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = settings.themeMode == ThemeMode.SYSTEM,
                            onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                            label = { Text("تلقائي (النظام)") },
                            shape = RoundedCornerShape(10.dp)
                        )
                        FilterChip(
                            selected = settings.themeMode == ThemeMode.LIGHT,
                            onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                            label = { Text("☀️ فاتح") },
                            shape = RoundedCornerShape(10.dp)
                        )
                        FilterChip(
                            selected = settings.themeMode == ThemeMode.DARK,
                            onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
                            label = { Text("🌙 داكن") },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }

        // Section: Background Customization
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "خلفية التطبيق",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = settings.backgroundMode == BackgroundMode.DEFAULT,
                            onClick = { viewModel.setBackgroundMode(BackgroundMode.DEFAULT) },
                            label = { Text("الافتراضية") },
                            shape = RoundedCornerShape(10.dp)
                        )
                        FilterChip(
                            selected = settings.backgroundMode == BackgroundMode.CUSTOM,
                            onClick = { viewModel.setBackgroundMode(BackgroundMode.CUSTOM) },
                            label = { Text("مخصصة") },
                            shape = RoundedCornerShape(10.dp)
                        )
                        FilterChip(
                            selected = settings.backgroundMode == BackgroundMode.DISABLED,
                            onClick = { viewModel.setBackgroundMode(BackgroundMode.DISABLED) },
                            label = { Text("بدون خلفية") },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Pick Custom Image
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("اختيار صورة من معرض الهاتف")
                    }

                    if (settings.backgroundMode != BackgroundMode.DISABLED) {
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "شفافية الخلفية (${(settings.backgroundOpacity * 100).toInt()}%):",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Slider(
                            value = settings.backgroundOpacity,
                            onValueChange = { viewModel.setBackgroundOpacity(it) },
                            valueRange = 0.05f..0.8f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "مقياس العرض:",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = settings.backgroundScale == BackgroundScale.CROP,
                                    onClick = { viewModel.setBackgroundScale(BackgroundScale.CROP) },
                                    label = { Text("ملء الشاشة") },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                FilterChip(
                                    selected = settings.backgroundScale == BackgroundScale.FIT,
                                    onClick = { viewModel.setBackgroundScale(BackgroundScale.FIT) },
                                    label = { Text("احتواء") },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { viewModel.resetBackground() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("استعادة الخلفية الافتراضية")
                    }
                }
            }
        }

        // Section: Font Size
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.FormatSize, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "حجم خط النصوص والأحاديث",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = settings.fontSize == AppFontSize.SMALL,
                            onClick = { viewModel.setFontSize(AppFontSize.SMALL) },
                            label = { Text("صغير") },
                            shape = RoundedCornerShape(10.dp)
                        )
                        FilterChip(
                            selected = settings.fontSize == AppFontSize.MEDIUM,
                            onClick = { viewModel.setFontSize(AppFontSize.MEDIUM) },
                            label = { Text("متوسط (الافتراضي)") },
                            shape = RoundedCornerShape(10.dp)
                        )
                        FilterChip(
                            selected = settings.fontSize == AppFontSize.LARGE,
                            onClick = { viewModel.setFontSize(AppFontSize.LARGE) },
                            label = { Text("كبير") },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }

        // Section: Daily Notifications
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تنبيه سُنّة اليوم",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Switch(
                            checked = settings.reminderEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.updateReminder(enabled, settings.reminderHour, settings.reminderMinute)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    if (settings.reminderEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val formattedTime = String.format("%02d:%02d", settings.reminderHour, settings.reminderMinute)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "وقت التنبيه اليومي: $formattedTime",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            OutlinedButton(
                                onClick = {
                                    selectedHour = settings.reminderHour
                                    selectedMinute = settings.reminderMinute
                                    showTimeDialog = true
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تغيير الوقت")
                            }
                        }
                    }
                }
            }
        }

        // Section: About App
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "عن تطبيق «سُنّة»",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "تطبيق إسلامي أصيل (Native) يعمل بالكامل دون الحاجة للاتصال بالإنترنت (Offline First)، يهدف إلى إحياء السنن النبوية الشريفة وتطبيقها في الحياة اليومية بالتدرج من الأسهل إلى الأعلى التزاماً.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• مصادر البيانات: صحيح البخاري (7,277 حديثاً) وصحيح مسلم (7,459 حديثاً).\n• الخصوصية: لا حسابات، لا خوادم، لا تتبع، لا إعلانات.\n• الإصدار: 1.0.0 (Release)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    )
                }
            }
        }
    }

    // Time Selection Dialog
    if (showTimeDialog) {
        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            title = { Text("اختر وقت التنبيه اليومي") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${String.format("%02d", selectedHour)}:${String.format("%02d", selectedMinute)}",
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Hour Selector
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("الساعة (0-23)", style = MaterialTheme.typography.labelSmall)
                            Row {
                                OutlinedButton(onClick = { selectedHour = (selectedHour + 1) % 24 }) { Text("+") }
                                Spacer(modifier = Modifier.width(4.dp))
                                OutlinedButton(onClick = { selectedHour = if (selectedHour > 0) selectedHour - 1 else 23 }) { Text("-") }
                            }
                        }

                        // Minute Selector
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("الدقيقة (0-59)", style = MaterialTheme.typography.labelSmall)
                            Row {
                                OutlinedButton(onClick = { selectedMinute = (selectedMinute + 5) % 60 }) { Text("+") }
                                Spacer(modifier = Modifier.width(4.dp))
                                OutlinedButton(onClick = { selectedMinute = if (selectedMinute >= 5) selectedMinute - 5 else 55 }) { Text("-") }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateReminder(true, selectedHour, selectedMinute)
                        showTimeDialog = false
                    }
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimeDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
