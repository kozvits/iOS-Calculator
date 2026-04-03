package com.example.ioscalculator.domain

import com.example.ioscalculator.domain.CalculatorConstants.E
import com.example.ioscalculator.domain.CalculatorConstants.EPSILON
import com.example.ioscalculator.domain.CalculatorConstants.PI
import kotlin.math.*

/**
 * Чистый вычислительный движок без Android-зависимостей.
 * Все функции — статические (object), покрываемые юнит-тестами.
 */
object CalculatorEngine {

    // ───────────────────────────────────────────────
    // БАЗОВЫЕ ОПЕРАЦИИ
    // ───────────────────────────────────────────────

    /**
     * Применяет базовый бинарный оператор: result = left op right
     * Защита от деления на ноль.
     */
    fun applyOperator(left: Double, operator: BinaryOp, right: Double): EngineResult {
        val raw = when (operator) {
            BinaryOp.ADD      -> left + right
            BinaryOp.SUBTRACT -> left - right
            BinaryOp.MULTIPLY -> left * right
            BinaryOp.DIVIDE   -> {
                if (abs(right) < EPSILON) return EngineResult.Error("Деление на ноль")
                left / right
            }
            BinaryOp.POWER    -> left.pow(right)
        }
        return sanitize(raw)
    }

    /**
     * Логика % в стиле iOS:
     * - Если есть pending-операция (contextValue != null): contextValue * (x / 100)
     * - Иначе: x / 100
     */
    fun applyPercent(value: Double, contextValue: Double?): EngineResult {
        val result = if (contextValue != null) {
            contextValue * (value / 100.0)
        } else {
            value / 100.0
        }
        return sanitize(result)
    }

    /**
     * Унарная инверсия: -x
     */
    fun negate(value: Double): EngineResult = sanitize(-value)

    // ───────────────────────────────────────────────
    // НАУЧНЫЕ ФУНКЦИИ
    // ───────────────────────────────────────────────

    /**
     * Тригонометрические функции с переводом угла при необходимости.
     * В режиме DEG: x_rad = x * (PI / 180)
     */
    fun applyScientific(func: ScientificFunc, value: Double, angleMode: AngleMode): EngineResult {
        val x = if (angleMode == AngleMode.DEG && func.needsAngle) {
            value * PI / 180.0
        } else value

        val raw: Double = when (func) {
            ScientificFunc.SIN   -> sin(x)
            ScientificFunc.COS   -> cos(x)
            ScientificFunc.TAN   -> {
                val cosVal = cos(x)
                if (abs(cosVal) < EPSILON) return EngineResult.Error("Не определено")
                tan(x)
            }
            ScientificFunc.ASIN  -> {
                if (value < -1.0 || value > 1.0) return EngineResult.Error("Домен")
                val r = asin(value)
                if (angleMode == AngleMode.DEG) r * 180.0 / PI else r
            }
            ScientificFunc.ACOS  -> {
                if (value < -1.0 || value > 1.0) return EngineResult.Error("Домен")
                val r = acos(value)
                if (angleMode == AngleMode.DEG) r * 180.0 / PI else r
            }
            ScientificFunc.ATAN  -> {
                val r = atan(value)
                if (angleMode == AngleMode.DEG) r * 180.0 / PI else r
            }
            ScientificFunc.LOG   -> {
                if (value <= 0) return EngineResult.Error("Домен")
                log10(value)
            }
            ScientificFunc.LN    -> {
                if (value <= 0) return EngineResult.Error("Домен")
                ln(value)
            }
            ScientificFunc.SQRT  -> {
                if (value < 0) return EngineResult.Error("Домен")
                sqrt(value)
            }
            ScientificFunc.SQUARE    -> value.pow(2.0)
            ScientificFunc.CUBE      -> value.pow(3.0)
            ScientificFunc.RECIPROCAL -> {
                if (abs(value) < EPSILON) return EngineResult.Error("Деление на ноль")
                1.0 / value
            }
            ScientificFunc.RAND  -> Math.random()
        }
        return sanitize(raw)
    }

    /**
     * Вставка константы — просто возвращает значение.
     */
    fun constant(c: CalculatorConstantValue): Double = when (c) {
        CalculatorConstantValue.PI -> PI
        CalculatorConstantValue.E  -> E
    }

