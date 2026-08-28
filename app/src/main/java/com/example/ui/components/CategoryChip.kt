package com.example.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CategoryChip(
    category: String,
    isSelected: Boolean,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = { onSelected(category) },
        label = {
            Text(
                text = category,
                style = MaterialTheme.typography.labelMedium
            )
        },
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier.padding(end = 6.dp)
    )
}
