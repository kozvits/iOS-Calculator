package com.example.ioscalculator.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ioscalculator.ui.theme.CalcColors

/**
 * Панель дисплея:
 * - Выравнивание по правому краю
 * - Авто-уменьшение шрифта при длинных строках (от 96 до 40 sp)
 * - Цвет текста: CalcColors.DisplayText
 */
@Composable
fun DisplayPanel(
    text: String,
    modifier: Modifier = Modifier,
) {
    // Авто-масштаб: чем длиннее строка, тем меньше шрифт
    val fontSize = when {
        text.length <= 6  -> 96.sp
        text.length <= 9  -> 80.sp
        text.length <= 12 -> 60.sp
        else              -> 40.sp
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomEnd,
    ) {
        BasicText(
            text = text,
            style = TextStyle(
                color = CalcColors.DisplayText,
                fontSize = fontSize,
                textAlign = TextAlign.End,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
