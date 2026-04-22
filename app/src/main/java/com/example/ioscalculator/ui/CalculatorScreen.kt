package com.example.ioscalculator.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ioscalculator.domain.*
import com.example.ioscalculator.state.CalculatorEvent
import com.example.ioscalculator.state.CalculatorState
import com.example.ioscalculator.viewmodel.CalculatorSettings
import com.example.ioscalculator.viewmodel.CalculatorViewModel
import com.example.ioscalculator.viewmodel.CalcTheme

// ── Цвета ──────────────────────────────────────────────────────────────────
private fun themeColors(theme: CalcTheme) = when (theme) {
    CalcTheme.DARK -> ThemeColors(
        bg      = Color(0xFF000000),
        dark    = Color(0xFF333336),
        gray    = Color(0xFFA5A5A5),
        orange  = Color(0xFFFF9F0A),
        white   = Color.White,
        display = Color.White,
        subtext = Color(0xFF8E8E93),
        sciDark = Color(0xFF1C1C1E)
    )
    CalcTheme.LIGHT -> ThemeColors(
        bg      = Color(0xFFFFFFFF),
        dark    = Color(0xFFF2F2F7),
        gray    = Color(0xFFD1D1D6),
        orange  = Color(0xFFFF9F0A),
        white   = Color.Black,
        display = Color.Black,
        subtext = Color(0xFF8E8E93),
        sciDark = Color(0xFFF2F2F7)
    )
}

private data class ThemeColors(
    val bg: Color,
    val dark: Color,
    val gray: Color,
    val orange: Color,
    val white: Color,
    val display: Color,
    val subtext: Color,
    val sciDark: Color
)

