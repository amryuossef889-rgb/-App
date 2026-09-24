package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.repository.BackgroundMode
import com.example.data.repository.BackgroundScale
import java.io.File

@Composable
fun AppBackground(
    mode: BackgroundMode,
    customPath: String?,
    opacity: Float,
    scale: BackgroundScale,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val contentScale = if (scale == BackgroundScale.FIT) ContentScale.Fit else ContentScale.Crop

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background.copy(alpha = 0.88f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        if (mode != BackgroundMode.DISABLED) {
            val bgModifier = Modifier.fillMaxSize()

            if (mode == BackgroundMode.CUSTOM && !customPath.isNullOrBlank()) {
                val file = File(customPath)
                if (file.exists()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(file)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = contentScale,
                        alpha = opacity * 0.72f,
                        modifier = bgModifier
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.default_background),
                        contentDescription = null,
                        contentScale = contentScale,
                        alpha = opacity * 0.72f,
                        modifier = bgModifier
                    )
                }
            } else {
                Image(
                    painter = painterResource(id = R.drawable.default_background),
                    contentDescription = null,
                    contentScale = contentScale,
                    alpha = opacity * 0.72f,
                    modifier = bgModifier
                )
            }

            // طبقة ضوء ناعمة فوق الخلفية لتكوين إحساس زجاجي وهادئ
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background.copy(alpha = 0.62f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.30f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.78f)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.035f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0f)
                            )
                        )
                    )
            )
        }

        content()
    }
}
