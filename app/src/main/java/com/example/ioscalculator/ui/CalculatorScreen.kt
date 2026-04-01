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
import com.example.ioscalculator.state.CalculatorState.Active
import com.example.ioscalculator.ui.components.DisplayPanel
import com.example.ioscalculator.ui.components.LandscapeButtonGrid
import com.example.ioscalculator.ui.components.PortraitButtonGrid
import com.example.ioscalculator.ui.theme.CalcColors
import com.example.ioscalculator.viewmodel.CalculatorViewModel

/**
 * Единственный экран приложения.
 * Автоматически переключает раскладку при смене ориентации.
 * Состояние сохраняется через CalculatorViewModel (survives config change).
 */
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val config = LocalConfiguration.current
    val isLandscape = config.screenWidthDp > config.screenHeightDp

    // Размер кнопки адаптируется к размеру экрана
    val buttonSize = if (isLandscape) {
        ((config.screenHeightDp - 5 * 8) / 5).coerceIn(44, 72).dp
    } else {
        ((config.screenWidthDp - 4 * 8) / 4).coerceIn(60, 100).dp
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
                // Дисплей
                if (state is Active) {
                    DisplayPanel(
                        text = (state as Active).displayText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Сетка кнопок
                if (state is Active) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (landscape) {
                            LandscapeButtonGrid(
                                state = state as Active,
                                buttonSize = buttonSize,
                                onEvent = viewModel::onEvent,
                            )
                        } else {
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
    }
}
