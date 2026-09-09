package com.zayaai.app

import java.util.*
import kotlin.math.*

class CalculatorEngine {

    fun evaluate(expression: String, isDegree: Boolean): Double {
        val tokens = tokenize(expression)
        val rpn = shuntingYard(tokens)
        return evaluateRPN(rpn, isDegree)
    }

    private fun tokenize(expression: String): List<String> {
        val result = mutableListOf<String>()
        var i = 0
        // Only replace operator symbols, NOT constants like e or Ï€ (handle those in the letter branch)
        val expr = expression.replace(" ", "")
            .replace("\u00D7", "*")  // Ã—
            .replace("\u00F7", "/")  // Ã·
            .replace("\u2212", "-")  // âˆ’ (unicode minus)
            .replace("\u221A", "sqrt") // âˆš symbol to sqrt function

        while (i < expr.length) {
            val c = expr[i]
            when {
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                        sb.append(expr[i++])
                    }
                    result.add(sb.toString())
                    continue
                }
                c == '\u03C0' -> {
                    // Ï€ constant â€” insert its numeric value as a token
                    result.add(PI.toString())
                    i++
                    continue
                }
                c.isLetter() -> {
                    val sb = StringBuilder()
                    while (i < expr.length && expr[i].isLetter()) {
                        sb.append(expr[i++])
                    }
                    val word = sb.toString()
                    // Check if the word is the standalone constant "e"
                    if (word == "e") {
                        result.add(E.toString())
                    } else {
                        result.add(word)
                    }
                    continue
                }
                c in "+-*/^()!|%" -> {
                    // Handle negative numbers at the start or after an opening parenthesis or after an operator
                    if (c == '-' && (result.isEmpty() || result.last() == "(" || result.last() in setOf("+", "-", "*", "/", "^", "%"))) {
                        result.add("0") // Represent -x as 0 - x
                    }
                    result.add(c.toString())
                }
            }
            i++
        }
        return result
    }

    private fun shuntingYard(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val operators = Stack<String>()
        val precedence = mapOf(
            "+" to 2, "-" to 2,
            "*" to 3, "/" to 3, "%" to 3,
            "^" to 4, "!" to 5
        )

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> output.add(token)
                isFunction(token) -> operators.push(token)
                token == "(" -> operators.push(token)
                token == ")" -> {
                    while (operators.isNotEmpty() && operators.peek() != "(") {
                        output.add(operators.pop())
                    }
                    if (operators.isNotEmpty()) operators.pop() // Pop "("
                    if (operators.isNotEmpty() && isFunction(operators.peek())) {
                        output.add(operators.pop())
                    }
                }
                else -> {
                    while (operators.isNotEmpty() && operators.peek() != "(" &&
                        (precedence[operators.peek()] ?: 0) >= (precedence[token] ?: 0)
                    ) {
                        output.add(operators.pop())
                    }
                    operators.push(token)
                }
            }
        }
        while (operators.isNotEmpty()) output.add(operators.pop())
        return output
    }

    private fun evaluateRPN(rpn: List<String>, isDegree: Boolean): Double {
        val stack = Stack<Double>()
        for (token in rpn) {
            val num = token.toDoubleOrNull()
            if (num != null) {
                stack.push(num)
            } else if (isFunction(token)) {
                if (stack.isEmpty()) throw IllegalArgumentException("Missing operand for $token")
                val a = stack.pop()
                stack.push(applyFunction(token, a, isDegree))
            } else {
                if (stack.isEmpty()) throw IllegalArgumentException("Missing operand")
                val b = stack.pop()
                val a = if (stack.isNotEmpty()) stack.pop() else 0.0
                stack.push(applyOperator(token, a, b))
            }
        }
        if (stack.isEmpty()) throw IllegalArgumentException("Empty expression")
        return stack.pop()
    }

    private fun isFunction(token: String) = token in setOf(
        "sin", "cos", "tan", "asin", "acos", "atan", "log", "ln", "sqrt", "abs", "exp"
    )

    private fun applyFunction(func: String, a: Double, isDegree: Boolean): Double {
        val value = if (isDegree && func in setOf("sin", "cos", "tan")) Math.toRadians(a) else a
        var result = when (func) {
            "sin" -> sin(value)
            "cos" -> cos(value)
            "tan" -> tan(value)
            "asin" -> asin(a)
            "acos" -> acos(a)
            "atan" -> atan(a)
            "log" -> log10(a)
            "ln" -> ln(a)
            "sqrt" -> sqrt(a)
            "abs" -> abs(a)
            "exp" -> exp(a)
            else -> 0.0
        }
        if (isDegree && func in setOf("asin", "acos", "atan")) result = Math.toDegrees(result)
        return result
    }

    private fun applyOperator(op: String, a: Double, b: Double): Double = when (op) {
        "+" -> a + b
        "-" -> a - b
        "*" -> a * b
        "/" -> if (b != 0.0) a / b else Double.NaN
        "%" -> a * (b / 100.0)
        "^" -> a.pow(b)
        "!" -> factorial(a.toInt()).toDouble()
        else -> 0.0
    }

    private fun factorial(n: Int): Long {
        if (n < 0) return 0
        var res = 1L
        for (i in 2..n) res *= i
        return res
    }
}

