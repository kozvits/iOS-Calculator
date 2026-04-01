# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# CalculatorEngine — чистые функции, не обфусцировать
-keep class com.example.ioscalculator.domain.** { *; }
-keep class com.example.ioscalculator.state.** { *; }
