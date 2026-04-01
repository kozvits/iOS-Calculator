package com.example.ioscalculator.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Модуль Hilt — зарезервирован для будущих зависимостей
 * (репозиторий истории, настройки через DataStore и т.д.).
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule
