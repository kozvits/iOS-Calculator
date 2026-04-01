# iOS Calculator Clone — Android

Точная визуальная и алгоритмическая копия нативного калькулятора iOS (iPhone 15 / iOS 17+), реализованная на современном Android-стеке.

## Стек

| Слой        | Технология                              |
|-------------|-----------------------------------------|
| Язык        | Kotlin 1.9+                             |
| UI          | Jetpack Compose (кастомная отрисовка)   |
| Архитектура | MVI + StateFlow                         |
| DI          | Hilt                                    |
| Coroutines  | kotlinx.coroutines 1.8+                 |
| Min SDK     | 24 (Android 7.0)                        |
| Target SDK  | 34 (Android 14)                         |

## Возможности

- **Портрет**: стандартный калькулятор (4×5 сетка)
- **Ландшафт**: научный режим с тригонометрией, логарифмами, скобками
- **iOS-логика**: немедленное выполнение операций (не AST)
- **Процент**: `100 + 10% = 110` (iOS-поведение)
- **Haptic Feedback**: тактильная отдача при каждом нажатии
- **Анимации**: масштабирование кнопок при нажатии
- **Поворот**: плавная смена раскладки с сохранением состояния

## Сборка

```bash
# Клонировать репозиторий
git clone https://github.com/YOUR_USERNAME/IOSCalculator.git
cd IOSCalculator

# Debug-сборка
./gradlew assembleDebug

# Запуск unit-тестов
./gradlew :app:testDebugUnitTest

# Установка на подключённое устройство
./gradlew installDebug
```

## Структура проекта

```
app/src/main/java/com/example/ioscalculator/
├── domain/
│   ├── CalculatorConstants.kt   # Все константы
│   ├── CalculatorEngine.kt      # Чистая логика (без Android)
│   └── CalculatorRepository.kt  # Точка расширения
├── state/
│   ├── CalculatorEvent.kt       # MVI Intent
│   └── CalculatorState.kt       # MVI State
├── viewmodel/
│   └── CalculatorViewModel.kt   # Редуктор + StateFlow
├── ui/
│   ├── CalculatorScreen.kt      # Главный экран
│   ├── components/
│   │   ├── CalcButton.kt        # Кнопка с анимацией
│   │   ├── DisplayPanel.kt      # Дисплей с авто-масштабом
│   │   └── ButtonGrid.kt        # Портрет + Ландшафт сетки
│   └── theme/
│       └── CalcColors.kt        # Цвета iOS-палитры
├── di/
│   └── AppModule.kt             # Hilt-модуль
├── MainActivity.kt
└── IOSCalculatorApp.kt
```

## Тестирование граничных случаев

| Действие              | Результат              |
|-----------------------|------------------------|
| `5 + 3 × 2 =`        | `16` (не 11!)          |
| `100 + 10% =`         | `110`                  |
| `5 ÷ 0 =`            | `Деление на ноль`      |
| `√(-1)`               | `Домен`                |
| `0.1 + 0.2 =`         | `0.3`                  |
| `3 + 5 = = =`         | `8 → 13 → 18`          |
| Поворот во время ввода | Дисплей сохраняется   |
| `sin(90°)` в DEG      | `1`                    |

## Лицензия

Проект создан в образовательных целях. Не использует ассеты Apple.
