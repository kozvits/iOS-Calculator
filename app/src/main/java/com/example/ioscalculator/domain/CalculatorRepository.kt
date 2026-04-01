package com.example.ioscalculator.domain

/**
 * Репозиторий — зарезервирован для будущего персистентного хранения
 * истории вычислений (Room / DataStore).
 * Сейчас реализация пустая — используется как точка расширения.
 */
interface CalculatorRepository {
    suspend fun saveResult(expression: String, result: String)
    suspend fun getHistory(): List<Pair<String, String>>
}

class InMemoryCalculatorRepository : CalculatorRepository {
    private val history = mutableListOf<Pair<String, String>>()

    override suspend fun saveResult(expression: String, result: String) {
        history.add(expression to result)
    }

    override suspend fun getHistory(): List<Pair<String, String>> = history.toList()
}
