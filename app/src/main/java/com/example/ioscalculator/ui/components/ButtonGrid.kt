package com.example.ioscalculator.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ioscalculator.domain.BinaryOp
import com.example.ioscalculator.domain.CalculatorConstantValue
import com.example.ioscalculator.domain.ScientificFunc
import com.example.ioscalculator.domain.AngleMode
import com.example.ioscalculator.state.CalculatorEvent
import com.example.ioscalculator.state.CalculatorState.Active
import com.example.ioscalculator.ui.theme.CalcColors

private const val GAP_DP = 8

/**
 * Портретная сетка 4x5: функции, цифры, операторы.
 */
@Composable
fun PortraitButtonGrid(
    state: Active,
    buttonSize: Dp,
    onEvent: (CalculatorEvent) -> Unit,
) {
    val clearLabel = if (!state.hasInput && state.pendingOp == null) "AC" else "C"

    Column(verticalArrangement = Arrangement.spacedBy(GAP_DP.dp)) {

        // Строка 1: AC/C  +/-  %  ÷
        Row(horizontalArrangement = Arrangement.spacedBy(GAP_DP.dp)) {
            CalcButton(clearLabel, CalcColors.FunctionButton, CalcColors.FunctionText,
                size = buttonSize) { onEvent(CalculatorEvent.ClearPressed) }
            CalcButton("+/-", CalcColors.FunctionButton, CalcColors.FunctionText,
                size = buttonSize) { onEvent(CalculatorEvent.NegatePressed) }
            CalcButton("%", CalcColors.FunctionButton, CalcColors.FunctionText,
                size = buttonSize) { onEvent(CalculatorEvent.PercentPressed) }
            CalcButton("÷", CalcColors.OperatorButton, CalcColors.OperatorText,
                fontSize = 32.sp, size = buttonSize,
                isActive = state.activeOp == BinaryOp.DIVIDE) {
                onEvent(CalculatorEvent.OperatorPressed(BinaryOp.DIVIDE))
            }
        }

        // Строка 2: 7  8  9  ×
        Row(horizontalArrangement = Arrangement.spacedBy(GAP_DP.dp)) {
            listOf("7", "8", "9").forEach { d ->
                CalcButton(d, CalcColors.DigitButton, CalcColors.DigitText, size = buttonSize) {
                    onEvent(CalculatorEvent.DigitPressed(d))
                }
            }
            CalcButton("×", CalcColors.OperatorButton, CalcColors.OperatorText,
                fontSize = 32.sp, size = buttonSize,
                isActive = state.activeOp == BinaryOp.MULTIPLY) {
                onEvent(CalculatorEvent.OperatorPressed(BinaryOp.MULTIPLY))
            }
        }

        // Строка 3: 4  5  6  −
        Row(horizontalArrangement = Arrangement.spacedBy(GAP_DP.dp)) {
            listOf("4", "5", "6").forEach { d ->
                CalcButton(d, CalcColors.DigitButton, CalcColors.DigitText, size = buttonSize) {
                    onEvent(CalculatorEvent.DigitPressed(d))
                }
            }
            CalcButton("−", CalcColors.OperatorButton, CalcColors.OperatorText,
                fontSize = 32.sp, size = buttonSize,
                isActive = state.activeOp == BinaryOp.SUBTRACT) {
                onEvent(CalculatorEvent.OperatorPressed(BinaryOp.SUBTRACT))
            }
        }

        // Строка 4: 1  2  3  +
        Row(horizontalArrangement = Arrangement.spacedBy(GAP_DP.dp)) {
            listOf("1", "2", "3").forEach { d ->
                CalcButton(d, CalcColors.DigitButton, CalcColors.DigitText, size = buttonSize) {
                    onEvent(CalculatorEvent.DigitPressed(d))
                }
            }
            CalcButton("+", CalcColors.OperatorButton, CalcColors.OperatorText,
                fontSize = 32.sp, size = buttonSize,
                isActive = state.activeOp == BinaryOp.ADD) {
                onEvent(CalculatorEvent.OperatorPressed(BinaryOp.ADD))
            }
        }

        // Строка 5: 0 (wide)  .  =
        Row(
            horizontalArrangement = Arrangement.spacedBy(GAP_DP.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CalcButton("0", CalcColors.DigitButton, CalcColors.DigitText,
                size = buttonSize, widthMultiplier = 2f) {
                onEvent(CalculatorEvent.DigitPressed("0"))
            }
            CalcButton(".", CalcColors.DigitButton, CalcColors.DigitText, size = buttonSize) {
                onEvent(CalculatorEvent.DecimalPressed)
            }
            CalcButton("=", CalcColors.OperatorButton, CalcColors.OperatorText,
                fontSize = 32.sp, size = buttonSize) {
                onEvent(CalculatorEvent.EqualsPressed)
            }
        }
    }
}

/**
 * Ландшафтная сетка: научные функции слева + базовая раскладка справа.
 * Размер кнопок адаптируется к высоте экрана.
 */
@Composable
fun LandscapeButtonGrid(
    state: Active,
    buttonSize: Dp,
    onEvent: (CalculatorEvent) -> Unit,
) {
    val clearLabel = if (!state.hasInput && state.pendingOp == null) "AC" else "C"
    val angleLabel = if (state.angleMode == AngleMode.RAD) "RAD" else "DEG"

    Row(horizontalArrangement = Arrangement.spacedBy(GAP_DP.dp)) {

        // ── Левая панель: научные функции ─────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(GAP_DP.dp)) {
            // Строка 1
            Row(horizontalArrangement = Arrangement.spacedBy(GAP_DP.dp)) {
                CalcButton(angleLabel, CalcColors.ScientificButton, CalcColors.ScientificText,
                    fontSize = 14.sp, size = buttonSize) {
                    onEvent(CalculatorEvent.AngleModeChanged(
                        if (state.angleMode == AngleMode.RAD) AngleMode.DEG else AngleMode.RAD
                    ))
                }
                CalcButton("sin", CalcColors.ScientificButton, CalcColors.ScientificText,
                    fontSize = 16.sp, size = buttonSize) {
                    onEvent(CalculatorEvent.ScientificFuncPressed(ScientificFunc.SIN))
                }
                CalcButton("cos", CalcColors.ScientificButton, CalcColors.ScientificText,
                    fontSize = 16.sp, size = buttonSize) {
                    onEvent(CalculatorEvent.ScientificFuncPressed(ScientificFunc.COS))
                }
                CalcButton("tan", CalcColors.ScientificButton, CalcColors.ScientificText,
                    fontSize = 16.sp, size = buttonSize) {
                    onEvent(CalculatorEvent.ScientificFuncPressed(ScientificFunc.TAN))
                }
            }
            // Строка 2
            Row(horizontalArrangement = Arrangement.spacedBy(GAP_DP.dp)) {
                CalcButton("(", CalcColors.ScientificButton, CalcColors.ScientificText,
                    fontSize = 20.sp, size = buttonSize) { onEvent(CalculatorEvent.OpenBracket) }
                CalcButton("sin⁻¹", CalcColors.ScientificButton, CalcColors.ScientificText,
                    fontSize = 12.sp, size = buttonSize) {
                    onEvent(CalculatorEvent.ScientificFuncPressed(ScientificFunc.ASIN))
                }
                CalcButton("cos⁻¹", CalcColors.ScientificButton, CalcColors.ScientificText,
                    fontSize = 12.sp, size = buttonSize) {
                    onEvent(CalculatorEvent.ScientificFuncPressed(ScientificFunc.ACOS))
                }
                CalcButton("tan⁻¹", CalcColors.ScientificButton, CalcColors.ScientificText,
                    fontSize = 12.sp, size = buttonSize) {
                    onEvent(CalculatorEvent.ScientificFuncPressed(ScientificFunc.ATAN))
                }
            }
            // Строка 3
            Row(horizontalArrangement = Arrangement.spacedBy(GAP_DP.dp)) {
                CalcButton(")", CalcColors.ScientificButton, CalcColors.ScientificText,
                    fontSize = 20.sp, size = buttonSize) { onEvent(CalculatorEvent.CloseBracket) }
                CalcButton("x²", CalcColors.ScientificButton, CalcColors.ScientificText,
                    fontSize = 16.sp, size = buttonSize) {
                    onEvent(CalculatorEvent.ScientificFuncPressed(ScientificFunc.SQUARE))
                }
                CalcButton("x³", CalcColors.ScientificButton, CalcColors.ScientificText,
                    fontSize = 16.sp, size = buttonSize) {
                    onEvent(CalculatorEvent.ScientificFuncPressed(ScientificFunc.CUBE))
                }
                CalcButton("xʸ", CalcColors.ScientificButton, CalcColors.ScientificText,
                    fontSize = 16.sp, size = buttonSize) {
                    onEvent(CalculatorEvent.OperatorPressed(BinaryOp.POWER))
                }
            }
            // Строка 4
            Row(horizontalArrangement = Arrangement.spacedBy(GAP_DP.dp)) {
                CalcButton("e", CalcColors.ScientificButton, CalcColors.ScientificText,
                    fontSize = 18.sp, size = buttonSize) {
                    onEvent(CalculatorEvent.ConstantPressed(CalculatorConstantValue.E))
                }
                CalcButton("ln", CalcColors.ScientificButton, CalcColors.ScientificText,
                    fontSize = 16.sp, size = buttonSize) {
                    onEvent(CalculatorEvent.ScientificFuncPressed(ScientificFunc.LN))
                }
                CalcButton("log", CalcColors.ScientificButton, CalcColors.ScientificText,
                    fontSize = 14.sp, size = buttonSize) {
                    onEvent(CalculatorEvent.ScientificFuncPressed(ScientificFunc.LOG))
                }
                CalcButton("√", CalcColors.ScientificButton, CalcColors.ScientificText,
                    fontSize = 20.sp, size = buttonSize) {
                    onEvent(CalculatorEvent.ScientificFuncPressed(ScientificFunc.SQRT))
                }
            }
            // Строка 5
            Row(horizontalArrangement = Arrangement.spacedBy(GAP_DP.dp)) {
                CalcButton("π", CalcColors.ScientificButton, CalcColors.ScientificText,
                    fontSize = 18.sp, size = buttonSize) {
                    onEvent(CalculatorEvent.ConstantPressed(CalculatorConstantValue.PI))
                }
                CalcButton("Rand", CalcColors.ScientificButton, CalcColors.ScientificText,
                    fontSize = 13.sp, size = buttonSize) {
                    onEvent(CalculatorEvent.ScientificFuncPressed(ScientificFunc.RAND))
                }
                CalcButton("1/x", CalcColors.ScientificButton, CalcColors.ScientificText,
                    fontSize = 14.sp, size = buttonSize) {
                    onEvent(CalculatorEvent.ScientificFuncPressed(ScientificFunc.RECIPROCAL))
                }
                CalcButton("%", CalcColors.FunctionButton, CalcColors.FunctionText,
                    size = buttonSize) { onEvent(CalculatorEvent.PercentPressed) }
            }
        }

        // ── Правая панель: стандартная раскладка ──────────────────
        Column(verticalArrangement = Arrangement.spacedBy(GAP_DP.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(GAP_DP.dp)) {
                CalcButton(clearLabel, CalcColors.FunctionButton, CalcColors.FunctionText,
                    size = buttonSize) { onEvent(CalculatorEvent.ClearPressed) }
                CalcButton("+/-", CalcColors.FunctionButton, CalcColors.FunctionText,
                    size = buttonSize) { onEvent(CalculatorEvent.NegatePressed) }
                Spacer(Modifier.width(buttonSize))
                CalcButton("÷", CalcColors.OperatorButton, CalcColors.OperatorText,
                    fontSize = 28.sp, size = buttonSize,
                    isActive = state.activeOp == BinaryOp.DIVIDE) {
                    onEvent(CalculatorEvent.OperatorPressed(BinaryOp.DIVIDE))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(GAP_DP.dp)) {
                listOf("7", "8", "9").forEach { d ->
                    CalcButton(d, CalcColors.DigitButton, CalcColors.DigitText, size = buttonSize) {
                        onEvent(CalculatorEvent.DigitPressed(d))
                    }
                }
                CalcButton("×", CalcColors.OperatorButton, CalcColors.OperatorText,
                    fontSize = 28.sp, size = buttonSize,
                    isActive = state.activeOp == BinaryOp.MULTIPLY) {
                    onEvent(CalculatorEvent.OperatorPressed(BinaryOp.MULTIPLY))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(GAP_DP.dp)) {
                listOf("4", "5", "6").forEach { d ->
                    CalcButton(d, CalcColors.DigitButton, CalcColors.DigitText, size = buttonSize) {
                        onEvent(CalculatorEvent.DigitPressed(d))
                    }
                }
                CalcButton("−", CalcColors.OperatorButton, CalcColors.OperatorText,
                    fontSize = 28.sp, size = buttonSize,
                    isActive = state.activeOp == BinaryOp.SUBTRACT) {
                    onEvent(CalculatorEvent.OperatorPressed(BinaryOp.SUBTRACT))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(GAP_DP.dp)) {
                listOf("1", "2", "3").forEach { d ->
                    CalcButton(d, CalcColors.DigitButton, CalcColors.DigitText, size = buttonSize) {
                        onEvent(CalculatorEvent.DigitPressed(d))
                    }
                }
                CalcButton("+", CalcColors.OperatorButton, CalcColors.OperatorText,
                    fontSize = 28.sp, size = buttonSize,
                    isActive = state.activeOp == BinaryOp.ADD) {
                    onEvent(CalculatorEvent.OperatorPressed(BinaryOp.ADD))
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(GAP_DP.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CalcButton("0", CalcColors.DigitButton, CalcColors.DigitText,
                    size = buttonSize, widthMultiplier = 2f) {
                    onEvent(CalculatorEvent.DigitPressed("0"))
                }
                CalcButton(".", CalcColors.DigitButton, CalcColors.DigitText, size = buttonSize) {
                    onEvent(CalculatorEvent.DecimalPressed)
                }
                CalcButton("=", CalcColors.OperatorButton, CalcColors.OperatorText,
                    fontSize = 28.sp, size = buttonSize) {
                    onEvent(CalculatorEvent.EqualsPressed)
                }
            }
        }
    }
}
