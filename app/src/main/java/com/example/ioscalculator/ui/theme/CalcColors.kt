package com.example.ioscalculator.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Цветовая палитра — точное соответствие iOS 17 калькулятору.
 */
object CalcColors {
    val Background       = Color(0xFF000000)
    val FunctionButton   = Color(0xFF333333)
    val FunctionText     = Color(0xFFA5A5A5)
    val DigitButton      = Color(0xFF505050)
    val DigitText        = Color(0xFFFFFFFF)
    val OperatorButton   = Color(0xFFFF9F0A)
    val OperatorText     = Color(0xFFFFFFFF)
    // Активный оператор (инверсия)
    val ActiveOpButton   = Color(0xFFFFFFFF)
    val ActiveOpText     = Color(0xFFFF9F0A)
    // Дисплей
    val DisplayText      = Color(0xFFFFFFFF)
    // Научный режим — доп. кнопки
    val ScientificButton = Color(0xFF1C1C1C)
    val ScientificText   = Color(0xFFFFFFFF)
}
