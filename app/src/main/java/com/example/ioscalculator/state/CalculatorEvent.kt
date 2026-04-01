package com.example.ioscalculator.state

import com.example.ioscalculator.domain.AngleMode
import com.example.ioscalculator.domain.BinaryOp
import com.example.ioscalculator.domain.CalculatorConstantValue
import com.example.ioscalculator.domain.ScientificFunc

/**
 * Все возможные пользовательские действия — Intent в терминах MVI.
 */
sealed interface CalculatorEvent {
    // ── Ввод ──────────────────────────────────────────────────────
    data class DigitPressed(val digit: String) : CalculatorEvent
    data object DecimalPressed : CalculatorEvent
    data object ClearPressed : CalculatorEvent          // C / AC
    data object NegatePressed : CalculatorEvent         // +/-
    data object PercentPressed : CalculatorEvent        // %

    // ── Бинарные операторы ────────────────────────────────────────
    data class OperatorPressed(val op: BinaryOp) : CalculatorEvent
    data object EqualsPressed : CalculatorEvent

    // ── Научный режим ─────────────────────────────────────────────
    data class ScientificFuncPressed(val func: ScientificFunc) : CalculatorEvent
    data class ConstantPressed(val constant: CalculatorConstantValue) : CalculatorEvent
    data class AngleModeChanged(val mode: AngleMode) : CalculatorEvent
    data object OpenBracket : CalculatorEvent
    data object CloseBracket : CalculatorEvent
}
