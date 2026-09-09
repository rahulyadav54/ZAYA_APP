package com.zayaai.app

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*

enum class TimerMode(val label: String, val durationMs: Long) {
    Focus("Focus", 25 * 60 * 1000L),
    ShortBreak("Short Break", 5 * 60 * 1000L),
    LongBreak("Long Break", 15 * 60 * 1000L)
}

class PomodoroViewModel : ViewModel() {

    var currentMode by mutableStateOf(TimerMode.Focus)
        private set

    var timeRemaining by mutableLongStateOf(TimerMode.Focus.durationMs)
    var isRunning by mutableStateOf(false)
    private var timerJob: Job? = null

    var completedSessions by mutableIntStateOf(0)
        private set

    private val motivationalQuotes = listOf(
        "Deep work is the superpower of the 21st century. ðŸ§ ",
        "Small steps every day lead to big results. ðŸš€",
        "You don't have to be great to start. âœ¨",
        "Focus is the art of knowing what to ignore. ðŸŽ¯",
        "Discipline is choosing what you want most. ðŸ’Ž",
        "The secret of getting ahead is getting started. ðŸ”¥",
        "Your future self will thank you. ðŸŒŸ",
        "One task at a time. That's the magic. âš¡"
    )

    var currentQuote by mutableStateOf(motivationalQuotes.random())
        private set

    fun switchMode(mode: TimerMode) {
        timerJob?.cancel()
        isRunning = false
        currentMode = mode
        timeRemaining = mode.durationMs
        currentQuote = motivationalQuotes.random()
    }

    fun toggleTimer() {
        if (isRunning) {
            timerJob?.cancel()
            isRunning = false
        } else {
            isRunning = true
            timerJob = viewModelScope.launch {
                while (timeRemaining > 0) {
                    delay(1000)
                    timeRemaining -= 1000
                }
                isRunning = false
                if (currentMode == TimerMode.Focus) {
                    completedSessions++
                }
                currentQuote = motivationalQuotes.random()
            }
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        isRunning = false
        timeRemaining = currentMode.durationMs
        currentQuote = motivationalQuotes.random()
    }

    val progress: Float
        get() = timeRemaining.toFloat() / currentMode.durationMs

    fun formatTime(): String {
        val minutes = (timeRemaining / 1000) / 60
        val seconds = (timeRemaining / 1000) % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}

