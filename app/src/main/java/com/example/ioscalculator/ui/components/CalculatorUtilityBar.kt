package com.example.ioscalculator.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Панель вспомогательных кнопок: Настройки, История, Backspace.
 * Подходит для размещения над дисплеем калькулятора или между дисплее и сеткой кнопок.
 */
@Composable
fun CalculatorUtilityBar(
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ⚙️ Настройки
        IconButton(
            onClick = onSettingsClick,
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Настройки",
                modifier = Modifier.size(28.dp),
                tint = iconColor
            )
        }

        // 🕒 История операций
        IconButton(
            onClick = onHistoryClick,
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = "История операций",
                modifier = Modifier.size(28.dp),
                tint = iconColor
            )
        }

        // ⌫ Backspace (удаление последнего символа)
        IconButton(
            onClick = onBackspaceClick,
        ) {
            Icon(
                imageVector = Icons.Default.Backspace,
                contentDescription = "Удалить символ",
                modifier = Modifier.size(28.dp),
                tint = iconColor
            )
        }
    }
}
