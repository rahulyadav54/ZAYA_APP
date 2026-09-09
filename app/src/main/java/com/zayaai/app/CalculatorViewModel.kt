package com.zayaai.app

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CalculatorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CalculatorState())
    val uiState: StateFlow<CalculatorState> = _uiState.asStateFlow()

    private val engine = CalculatorEngine()

    // Known function/token suffixes that should be deleted as a unit
    private val functionTokens = listOf(
        "asin(", "acos(", "atan(",
        "sqrt(", "sin(", "cos(", "tan(",
        "log(", "abs(", "exp(", "ln("
    )

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Number -> enterNumber(action.number)
            is CalculatorAction.Operation -> enterOperation(action.operation)
            is CalculatorAction.Decimal -> enterDecimal()
            is CalculatorAction.Clear -> clear()
            is CalculatorAction.Delete -> delete()
            is CalculatorAction.Calculate -> calculate()
            is CalculatorAction.ToggleScientific -> toggleScientific()
            is CalculatorAction.ToggleAngleMode -> toggleAngleMode()
            is CalculatorAction.Function -> enterFunction(action.function)
            is CalculatorAction.Parentheses -> enterParentheses(action.paren)
            is CalculatorAction.Constant -> enterConstant(action.constant)
            is CalculatorAction.Memory -> handleMemory(action.type)
            is CalculatorAction.ClearHistory -> clearHistory()
        }
    }

    private fun enterNumber(number: Int) {
        _uiState.update { it.copy(expression = it.expression + number.toString()) }
    }

    private fun enterOperation(op: String) {
        val expr = _uiState.value.expression
        // Prevent double operators (replace last operator if present)
        if (expr.isNotEmpty() && expr.last().toString() in listOf("+", "-", "*", "/", "^", "%")) {
            _uiState.update { it.copy(expression = expr.dropLast(1) + op) }
        } else {
            _uiState.update { it.copy(expression = expr + op) }
        }
    }

    private fun enterDecimal() {
        val expr = _uiState.value.expression
        // Find last number segment and check if it already has a decimal
        val lastSegment = expr.takeLastWhile { it.isDigit() || it == '.' }
        if (!lastSegment.contains('.')) {
            val prefix = if (expr.isEmpty() || !expr.last().isDigit()) "0" else ""
            _uiState.update { it.copy(expression = expr + prefix + ".") }
        }
    }

    private fun enterFunction(func: String) {
        _uiState.update { it.copy(expression = it.expression + "$func(") }
    }

    private fun enterParentheses(paren: String) {
        _uiState.update { it.copy(expression = it.expression + paren) }
    }

    private fun enterConstant(constant: String) {
        _uiState.update { it.copy(expression = it.expression + constant) }
    }

    private fun clear() {
        _uiState.update { it.copy(expression = "", result = "0") }
    }

    private fun delete() {
        _uiState.update {
            if (it.expression.isEmpty()) return@update it

            // Check if expression ends with a known function token
            val matchedToken = functionTokens.firstOrNull { token ->
                it.expression.endsWith(token)
            }

            if (matchedToken != null) {
                it.copy(expression = it.expression.dropLast(matchedToken.length))
            } else {
                it.copy(expression = it.expression.dropLast(1))
            }
        }
    }

    private fun toggleScientific() {
        _uiState.update { it.copy(isScientific = !it.isScientific) }
    }

    private fun toggleAngleMode() {
        _uiState.update { it.copy(isDegree = !it.isDegree) }
    }

    private fun handleMemory(type: String) {
        val currentVal = _uiState.value.result.toDoubleOrNull() ?: 0.0
        when (type) {
            "MC" -> _uiState.update { it.copy(memory = 0.0) }
            "MR" -> _uiState.update { it.copy(expression = it.expression + it.memory.toString()) }
            "M+" -> _uiState.update { it.copy(memory = it.memory + currentVal) }
            "M-" -> _uiState.update { it.copy(memory = it.memory - currentVal) }
            "MS" -> _uiState.update { it.copy(memory = currentVal) }
        }
    }

    private fun calculate() {
        var expr = _uiState.value.expression
        if (expr.isBlank()) return

        // Auto-close unclosed parentheses
        val openCount = expr.count { it == '(' }
        val closeCount = expr.count { it == ')' }
        if (openCount > closeCount) {
            expr += ")".repeat(openCount - closeCount)
        }

        try {
            val res = engine.evaluate(expr, _uiState.value.isDegree)

            // Handle special results
            val formattedResult = when {
                res.isNaN() -> "Error"
                res.isInfinite() -> "Error"
                res % 1.0 == 0.0 && res >= Long.MIN_VALUE && res <= Long.MAX_VALUE ->
                    res.toLong().toString()
                else ->
                    String.format(java.util.Locale.US, "%.10f", res)
                        .trimEnd('0').trimEnd('.')
            }

            _uiState.update { state ->
                state.copy(
                    result = formattedResult,
                    history = (listOf("$expr = $formattedResult") + state.history).take(10)
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(result = "Error") }
        }
    }

    private fun clearHistory() {
        _uiState.update { it.copy(history = emptyList()) }
    }
}

sealed class CalculatorAction {
    data class Number(val number: Int) : CalculatorAction()
    data class Operation(val operation: String) : CalculatorAction()
    object Decimal : CalculatorAction()
    object Clear : CalculatorAction()
    object Delete : CalculatorAction()
    object Calculate : CalculatorAction()
    object ToggleScientific : CalculatorAction()
    object ToggleAngleMode : CalculatorAction()
    data class Function(val function: String) : CalculatorAction()
    data class Parentheses(val paren: String) : CalculatorAction()
    data class Constant(val constant: String) : CalculatorAction()
    data class Memory(val type: String) : CalculatorAction()
    object ClearHistory : CalculatorAction()
}

