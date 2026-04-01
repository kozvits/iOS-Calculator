package com.example.ioscalculator

import com.example.ioscalculator.domain.*
import org.junit.Assert.*
import org.junit.Test

class CalculatorEngineTest {

    // ── Базовые операции ──────────────────────────────────────────

    @Test fun `сложение двух положительных чисел`() {
        val r = CalculatorEngine.applyOperator(3.0, BinaryOp.ADD, 4.0)
        assertEquals(7.0, (r as EngineResult.Value).number, 1e-10)
    }

    @Test fun `деление на ноль возвращает ошибку`() {
        val r = CalculatorEngine.applyOperator(5.0, BinaryOp.DIVIDE, 0.0)
        assertTrue(r is EngineResult.Error)
    }

    @Test fun `вычитание`() {
        val r = CalculatorEngine.applyOperator(10.0, BinaryOp.SUBTRACT, 3.0)
        assertEquals(7.0, (r as EngineResult.Value).number, 1e-10)
    }

    @Test fun `умножение`() {
        val r = CalculatorEngine.applyOperator(6.0, BinaryOp.MULTIPLY, 7.0)
        assertEquals(42.0, (r as EngineResult.Value).number, 1e-10)
    }

    @Test fun `деление`() {
        val r = CalculatorEngine.applyOperator(10.0, BinaryOp.DIVIDE, 4.0)
        assertEquals(2.5, (r as EngineResult.Value).number, 1e-10)
    }

    @Test fun `возведение в степень`() {
        val r = CalculatorEngine.applyOperator(2.0, BinaryOp.POWER, 10.0)
        assertEquals(1024.0, (r as EngineResult.Value).number, 1e-10)
    }

    // ── Процент iOS-style ─────────────────────────────────────────

    @Test fun `100 + 10 процентов = 110`() {
        // При наличии контекста: 100 + 100 * (10/100) = 110
        val pct = CalculatorEngine.applyPercent(10.0, contextValue = 100.0)
        assertEquals(10.0, (pct as EngineResult.Value).number, 1e-10)
        val result = CalculatorEngine.applyOperator(100.0, BinaryOp.ADD, pct.number)
        assertEquals(110.0, (result as EngineResult.Value).number, 1e-10)
    }

    @Test fun `процент без контекста делит на 100`() {
        val r = CalculatorEngine.applyPercent(50.0, contextValue = null)
        assertEquals(0.5, (r as EngineResult.Value).number, 1e-10)
    }

    @Test fun `процент от нуля = 0`() {
        val r = CalculatorEngine.applyPercent(0.0, contextValue = 200.0)
        assertEquals(0.0, (r as EngineResult.Value).number, 1e-10)
    }

    // ── Научные функции ───────────────────────────────────────────

    @Test fun `sin 90 градусов = 1`() {
        val r = CalculatorEngine.applyScientific(ScientificFunc.SIN, 90.0, AngleMode.DEG)
        assertEquals(1.0, (r as EngineResult.Value).number, 1e-10)
    }

    @Test fun `sin 0 радиан = 0`() {
        val r = CalculatorEngine.applyScientific(ScientificFunc.SIN, 0.0, AngleMode.RAD)
        assertEquals(0.0, (r as EngineResult.Value).number, 1e-10)
    }

    @Test fun `cos 0 = 1`() {
        val r = CalculatorEngine.applyScientific(ScientificFunc.COS, 0.0, AngleMode.DEG)
        assertEquals(1.0, (r as EngineResult.Value).number, 1e-10)
    }

    @Test fun `sqrt отрицательного числа — ошибка`() {
        val r = CalculatorEngine.applyScientific(ScientificFunc.SQRT, -1.0, AngleMode.RAD)
        assertTrue(r is EngineResult.Error)
    }

    @Test fun `sqrt 4 = 2`() {
        val r = CalculatorEngine.applyScientific(ScientificFunc.SQRT, 4.0, AngleMode.RAD)
        assertEquals(2.0, (r as EngineResult.Value).number, 1e-10)
    }

    @Test fun `ln 1 = 0`() {
        val r = CalculatorEngine.applyScientific(ScientificFunc.LN, 1.0, AngleMode.RAD)
        assertEquals(0.0, (r as EngineResult.Value).number, 1e-10)
    }

    @Test fun `log 1000 = 3`() {
        val r = CalculatorEngine.applyScientific(ScientificFunc.LOG, 1000.0, AngleMode.RAD)
        assertEquals(3.0, (r as EngineResult.Value).number, 1e-10)
    }

    @Test fun `log отрицательного числа — ошибка`() {
        val r = CalculatorEngine.applyScientific(ScientificFunc.LOG, -5.0, AngleMode.RAD)
        assertTrue(r is EngineResult.Error)
    }

    @Test fun `x в квадрате`() {
        val r = CalculatorEngine.applyScientific(ScientificFunc.SQUARE, 5.0, AngleMode.RAD)
        assertEquals(25.0, (r as EngineResult.Value).number, 1e-10)
    }

    @Test fun `x в кубе`() {
        val r = CalculatorEngine.applyScientific(ScientificFunc.CUBE, 3.0, AngleMode.RAD)
        assertEquals(27.0, (r as EngineResult.Value).number, 1e-10)
    }

    @Test fun `1 делить на x`() {
        val r = CalculatorEngine.applyScientific(ScientificFunc.RECIPROCAL, 4.0, AngleMode.RAD)
        assertEquals(0.25, (r as EngineResult.Value).number, 1e-10)
    }

    @Test fun `1 делить на 0 — ошибка`() {
        val r = CalculatorEngine.applyScientific(ScientificFunc.RECIPROCAL, 0.0, AngleMode.RAD)
        assertTrue(r is EngineResult.Error)
    }

    @Test fun `отрицание нуля = 0`() {
        val r = CalculatorEngine.negate(0.0)
        assertEquals(0.0, (r as EngineResult.Value).number, 1e-10)
    }

    @Test fun `отрицание положительного числа`() {
        val r = CalculatorEngine.negate(5.0)
        assertEquals(-5.0, (r as EngineResult.Value).number, 1e-10)
    }

    // ── Форматирование ────────────────────────────────────────────

    @Test fun `целое число форматируется без дроби`() {
        assertEquals("1,000", CalculatorEngine.formatForDisplay(1000.0))
    }

    @Test fun `маленькое число ниже эпсилон = 0`() {
        assertEquals("0", CalculatorEngine.formatForDisplay(1e-14))
    }

    @Test fun `большое число переходит в E-нотацию`() {
        val s = CalculatorEngine.formatForDisplay(1.23456789e15)
        assertTrue(s.contains('e'))
    }

    @Test fun `дробное число форматируется корректно`() {
        val s = CalculatorEngine.formatForDisplay(3.14159)
        assertTrue(s.startsWith("3.14159"))
    }

    @Test fun `константа PI корректна`() {
        val pi = CalculatorEngine.constant(CalculatorConstantValue.PI)
        assertEquals(Math.PI, pi, 1e-10)
    }

    @Test fun `константа E корректна`() {
        val e = CalculatorEngine.constant(CalculatorConstantValue.E)
        assertEquals(Math.E, e, 1e-10)
    }
}
