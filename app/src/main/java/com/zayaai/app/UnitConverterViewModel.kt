package com.zayaai.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class UnitConverterViewModel : ViewModel() {

    var inputValue by mutableStateOf("")
        private set
    var outputValue by mutableStateOf("0.0")
        private set
    var selectedCategory by mutableStateOf("Length")
        private set
    var inputUnit by mutableStateOf("Meter")
        private set
    var outputUnit by mutableStateOf("Feet")
        private set

    private val lengthUnits = listOf("Meter", "Feet", "Inch", "Kilometer", "Mile")
    private val weightUnits = listOf("Kilogram", "Pound", "Gram", "Ounce")
    private val temperatureUnits = listOf("Celsius", "Fahrenheit", "Kelvin")
    private val timeUnits = listOf("Second", "Minute", "Hour", "Day")

    val unitsForSelectedCategory: List<String>
        get() = when (selectedCategory) {
            "Length" -> lengthUnits
            "Weight" -> weightUnits
            "Temp" -> temperatureUnits
            "Time" -> timeUnits
            else -> emptyList()
        }

    fun updateInputValue(value: String) {
        inputValue = value
        convertUnits()
    }

    fun updateSelectedCategory(category: String) {
        selectedCategory = category
        // Reset units when category changes
        inputUnit = unitsForSelectedCategory.firstOrNull() ?: ""
        outputUnit = unitsForSelectedCategory.getOrNull(1) ?: unitsForSelectedCategory.firstOrNull() ?: ""
        convertUnits()
    }

    fun updateInputUnit(unit: String) {
        inputUnit = unit
        convertUnits()
    }

    fun updateOutputUnit(unit: String) {
        outputUnit = unit
        convertUnits()
    }

    private fun convertUnits() {
        val value = inputValue.toDoubleOrNull() ?: 0.0
        if (inputUnit.isBlank() || outputUnit.isBlank()) {
            outputValue = "Error"
            return
        }

        val converted = when (selectedCategory) {
            "Length" -> convertLength(value, inputUnit, outputUnit)
            "Weight" -> convertWeight(value, inputUnit, outputUnit)
            "Temp" -> convertTemperature(value, inputUnit, outputUnit)
            "Time" -> convertTime(value, inputUnit, outputUnit)
            else -> Double.NaN
        }
        outputValue = if (converted.isNaN()) "Error" else String.format("%.4f", converted)
    }

    private fun convertLength(value: Double, fromUnit: String, toUnit: String): Double {
        val meters = when (fromUnit) {
            "Meter" -> value
            "Feet" -> value * 0.3048
            "Inch" -> value * 0.0254
            "Kilometer" -> value * 1000
            "Mile" -> value * 1609.34
            else -> Double.NaN
        }
        return when (toUnit) {
            "Meter" -> meters
            "Feet" -> meters / 0.3048
            "Inch" -> meters / 0.0254
            "Kilometer" -> meters / 1000
            "Mile" -> meters / 1609.34
            else -> Double.NaN
        }
    }

    private fun convertWeight(value: Double, fromUnit: String, toUnit: String): Double {
        val kilograms = when (fromUnit) {
            "Kilogram" -> value
            "Pound" -> value * 0.453592
            "Gram" -> value / 1000
            "Ounce" -> value * 0.0283495
            else -> Double.NaN
        }
        return when (toUnit) {
            "Kilogram" -> kilograms
            "Pound" -> kilograms / 0.453592
            "Gram" -> kilograms * 1000
            "Ounce" -> kilograms / 0.0283495
            else -> Double.NaN
        }
    }

    private fun convertTemperature(value: Double, fromUnit: String, toUnit: String): Double {
        val celsius = when (fromUnit) {
            "Celsius" -> value
            "Fahrenheit" -> (value - 32) * 5 / 9
            "Kelvin" -> value - 273.15
            else -> Double.NaN
        }
        return when (toUnit) {
            "Celsius" -> celsius
            "Fahrenheit" -> (celsius * 9 / 5) + 32
            "Kelvin" -> celsius + 273.15
            else -> Double.NaN
        }
    }
    
    private fun convertTime(value: Double, fromUnit: String, toUnit: String): Double {
        val seconds = when (fromUnit) {
            "Second" -> value
            "Minute" -> value * 60
            "Hour" -> value * 3600
            "Day" -> value * 86400
            else -> Double.NaN
        }
        return when (toUnit) {
            "Second" -> seconds
            "Minute" -> seconds / 60
            "Hour" -> seconds / 3600
            "Day" -> seconds / 86400
            else -> Double.NaN
        }
    }
}

