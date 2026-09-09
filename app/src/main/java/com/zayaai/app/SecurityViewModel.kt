package com.zayaai.app

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SecurityViewModel(
    private val dataStoreManager: DataStoreManager,
    private val context: Context? = null
) : ViewModel() {

    val settings: StateFlow<AppSettings> = dataStoreManager.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings(false, "PIN", "", "", true, true, "light")
    )

    private val _isUnlocked = mutableStateOf(false)
    val isUnlocked: State<Boolean> = _isUnlocked

    fun setUnlocked(unlocked: Boolean) {
        _isUnlocked.value = unlocked
    }

    fun updateAppLock(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.updateAppLock(enabled)
        }
    }

    fun setLockType(type: String) {
        viewModelScope.launch {
            dataStoreManager.setLockType(type)
        }
    }

    fun setPinCode(pin: String) {
        viewModelScope.launch {
            dataStoreManager.setPinCode(pin)
        }
    }

    fun setPatternCode(pattern: String) {
        viewModelScope.launch {
            dataStoreManager.setPatternCode(pattern)
        }
    }

    fun setVoiceEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setVoiceEnabled(enabled)
        }
    }

    fun setTypingAnimation(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setTypingAnimation(enabled)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            dataStoreManager.setThemeMode(mode)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setNotificationsEnabled(enabled)
        }
    }

    fun setStudyReminders(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setStudyReminders(enabled)
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            try {
                context?.let {
                    val db = AiZayaDatabase.getDatabase(it)
                    db.chatHistoryDao().clearHistory()
                }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    fun clearAiCache() {
        viewModelScope.launch {
            try {
                context?.let {
                    val db = AiZayaDatabase.getDatabase(it)
                    db.aiCacheDao().clearOldCache(System.currentTimeMillis() + 1) // Clear all
                }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
}

class SecurityViewModelFactory(
    private val dataStoreManager: DataStoreManager,
    private val context: Context? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SecurityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SecurityViewModel(dataStoreManager, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

