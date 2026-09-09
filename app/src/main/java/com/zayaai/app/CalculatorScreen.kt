package com.zayaai.app

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.LocalIsDarkTheme

// ─── Proper Unicode symbols ───
private const val MULTIPLY = "\u00D7"   // ×
private const val DIVIDE   = "\u00F7"   // ÷
private const val MINUS    = "\u2212"   // −  (proper minus, not hyphen)

// ─── Theme-aware calculator colors ───
private object CalcColors {
    // Dark theme
    private val DarkNumBg       = Color(0xFF2A2D3A)
    private val DarkOpBg        = Color(0xFF3D3564)
    private val DarkEqualsBg    = Color(0xFF6C63FF)
    private val DarkClearBg     = Color(0xFFCF6679)
    private val DarkDelBg       = Color(0xFFFF9800)
    private val DarkNumText     = Color.White
    private val DarkOpText      = Color(0xFFBBB2FF)
    private val DarkPadBg       = Color(0xFF1A1B2E)
    private val DarkSciBg       = Color(0xFF252738)
    private val DarkSciBorder   = Color(0xFF3D3F54)
    private val DarkSciText     = Color(0xFFAEAFFF)

    // Light theme
    private val LightNumBg      = Color(0xFFFFFFFF)
    private val LightOpBg       = Color(0xFFEDE7F6)
    private val LightEqualsBg   = Color(0xFF6C63FF)
    private val LightClearBg    = Color(0xFFFFCDD2)
    private val LightDelBg      = Color(0xFFFFE0B2)
    private val LightNumText    = Color(0xFF1A1A2E)
    private val LightOpText     = Color(0xFF5C4DB1)
    private val LightPadBg      = Color(0xFFF0F1F5)
    private val LightSciBg      = Color(0xFFFFFFFF)
    private val LightSciBorder  = Color(0xFFDDD8EF)
    private val LightSciText    = Color(0xFF5C4DB1)

    @Composable fun numBg()      = if (LocalIsDarkTheme.current) DarkNumBg else LightNumBg
    @Composable fun opBg()       = if (LocalIsDarkTheme.current) DarkOpBg else LightOpBg
    @Composable fun equalsBg()   = if (LocalIsDarkTheme.current) DarkEqualsBg else LightEqualsBg
    @Composable fun clearBg()    = if (LocalIsDarkTheme.current) DarkClearBg else LightClearBg
    @Composable fun delBg()      = if (LocalIsDarkTheme.current) DarkDelBg else LightDelBg
    @Composable fun numText()    = if (LocalIsDarkTheme.current) DarkNumText else LightNumText
    @Composable fun opText()     = if (LocalIsDarkTheme.current) DarkOpText else LightOpText
    @Composable fun clearText()  = if (LocalIsDarkTheme.current) Color.White else Color(0xFFC62828)
    @Composable fun delText()    = if (LocalIsDarkTheme.current) Color.White else Color(0xFFE65100)
    @Composable fun padBg()      = if (LocalIsDarkTheme.current) DarkPadBg else LightPadBg
    @Composable fun sciBg()      = if (LocalIsDarkTheme.current) DarkSciBg else LightSciBg
    @Composable fun sciBorder()  = if (LocalIsDarkTheme.current) DarkSciBorder else LightSciBorder
    @Composable fun sciText()    = if (LocalIsDarkTheme.current) DarkSciText else LightSciText
    @Composable fun accentColor() = if (LocalIsDarkTheme.current) Color(0xFFAEAFFF) else Color(0xFF6C63FF)
}

// ════════════════════════════════════════════════════════════════════
//  MAIN SCREEN
// ════════════════════════════════════════════════════════════════════

