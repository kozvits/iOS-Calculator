Вот полный, готовый к замене файл `CalculatorViewModel.kt`. Все изменения интегрированы в вашу архитектуру без потери существующей логики.

```kotlin
package com.example.ioscalculator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ioscalculator.domain.*
import com.example.ioscalculator.domain.CalculatorConstants.DISPLAY_ZERO
import com.example.ioscalculator.domain.CalculatorConstants.INPUT_MAX_LENGTH
import com.example.ioscalculator.state.CalculatorEvent
import com.example.ioscalculator.state.CalculatorEvent.*
import com.example.ioscalculator.state.CalculatorState
import com.example.ioscalculator.state.CalculatorState.Active
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalculatorViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow<CalculatorState>(Active())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    /** Единственная точка входа для UI — обработка всех событий. */
    fun onEvent(event: CalculatorEvent) {
        viewModelScope.launch {
            _state.update { current ->
                if (current !is Active) return@update current
                reduce(current, event)
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // РЕДУКТОР
    // ─────────────────────────────────────────────────────────────────

    private fun reduce(s: Active, event: CalculatorEvent): Active = when (event) {
        is DigitPressed          -> handleDigit(s, event.digit)
        is DecimalPressed        -> handleDecimal(s)
        is ClearPressed          -> handleClear(s)
        is NegatePressed         -> handleNegate(s)
        is PercentPressed        -> handlePercent(s)
        is OperatorPressed       -> handleOperator(s, event.op)
        is EqualsPressed         -> handleEquals(s)
        is ScientificFuncPressed -> handleScientific(s, event.func)
        is ConstantPressed       -> handleConstant(s, event.constant)
        is AngleModeChanged      -> s.copy(angleMode = event.mode)
        is OpenBracket           -> handleOpenBracket(s)
        is CloseBracket          -> handleCloseBracket(s)
        
        // 🔹 Новые события UI
        is Backspace             -> handleBackspace(s)
        is OpenSettings, 
        is OpenHistory           -> s // Навигация не меняет состояние калькулятора
    }

    // ─────────────────────────────────────────────────────────────────
    // ОБРАБОТЧИКИ ВВОДА
    // ─────────────────────────────────────────────────────────────────

    private fun handleDigit(s: Active, digit: String): Active {
        if (s.isError) return s
        val base = if (s.startNewInput || s.currentInput == DISPLAY_ZERO) "" else s.currentInput
        // Ограничение длины ввода
        if (base.replace("-", "").replace(".", "").length >= INPUT_MAX_LENGTH) return s
        val newInput = base + digit
        return s.copy(
            currentInput = newInput,
            displayText = CalculatorEngine.formatForDisplay(newInput.toDoubleOrNull() ?: 0.0),
            startNewInput = false,
            hasInput = true,
            activeOp = s.activeOp,
        )
    }

    private fun handleDecimal(s: Active): Active {
        if (s.isError) return s
        val base = if (s.startNewInput) "0" else s.currentInput
        if (base.contains('.')) return s
        val newInput = "$base."
        return s.copy(
            currentInput = newInput,
            displayText = newInput,
            startNewInput = false,
            hasInput = true,
        )
    }

    private fun handleClear(s: Active): Active {
        // AC — полный сброс; C — только текущий операнд
        return if (!s.hasInput && s.pendingOp == null) {
            Active() // полный сброс
        } else {
            s.copy(
                currentInput = DISPLAY_ZERO,
                displayText = DISPLAY_ZERO,
                hasInput = false,
                startNewInput = true,
                isError = false,
            )
        }
    }

    private fun handleNegate(s: Active): Active {
        if (s.isError) return s
        val current = currentDouble(s)
        return when (val r = CalculatorEngine.negate(current)) {
            is EngineResult.Value -> s.copy(
                currentInput = r.number.toString(),
                displayText = CalculatorEngine.formatForDisplay(r.number),
                accumulator = if (s.startNewInput) r.number else s.accumulator,
            )
            is EngineResult.Error -> errorState(s, r.message)
        }
    }

    private fun handlePercent(s: Active): Active {
        if (s.isError) return s
        val value = currentDouble(s)
        val context = if (s.pendingOp != null) s.accumulator else null
        return when (val r = CalculatorEngine.applyPercent(value, context)) {
            is EngineResult.Value -> s.copy(
                currentInput = r.number.toString(),
                displayText = CalculatorEngine.formatForDisplay(r.number),
                startNewInput = false,
            )
            is EngineResult.Error -> errorState(s, r.message)
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // ОПЕРАТОРЫ
    // ─────────────────────────────────────────────────────────────────

    private fun handleOperator(s: Active, op: BinaryOp): Active {
        if (s.isError) return s
        val rhs = currentDouble(s)

        // Если уже есть pending-оп и пользователь не начал новый ввод — вычисляем
        val newAcc = if (s.pendingOp != null && !s.startNewInput) {
            when (val r = CalculatorEngine.applyOperator(s.accumulator, s.pendingOp, rhs)) {
                is EngineResult.Value -> r.number
                is EngineResult.Error -> return errorState(s, r.message)
            }
        } else if (s.startNewInput && s.pendingOp != null) {
            // Повторное нажатие оператора без ввода — меняем оператор, не вычисляем
            return s.copy(pendingOp = op, activeOp = op)
        } else {
            rhs
        }

        return s.copy(
            accumulator = newAcc,
            pendingOp = op,
            activeOp = op,
            displayText = CalculatorEngine.formatForDisplay(newAcc),
            startNewInput = true,
            hasInput = false,
            justEvaluated = false,
        )
    }

    private fun handleEquals(s: Active): Active {
        if (s.isError) return s

        val rhs: Double
        val op: BinaryOp

        if (s.justEvaluated) {
            // Повторное = — применяем последний оператор и операнд
            rhs = s.lastRhs ?: currentDouble(s)
            op  = s.lastOp  ?: return s
        } else {
            rhs = currentDouble(s)
            op  = s.pendingOp ?: return s.copy(
                justEvaluated = true,
                lastRhs = currentDouble(s),
                startNewInput = true,
            )
        }

        return when (val r = CalculatorEngine.applyOperator(s.accumulator, op, rhs)) {
            is EngineResult.Value -> s.copy(
                accumulator = r.number,
                currentInput = r.number.toString(),
                displayText = CalculatorEngine.formatForDisplay(r.number),
                pendingOp = null,
                activeOp = null,
                startNewInput = true,
                hasInput = false,
                justEvaluated = true,
                lastRhs = rhs,
                lastOp = op,
                isError = false,
            )
            is EngineResult.Error -> errorState(s, r.message)
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // НАУЧНЫЙ РЕЖИМ
    // ─────────────────────────────────────────────────────────────────

    private fun handleScientific(s: Active, func: ScientificFunc): Active {
        if (s.isError) return s
        val value = currentDouble(s)
        return when (val r = CalculatorEngine.applyScientific(func, value, s.angleMode)) {
            is EngineResult.Value -> s.copy(
                currentInput = r.number.toString(),
                displayText = CalculatorEngine.formatForDisplay(r.number),
                accumulator = r.number,
                startNewInput = true,
                hasInput = false,
                justEvaluated = true,
            )
            is EngineResult.Error -> errorState(s, r.message)
        }
    }

    private fun handleConstant(s: Active, c: CalculatorConstantValue): Active {
        val v = CalculatorEngine.constant(c)
        return s.copy(
            currentInput = v.toString(),
            displayText = CalculatorEngine.formatForDisplay(v),
            startNewInput = false,
            hasInput = true,
        )
    }

    private fun handleOpenBracket(s: Active): Active {
        return s.copy(
            bracketStack = s.bracketStack + s.accumulator,
            bracketOps = s.bracketOps + s.pendingOp,
            accumulator = 0.0,
            pendingOp = null,
            currentInput = DISPLAY_ZERO,
            displayText = DISPLAY_ZERO,
            startNewInput = false,
            hasInput = false,
        )
    }

    private fun handleCloseBracket(s: Active): Active {
        if (s.bracketStack.isEmpty()) return s
        val innerResult = currentDouble(s)
        val restoredAcc = s.bracketStack.last()
        val restoredOp  = s.bracketOps.last()
        val newStack = s.bracketStack.dropLast(1)
        val newOps   = s.bracketOps.dropLast(1)

        val newAcc = if (restoredOp != null) {
            when (val r = CalculatorEngine.applyOperator(restoredAcc, restoredOp, innerResult)) {
                is EngineResult.Value -> r.number
                is EngineResult.Error -> return errorState(s, r.message)
            }
        } else innerResult

        return s.copy(
            accumulator = newAcc,
            bracketStack = newStack,
            bracketOps = newOps,
            pendingOp = null,
            currentInput = newAcc.toString(),
            displayText = CalculatorEngine.formatForDisplay(newAcc),
            startNewInput = true,
            hasInput = false,
        )
    }

    // ─────────────────────────────────────────────────────────────────
    // BACKSPACE (Новое)
    // ─────────────────────────────────────────────────────────────────

    private fun handleBackspace(s: Active): Active {
        if (s.isError) return s
        
        // Если показан готовый результат, делаем его редактируемым
        val rawText = if (s.startNewInput) {
            s.displayText.replace(" ", "").replace(",", ".").removeSuffix(".0")
        } else {
            s.currentInput
        }

        // Защита: если остался 1 символ (или "-"), сбрасываем в 0
        if (rawText.length <= 1) {
            return s.copy(
                currentInput = DISPLAY_ZERO,
                displayText = DISPLAY_ZERO,
                startNewInput = true,
                hasInput = false
            )
        }

        val newInput = rawText.dropLast(1)
        val finalInput = if (newInput == "-" || newInput.isEmpty()) DISPLAY_ZERO else newInput
        val display = finalInput.toDoubleOrNull()
            ?.takeIf { it.isFinite() }
            ?.let { CalculatorEngine.formatForDisplay(it) } 
            ?: finalInput

        return s.copy(
            currentInput = finalInput,
            displayText = display,
            startNewInput = false,
            hasInput = finalInput != DISPLAY_ZERO
        )
    }

    // ─────────────────────────────────────────────────────────────────
    // ВСПОМОГАТЕЛЬНЫЕ
    // ─────────────────────────────────────────────────────────────────

    private fun currentDouble(s: Active): Double =
        s.currentInput.toDoubleOrNull() ?: 0.0

    private fun errorState(s: Active, message: String): Active =
        s.copy(
            displayText = message,
            isError = true,
            pendingOp = null,
            activeOp = null,
            startNewInput = true,
        )
}
```

### ⚠️ Важно перед запуском:
Убедитесь, что в файле, где объявлен `CalculatorEvent` (обычно `state/CalculatorEvent.kt`), добавлены эти три события:
```kotlin
sealed interface CalculatorEvent {
    // ... ваши существующие события ...
    
    object Backspace : CalculatorEvent
    object OpenSettings : CalculatorEvent
    object OpenHistory : CalculatorEvent
}
```
Без этого компилятор выдаст `Unresolved reference`. Как только добавите их, код выше соберётся и заработает сразу. Файл полностью готов к копированию.
