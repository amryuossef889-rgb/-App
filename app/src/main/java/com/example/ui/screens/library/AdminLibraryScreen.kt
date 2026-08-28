package com.example.ui.screens.library

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Composable
fun AdminLibraryScreen(
    viewModel: LibraryViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var bookTitleInput by remember { mutableStateOf("") }
    var bookDescInput by remember { mutableStateOf("") }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            bookTitleInput = ""
            bookDescInput = ""
            showAddDialog = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_library_screen")
    ) {
        // Admin Header Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🛡️ لوحة إدارة الكتب (PDF)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            OutlinedButton(
                onClick = {
                    viewModel.logoutAdmin()
                    onNavigateBack()
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("قفل الإدارة")
            }
        }

        // Add Book Button
        Button(
            onClick = { pdfPickerLauncher.launch("application/pdf") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("إضافة كتاب جديد من الجهاز (PDF)")
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.books.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لم يتم إضافة أي كتب بعد. اضغط على الزر أعلاه لإضافة كتاب PDF.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.books, key = { it.id }) { book ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = book.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                if (book.description.isNotBlank()) {
                                    Text(
                                        text = book.description,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }

                            if (!book.isBuiltin) {
                                IconButton(
                                    onClick = {
                                        // Delete file from internal storage
                                        val file = File(File(context.filesDir, "pdfs"), book.filename)
                                        if (file.exists()) file.delete()
                                        viewModel.deleteBook(book.id)
                                        Toast.makeText(context, "تم حذف الكتاب", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "حذف الكتاب",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Book Info Dialog
    if (showAddDialog && selectedUri != null) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("معلومات الكتاب المضاف") },
            text = {
                Column {
                    OutlinedTextField(
                        value = bookTitleInput,
                        onValueChange = { bookTitleInput = it },
                        label = { Text("عنوان الكتاب *") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = bookDescInput,
                        onValueChange = { bookDescInput = it },
                        label = { Text("وصف مختصر أو المؤلف") },
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (bookTitleInput.isNotBlank()) {
                            try {
                                val pdfDir = File(context.filesDir, "pdfs").apply { mkdirs() }
                                val filename = "pdf_${UUID.randomUUID()}.pdf"
                                val destFile = File(pdfDir, filename)

                                context.contentResolver.openInputStream(selectedUri!!)?.use { input ->
                                    FileOutputStream(destFile).use { output ->
                                        input.copyTo(output)
                                    }
                                }

                                val fileSize = destFile.length()
                                viewModel.addBook(
                                    title = bookTitleInput.trim(),
                                    description = bookDescInput.trim(),
                                    filename = filename,
                                    size = fileSize
                                )

                                showAddDialog = false
                                Toast.makeText(context, "تم حفظ الكتاب بنجاح", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "فشل نسخ الملف: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