@Composable
fun CalculatorScreen(vm: CalculatorViewModel = hiltViewModel()) {
    val state       by vm.state.collectAsStateWithLifecycle()
    val history     by vm.history.collectAsStateWithLifecycle()
    val settings    by vm.settings.collectAsStateWithLifecycle()
    val showHistory by vm.showHistory.collectAsStateWithLifecycle()
    val showSettings by vm.showSettings.collectAsStateWithLifecycle()

    val active = state as? CalculatorState.Active ?: return
    val isLandscape = LocalConfiguration.current.orientation ==
            android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val colors = themeColors(settings.theme)

    Box(
        Modifier
            .fillMaxSize()
            .background(colors.bg)
            .systemBarsPadding()
    ) {
        if (isLandscape) {
            LandscapeLayout(active, vm::onEvent, colors)
        } else {
            PortraitLayout(active, vm::onEvent, colors)
        }

        // ── История (bottom sheet стиль) ──────────────────────────────────
        AnimatedVisibility(
            visible = showHistory,
            enter = slideInVertically { it } + fadeIn(),
            exit  = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            HistorySheet(
                entries = history,
                onDismiss = vm::dismissHistory,
                onClear = vm::clearHistory,
                colors = colors
            )
        }

        // ── Настройки ─────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = showSettings,
            enter = slideInVertically { it } + fadeIn(),
            exit  = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            SettingsSheet(
                settings = settings,
                onDismiss = vm::dismissSettings,
                onUpdate  = vm::updateSettings,
                colors = colors
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// PORTRAIT LAYOUT
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun PortraitLayout(
    state: CalculatorState.Active,
    onEvent: (CalculatorEvent) -> Unit,
    colors: ThemeColors
) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        // Дисплей
        PortraitDisplay(state, colors)

        // Иконки под дисплеем, справа
        TopIconRow(onEvent, colors)

        // Кнопки
        Spacer(Modifier.height(4.dp))
        PortraitButtons(state, onEvent, colors)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun TopIconRow(
    onEvent: (CalculatorEvent) -> Unit,
    colors: ThemeColors = themeColors(CalcTheme.DARK)
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // История
        IconButton(onClick = { onEvent(CalculatorEvent.OpenHistory) }) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = "История",
                tint = colors.subtext,
                modifier = Modifier.size(24.dp)
            )
        }
        // Настройки
        IconButton(onClick = { onEvent(CalculatorEvent.OpenSettings) }) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Настройки",
                tint = colors.subtext,
                modifier = Modifier.size(24.dp)
            )
        }
        // Backspace — стрелка влево как в iOS
        IconButton(onClick = { onEvent(CalculatorEvent.Backspace) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Backspace",
                tint = colors.subtext,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PortraitDisplay(
    state: CalculatorState.Active,
    colors: ThemeColors = themeColors(CalcTheme.DARK)
) {
    val mainFontSize = when {
        state.displayText.length > 11 -> 44.sp
        state.displayText.length > 8  -> 60.sp
        state.displayText.length > 6  -> 72.sp
        else -> 88.sp
    }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom,
    ) {
        // Верхняя строка — выражение (появляется при наличии)
        AnimatedVisibility(
            visible = state.expressionText.isNotEmpty(),
            enter = fadeIn() + slideInVertically { -it / 2 },
            exit  = fadeOut(),
        ) {
            Text(
                text = state.expressionText,
                fontSize = 22.sp,
                fontWeight = FontWeight.W300,
                color = colors.subtext,
                textAlign = TextAlign.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(4.dp))
        // Нижняя строка — текущее число / результат
        Text(
            text = state.displayText,
            fontSize = mainFontSize,
            fontWeight = FontWeight.W200,
            color = colors.display,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PortraitButtons(
    state: CalculatorState.Active,
    onEvent: (CalculatorEvent) -> Unit,
    colors: ThemeColors
) {
    val gap = 12.dp
    Column(
        Modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        val acLabel = if (state.hasInput && !state.startNewInput) "C" else "AC"
        // Row 1
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
            CalcButton(acLabel, BtnStyle.Gray, Modifier.weight(1f), colors = colors) { onEvent(CalculatorEvent.ClearPressed) }
            CalcButton("+/-",  BtnStyle.Gray, Modifier.weight(1f), colors = colors) { onEvent(CalculatorEvent.NegatePressed) }
            CalcButton("%",    BtnStyle.Gray, Modifier.weight(1f), colors = colors) { onEvent(CalculatorEvent.PercentPressed) }
            CalcButton("÷",    BtnStyle.Orange, Modifier.weight(1f), active = state.activeOp == BinaryOp.DIVIDE, colors = colors)
            { onEvent(CalculatorEvent.OperatorPressed(BinaryOp.DIVIDE)) }
        }
        // Row 2
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
            for (d in listOf("7","8","9")) {
                CalcButton(d, BtnStyle.Dark, Modifier.weight(1f), colors = colors) { onEvent(CalculatorEvent.DigitPressed(d)) }
            }
            CalcButton("×", BtnStyle.Orange, Modifier.weight(1f), active = state.activeOp == BinaryOp.MULTIPLY, colors = colors)
            { onEvent(CalculatorEvent.OperatorPressed(BinaryOp.MULTIPLY)) }
        }
        // Row 3
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
            for (d in listOf("4","5","6")) {
                CalcButton(d, BtnStyle.Dark, Modifier.weight(1f), colors = colors) { onEvent(CalculatorEvent.DigitPressed(d)) }
            }
            CalcButton("−", BtnStyle.Orange, Modifier.weight(1f), active = state.activeOp == BinaryOp.SUBTRACT, colors = colors)
            { onEvent(CalculatorEvent.OperatorPressed(BinaryOp.SUBTRACT)) }
        }
        // Row 4
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
            for (d in listOf("1","2","3")) {
                CalcButton(d, BtnStyle.Dark, Modifier.weight(1f), colors = colors) { onEvent(CalculatorEvent.DigitPressed(d)) }
            }
            CalcButton("+", BtnStyle.Orange, Modifier.weight(1f), active = state.activeOp == BinaryOp.ADD, colors = colors)
            { onEvent(CalculatorEvent.OperatorPressed(BinaryOp.ADD)) }
        }
        // Row 5 — ноль широкий
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
            CalcButton("0", BtnStyle.Dark, Modifier.weight(2f), wide = true, colors = colors) { onEvent(CalculatorEvent.DigitPressed("0")) }
            CalcButton(".", BtnStyle.Dark, Modifier.weight(1f), colors = colors) { onEvent(CalculatorEvent.DecimalPressed) }
            CalcButton("=", BtnStyle.Orange, Modifier.weight(1f), colors = colors) { onEvent(CalculatorEvent.EqualsPressed) }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// LANDSCAPE LAYOUT — научный режим с дисплеем
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun LandscapeLayout(
    state: CalculatorState.Active,
    onEvent: (CalculatorEvent) -> Unit,
    colors: ThemeColors
) {
    val gap = 6.dp
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // Всего 6 рядов кнопок + дисплей (44dp) + отступы
        val displayH = 56.dp
        val totalGaps = gap * 7 // 6 рядов = 7 промежутков (включая под дисплеем)
        val btnH = (maxHeight - displayH - totalGaps) / 6

        Row(
            Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            // ── Левая колонка: научные кнопки ──────────────────────────
            Column(
                Modifier.weight(1.1f),
                verticalArrangement = Arrangement.spacedBy(gap)
            ) {
                // Ряд 0: DEG/RAD + скобки
                Row(
                    Modifier.fillMaxWidth().height(btnH),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AngleModeToggle(
                        current = state.angleMode,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = colors
                    ) { onEvent(CalculatorEvent.AngleModeChanged(it)) }
                    LandBtn("(", BtnStyle.Dark, btnH, Modifier.weight(1f), 16, colors = colors)
                    { onEvent(CalculatorEvent.OpenBracket) }
                    LandBtn(")", BtnStyle.Dark, btnH, Modifier.weight(1f), 16, colors = colors)
                    { onEvent(CalculatorEvent.CloseBracket) }
                }
                // Ряды 1-5: научные функции
                val sciRows = listOf(
                    listOf(ScientificFunc.SQUARE to "x²",   ScientificFunc.CUBE to "x³",    null to "xʸ",       ScientificFunc.RECIPROCAL to "1/x"),
                    listOf(ScientificFunc.SQRT   to "√x",   ScientificFunc.LOG  to "log",   ScientificFunc.LN   to "ln",    null to "eˣ"),
                    listOf(ScientificFunc.SIN    to "sin",  ScientificFunc.COS  to "cos",   ScientificFunc.TAN  to "tan",   ScientificFunc.RAND to "Rand"),
                    listOf(ScientificFunc.ASIN   to "sin⁻¹",ScientificFunc.ACOS to "cos⁻¹",ScientificFunc.ATAN to "tan⁻¹",null to "π"),
                    listOf(null to "e",            null to "EE",              null to "mc",               null to "mr"),
                )
                sciRows.forEach { row ->
                    Row(
                        Modifier.fillMaxWidth().height(btnH),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        row.forEach { (func, label) ->
                            LandBtn(label, BtnStyle.SciDark, btnH, Modifier.weight(1f), 13, colors = colors) {
                                when {
                                    func != null  -> onEvent(CalculatorEvent.ScientificFuncPressed(func))
                                    label == "π"  -> onEvent(CalculatorEvent.ConstantPressed(CalculatorConstantValue.PI))
                                    label == "e"  -> onEvent(CalculatorEvent.ConstantPressed(CalculatorConstantValue.E))
                                    label == "xʸ" -> onEvent(CalculatorEvent.OperatorPressed(BinaryOp.POWER))
                                }
                            }
                        }
                    }
                }
            }

            // ── Правая колонка: дисплей + основные кнопки ──────────────
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(gap)
            ) {
                // Дисплей
                LandscapeDisplay(state, displayH, colors)

                // Основные кнопки
                val acLabel = if (state.hasInput && !state.startNewInput) "C" else "AC"
                Row(Modifier.fillMaxWidth().height(btnH), horizontalArrangement = Arrangement.spacedBy(gap)) {
                    LandBtn(acLabel, BtnStyle.Gray, btnH, Modifier.weight(1f), 16, colors = colors) { onEvent(CalculatorEvent.ClearPressed) }
                    LandBtn("+/-",  BtnStyle.Gray,   btnH, Modifier.weight(1f), 14, colors = colors) { onEvent(CalculatorEvent.NegatePressed) }
                    LandBtn("%",    BtnStyle.Gray,   btnH, Modifier.weight(1f), 16, colors = colors) { onEvent(CalculatorEvent.PercentPressed) }
                    LandBtn("÷",   BtnStyle.Orange, btnH, Modifier.weight(1f), 18, active = state.activeOp == BinaryOp.DIVIDE, colors = colors)
                    { onEvent(CalculatorEvent.OperatorPressed(BinaryOp.DIVIDE)) }
                }
                Row(Modifier.fillMaxWidth().height(btnH), horizontalArrangement = Arrangement.spacedBy(gap)) {
                    listOf("7","8","9").forEach { d -> LandBtn(d, BtnStyle.Dark, btnH, Modifier.weight(1f), 18, colors = colors) { onEvent(CalculatorEvent.DigitPressed(d)) } }
                    LandBtn("×", BtnStyle.Orange, btnH, Modifier.weight(1f), 18, active = state.activeOp == BinaryOp.MULTIPLY, colors = colors)
                    { onEvent(CalculatorEvent.OperatorPressed(BinaryOp.MULTIPLY)) }
                }
                Row(Modifier.fillMaxWidth().height(btnH), horizontalArrangement = Arrangement.spacedBy(gap)) {
                    listOf("4","5","6").forEach { d -> LandBtn(d, BtnStyle.Dark, btnH, Modifier.weight(1f), 18, colors = colors) { onEvent(CalculatorEvent.DigitPressed(d)) } }
                    LandBtn("−", BtnStyle.Orange, btnH, Modifier.weight(1f), 18, active = state.activeOp == BinaryOp.SUBTRACT, colors = colors)
                    { onEvent(CalculatorEvent.OperatorPressed(BinaryOp.SUBTRACT)) }
                }
                Row(Modifier.fillMaxWidth().height(btnH), horizontalArrangement = Arrangement.spacedBy(gap)) {
                    listOf("1","2","3").forEach { d -> LandBtn(d, BtnStyle.Dark, btnH, Modifier.weight(1f), 18, colors = colors) { onEvent(CalculatorEvent.DigitPressed(d)) } }
                    LandBtn("+", BtnStyle.Orange, btnH, Modifier.weight(1f), 18, active = state.activeOp == BinaryOp.ADD, colors = colors)
                    { onEvent(CalculatorEvent.OperatorPressed(BinaryOp.ADD)) }
                }
                Row(Modifier.fillMaxWidth().height(btnH), horizontalArrangement = Arrangement.spacedBy(gap)) {
                    LandBtn("0", BtnStyle.Dark, btnH, Modifier.weight(2f), 18, wide = true, colors = colors) { onEvent(CalculatorEvent.DigitPressed("0")) }
                    LandBtn(".", BtnStyle.Dark, btnH, Modifier.weight(1f), 18, colors = colors) { onEvent(CalculatorEvent.DecimalPressed) }
                    LandBtn("=", BtnStyle.Orange, btnH, Modifier.weight(1f), 18, colors = colors) { onEvent(CalculatorEvent.EqualsPressed) }
                }
            }
        }
    }
}

/** Кнопка для ландшафтного режима — высота задана явно, форма адаптируется. */
@Composable
private fun LandBtn(
    label: String,
    style: BtnStyle,
    height: Dp,
    modifier: Modifier = Modifier,
    textSize: Int = 16,
    wide: Boolean = false,
    active: Boolean = false,
    onClick: () -> Unit,
    colors: ThemeColors
) {
    val bg = when {
        active && style == BtnStyle.Orange -> colors.white
        style == BtnStyle.Dark    -> colors.dark
        style == BtnStyle.Gray    -> colors.gray
        style == BtnStyle.Orange  -> colors.orange
        style == BtnStyle.SciDark -> colors.sciDark
        else -> colors.dark
    }
    val fg = when {
        active && style == BtnStyle.Orange -> colors.orange
        style == BtnStyle.Gray -> Color.Black
        else -> colors.white
    }
    val radius = height / 2
    val shape = RoundedCornerShape(radius)

    Box(
        modifier
            .height(height)
            .clip(shape)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = if (wide) Alignment.CenterStart else Alignment.Center,
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = textSize.sp,
            fontWeight = FontWeight.W400,
            modifier = if (wide) Modifier.padding(start = height * 0.28f) else Modifier,
            maxLines = 1,
        )
    }
}

