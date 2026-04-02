package com.example.ioscalculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .background(Color(0xFF1C1C1E))
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Заголовок с кнопкой закрытия
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Настройки",
                        color = Color.White,
                        fontSize = 20.sp,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = Color.White,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Опции настроек (как в iOS калькуляторе)
                SettingsSection(title = "Формат чисел") {
                    SettingsItem(text = "Разделитель разрядов: апостроф (')")
                    SettingsItem(text = "Десятичный разделитель: точка")
                }

                Spacer(Modifier.height(16.dp))

                SettingsSection(title = "Режим углов") {
                    SettingsItem(text = "Радианы (RAD)")
                    SettingsItem(text = "Градусы (DEG)")
                }

                Spacer(Modifier.height(16.dp))

                SettingsSection(title = "О приложении") {
                    SettingsItem(text = "iOS Calculator Clone")
                    SettingsItem(text = "Версия: 1.0")
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = title,
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        content()
    }
}

@Composable
private fun SettingsItem(
    text: String,
    onClick: (() -> Unit)? = null,
) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 16.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
    )
}
