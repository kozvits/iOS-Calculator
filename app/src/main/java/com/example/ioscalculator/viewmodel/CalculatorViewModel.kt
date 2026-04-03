package com.example.ioscalculator.viewmodel

import androidx.lifecycle.ViewModel
import com.example.ioscalculator.domain.*
import com.example.ioscalculator.state.CalculatorEvent
import com.example.ioscalculator.state.CalculatorState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class HistoryEntry(val expression: String, val result: String)

data class CalculatorSettings(
    val soundEnabled: Boolean = false,
    val hapticEnabled: Boolean = true,
    val angleMode: AngleMode = AngleMode.RAD,
    val theme: CalcTheme = CalcTheme.DARK,
)

enum class CalcTheme { DARK, LIGHT }

@HiltViewModel
class CalculatorViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow<CalculatorState>(CalculatorState.Active())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    private val _history = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val history: StateFlow<List<HistoryEntry>> = _history.asStateFlow()

    private val _settings = MutableStateFlow(CalculatorSettings())
    val settings: StateFlow<CalculatorSettings> = _settings.asStateFlow()

    private val _showHistory = MutableStateFlow(false)
    val showHistory: StateFlow<Boolean> = _showHistory.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    fun onEvent(event: CalculatorEvent) {
        val current = _state.value as? CalculatorState.Active ?: return
        when (event) {
            is CalculatorEvent.DigitPressed          -> handleDigit(current, event.digit)
            is CalculatorEvent.DecimalPressed         -> handleDecimal(current)
            is CalculatorEvent.ClearPressed           -> handleClear(current)
            is CalculatorEvent.NegatePressed          -> handleNegate(current)
            is CalculatorEvent.PercentPressed         -> handlePercent(current)
            is CalculatorEvent.OperatorPressed        -> handleOperator(current, event.op)
            is CalculatorEvent.EqualsPressed          -> handleEquals(current)
            is CalculatorEvent.ScientificFuncPressed  -> handleScientific(current, event.func)
            is CalculatorEvent.ConstantPressed        -> handleConstant(current, event.constant)
            is CalculatorEvent.AngleModeChanged       -> updateAngleMode(event.mode)
            is CalculatorEvent.OpenBracket            -> handleOpenBracket(current)
            is CalculatorEvent.CloseBracket           -> handleCloseBracket(current)
            is CalculatorEvent.Backspace              -> handleBackspace(current)
            is CalculatorEvent.OpenHistory            -> _showHistory.value = !_showHistory.value
            is CalculatorEvent.OpenSettings           -> _showSettings.value = !_showSettings.value
            else -> {}
        }
    }

    fun dismissHistory()  { _showHistory.value = false }
    fun dismissSettings() { _showSettings.value = false }
    fun clearHistory()    { _history.value = emptyList() }

    fun updateSettings(new: CalculatorSettings) {
        _settings.value = new
        _state.update { s ->
            (s as? CalculatorState.Active)?.copy(angleMode = new.angleMode) ?: s
        }
    }

    // ── Digit ────────────────────────────────────────────────────────────────
    private fun handleDigit(s: CalculatorState.Active, digit: String) {
        val maxLen = CalculatorConstants.INPUT_MAX_LENGTH
        val newInput = when {
            s.startNewInput || s.justEvaluated -> digit
            s.currentInput == "0"              -> digit
            s.currentInput.replace("-","").replace(".","").length >= maxLen -> return
            else -> s.currentInput + digit
        }
        // Обновляем выражение: если начинаем после оператора — просто добавляем цифру
        val newExpr = when {
            s.justEvaluated -> digit                     // после = начинаем заново
            s.startNewInput -> s.expressionText + digit  // после оператора — продолжаем
            s.expressionText.isEmpty() -> digit
            else -> {
                // Заменяем последний операнд в выражении
                val lastOpIdx = lastOperatorIndex(s.expressionText)
                if (lastOpIdx < 0) newInput
                else s.expressionText.substring(0, lastOpIdx + 1) + newInput
            }
        }
        _state.value = s.copy(
            currentInput   = newInput,
            displayText    = newInput,
            expressionText = newExpr,
            startNewInput  = false,
            justEvaluated  = false,
            hasInput       = true,
            activeOp       = if (s.startNewInput) s.activeOp else null,
        )
    }

    // ── Decimal ──────────────────────────────────────────────────────────────
    private fun handleDecimal(s: CalculatorState.Active) {
        val input = if (s.startNewInput || s.justEvaluated) "0." else {
            if (s.currentInput.contains('.')) return
            s.currentInput + "."
        }
        val newExpr = when {
            s.justEvaluated -> "0."
            s.startNewInput -> s.expressionText + "0."
            s.expressionText.isEmpty() -> "0."
            else -> {
                val lastOpIdx = lastOperatorIndex(s.expressionText)
                if (lastOpIdx < 0) input
                else s.expressionText.substring(0, lastOpIdx + 1) + input
            }
        }
        _state.value = s.copy(
            currentInput   = input,
            displayText    = input,
            expressionText = newExpr,
            startNewInput  = false,
            justEvaluated  = false,
            hasInput       = true,
        )
    }

    // ── Clear (AC / C) ───────────────────────────────────────────────────────
    private fun handleClear(s: CalculatorState.Active) {
        _state.value = if (s.hasInput && !s.startNewInput) {
            s.copy(
                currentInput   = "0",
                displayText    = "0",
                expressionText = "",
                hasInput       = false,
                startNewInput  = true,
            )
        } else {
            CalculatorState.Active(angleMode = s.angleMode)
        }
    }

    // ── Negate ───────────────────────────────────────────────────────────────
    private fun handleNegate(s: CalculatorState.Active) {
        val value = s.currentInput.toDoubleOrNull() ?: return
        val result = CalculatorEngine.negate(value)
        if (result is EngineResult.Value) {
            val newInput = formatRaw(result.number)
            val newExpr = when {
                s.expressionText.isEmpty() -> newInput
                else -> {
                    val lastOpIdx = lastOperatorIndex(s.expressionText)
                    if (lastOpIdx < 0) newInput
                    else s.expressionText.substring(0, lastOpIdx + 1) + newInput
                }
            }
            _state.value = s.copy(
                currentInput   = newInput,
                displayText    = newInput,
                expressionText = newExpr,
            )
        }
    }

    // ── Percent ──────────────────────────────────────────────────────────────
    private fun handlePercent(s: CalculatorState.Active) {
        val value = s.currentInput.toDoubleOrNull() ?: return
        val ctx   = if (s.pendingOp != null) s.accumulator else null
        val result = CalculatorEngine.applyPercent(value, ctx)
        if (result is EngineResult.Value) {
            val newInput = formatRaw(result.number)
            val newExpr = when {
                s.expressionText.isEmpty() -> newInput
                else -> {
                    val lastOpIdx = lastOperatorIndex(s.expressionText)
                    if (lastOpIdx < 0) newInput
                    else s.expressionText.substring(0, lastOpIdx + 1) + newInput
                }
            }
            _state.value = s.copy(
                currentInput   = newInput,
                displayText    = newInput,
                expressionText = newExpr,
            )
        }
    }

    // ── Operator ─────────────────────────────────────────────────────────────
    private fun handleOperator(s: CalculatorState.Active, op: BinaryOp) {
        val current = s.currentInput.toDoubleOrNull() ?: 0.0
        val newAccumulator = if (s.pendingOp != null && !s.startNewInput && !s.justEvaluated) {
            val r = CalculatorEngine.applyOperator(s.accumulator, s.pendingOp, current)
            if (r is EngineResult.Error) { showError(s, r.message); return }
            (r as EngineResult.Value).number
        } else current

        // Добавляем символ оператора в выражение
        val opSymbol = " ${op.symbol} "
        val newExpr = when {
            s.justEvaluated ->
                // После = начинаем с результата
                formatRaw(newAccumulator) + opSymbol
            s.startNewInput ->
                // Заменяем последний оператор если уже стоит
                s.expressionText.trimEnd() + opSymbol
            s.expressionText.isEmpty() ->
                formatRaw(newAccumulator) + opSymbol
            else ->
                s.expressionText + opSymbol
        }

        _state.value = s.copy(
            accumulator    = newAccumulator,
            pendingOp      = op,
            activeOp       = op,
            displayText    = formatRaw(newAccumulator),
            expressionText = newExpr,
            startNewInput  = true,
            justEvaluated  = false,
        )
    }

    // ── Equals ───────────────────────────────────────────────────────────────
    private fun handleEquals(s: CalculatorState.Active) {
        val op  = s.pendingOp ?: s.lastOp ?: return
        val rhs = if (s.justEvaluated) s.lastRhs ?: 0.0
                  else s.currentInput.toDoubleOrNull() ?: 0.0
        val lhs = s.accumulator

        val result = CalculatorEngine.applyOperator(lhs, op, rhs)
        if (result is EngineResult.Error) { showError(s, result.message); return }
        val value = (result as EngineResult.Value).number
        val resStr = formatRaw(value)

        // Полное выражение для верхней строки: "3 + 5 ="
        val fullExpr = when {
            s.justEvaluated -> "${formatRaw(lhs)} ${op.symbol} ${formatRaw(rhs)} ="
            s.expressionText.isNotEmpty() -> s.expressionText.trimEnd() + " ="
            else -> "${formatRaw(lhs)} ${op.symbol} ${formatRaw(rhs)} ="
        }

        // Записываем в историю
        addHistory(fullExpr.removeSuffix(" ="), resStr)

        _state.value = s.copy(
            accumulator    = value,
            displayText    = resStr,
            currentInput   = resStr,
            expressionText = fullExpr,   // верхняя строка показывает выражение
            pendingOp      = null,
            activeOp       = null,
            lastOp         = op,
            lastRhs        = rhs,
            startNewInput  = true,
            justEvaluated  = true,
            hasInput       = false,
        )
    }

    // ── Brackets ─────────────────────────────────────────────────────────────
    private fun handleOpenBracket(s: CalculatorState.Active) {
        val newExpr = if (s.expressionText.isEmpty()) "(" else s.expressionText + "("
        _state.value = s.copy(
            expressionText = newExpr,
            displayText    = newExpr,
            startNewInput  = true,
        )
    }

    private fun handleCloseBracket(s: CalculatorState.Active) {
        val newExpr = s.expressionText + ")"
        _state.value = s.copy(
            expressionText = newExpr,
            displayText    = s.currentInput,
        )
    }

    // ── Scientific ───────────────────────────────────────────────────────────
    private fun handleScientific(s: CalculatorState.Active, func: ScientificFunc) {
        val value = s.currentInput.toDoubleOrNull() ?: return
        val result = CalculatorEngine.applyScientific(func, value, s.angleMode)
        if (result is EngineResult.Error) { showError(s, result.message); return }
        val v = (result as EngineResult.Value).number
        val newInput = formatRaw(v)
        val funcExpr = "${func.label}(${formatRaw(value)})"
        val newExpr = when {
            s.expressionText.isEmpty() -> funcExpr
            s.startNewInput -> s.expressionText + funcExpr
            else -> {
                val lastOpIdx = lastOperatorIndex(s.expressionText)
                if (lastOpIdx < 0) funcExpr
                else s.expressionText.substring(0, lastOpIdx + 1) + funcExpr
            }
        }
        _state.value = s.copy(
            currentInput   = newInput,
            displayText    = newInput,
            expressionText = newExpr,
            startNewInput  = true,
            justEvaluated  = false,
        )
    }

    // ── Constant ─────────────────────────────────────────────────────────────
    private fun handleConstant(s: CalculatorState.Active, c: CalculatorConstantValue) {
        val v = CalculatorEngine.constant(c)
        val label = if (c == CalculatorConstantValue.PI) "π" else "e"
        val newInput = formatRaw(v)
        val newExpr = when {
            s.expressionText.isEmpty() -> label
            s.startNewInput -> s.expressionText + label
            else -> label
        }
        _state.value = s.copy(
            currentInput   = newInput,
            displayText    = newInput,
            expressionText = newExpr,
            startNewInput  = true,
            hasInput       = true,
        )
    }

    // ── Backspace ────────────────────────────────────────────────────────────
    private fun handleBackspace(s: CalculatorState.Active) {
        if (s.startNewInput || s.justEvaluated) return
        val newInput = when {
            s.currentInput.length <= 1 -> "0"
            else -> s.currentInput.dropLast(1)
        }
        val newExpr = when {
            s.expressionText.isEmpty() -> ""
            else -> {
                val lastOpIdx = lastOperatorIndex(s.expressionText)
                if (lastOpIdx < 0) newInput
                else s.expressionText.substring(0, lastOpIdx + 1) + newInput
            }
        }
        _state.value = s.copy(
            currentInput   = newInput,
            displayText    = newInput,
            expressionText = newExpr,
            hasInput       = newInput != "0",
        )
    }

    // ── AngleMode ────────────────────────────────────────────────────────────
    private fun updateAngleMode(mode: AngleMode) {
        _settings.update { it.copy(angleMode = mode) }
        _state.update { s -> (s as? CalculatorState.Active)?.copy(angleMode = mode) ?: s }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private fun showError(s: CalculatorState.Active, msg: String) {
        _state.value = s.copy(
            displayText    = msg,
            expressionText = s.expressionText,
            isError        = true,
            hasInput       = false,
        )
    }

    private fun addHistory(expression: String, result: String) {
        _history.update { list ->
            listOf(HistoryEntry(expression, result)) + list.take(99)
        }
    }

    private fun formatRaw(v: Double): String {
        if (v == kotlin.math.floor(v) && kotlin.math.abs(v) < 1e15) return v.toLong().toString()
        return v.toBigDecimal().stripTrailingZeros().toPlainString()
    }

    /**
     * Возвращает индекс последнего оператора (+, −, ×, ÷) в строке выражения.
     * Пропускает символы внутри скобок.
     */
    private fun lastOperatorIndex(expr: String): Int {
        val ops = setOf('+', '−', '×', '÷', ' ')
        for (i in expr.indices.reversed()) {
            val c = expr[i]
            if (c in ops && i > 0) return i
        }
        return -1
    }
}
