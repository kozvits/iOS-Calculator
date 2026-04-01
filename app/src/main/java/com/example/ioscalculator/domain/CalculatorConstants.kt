package com.example.ioscalculator.domain

/**
 * Все магические числа и строковые константы вынесены сюда.
 * Математические значения: PI, e, пороги точности.
 */
object CalculatorConstants {
    // Порог обрезки хвостов: значения |x| < epsilon трактуются как 0
    const val EPSILON: Double = 1e-12

    // Максимум символов на дисплее до перехода в E-нотацию
    const val DISPLAY_MAX_DIGITS: Int = 9

    // Максимальное количество цифр ввода
    const val INPUT_MAX_LENGTH: Int = 9

    // Множитель масштаба при нажатии кнопки: 0.95
    const val PRESS_SCALE: Float = 0.95f

    // Длительность анимации нажатия (мс)
    const val PRESS_ANIM_DURATION: Int = 80

    // Длительность haptic-вибрации (мс)
    const val HAPTIC_DURATION_MS: Long = 20L

    // Форматирование: кол-во знаков после запятой при выводе
    const val OUTPUT_DECIMAL_PLACES: Int = 10

    const val PI: Double = Math.PI
    const val E: Double = Math.E

    // Строки состояния дисплея
    const val DISPLAY_ERROR: String = "Ошибка"
    const val DISPLAY_ZERO: String = "0"
    const val DISPLAY_INFINITY: String = "∞"
}
