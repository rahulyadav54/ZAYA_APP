package com.zayaai.app

data class CalculatorState(
    val expression: String = "",
    val result: String = "0",
    val history: List<String> = emptyList(),
    val isScientific: Boolean = false,
    val isDegree: Boolean = true,
    val memory: Double = 0.0
)

