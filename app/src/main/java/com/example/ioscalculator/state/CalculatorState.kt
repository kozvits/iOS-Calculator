package com.example.ioscalculator.state

import com.example.ioscalculator.domain.AngleMode
import com.example.ioscalculator.domain.BinaryOp

/**
 * Полное состояние экрана калькулятора.
 * Один sealed interface — единственный источник истины для UI.
 */
sealed interface CalculatorState {

    /**
     * Рабочее состояние — всегда активно (нет отдельных loading/error-ветвей,
     * ошибки отображаются на дисплее).
     */
    data class Active(
        // ── Дисплей ──────────────────────────────────────────────
        /** Основной результат на дисплее (нижняя строка). */
        val displayText: String = "0",

        /** Строка выражения — накапливается при вводе (верхняя строка). */
        val expressionText: String = "",

        // ── Операнды ─────────────────────────────────────────────
        /** Текущий накапливаемый операнд (строка для ввода). */
        val currentInput: String = "0",

        /** Аккумулятор — результат предыдущей операции. */
        val accumulator: Double = 0.0,

        /** Ожидающий бинарный оператор (null = нет активной операции). */
        val pendingOp: BinaryOp? = null,

        /** Флаг: следующий ввод цифры начнёт новый операнд (после = или оператора). */
        val startNewInput: Boolean = false,

        /** Флаг: пользователь ввёл хотя бы одну цифру (переключает C -> AC). */
        val hasInput: Boolean = false,

        /** Флаг: последнее нажатие было = (для повторного вычисления). */
        val justEvaluated: Boolean = false,

        /** Сохранённый правый операнд для повторного =. */
        val lastRhs: Double? = null,

        /** Сохранённая операция для повторного =. */
        val lastOp: BinaryOp? = null,

        // ── Научный режим ─────────────────────────────────────────
        val angleMode: AngleMode = AngleMode.RAD,

        /** Стек скобок для научного режима (упрощённая реализация). */
        val bracketStack: List<Double> = emptyList(),
        val bracketOps: List<BinaryOp?> = emptyList(),

        // ── Активный оператор (для визуальной инверсии кнопки) ───
        val activeOp: BinaryOp? = null,

        // ── Признак ошибки ────────────────────────────────────────
        val isError: Boolean = false,
    ) : CalculatorState
}
