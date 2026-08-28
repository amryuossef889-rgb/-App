package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
            .background(MaterialTheme.colorScheme.background)
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
                        alpha = opacity,
                        modifier = bgModifier
                    )
                } else {
                    // Fallback to default if file missing
                    Image(
                        painter = painterResource(id = R.drawable.default_background),
                        contentDescription = null,
                        contentScale = contentScale,
                        alpha = opacity,
                        modifier = bgModifier
                    )
                }
            } else {
                // Default background
                Image(
                    painter = painterResource(id = R.drawable.default_background),
                    contentDescription = null,
                    contentScale = contentScale,
                    alpha = opacity,
                    modifier = bgModifier
                )
            }

            // Subtle gradient overlay to enhance contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.70f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.90f)
                            )
                        )
                    )
            )
        }

        content()
    }
}
