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
    val thousandsSeparator: Boolean = true,
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
            is CalculatorEvent.DigitPressed      -> handleDigit(current, event.digit)
            is CalculatorEvent.DecimalPressed     -> handleDecimal(current)
            is CalculatorEvent.ClearPressed       -> handleClear(current)
            is CalculatorEvent.NegatePressed      -> handleNegate(current)
            is CalculatorEvent.PercentPressed     -> handlePercent(current)
            is CalculatorEvent.OperatorPressed    -> handleOperator(current, event.op)
            is CalculatorEvent.EqualsPressed      -> handleEquals(current)
            is CalculatorEvent.ScientificFuncPressed -> handleScientific(current, event.func)
            is CalculatorEvent.ConstantPressed    -> handleConstant(current, event.constant)
            is CalculatorEvent.AngleModeChanged   -> updateAngleMode(event.mode)
            is CalculatorEvent.Backspace          -> handleBackspace(current)
            is CalculatorEvent.OpenHistory        -> _showHistory.value = !_showHistory.value
            is CalculatorEvent.OpenSettings       -> _showSettings.value = !_showSettings.value
            else -> {}
        }
    }

    fun dismissHistory()  { _showHistory.value = false }
    fun dismissSettings() { _showSettings.value = false }
    fun clearHistory()    { _history.value = emptyList() }

    fun updateSettings(new: CalculatorSettings) {
        _settings.value = new
        // Синхронизируем angleMode в состояние калькулятора
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
            s.currentInput.replace("-", "").replace(".", "").length >= maxLen -> return
            else -> s.currentInput + digit
        }
        _state.value = s.copy(
            currentInput  = newInput,
            displayText   = formatInput(newInput),
            startNewInput = false,
            justEvaluated = false,
            hasInput      = true,
            activeOp      = if (s.startNewInput) s.activeOp else null,
        )
    }

    // ── Decimal ──────────────────────────────────────────────────────────────
    private fun handleDecimal(s: CalculatorState.Active) {
        val input = if (s.startNewInput || s.justEvaluated) "0." else {
            if (s.currentInput.contains('.')) return
            s.currentInput + "."
        }
        _state.value = s.copy(
            currentInput  = input,
            displayText   = formatInput(input),
            startNewInput = false,
            justEvaluated = false,
            hasInput      = true,
        )
    }

    // ── Clear (AC / C) ───────────────────────────────────────────────────────
    private fun handleClear(s: CalculatorState.Active) {
        _state.value = if (s.hasInput && !s.startNewInput) {
            s.copy(
                currentInput  = "0",
                displayText   = "0",
                hasInput      = false,
                startNewInput = true,
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
            _state.value = s.copy(
                currentInput = newInput,
                displayText  = formatInput(newInput),
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
            _state.value = s.copy(
                currentInput = newInput,
                displayText  = formatInput(newInput),
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
        _state.value = s.copy(
            accumulator   = newAccumulator,
            pendingOp     = op,
            activeOp      = op,
            displayText   = CalculatorEngine.formatForDisplay(newAccumulator),
            startNewInput = true,
            justEvaluated = false,
        )
    }

    // ── Equals ───────────────────────────────────────────────────────────────
    private fun handleEquals(s: CalculatorState.Active) {
        val op  = s.pendingOp ?: s.lastOp ?: return
        val rhs = if (s.justEvaluated) s.lastRhs ?: 0.0
                  else s.currentInput.toDoubleOrNull() ?: 0.0
        val lhs = if (s.justEvaluated) s.accumulator
                  else s.accumulator

        val result = CalculatorEngine.applyOperator(lhs, op, rhs)
        if (result is EngineResult.Error) { showError(s, result.message); return }
        val value = (result as EngineResult.Value).number

        // Записываем в историю
        val exprLhs = CalculatorEngine.formatForDisplay(lhs)
        val exprRhs = CalculatorEngine.formatForDisplay(rhs)
        val exprStr = "$exprLhs ${op.symbol} $exprRhs"
        val resStr  = CalculatorEngine.formatForDisplay(value)
        addHistory(exprStr, resStr)

        _state.value = s.copy(
            accumulator   = value,
            displayText   = resStr,
            currentInput  = formatRaw(value),
            pendingOp     = null,
            activeOp      = null,
            lastOp        = op,
            lastRhs       = rhs,
            startNewInput = true,
            justEvaluated = true,
            hasInput      = false,
        )
    }

    // ── Scientific ───────────────────────────────────────────────────────────
    private fun handleScientific(s: CalculatorState.Active, func: ScientificFunc) {
        val value = s.currentInput.toDoubleOrNull() ?: return
        val result = CalculatorEngine.applyScientific(func, value, s.angleMode)
        if (result is EngineResult.Error) { showError(s, result.message); return }
        val v = (result as EngineResult.Value).number
        val newInput = formatRaw(v)
        _state.value = s.copy(
            currentInput  = newInput,
            displayText   = CalculatorEngine.formatForDisplay(v),
            startNewInput = true,
            justEvaluated = false,
        )
    }

    // ── Constant ─────────────────────────────────────────────────────────────
    private fun handleConstant(s: CalculatorState.Active, c: CalculatorConstantValue) {
        val v = CalculatorEngine.constant(c)
        val newInput = formatRaw(v)
        _state.value = s.copy(
            currentInput  = newInput,
            displayText   = CalculatorEngine.formatForDisplay(v),
            startNewInput = true,
            hasInput      = true,
        )
    }

    // ── Backspace ────────────────────────────────────────────────────────────
    private fun handleBackspace(s: CalculatorState.Active) {
        if (s.startNewInput || s.justEvaluated) return
        val newInput = when {
            s.currentInput.length <= 1 -> "0"
            else -> s.currentInput.dropLast(1)
        }
        _state.value = s.copy(
            currentInput = newInput,
            displayText  = formatInput(newInput),
            hasInput     = newInput != "0",
        )
    }

    // ── AngleMode ────────────────────────────────────────────────────────────
    private fun updateAngleMode(mode: AngleMode) {
        _settings.update { it.copy(angleMode = mode) }
        _state.update { s -> (s as? CalculatorState.Active)?.copy(angleMode = mode) ?: s }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private fun showError(s: CalculatorState.Active, msg: String) {
        _state.value = s.copy(displayText = msg, isError = true, hasInput = false)
    }

    private fun addHistory(expression: String, result: String) {
        _history.update { list ->
            listOf(HistoryEntry(expression, result)) + list.take(99)
        }
    }

    /** Форматирует строку ввода для дисплея: добавляет разделители тысяч. */
    private fun formatInput(input: String): String {
        if (input.endsWith('.')) return input
        val d = input.toDoubleOrNull() ?: return input
        if (input.contains('.')) {
            val intPart = input.substringBefore('.')
            val fracPart = input.substringAfter('.')
            return formatIntPart(intPart) + "." + fracPart
        }
        return formatIntPart(input)
    }

    private fun formatIntPart(intStr: String): String {
        val neg = intStr.startsWith('-')
        val digits = if (neg) intStr.drop(1) else intStr
        val grouped = digits.reversed().chunked(3).joinToString(",").reversed()
        return if (neg) "-$grouped" else grouped
    }

    private fun formatRaw(v: Double): String {
        if (v == kotlin.math.floor(v) && kotlin.math.abs(v) < 1e15) return v.toLong().toString()
        return v.toBigDecimal().stripTrailingZeros().toPlainString()
    }
}
