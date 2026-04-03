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
import kotlin.math.abs
import kotlin.math.floor

data class HistoryEntry(val expression: String, val result: String)

data class CalculatorSettings(
    val soundEnabled: Boolean = false,
    val hapticEnabled: Boolean = true,
    val angleMode: AngleMode = AngleMode.RAD,
    val theme: CalcTheme = CalcTheme.DARK,
)

enum class CalcTheme { DARK, LIGHT }

// ═══════════════════════════════════════════════════════════════════════════
// Парсер математических выражений со скобками и приоритетами операций
// Грамматика (рекурсивный спуск):
//   expr    = term   (('+' | '−') term)*
//   term    = factor (('×' | '÷') factor)*
//   factor  = unary  ('^' unary)*
//   unary   = '-' unary | primary
//   primary = NUMBER | '(' expr ')'
// ═══════════════════════════════════════════════════════════════════════════
private class ExpressionParser(private val tokens: List<Token>) {
    private var pos = 0

    sealed class Token {
        data class Num(val value: Double) : Token()
        data class Op(val ch: Char) : Token()
        data object LParen : Token()
        data object RParen : Token()
    }

    companion object {
        fun tokenize(expr: String): List<Token> {
            val tokens = mutableListOf<Token>()
            var i = 0
            val s = expr.trim()
            while (i < s.length) {
                when {
                    s[i].isWhitespace() -> i++
                    s[i].isDigit() || s[i] == '.' -> {
                        var j = i
                        while (j < s.length && (s[j].isDigit() || s[j] == '.')) j++
                        tokens += Token.Num(s.substring(i, j).toDouble())
                        i = j
                    }
                    s[i] == '(' -> { tokens += Token.LParen; i++ }
                    s[i] == ')' -> { tokens += Token.RParen; i++ }
                    s[i] in "+-−×÷^*/" -> { tokens += Token.Op(s[i]); i++ }
                    else -> i++ // пропускаем неизвестные символы
                }
            }
            return tokens
        }

        fun evaluate(expr: String, angleMode: AngleMode = AngleMode.RAD): EngineResult {
            return try {
                val tokens = tokenize(expr)
                if (tokens.isEmpty()) return EngineResult.Value(0.0)
                val parser = ExpressionParser(tokens)
                val result = parser.parseExpr()
                if (result.isNaN() || result.isInfinite())
                    EngineResult.Error("Ошибка")
                else
                    EngineResult.Value(result)
            } catch (e: Exception) {
                EngineResult.Error("Ошибка")
            }
        }
    }

    private fun peek(): Token? = tokens.getOrNull(pos)
    private fun consume(): Token = tokens[pos++]

    fun parseExpr(): Double {
        var left = parseTerm()
        while (true) {
            val t = peek()
            if (t is Token.Op && (t.ch == '+' || t.ch == '-' || t.ch == '−')) {
                consume()
                val right = parseTerm()
                left = if (t.ch == '+') left + right else left - right
            } else break
        }
        return left
    }

    private fun parseTerm(): Double {
        var left = parseFactor()
        while (true) {
            val t = peek()
            if (t is Token.Op && (t.ch == '×' || t.ch == '*' || t.ch == '÷' || t.ch == '/')) {
                consume()
                val right = parseFactor()
                left = when (t.ch) {
                    '×', '*' -> left * right
                    '÷', '/' -> if (abs(right) < 1e-12) throw ArithmeticException("div0") else left / right
                    else -> left
                }
            } else break
        }
        return left
    }

    private fun parseFactor(): Double {
        var left = parseUnary()
        while (true) {
            val t = peek()
            if (t is Token.Op && t.ch == '^') {
                consume()
                val right = parseUnary()
                left = Math.pow(left, right)
            } else break
        }
        return left
    }

    private fun parseUnary(): Double {
        val t = peek()
        if (t is Token.Op && (t.ch == '-' || t.ch == '−')) {
            consume()
            return -parseUnary()
        }
        return parsePrimary()
    }

