package com.example.ioscalculator

import com.example.ioscalculator.domain.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Юнит-тесты для CalculatorEngine.
 * Разделитель тысяч — точка "." (европейский формат).
 */
class CalculatorEngineTest {

    // ── applyOperator ────────────────────────────────────────────────────────

    @Test
    fun `сложение двух положительных чисел`() {
        val result = CalculatorEngine.applyOperator(2.0, BinaryOp.ADD, 3.0)
        assertEquals(EngineResult.Value(5.0), result)
    }

    @Test
    fun `вычитание возвращает отрицательный результат`() {
        val result = CalculatorEngine.applyOperator(3.0, BinaryOp.SUBTRACT, 7.0)
        assertEquals(EngineResult.Value(-4.0), result)
    }

    @Test
    fun `умножение на ноль даёт ноль`() {
        val result = CalculatorEngine.applyOperator(999.0, BinaryOp.MULTIPLY, 0.0)
        assertEquals(EngineResult.Value(0.0), result)
    }

    @Test
    fun `деление на ноль возвращает Error`() {
        val result = CalculatorEngine.applyOperator(5.0, BinaryOp.DIVIDE, 0.0)
        assertTrue(result is EngineResult.Error)
    }

    @Test
    fun `деление с точным результатом`() {
        val result = CalculatorEngine.applyOperator(10.0, BinaryOp.DIVIDE, 4.0)
        assertEquals(EngineResult.Value(2.5), result)
    }

    @Test
    fun `возведение в степень`() {
        val result = CalculatorEngine.applyOperator(2.0, BinaryOp.POWER, 10.0)
        assertEquals(EngineResult.Value(1024.0), result)
    }

    // ── applyPercent ─────────────────────────────────────────────────────────

    @Test
    fun `процент без контекста делит на 100`() {
        val result = CalculatorEngine.applyPercent(50.0, null)
        assertEquals(EngineResult.Value(0.5), result)
    }

    @Test
    fun `процент с контекстом iOS-стиль`() {
        // 200 + 10% = 200 + 20 = 220: context=200, value=10
        val result = CalculatorEngine.applyPercent(10.0, 200.0)
        assertEquals(EngineResult.Value(20.0), result)
    }

    // ── negate ───────────────────────────────────────────────────────────────

    @Test
    fun `инверсия положительного числа`() {
        val result = CalculatorEngine.negate(42.0)
        assertEquals(EngineResult.Value(-42.0), result)
    }

    @Test
    fun `инверсия нуля остаётся нулём`() {
        val result = CalculatorEngine.negate(0.0)
        assertEquals(EngineResult.Value(0.0), result)
    }

    // ── applyScientific ──────────────────────────────────────────────────────

    @Test
    fun `sin нуля равен нулю`() {
        val result = CalculatorEngine.applyScientific(ScientificFunc.SIN, 0.0, AngleMode.RAD)
        assertEquals(EngineResult.Value(0.0), result)
    }

    @Test
    fun `cos нуля равен единице`() {
        val result = CalculatorEngine.applyScientific(ScientificFunc.COS, 0.0, AngleMode.RAD)
        assertEquals(EngineResult.Value(1.0), result)
    }

    @Test
    fun `sqrt четырёх равен двум`() {
        val result = CalculatorEngine.applyScientific(ScientificFunc.SQRT, 4.0, AngleMode.RAD)
        assertEquals(EngineResult.Value(2.0), result)
    }

    @Test
    fun `sqrt отрицательного числа возвращает Error`() {
        val result = CalculatorEngine.applyScientific(ScientificFunc.SQRT, -1.0, AngleMode.RAD)
        assertTrue(result is EngineResult.Error)
    }

    @Test
    fun `log нуля и отрицательного возвращает Error`() {
        assertTrue(CalculatorEngine.applyScientific(ScientificFunc.LOG, 0.0, AngleMode.RAD) is EngineResult.Error)
        assertTrue(CalculatorEngine.applyScientific(ScientificFunc.LOG, -5.0, AngleMode.RAD) is EngineResult.Error)
    }

    @Test
    fun `ln единицы равен нулю`() {
        val result = CalculatorEngine.applyScientific(ScientificFunc.LN, 1.0, AngleMode.RAD)
        assertEquals(EngineResult.Value(0.0), result)
    }

    @Test
    fun `sin 90 градусов равен 1`() {
        val result = CalculatorEngine.applyScientific(ScientificFunc.SIN, 90.0, AngleMode.DEG)
        val v = (result as EngineResult.Value).number
        assertEquals(1.0, v, 1e-10)
    }

    @Test
    fun `обратная величина нуля возвращает Error`() {
        val result = CalculatorEngine.applyScientific(ScientificFunc.RECIPROCAL, 0.0, AngleMode.RAD)
        assertTrue(result is EngineResult.Error)
    }

    @Test
    fun `квадрат числа`() {
        val result = CalculatorEngine.applyScientific(ScientificFunc.SQUARE, 5.0, AngleMode.RAD)
        assertEquals(EngineResult.Value(25.0), result)
    }

    @Test
    fun `куб числа`() {
        val result = CalculatorEngine.applyScientific(ScientificFunc.CUBE, 3.0, AngleMode.RAD)
        assertEquals(EngineResult.Value(27.0), result)
    }

    // ── formatForDisplay ─────────────────────────────────────────────────────

    @Test
    fun `ноль форматируется как строка ноль`() {
        assertEquals("0", CalculatorEngine.formatForDisplay(0.0))
    }

    @Test
    fun `целое число форматируется без дроби`() {
        // Разделитель тысяч — точка
        assertEquals("1000", CalculatorEngine.formatForDisplay(1000.0))
    }

    @Test
    fun `большое число форматируется с разделителями через точку`() {
        assertEquals("1000000", CalculatorEngine.formatForDisplay(1_000_000.0))
    }

    @Test
    fun `дробное число обрезает незначащие нули`() {
        val result = CalculatorEngine.formatForDisplay(1.5)
        assertEquals("1.5", result)
    }

    @Test
    fun `отрицательное число форматируется с минусом`() {
        assertEquals("-42", CalculatorEngine.formatForDisplay(-42.0))
    }

    @Test
    fun `очень маленькое число близкое к нулю становится нулём`() {
        assertEquals("0", CalculatorEngine.formatForDisplay(1e-13))
    }

    @Test
    fun `константа PI форматируется корректно`() {
        val result = CalculatorEngine.formatForDisplay(Math.PI)
        assertTrue(result.startsWith("3.14159"))
    }

    // ── constant ─────────────────────────────────────────────────────────────

    @Test
    fun `константа PI совпадает с Math PI`() {
        assertEquals(Math.PI, CalculatorEngine.constant(CalculatorConstantValue.PI), 1e-15)
    }

    @Test
    fun `константа E совпадает с Math E`() {
        assertEquals(Math.E, CalculatorEngine.constant(CalculatorConstantValue.E), 1e-15)
    }
}
