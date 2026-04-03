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
private val ColorBg      = Color(0xFF000000)
private val ColorDark    = Color(0xFF333336)
private val ColorGray    = Color(0xFFA5A5A5)
private val ColorOrange  = Color(0xFFFF9F0A)
private val ColorWhite   = Color.White
private val ColorDisplay = Color.White
private val ColorSubtext = Color(0xFF8E8E93)

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

    Box(
        Modifier
            .fillMaxSize()
            .background(ColorBg)
            .systemBarsPadding()
    ) {
        if (isLandscape) {
            LandscapeLayout(active, vm::onEvent)
        } else {
            PortraitLayout(active, vm::onEvent)
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
) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        // Иконки вверху справа
        TopIconRow(onEvent)

        // Дисплей
        PortraitDisplay(state)

        // Кнопки
        Spacer(Modifier.height(8.dp))
        PortraitButtons(state, onEvent)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun TopIconRow(onEvent: (CalculatorEvent) -> Unit) {
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
                tint = ColorSubtext,
                modifier = Modifier.size(24.dp)
            )
        }
        // Настройки
        IconButton(onClick = { onEvent(CalculatorEvent.OpenSettings) }) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Настройки",
                tint = ColorSubtext,
                modifier = Modifier.size(24.dp)
            )
        }
        // Backspace — стрелка влево как в iOS
        IconButton(onClick = { onEvent(CalculatorEvent.Backspace) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Backspace",
                tint = ColorSubtext,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PortraitDisplay(state: CalculatorState.Active) {
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
                color = ColorSubtext,
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
            color = ColorDisplay,
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
) {
    val gap = 12.dp
    Column(
        Modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        val acLabel = if (state.hasInput && !state.startNewInput) "C" else "AC"
        // Row 1
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
            CalcButton(acLabel, BtnStyle.Gray, Modifier.weight(1f)) { onEvent(CalculatorEvent.ClearPressed) }
            CalcButton("+/-",  BtnStyle.Gray, Modifier.weight(1f)) { onEvent(CalculatorEvent.NegatePressed) }
            CalcButton("%",    BtnStyle.Gray, Modifier.weight(1f)) { onEvent(CalculatorEvent.PercentPressed) }
            CalcButton("÷",    BtnStyle.Orange, Modifier.weight(1f), active = state.activeOp == BinaryOp.DIVIDE)
            { onEvent(CalculatorEvent.OperatorPressed(BinaryOp.DIVIDE)) }
        }
        // Row 2
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
            for (d in listOf("7","8","9")) {
                CalcButton(d, BtnStyle.Dark, Modifier.weight(1f)) { onEvent(CalculatorEvent.DigitPressed(d)) }
            }
            CalcButton("×", BtnStyle.Orange, Modifier.weight(1f), active = state.activeOp == BinaryOp.MULTIPLY)
            { onEvent(CalculatorEvent.OperatorPressed(BinaryOp.MULTIPLY)) }
        }
        // Row 3
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
            for (d in listOf("4","5","6")) {
                CalcButton(d, BtnStyle.Dark, Modifier.weight(1f)) { onEvent(CalculatorEvent.DigitPressed(d)) }
            }
            CalcButton("−", BtnStyle.Orange, Modifier.weight(1f), active = state.activeOp == BinaryOp.SUBTRACT)
            { onEvent(CalculatorEvent.OperatorPressed(BinaryOp.SUBTRACT)) }
        }
        // Row 4
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
            for (d in listOf("1","2","3")) {
                CalcButton(d, BtnStyle.Dark, Modifier.weight(1f)) { onEvent(CalculatorEvent.DigitPressed(d)) }
            }
            CalcButton("+", BtnStyle.Orange, Modifier.weight(1f), active = state.activeOp == BinaryOp.ADD)
            { onEvent(CalculatorEvent.OperatorPressed(BinaryOp.ADD)) }
        }
        // Row 5 — ноль широкий
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
            CalcButton("0", BtnStyle.Dark, Modifier.weight(2f), wide = true) { onEvent(CalculatorEvent.DigitPressed("0")) }
            CalcButton(".", BtnStyle.Dark, Modifier.weight(1f)) { onEvent(CalculatorEvent.DecimalPressed) }
            CalcButton("=", BtnStyle.Orange, Modifier.weight(1f)) { onEvent(CalculatorEvent.EqualsPressed) }
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
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    ) { onEvent(CalculatorEvent.AngleModeChanged(it)) }
                    LandBtn("(", BtnStyle.Dark, btnH, Modifier.weight(1f), 16)
                    { onEvent(CalculatorEvent.OpenBracket) }
                    LandBtn(")", BtnStyle.Dark, btnH, Modifier.weight(1f), 16)
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
                            LandBtn(label, BtnStyle.SciDark, btnH, Modifier.weight(1f), 13) {
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
                LandscapeDisplay(state, displayH)

                // Основные кнопки
                val acLabel = if (state.hasInput && !state.startNewInput) "C" else "AC"
                Row(Modifier.fillMaxWidth().height(btnH), horizontalArrangement = Arrangement.spacedBy(gap)) {
                    LandBtn(acLabel, BtnStyle.Gray, btnH, Modifier.weight(1f), 16) { onEvent(CalculatorEvent.ClearPressed) }
                    LandBtn("+/-",  BtnStyle.Gray,   btnH, Modifier.weight(1f), 14) { onEvent(CalculatorEvent.NegatePressed) }
                    LandBtn("%",    BtnStyle.Gray,   btnH, Modifier.weight(1f), 16) { onEvent(CalculatorEvent.PercentPressed) }
                    LandBtn("÷",   BtnStyle.Orange, btnH, Modifier.weight(1f), 18, active = state.activeOp == BinaryOp.DIVIDE)
                    { onEvent(CalculatorEvent.OperatorPressed(BinaryOp.DIVIDE)) }
                }
                Row(Modifier.fillMaxWidth().height(btnH), horizontalArrangement = Arrangement.spacedBy(gap)) {
                    listOf("7","8","9").forEach { d -> LandBtn(d, BtnStyle.Dark, btnH, Modifier.weight(1f), 18) { onEvent(CalculatorEvent.DigitPressed(d)) } }
                    LandBtn("×", BtnStyle.Orange, btnH, Modifier.weight(1f), 18, active = state.activeOp == BinaryOp.MULTIPLY)
                    { onEvent(CalculatorEvent.OperatorPressed(BinaryOp.MULTIPLY)) }
                }
                Row(Modifier.fillMaxWidth().height(btnH), horizontalArrangement = Arrangement.spacedBy(gap)) {
                    listOf("4","5","6").forEach { d -> LandBtn(d, BtnStyle.Dark, btnH, Modifier.weight(1f), 18) { onEvent(CalculatorEvent.DigitPressed(d)) } }
                    LandBtn("−", BtnStyle.Orange, btnH, Modifier.weight(1f), 18, active = state.activeOp == BinaryOp.SUBTRACT)
                    { onEvent(CalculatorEvent.OperatorPressed(BinaryOp.SUBTRACT)) }
                }
                Row(Modifier.fillMaxWidth().height(btnH), horizontalArrangement = Arrangement.spacedBy(gap)) {
                    listOf("1","2","3").forEach { d -> LandBtn(d, BtnStyle.Dark, btnH, Modifier.weight(1f), 18) { onEvent(CalculatorEvent.DigitPressed(d)) } }
                    LandBtn("+", BtnStyle.Orange, btnH, Modifier.weight(1f), 18, active = state.activeOp == BinaryOp.ADD)
                    { onEvent(CalculatorEvent.OperatorPressed(BinaryOp.ADD)) }
                }
                Row(Modifier.fillMaxWidth().height(btnH), horizontalArrangement = Arrangement.spacedBy(gap)) {
                    LandBtn("0", BtnStyle.Dark, btnH, Modifier.weight(2f), 18, wide = true) { onEvent(CalculatorEvent.DigitPressed("0")) }
                    LandBtn(".", BtnStyle.Dark, btnH, Modifier.weight(1f), 18) { onEvent(CalculatorEvent.DecimalPressed) }
                    LandBtn("=", BtnStyle.Orange, btnH, Modifier.weight(1f), 18) { onEvent(CalculatorEvent.EqualsPressed) }
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
) {
    val bg = when {
        active && style == BtnStyle.Orange -> ColorWhite
        style == BtnStyle.Dark    -> ColorDark
        style == BtnStyle.Gray    -> ColorGray
        style == BtnStyle.Orange  -> ColorOrange
        style == BtnStyle.SciDark -> Color(0xFF1C1C1E)
        else -> ColorDark
    }
    val fg = when {
        active && style == BtnStyle.Orange -> ColorOrange
        style == BtnStyle.Gray -> Color.Black
        else -> ColorWhite
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
private fun LandscapeDisplay(state: CalculatorState.Active, height: Dp = 56.dp) {
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
            .background(Color(0xFF1C1C1E), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        // Строка выражения
        Text(
            text = state.expressionText.ifEmpty { " " },
            fontSize = 12.sp,
            fontWeight = FontWeight.W300,
            color = ColorSubtext,
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
            color = ColorDisplay,
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
) {
    Row(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF2C2C2E)),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf(AngleMode.DEG, AngleMode.RAD).forEach { mode ->
            val selected = current == mode
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (selected) Color(0xFF636366) else Color.Transparent)
                    .clickable { onChange(mode) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.name,
                    color = if (selected) ColorWhite else ColorSubtext,
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
) {
    val bg = when {
        active && style == BtnStyle.Orange -> ColorWhite
        style == BtnStyle.Dark    -> ColorDark
        style == BtnStyle.Gray    -> ColorGray
        style == BtnStyle.Orange  -> ColorOrange
        style == BtnStyle.SciDark -> Color(0xFF1C1C1E)
        else -> ColorDark
    }
    val fg = when {
        active && style == BtnStyle.Orange -> ColorOrange
        style == BtnStyle.Gray -> Color.Black
        else -> ColorWhite
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
) {
    Surface(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.55f),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = Color(0xFF1C1C1E),
    ) {
        Column(Modifier.padding(16.dp)) {
            // Шапка
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("История", color = ColorWhite, fontSize = 17.sp, fontWeight = FontWeight.W600)
                Row {
                    if (entries.isNotEmpty()) {
                        TextButton(onClick = onClear) {
                            Text("Очистить", color = ColorOrange, fontSize = 15.sp)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = ColorSubtext)
                    }
                }
            }
            HorizontalDivider(color = Color(0xFF3A3A3C), thickness = 0.5.dp)
            Spacer(Modifier.height(8.dp))

            if (entries.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "История пуста",
                        color = ColorSubtext,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(entries) { entry ->
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                            Text(entry.expression, color = ColorSubtext, fontSize = 14.sp)
                            Text(
                                "= ${entry.result}",
                                color = ColorWhite,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.W300,
                            )
                        }
                        HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.5.dp)
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
) {
    Surface(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.65f),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = Color(0xFF1C1C1E),
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
                Text("Настройки", color = ColorWhite, fontSize = 17.sp, fontWeight = FontWeight.W600)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = ColorSubtext)
                }
            }
            HorizontalDivider(color = Color(0xFF3A3A3C), thickness = 0.5.dp)
            Spacer(Modifier.height(8.dp))

            // Звук
            SettingsToggle(
                title   = "Звук кнопок",
                subtitle = "Звуковой отклик при нажатии",
                checked = settings.soundEnabled,
            ) { onUpdate(settings.copy(soundEnabled = it)) }

            // Haptic
            SettingsToggle(
                title    = "Тактильный отклик",
                subtitle = "Вибрация при нажатии кнопок",
                checked  = settings.hapticEnabled,
            ) { onUpdate(settings.copy(hapticEnabled = it)) }

            Spacer(Modifier.height(16.dp))
            Text("Угол", color = ColorSubtext, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))

            // Режим угла
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF2C2C2E)),
            ) {
                listOf(AngleMode.DEG to "Градусы", AngleMode.RAD to "Радианы").forEach { (mode, label) ->
                    val selected = settings.angleMode == mode
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) ColorOrange else Color.Transparent)
                            .clickable { onUpdate(settings.copy(angleMode = mode)) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            label,
                            color = if (selected) ColorWhite else ColorSubtext,
                            fontSize = 15.sp,
                            fontWeight = if (selected) FontWeight.W600 else FontWeight.W400,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Тема", color = ColorSubtext, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))

            // Тема
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF2C2C2E)),
            ) {
                listOf(CalcTheme.DARK to "Тёмная", CalcTheme.LIGHT to "Светлая").forEach { (theme, label) ->
                    val selected = settings.theme == theme
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) ColorOrange else Color.Transparent)
                            .clickable { onUpdate(settings.copy(theme = theme)) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            label,
                            color = if (selected) ColorWhite else ColorSubtext,
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
                color = ColorSubtext,
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
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = ColorWhite, fontSize = 15.sp)
            Text(subtitle, color = ColorSubtext, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ColorWhite,
                checkedTrackColor = Color(0xFF34C759),
                uncheckedThumbColor = ColorWhite,
                uncheckedTrackColor = Color(0xFF3A3A3C),
            )
        )
    }
    HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.5.dp)
}