    private fun parsePrimary(): Double {
        val t = peek() ?: throw IllegalStateException("Unexpected end")
        return when (t) {
            is Token.Num -> { consume(); t.value }
            is Token.LParen -> {
                consume()
                val v = parseExpr()
                if (peek() is Token.RParen) consume()
                v
            }
            else -> throw IllegalStateException("Unexpected token: $t")
        }
    }
}

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

    // Флаг: идёт ввод внутри скобок
    private var bracketDepth = 0

    fun onEvent(event: CalculatorEvent) {
        val s = _state.value as? CalculatorState.Active ?: return
        when (event) {
            is CalculatorEvent.DigitPressed         -> handleDigit(s, event.digit)
            is CalculatorEvent.DecimalPressed        -> handleDecimal(s)
            is CalculatorEvent.ClearPressed          -> handleClear(s)
            is CalculatorEvent.NegatePressed         -> handleNegate(s)
            is CalculatorEvent.PercentPressed        -> handlePercent(s)
            is CalculatorEvent.OperatorPressed       -> handleOperator(s, event.op)
            is CalculatorEvent.EqualsPressed         -> handleEquals(s)
            is CalculatorEvent.ScientificFuncPressed -> handleScientific(s, event.func)
            is CalculatorEvent.ConstantPressed       -> handleConstant(s, event.constant)
            is CalculatorEvent.AngleModeChanged      -> updateAngleMode(event.mode)
            is CalculatorEvent.OpenBracket           -> handleOpenBracket(s)
            is CalculatorEvent.CloseBracket          -> handleCloseBracket(s)
            is CalculatorEvent.Backspace             -> handleBackspace(s)
            is CalculatorEvent.OpenHistory           -> _showHistory.value = !_showHistory.value
            is CalculatorEvent.OpenSettings          -> _showSettings.value = !_showSettings.value
            else -> {}
        }
    }

    fun dismissHistory()  { _showHistory.value = false }
    fun dismissSettings() { _showSettings.value = false }
    fun clearHistory()    { _history.value = emptyList() }

    fun updateSettings(new: CalculatorSettings) {
        _settings.value = new
        _state.update { s -> (s as? CalculatorState.Active)?.copy(angleMode = new.angleMode) ?: s }
    }

    // ── Digit ────────────────────────────────────────────────────────────────
    private fun handleDigit(s: CalculatorState.Active, digit: String) {
        val maxLen = CalculatorConstants.INPUT_MAX_LENGTH
        val newInput = when {
            s.startNewInput || s.justEvaluated -> digit
            s.currentInput == "0"             -> digit
            s.currentInput.replace("-","").replace(".","").length >= maxLen -> return
            else -> s.currentInput + digit
        }
        val newExpr = buildExprAfterInput(s, newInput)
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
        val input = when {
            s.startNewInput || s.justEvaluated -> "0."
            s.currentInput.contains('.') -> return
            else -> s.currentInput + "."
        }
        val newExpr = buildExprAfterInput(s, input)
        _state.value = s.copy(
            currentInput   = input,
            displayText    = input,
            expressionText = newExpr,
            startNewInput  = false,
            justEvaluated  = false,
            hasInput       = true,
        )
    }

    // ── Clear ────────────────────────────────────────────────────────────────
    private fun handleClear(s: CalculatorState.Active) {
        bracketDepth = 0
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
        val v = s.currentInput.toDoubleOrNull() ?: return
        val r = CalculatorEngine.negate(v)
        if (r is EngineResult.Value) {
            val newInput = formatRaw(r.number)
            _state.value = s.copy(
                currentInput   = newInput,
                displayText    = newInput,
                expressionText = buildExprAfterInput(s, newInput),
            )
        }
    }

    // ── Percent ──────────────────────────────────────────────────────────────
    private fun handlePercent(s: CalculatorState.Active) {
        val v   = s.currentInput.toDoubleOrNull() ?: return
        val ctx = if (s.pendingOp != null) s.accumulator else null
        val r   = CalculatorEngine.applyPercent(v, ctx)
        if (r is EngineResult.Value) {
            val newInput = formatRaw(r.number)
            _state.value = s.copy(
                currentInput   = newInput,
                displayText    = newInput,
                expressionText = buildExprAfterInput(s, newInput),
            )
        }
    }

    // ── Operator ─────────────────────────────────────────────────────────────
    private fun handleOperator(s: CalculatorState.Active, op: BinaryOp) {
        val opSymbol = " ${op.symbol} "

        // Если уже стоит оператор в конце — заменяем его
        val baseExpr = when {
            s.expressionText.trimEnd().lastOrNull()
                ?.let { it in "+-−×÷" } == true ->
                s.expressionText.trimEnd().dropLastWhile { it in "+-−×÷ " } + opSymbol
            s.justEvaluated ->
                s.displayText + opSymbol
            s.expressionText.isEmpty() ->
                s.currentInput + opSymbol
            s.startNewInput ->
                s.expressionText.trimEnd() + opSymbol
            else ->
                s.expressionText + opSymbol
        }

        // Частичный результат для дисплея (без скобок — пробуем вычислить левую часть)
        val displayVal = if (bracketDepth == 0) {
            val partialExpr = baseExpr.trimEnd().trimEnd { it in "+-−×÷ " }
            val r = ExpressionParser.evaluate(partialExpr, s.angleMode)
            if (r is EngineResult.Value) r.number else s.accumulator
        } else {
            s.accumulator
        }

        _state.value = s.copy(
            accumulator    = displayVal,
            pendingOp      = op,
            activeOp       = op,
            displayText    = formatRaw(displayVal),
            expressionText = baseExpr,
            startNewInput  = true,
            justEvaluated  = false,
        )
    }

    // ── Equals ───────────────────────────────────────────────────────────────
    private fun handleEquals(s: CalculatorState.Active) {
        // Закрываем незакрытые скобки
        val closedExpr = s.expressionText + ")".repeat(bracketDepth)
        bracketDepth = 0

        val exprToEval = closedExpr.trimEnd().trimEnd { it in "+-−×÷ " }
        if (exprToEval.isBlank()) return

        val result = ExpressionParser.evaluate(exprToEval, s.angleMode)
        if (result is EngineResult.Error) { showError(s, result.message); return }

        val value  = (result as EngineResult.Value).number
        val resStr = formatRaw(value)
        val fullExpr = "$exprToEval ="

        addHistory(exprToEval, resStr)

        _state.value = s.copy(
            accumulator    = value,
            displayText    = resStr,
            currentInput   = resStr,
            expressionText = fullExpr,
            pendingOp      = null,
            activeOp       = null,
            lastOp         = s.pendingOp,
            lastRhs        = s.currentInput.toDoubleOrNull(),
            startNewInput  = true,
            justEvaluated  = true,
            hasInput       = false,
        )
    }

    // ── Brackets ─────────────────────────────────────────────────────────────
    private fun handleOpenBracket(s: CalculatorState.Active) {
        bracketDepth++
        val newExpr = when {
            s.expressionText.isEmpty() -> "("
            s.startNewInput            -> s.expressionText + "("
            else                       -> s.expressionText + " × ("
        }
        _state.value = s.copy(
            expressionText = newExpr,
            displayText    = s.displayText,
            startNewInput  = true,
            activeOp       = null,
        )
    }

    private fun handleCloseBracket(s: CalculatorState.Active) {
        if (bracketDepth <= 0) return
        bracketDepth--
        val newExpr = s.expressionText + ")"

        // Вычисляем подвыражение для показа на дисплее
        val r = ExpressionParser.evaluate(newExpr, s.angleMode)
        val displayVal = if (r is EngineResult.Value) formatRaw(r.number) else s.displayText

        _state.value = s.copy(
            expressionText = newExpr,
            displayText    = displayVal,
            currentInput   = displayVal,
            startNewInput  = true,
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
            s.startNewInput            -> s.expressionText + funcExpr
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
        val v     = CalculatorEngine.constant(c)
        val label = if (c == CalculatorConstantValue.PI) "π" else "e"
        val newInput = formatRaw(v)
        val newExpr = when {
            s.expressionText.isEmpty() -> label
            s.startNewInput            -> s.expressionText + label
            else                       -> label
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
        val newInput = if (s.currentInput.length <= 1) "0" else s.currentInput.dropLast(1)
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
        _state.value = s.copy(displayText = msg, isError = true, hasInput = false)
    }

    private fun addHistory(expression: String, result: String) {
        _history.update { list -> listOf(HistoryEntry(expression, result)) + list.take(99) }
    }

    fun formatRaw(v: Double): String {
        if (v == floor(v) && abs(v) < 1e15) return v.toLong().toString()
        return v.toBigDecimal().stripTrailingZeros().toPlainString()
    }

    /**
     * Строит строку выражения после ввода нового операнда.
     * Заменяет последний операнд в выражении на newInput.
     */
    private fun buildExprAfterInput(s: CalculatorState.Active, newInput: String): String {
        return when {
            s.justEvaluated            -> newInput
            s.startNewInput            -> s.expressionText + newInput
            s.expressionText.isEmpty() -> newInput
            else -> {
                val lastOpIdx = lastOperatorIndex(s.expressionText)
                if (lastOpIdx < 0) newInput
                else s.expressionText.substring(0, lastOpIdx + 1) + newInput
            }
        }
    }

    private fun lastOperatorIndex(expr: String): Int {
        for (i in expr.indices.reversed()) {
            val c = expr[i]
            if (c in "+-−×÷" && i > 0) return i
        }
        return -1
    }
}
