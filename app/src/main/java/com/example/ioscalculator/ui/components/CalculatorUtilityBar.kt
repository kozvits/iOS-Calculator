package com.example.ioscalculator.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ioscalculator.ui.theme.CalcColors

@Composable
fun CalculatorUtilityBar(
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconColor: Color = CalcColors.ButtonTextGray
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ⚙️ Настройки
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Настройки",
                modifier = Modifier.size(26.dp),
                tint = iconColor
            )
        }
        // 🕒 История
        IconButton(onClick = onHistoryClick) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = "История",
                modifier = Modifier.size(26.dp),
                tint = iconColor
            )
        }
        // ⌫ Удаление
        IconButton(onClick = onBackspaceClick) {
            Icon(
                imageVector = Icons.Default.Backspace,
                contentDescription = "Удалить",
                modifier = Modifier.size(26.dp),
                tint = iconColor
            )
        }
    }
}