    // ───────────────────────────────────────────────
    // ФОРМАТИРОВАНИЕ ДИСПЛЕЯ
    // ───────────────────────────────────────────────

    /**
     * Форматирует Double для отображения:
     * - До 9 символов — с разделителями тысяч
     * - Иначе — E-нотация в компактном виде
     * - Обрезает незначащие нули после запятой
     */
    fun formatForDisplay(value: Double): String {
        if (value.isNaN()) return CalculatorConstants.DISPLAY_ERROR
        if (value.isInfinite()) return CalculatorConstants.DISPLAY_INFINITY

        val clean = cleanTinyValue(value)

        // Целое число — форматируем без дроби
        if (clean == kotlin.math.floor(clean) && abs(clean) < 1e15) {
            val long = clean.toLong()
            return formatWithThousands(long.toString())
        }

        // Пробуем представить с ограниченным числом знаков
        val formatted = "%.${CalculatorConstants.OUTPUT_DECIMAL_PLACES}f".format(clean)
            .trimEnd('0')
            .trimEnd('.')

        // Если символов больше лимита — переходим на E-нотацию
        return if (stripped(formatted).length > CalculatorConstants.DISPLAY_MAX_DIGITS) {
            compactScientific(clean)
        } else {
            formatWithThousands(formatted)
        }
    }

    // ───────────────────────────────────────────────
    // ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ
    // ───────────────────────────────────────────────

    /** Защита от NaN/Infinity и обрезка хвостов. */
    private fun sanitize(value: Double): EngineResult {
        if (value.isNaN()) return EngineResult.Error("Ошибка")
        if (value.isInfinite()) return EngineResult.Error("Переполнение")
        return EngineResult.Value(cleanTinyValue(value))
    }

    /** |x| < epsilon => 0 */
    private fun cleanTinyValue(v: Double): Double =
        if (abs(v) < EPSILON) 0.0 else v

    /** Убирает минус, точку, пробелы для подсчёта реальных цифр. */
    private fun stripped(s: String): String =
        s.replace("-", "").replace(".", "").replace(",", "").replace(" ", "")

    /** Вставляет разделители тысяч в целую часть числа. */
    private fun formatWithThousands(s: String): String {
        val dotIdx = s.indexOf('.')
        val intPart = if (dotIdx >= 0) s.substring(0, dotIdx) else s
        val fracPart = if (dotIdx >= 0) s.substring(dotIdx) else ""
        val negative = intPart.startsWith('-')
        val digits = if (negative) intPart.drop(1) else intPart
        val grouped = digits.reversed().chunked(3).joinToString(" ").reversed()
        return (if (negative) "-" else "") + grouped + fracPart
    }

    /** Компактная E-нотация: заменяет E+ формат на читаемый. */
    private fun compactScientific(v: Double): String {
        val s = "%.6e".format(v)
        val parts = s.split("e", ignoreCase = true)
        if (parts.size != 2) return s
        val mantissa = parts[0].trimEnd('0').trimEnd('.')
        val exp = parts[1].toIntOrNull() ?: return s
        return "${mantissa}e${exp}"
    }
}

// ───────────────────────────────────────────────
// ПЕРЕЧИСЛЕНИЯ ДОМЕНА
// ───────────────────────────────────────────────

enum class BinaryOp(val symbol: String) {
    ADD("+"), SUBTRACT("−"), MULTIPLY("×"), DIVIDE("÷"), POWER("xʸ")
}

enum class ScientificFunc(val label: String, val needsAngle: Boolean = false) {
    SIN("sin", true), COS("cos", true), TAN("tan", true),
    ASIN("sin⁻¹", true), ACOS("cos⁻¹", true), ATAN("tan⁻¹", true),
    LOG("log"), LN("ln"), SQRT("√"), SQUARE("x²"), CUBE("x³"),
    RECIPROCAL("1/x"), RAND("Rand")
}

enum class AngleMode { RAD, DEG }

enum class CalculatorConstantValue { PI, E }

/** Результат вычисления движка — либо значение, либо ошибка. */
sealed interface EngineResult {
    data class Value(val number: Double) : EngineResult
    data class Error(val message: String) : EngineResult
}
