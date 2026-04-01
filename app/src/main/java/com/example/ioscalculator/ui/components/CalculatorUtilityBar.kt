package com.example.ioscalculator.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CalculatorUtilityBar(
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Адаптивный приглушённый цвет (не зависит от CalcColors)
    val iconTint = LocalContentColor.current.copy(alpha = 0.6f)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Настройки",
                modifier = Modifier.size(26.dp),
                tint = iconTint
            )
        }
        IconButton(onClick = onHistoryClick) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = "История",
                modifier = Modifier.size(26.dp),
                tint = iconTint
            )
        }
        IconButton(onClick = onBackspaceClick) {
            // Delete есть в material-icons-core, работает сразу
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Удалить",
                modifier = Modifier.size(26.dp),
                tint = iconTint
            )
        }
    }
}
