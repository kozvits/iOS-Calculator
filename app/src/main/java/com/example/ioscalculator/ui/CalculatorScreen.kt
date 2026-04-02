package com.example.ioscalculator.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ioscalculator.state.CalculatorEvent
import com.example.ioscalculator.state.CalculatorState.Active
import com.example.ioscalculator.ui.components.CalculatorUtilityBar
import com.example.ioscalculator.ui.components.DisplayPanel
import com.example.ioscalculator.ui.components.HistoryDialog
import com.example.ioscalculator.ui.components.LandscapeButtonGrid
import com.example.ioscalculator.ui.components.PortraitButtonGrid
import com.example.ioscalculator.ui.components.SettingsDialog
import com.example.ioscalculator.ui.theme.CalcColors
import com.example.ioscalculator.viewmodel.CalculatorViewModel

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val config = LocalConfiguration.current
    val isLandscape = config.screenWidthDp > config.screenHeightDp

    // 🔧 Оптимизированный расчет: учтена высота панели утилит (~48dp) + отступы
    val buttonSize = if (isLandscape) {
        ((config.screenHeightDp - 100) / 5).coerceIn(36, 56).dp
    } else {
        ((config.screenWidthDp - 32) / 4).coerceIn(68, 96).dp
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CalcColors.Background)
            .systemBarsPadding(),
    ) {
        AnimatedContent(
            targetState = isLandscape,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "orientationSwitch",
        ) { landscape ->
            if (landscape) {
                // Ландшафтная ориентация: экран сверху, кнопки снизу
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                ) {
                    // 1️⃣ Дисплей в верхней части
                    if (state is Active) {
                        DisplayPanel(
                            text = (state as Active).displayText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.3f)
                                .padding(top = 8.dp, bottom = 8.dp),
                        )
                    }

                    // 2️⃣ Панель утилит
                    if (state is Active) {
                        CalculatorUtilityBar(
                            onSettingsClick  = { viewModel.onEvent(CalculatorEvent.OpenSettings) },
                            onHistoryClick   = { viewModel.onEvent(CalculatorEvent.OpenHistory)  },
                            onBackspaceClick = { viewModel.onEvent(CalculatorEvent.Backspace)    }
                        )
                        Spacer(Modifier.height(4.dp))
                    }

                    // 3️⃣ Сетка кнопок
                    if (state is Active) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.7f),
                            contentAlignment = Alignment.Center,
                        ) {
                            LandscapeButtonGrid(
                                state = state as Active,
                                buttonSize = buttonSize,
                                onEvent = viewModel::onEvent,
                            )
                        }
                    }
                }
            } else {
                // Портретная ориентация
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    // 1️⃣ Дисплей (занимает всё свободное место)
                    if (state is Active) {
                        DisplayPanel(
                            text = (state as Active).displayText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(bottom = 8.dp),
                        )
                    }

                    // 🔹 2️⃣ Панель утилит (фиксированная высота, не сжимается)
                    if (state is Active) {
                        CalculatorUtilityBar(
                            onSettingsClick  = { viewModel.onEvent(CalculatorEvent.OpenSettings) },
                            onHistoryClick   = { viewModel.onEvent(CalculatorEvent.OpenHistory)  },
                            onBackspaceClick = { viewModel.onEvent(CalculatorEvent.Backspace)    }
                        )
                        Spacer(Modifier.height(4.dp))
                    }

                    // 3️⃣ Сетка кнопок
                    if (state is Active) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            PortraitButtonGrid(
                                state = state as Active,
                                buttonSize = buttonSize,
                                onEvent = viewModel::onEvent,
                            )
                        }
                    }
                }
            }
        }

        // Диалоги поверх основного контента
        if (state is Active) {
            val activeState = state as Active
            if (activeState.showHistory) {
                HistoryDialog(
                    history = activeState.history,
                    onDismiss = { viewModel.onEvent(CalculatorEvent.OpenHistory) },
                )
            }
            if (activeState.showSettings) {
                SettingsDialog(
                    onDismiss = { viewModel.onEvent(CalculatorEvent.OpenSettings) },
                )
            }
        }
    }
}
