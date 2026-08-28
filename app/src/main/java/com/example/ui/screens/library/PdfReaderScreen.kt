package com.example.ui.screens.library

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PdfReaderScreen(
    bookId: Int,
    title: String,
    filename: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentPageIndex by remember { mutableIntStateOf(0) }
    var totalPages by remember { mutableIntStateOf(0) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Zoom and pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    var showJumpDialog by remember { mutableStateOf(false) }
    var jumpPageInput by remember { mutableStateOf("") }

    var pdfRenderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var parcelFileDescriptor by remember { mutableStateOf<ParcelFileDescriptor?>(null) }

    // Initialize PdfRenderer
    LaunchedEffect(filename) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(File(context.filesDir, "pdfs"), filename)
                if (!file.exists()) {
                    errorMessage = "الملف غير موجود على الجهاز"
                    isLoading = false
                    return@withContext
                }

                val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)

                parcelFileDescriptor = pfd
                pdfRenderer = renderer
                totalPages = renderer.pageCount
                currentPageIndex = 0
                isLoading = false
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "تعذر فتح ملف PDF: ${e.localizedMessage}"
                isLoading = false
            }
        }
    }

    // Render current page when currentPageIndex changes
    LaunchedEffect(currentPageIndex, pdfRenderer) {
        val renderer = pdfRenderer ?: return@LaunchedEffect
        if (totalPages == 0) return@LaunchedEffect

        withContext(Dispatchers.IO) {
            try {
                val page = renderer.openPage(currentPageIndex)
                val width = page.width * 2
                val height = page.height * 2
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                currentBitmap = bitmap
                scale = 1f
                offsetX = 0f
                offsetY = 0f
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Clean up
    DisposableEffect(Unit) {
        onDispose {
            try {
                pdfRenderer?.close()
                parcelFileDescriptor?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("pdf_reader_screen")
    ) {
        // PDF Canvas Viewer
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF2C2C2C))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.8f, 4f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(24.dp)
                )
            } else if (currentBitmap != null) {
                Image(
                    bitmap = currentBitmap!!.asImageBitmap(),
                    contentDescription = "صفحة ${currentPageIndex + 1}",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                )
            }
        }

        // Bottom Controls Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Zoom Controls
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { scale = (scale + 0.25f).coerceAtMost(4f) }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "تكبير")
                        }
                        IconButton(onClick = { scale = (scale - 0.25f).coerceAtLeast(0.8f) }) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "تصغير")
                        }
                        IconButton(onClick = { scale = 1f; offsetX = 0f; offsetY = 0f }) {
                            Icon(imageVector = Icons.Default.RestartAlt, contentDescription = "إعادة ضبط")
                        }
                    }

                    // Page indicator (Clickable to jump)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable {
                                jumpPageInput = "${currentPageIndex + 1}"
                                showJumpDialog = true
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "صفحة ${currentPageIndex + 1} من $totalPages",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }

                    // Navigation Arrows (Next / Prev)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (currentPageIndex > 0) currentPageIndex--
                            },
                            enabled = currentPageIndex > 0
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "الصفحة السابقة"
                            )
                        }

                        IconButton(
                            onClick = {
                                if (currentPageIndex < totalPages - 1) currentPageIndex++
                            },
                            enabled = currentPageIndex < totalPages - 1
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "الصفحة التالية"
                            )
                        }
                    }
                }
            }
        }
    }

    // Jump to Page Dialog
    if (showJumpDialog) {
        AlertDialog(
            onDismissRequest = { showJumpDialog = false },
            title = { Text("الانتقال إلى صفحة") },
            text = {
                OutlinedTextField(
                    value = jumpPageInput,
                    onValueChange = { jumpPageInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("رقم الصفحة (1 - $totalPages)") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pageNum = jumpPageInput.toIntOrNull()
                        if (pageNum != null && pageNum in 1..totalPages) {
                            currentPageIndex = pageNum - 1
                        }
                        showJumpDialog = false
                    }
                ) {
                    Text("انتقال")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJumpDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
