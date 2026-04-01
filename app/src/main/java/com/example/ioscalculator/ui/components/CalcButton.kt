package com.example.ioscalculator.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ioscalculator.domain.CalculatorConstants.PRESS_ANIM_DURATION
import com.example.ioscalculator.domain.CalculatorConstants.PRESS_SCALE
import com.example.ioscalculator.ui.theme.CalcColors

/**
 * Универсальная кнопка калькулятора.
 * Геометрия: круг через CircleShape, без Material-компонентов.
 * Анимация: масштаб 0.95 -> 1.0 при нажатии через animateFloatAsState.
 * Haptic: HapticFeedbackType.TextHandleMove — ближайший аналог click.
 *
 * @param label           Текст на кнопке
 * @param backgroundColor Цвет фона
 * @param textColor       Цвет текста
 * @param fontSize        Размер шрифта
 * @param size            Диаметр кнопки
 * @param widthMultiplier Множитель ширины (кнопка 0 = 2.0)
 * @param isActive        Инверсия цветов (активный оператор)
 * @param contentDesc     Описание для доступности
 * @param onClick         Обработчик нажатия
 */
@Composable
fun CalcButton(
    label: String,
    backgroundColor: Color,
    textColor: Color,
    fontSize: TextUnit = 30.sp,
    size: Dp = 80.dp,
    widthMultiplier: Float = 1f,
    isActive: Boolean = false,
    contentDesc: String = label,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    var pressed by remember { mutableStateOf(false) }

    // Анимация масштаба: scale = 0.95 при нажатии, 1.0 в покое
    val scale by animateFloatAsState(
        targetValue = if (pressed) PRESS_SCALE else 1f,
        animationSpec = tween(durationMillis = PRESS_ANIM_DURATION),
        label = "buttonScale",
    )

    val effectiveBg   = if (isActive) CalcColors.ActiveOpButton else backgroundColor
    val effectiveText = if (isActive) CalcColors.ActiveOpText   else textColor

    // 🔧 ИСПРАВЛЕНИЕ: явное приведение типов Dp -> Float -> Dp
    val width = if (widthMultiplier > 1f) {
        ((size.value * widthMultiplier) + ((widthMultiplier - 1f) * 8f)).dp
    } else {
        size
    }

    Box(
        modifier = Modifier
            .width(width)
            .height(size)
            .scale(scale)
            .clip(
                if (widthMultiplier > 1f) RoundedCornerShape(50)
                else CircleShape
            )
            .background(effectiveBg)
            .pointerInput(onClick) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        tryAwaitRelease()
                        pressed = false
                        onClick()
                    }
                )
            }
            .semantics { contentDescription = contentDesc },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = effectiveText,
            fontSize = fontSize,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center,
        )
    }
}