@Composable
fun CalculatorScreen(paddingValues: PaddingValues, viewModel: CalculatorViewModel) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val exprScrollState = rememberScrollState()

    // Auto-scroll expression to end when it changes
    LaunchedEffect(state.expression) {
        exprScrollState.animateScrollTo(exprScrollState.maxValue)
    }

    // Format expression for display: replace internal * / - with × ÷ −
    val displayExpression = state.expression
        .replace("*", MULTIPLY)
        .replace("/", DIVIDE)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
    ) {
        // ── SECTION 1: Display ──────────────────────────────────────
        DisplaySection(
            expression = displayExpression,
            result = state.result,
            exprScrollState = exprScrollState,
            onCopyResult = {
                clipboardManager.setText(AnnotatedString(state.result))
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        // ── SECTION 2 + 3: Button Pad ──────────────────────────────
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
            color = CalcColors.padBg(),
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 12.dp)
                    .navigationBarsPadding()
            ) {
                // Mode toggle row
                ModeToggleRow(
                    isScientific = state.isScientific,
                    isDegree = state.isDegree,
                    onToggleScientific = { viewModel.onAction(CalculatorAction.ToggleScientific) },
                    onToggleAngle = { viewModel.onAction(CalculatorAction.ToggleAngleMode) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ── SECTION 2: Scientific panel (animated) ──────────
                AnimatedVisibility(
                    visible = state.isScientific,
                    enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                ) {
                    Column {
                        ScientificPanel(viewModel)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // ── SECTION 3: Fixed keypad ─────────────────────────
                BasicKeypad(viewModel)
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  DISPLAY SECTION
// ════════════════════════════════════════════════════════════════════

@Composable
private fun DisplaySection(
    expression: String,
    result: String,
    exprScrollState: androidx.compose.foundation.ScrollState,
    onCopyResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        // Expression line
        Text(
            text = expression.ifEmpty { " " },
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Visible,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(exprScrollState)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Result line
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (result != "0" && result != "Error") {
                IconButton(
                    onClick = onCopyResult,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Rounded.ContentCopy,
                        contentDescription = "Copy result",
                        tint = CalcColors.accentColor(),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = result,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = when {
                        result.length > 14 -> 30.sp
                        result.length > 10 -> 36.sp
                        else -> 48.sp
                    }
                ),
                color = if (result == "Error")
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.End,
                maxLines = 1
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  MODE TOGGLE ROW
// ════════════════════════════════════════════════════════════════════

@Composable
private fun ModeToggleRow(
    isScientific: Boolean,
    isDegree: Boolean,
    onToggleScientific: () -> Unit,
    onToggleAngle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Basic ↔ Scientific toggle chip
        Surface(
            onClick = onToggleScientific,
            shape = RoundedCornerShape(20.dp),
            color = if (isScientific)
                CalcColors.accentColor().copy(alpha = 0.15f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isScientific) CalcColors.accentColor().copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        ) {
            Text(
                text = if (isScientific) "Scientific" else "Basic",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isScientific)
                    CalcColors.accentColor()
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }

        // DEG / RAD toggle (only in scientific mode)
        AnimatedVisibility(
            visible = isScientific,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
            Surface(
                onClick = onToggleAngle,
                shape = RoundedCornerShape(20.dp),
                color = CalcColors.accentColor().copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, CalcColors.accentColor().copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = if (isDegree) "DEG" else "RAD",
                    color = CalcColors.accentColor(),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  SCIENTIFIC PANEL — Horizontal scrollable row (compact)
// ════════════════════════════════════════════════════════════════════

@Composable
private fun ScientificPanel(viewModel: CalculatorViewModel) {
    val scrollState = rememberScrollState()

    // Single horizontal scrollable row with all scientific buttons
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Trig
        SciChip("sin")  { viewModel.onAction(CalculatorAction.Function("sin")) }
        SciChip("cos")  { viewModel.onAction(CalculatorAction.Function("cos")) }
        SciChip("tan")  { viewModel.onAction(CalculatorAction.Function("tan")) }
        // Inverse trig
        SciChip("sin\u207B\u00B9") { viewModel.onAction(CalculatorAction.Function("asin")) }
        SciChip("cos\u207B\u00B9") { viewModel.onAction(CalculatorAction.Function("acos")) }
        SciChip("tan\u207B\u00B9") { viewModel.onAction(CalculatorAction.Function("atan")) }
        // Other functions
        SciChip("log")  { viewModel.onAction(CalculatorAction.Function("log")) }
        SciChip("ln")   { viewModel.onAction(CalculatorAction.Function("ln")) }
        SciChip("\u221A") { viewModel.onAction(CalculatorAction.Function("sqrt")) }
        SciChip("x\u02B8") { viewModel.onAction(CalculatorAction.Operation("^")) }
        // Constants
        SciChip("\u03C0") { viewModel.onAction(CalculatorAction.Constant("\u03C0")) }
        SciChip("e")    { viewModel.onAction(CalculatorAction.Constant("e")) }
        // Parentheses
        SciChip("(")    { viewModel.onAction(CalculatorAction.Parentheses("(")) }
        SciChip(")")    { viewModel.onAction(CalculatorAction.Parentheses(")")) }
        // Extra
        SciChip("abs")  { viewModel.onAction(CalculatorAction.Function("abs")) }
    }
}

@Composable
private fun SciChip(label: String, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        shape = RoundedCornerShape(12.dp),
        color = CalcColors.sciBg(),
        border = androidx.compose.foundation.BorderStroke(1.dp, CalcColors.sciBorder())
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = label,
                color = CalcColors.sciText(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  BASIC KEYPAD — Fixed grid, never resizes
// ════════════════════════════════════════════════════════════════════

private data class CalcKey(
    val label: String,
    val type: KeyType
)

private enum class KeyType { NUMBER, OPERATOR, CLEAR, DELETE, EQUALS, DECIMAL, DOUBLE_ZERO }

@Composable
private fun BasicKeypad(viewModel: CalculatorViewModel) {
    val haptic = LocalHapticFeedback.current

    val rows = listOf(
        listOf(
            CalcKey("C", KeyType.CLEAR),
            CalcKey("DEL", KeyType.DELETE),
            CalcKey("%", KeyType.OPERATOR),
            CalcKey(DIVIDE, KeyType.OPERATOR)
        ),
        listOf(
            CalcKey("7", KeyType.NUMBER),
            CalcKey("8", KeyType.NUMBER),
            CalcKey("9", KeyType.NUMBER),
            CalcKey(MULTIPLY, KeyType.OPERATOR)
        ),
        listOf(
            CalcKey("4", KeyType.NUMBER),
            CalcKey("5", KeyType.NUMBER),
            CalcKey("6", KeyType.NUMBER),
            CalcKey(MINUS, KeyType.OPERATOR)
        ),
        listOf(
            CalcKey("1", KeyType.NUMBER),
            CalcKey("2", KeyType.NUMBER),
            CalcKey("3", KeyType.NUMBER),
            CalcKey("+", KeyType.OPERATOR)
        ),
        listOf(
            CalcKey("00", KeyType.DOUBLE_ZERO),
            CalcKey("0", KeyType.NUMBER),
            CalcKey(".", KeyType.DECIMAL),
            CalcKey("=", KeyType.EQUALS)
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { key ->
                    CalcButton(
                        key = key,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            dispatchAction(viewModel, key)
                        }
                    )
                }
            }
        }
    }
}

private fun dispatchAction(viewModel: CalculatorViewModel, key: CalcKey) {
    when (key.type) {
        KeyType.CLEAR -> viewModel.onAction(CalculatorAction.Clear)
        KeyType.DELETE -> viewModel.onAction(CalculatorAction.Delete)
        KeyType.EQUALS -> viewModel.onAction(CalculatorAction.Calculate)
        KeyType.DECIMAL -> viewModel.onAction(CalculatorAction.Decimal)
        KeyType.DOUBLE_ZERO -> {
            viewModel.onAction(CalculatorAction.Number(0))
            viewModel.onAction(CalculatorAction.Number(0))
        }
        KeyType.NUMBER -> viewModel.onAction(CalculatorAction.Number(key.label.toInt()))
        KeyType.OPERATOR -> {
            val op = when (key.label) {
                MULTIPLY -> "*"
                DIVIDE   -> "/"
                MINUS    -> "-"
                else     -> key.label
            }
            viewModel.onAction(CalculatorAction.Operation(op))
        }
    }
}

@Composable
private fun CalcButton(
    key: CalcKey,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = when (key.type) {
        KeyType.CLEAR       -> CalcColors.clearBg()
        KeyType.DELETE       -> CalcColors.delBg()
        KeyType.EQUALS       -> CalcColors.equalsBg()
        KeyType.OPERATOR     -> CalcColors.opBg()
        else                 -> CalcColors.numBg()
    }
    val textColor = when (key.type) {
        KeyType.CLEAR       -> CalcColors.clearText()
        KeyType.DELETE       -> CalcColors.delText()
        KeyType.EQUALS       -> Color.White
        KeyType.OPERATOR     -> CalcColors.opText()
        else                 -> CalcColors.numText()
    }
    val fontSize = when {
        key.label == "DEL" -> 16.sp
        key.type == KeyType.OPERATOR || key.type == KeyType.EQUALS -> 26.sp
        else -> 24.sp
    }

    Surface(
        onClick = onClick,
        modifier = modifier.aspectRatio(1.15f),
        shape = RoundedCornerShape(18.dp),
        color = bgColor,
        shadowElevation = if (LocalIsDarkTheme.current) 0.dp else 2.dp,
        tonalElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (key.type == KeyType.DELETE) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Backspace,
                    contentDescription = "Delete",
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = key.label,
                    fontSize = fontSize,
                    fontWeight = if (key.type == KeyType.NUMBER || key.type == KeyType.DOUBLE_ZERO)
                        FontWeight.Medium else FontWeight.Bold,
                    color = textColor,
                    maxLines = 1
                )
            }
        }
    }
}

