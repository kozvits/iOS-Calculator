package com.example.ioscalculator.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CalculatorUtilityBar(
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 🔧 Жесткий видимый цвет (серый, как в iOS-калькуляторе)
    val iconTint = Color(0xFF999999)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Настройки",
                modifier = Modifier.size(28.dp),
                tint = iconTint
            )
        }
        IconButton(onClick = onHistoryClick) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = "История",
                modifier = Modifier.size(28.dp),
                tint = iconTint
            )
        }
        IconButton(onClick = onBackspaceClick) {
            Icon(
                imageVector = Icons.Rounded.Backspace,
                contentDescription = "Удалить",
                modifier = Modifier.size(28.dp),
                tint = iconTint
            )
        }
    }
}