@Composable
private fun LandscapeDisplay(
    state: CalculatorState.Active,
    height: Dp = 56.dp,
    colors: ThemeColors
) {
    val mainFontSize = when {
        state.displayText.length > 12 -> 16.sp
        state.displayText.length > 9  -> 20.sp
        state.displayText.length > 6  -> 24.sp
        else -> 28.sp
    }
    Column(
        Modifier
            .fillMaxWidth()
            .height(height)
            .background(colors.sciDark, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        // Строка выражения
        Text(
            text = state.expressionText.ifEmpty { " " },
            fontSize = 12.sp,
            fontWeight = FontWeight.W300,
            color = colors.subtext,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
        // Результат
        Text(
            text = state.displayText,
            fontSize = mainFontSize,
            fontWeight = FontWeight.W300,
            color = colors.display,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AngleModeToggle(
    current: AngleMode,
    modifier: Modifier = Modifier,
    onChange: (AngleMode) -> Unit,
    colors: ThemeColors
) {
    Row(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.dark),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf(AngleMode.DEG, AngleMode.RAD).forEach { mode ->
            val selected = current == mode
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (selected) colors.subtext else Color.Transparent)
                    .clickable { onChange(mode) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.name,
                    color = if (selected) colors.white else colors.subtext,
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.W600 else FontWeight.W400,
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// КНОПКА КАЛЬКУЛЯТОРА
// ══════════════════════════════════════════════════════════════════════════════
private enum class BtnStyle { Dark, Gray, Orange, SciDark }

@Composable
private fun CalcButton(
    label: String,
    style: BtnStyle,
    modifier: Modifier = Modifier,
    textSize: Int = 32,
    wide: Boolean = false,
    active: Boolean = false,
    onClick: () -> Unit,
    colors: ThemeColors
) {
    val bg = when {
        active && style == BtnStyle.Orange -> colors.white
        style == BtnStyle.Dark    -> colors.dark
        style == BtnStyle.Gray    -> colors.gray
        style == BtnStyle.Orange  -> colors.orange
        style == BtnStyle.SciDark -> colors.sciDark
        else -> colors.dark
    }
    val fg = when {
        active && style == BtnStyle.Orange -> colors.orange
        style == BtnStyle.Gray -> Color.Black
        else -> colors.white
    }
    val shape = if (wide) RoundedCornerShape(50) else CircleShape

    Box(
        modifier
            .aspectRatio(if (wide) 2.08f else 1f)
            .clip(shape)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = if (wide) Alignment.CenterStart else Alignment.Center,
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = textSize.sp,
            fontWeight = FontWeight.W400,
            modifier = if (wide) Modifier.padding(start = (textSize * 0.8f).dp) else Modifier,
            maxLines = 1,
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ИСТОРИЯ
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun HistorySheet(
    entries: List<com.example.ioscalculator.viewmodel.HistoryEntry>,
    onDismiss: () -> Unit,
    onClear: () -> Unit,
    colors: ThemeColors
) {
    Surface(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.55f),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = colors.sciDark,
    ) {
        Column(Modifier.padding(16.dp)) {
            // Шапка
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("История", color = colors.white, fontSize = 17.sp, fontWeight = FontWeight.W600)
                Row {
                    if (entries.isNotEmpty()) {
                        TextButton(onClick = onClear) {
                            Text("Очистить", color = colors.orange, fontSize = 15.sp)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = colors.subtext)
                    }
                }
            }
            HorizontalDivider(color = colors.dark, thickness = 0.5.dp)
            Spacer(Modifier.height(8.dp))

            if (entries.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "История пуста",
                        color = colors.subtext,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(entries) { entry ->
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                            Text(entry.expression, color = colors.subtext, fontSize = 14.sp)
                            Text(
                                "= ${entry.result}",
                                color = colors.white,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.W300,
                            )
                        }
                        HorizontalDivider(color = colors.dark, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// НАСТРОЙКИ
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun SettingsSheet(
    settings: CalculatorSettings,
    onDismiss: () -> Unit,
    onUpdate: (CalculatorSettings) -> Unit,
    colors: ThemeColors
) {
    Surface(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.65f),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = colors.sciDark,
    ) {
        Column(
            Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Шапка
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Настройки", color = colors.white, fontSize = 17.sp, fontWeight = FontWeight.W600)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = colors.subtext)
                }
            }
            HorizontalDivider(color = colors.dark, thickness = 0.5.dp)
            Spacer(Modifier.height(8.dp))

            // Звук
            SettingsToggle(
                title   = "Звук кнопок",
                subtitle = "Звуковой отклик при нажатии",
                checked = settings.soundEnabled,
                colors = colors
            ) { onUpdate(settings.copy(soundEnabled = it)) }

            // Haptic
            SettingsToggle(
                title    = "Тактильный отклик",
                subtitle = "Вибрация при нажатии кнопок",
                checked  = settings.hapticEnabled,
                colors = colors
            ) { onUpdate(settings.copy(hapticEnabled = it)) }

            Spacer(Modifier.height(16.dp))
            Text("Угол", color = colors.subtext, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))

            // Режим угла
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.dark),
            ) {
                listOf(AngleMode.DEG to "Градусы", AngleMode.RAD to "Радианы").forEach { (mode, label) ->
                    val selected = settings.angleMode == mode
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) colors.orange else Color.Transparent)
                            .clickable { onUpdate(settings.copy(angleMode = mode)) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            label,
                            color = if (selected) colors.white else colors.subtext,
                            fontSize = 15.sp,
                            fontWeight = if (selected) FontWeight.W600 else FontWeight.W400,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Тема", color = colors.subtext, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))

            // Тема
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.dark),
            ) {
                listOf(CalcTheme.DARK to "Тёмная", CalcTheme.LIGHT to "Светлая").forEach { (theme, label) ->
                    val selected = settings.theme == theme
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) colors.orange else Color.Transparent)
                            .clickable { onUpdate(settings.copy(theme = theme)) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            label,
                            color = if (selected) colors.white else colors.subtext,
                            fontSize = 15.sp,
                            fontWeight = if (selected) FontWeight.W600 else FontWeight.W400,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Версия
            Text(
                "iOS Calculator Clone v1.0",
                color = colors.subtext,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    colors: ThemeColors
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = colors.white, fontSize = 15.sp)
            Text(subtitle, color = colors.subtext, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.white,
                checkedTrackColor = Color(0xFF34C759),
                uncheckedThumbColor = colors.white,
                uncheckedTrackColor = colors.dark,
            )
        )
    }
    HorizontalDivider(color = colors.dark, thickness = 0.5.dp)
}
