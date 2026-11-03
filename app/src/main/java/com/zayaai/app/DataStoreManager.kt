package com.zayaai.app

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class SavedSession(val email: String, val role: String, val name: String)

class DataStoreManager(private val context: Context) {

    companion object {
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val LOCK_TYPE = stringPreferencesKey("lock_type")
        val PIN_CODE = stringPreferencesKey("pin_code")
        val PATTERN_CODE = stringPreferencesKey("pattern_code")
        val VOICE_RESPONSE_ENABLED = booleanPreferencesKey("voice_enabled")
        val AI_TYPING_ANIMATION = booleanPreferencesKey("typing_animation")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val STUDY_REMINDERS = booleanPreferencesKey("study_reminders")

        // Session persistence
        val LOGGED_IN_EMAIL = stringPreferencesKey("logged_in_email")
        val LOGGED_IN_ROLE = stringPreferencesKey("logged_in_role")
        val LOGGED_IN_NAME = stringPreferencesKey("logged_in_name")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val BRAIN_GAME_HINTS = intPreferencesKey("brain_game_hints")
        val BRAIN_GAME_XP = intPreferencesKey("brain_game_xp")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            AppSettings(
                appLockEnabled = preferences[APP_LOCK_ENABLED] ?: false,
                lockType = preferences[LOCK_TYPE] ?: "PIN",
                pinCode = preferences[PIN_CODE] ?: "",
                patternCode = preferences[PATTERN_CODE] ?: "",
                voiceEnabled = preferences[VOICE_RESPONSE_ENABLED] ?: true,
                typingAnimation = preferences[AI_TYPING_ANIMATION] ?: true,
                themeMode = preferences[THEME_MODE] ?: "light",
                notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true,
                studyReminders = preferences[STUDY_REMINDERS] ?: false
            )
        }

    suspend fun updateAppLock(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[APP_LOCK_ENABLED] = enabled
        }
    }

    suspend fun setLockType(type: String) {
        context.dataStore.edit { preferences ->
            preferences[LOCK_TYPE] = type
        }
    }

    suspend fun setPinCode(pin: String) {
        context.dataStore.edit { preferences ->
            preferences[PIN_CODE] = pin
        }
    }

    suspend fun setPatternCode(pattern: String) {
        context.dataStore.edit { preferences ->
            preferences[PATTERN_CODE] = pattern
        }
    }

    suspend fun setVoiceEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[VOICE_RESPONSE_ENABLED] = enabled
        }
    }

    suspend fun setTypingAnimation(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AI_TYPING_ANIMATION] = enabled
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setStudyReminders(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[STUDY_REMINDERS] = enabled
        }
    }

    // ===== Session Persistence =====

    suspend fun saveSession(email: String, role: String, name: String) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[LOGGED_IN_EMAIL] = email
            preferences[LOGGED_IN_ROLE] = role
            preferences[LOGGED_IN_NAME] = name
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = false
            preferences.remove(LOGGED_IN_EMAIL)
            preferences.remove(LOGGED_IN_ROLE)
            preferences.remove(LOGGED_IN_NAME)
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun isOnboardingCompleted(): Boolean {
        return try {
            val prefs = context.dataStore.data.catch { emit(emptyPreferences()) }.first()
            prefs[ONBOARDING_COMPLETED] ?: false
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getSavedSession(): SavedSession? {
        return try {
            val prefs = context.dataStore.data.catch { emit(emptyPreferences()) }.first()
            val isLoggedIn = prefs[IS_LOGGED_IN] ?: false
            if (isLoggedIn) {
                val email = prefs[LOGGED_IN_EMAIL]
                val role = prefs[LOGGED_IN_ROLE]
                val name = prefs[LOGGED_IN_NAME] ?: ""
                if (email != null && role != null) {
                    SavedSession(email = email, role = role, name = name)
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }

    val brainGameHintsFlow: Flow<Int> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[BRAIN_GAME_HINTS] ?: 5 }

    val brainGameXpFlow: Flow<Int> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[BRAIN_GAME_XP] ?: 120 }

    suspend fun addHints(count: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[BRAIN_GAME_HINTS] ?: 5
            prefs[BRAIN_GAME_HINTS] = current + count
            val currentXp = prefs[BRAIN_GAME_XP] ?: 120
            prefs[BRAIN_GAME_XP] = currentXp + (count * 25)
        }
    }

    suspend fun useHint(): Boolean {
        var used = false
        context.dataStore.edit { prefs ->
            val current = prefs[BRAIN_GAME_HINTS] ?: 5
            if (current > 0) {
                prefs[BRAIN_GAME_HINTS] = current - 1
                used = true
            }
        }
        return used
    }
}

data class AppSettings(
    val appLockEnabled: Boolean,
    val lockType: String,
    val pinCode: String,
    val patternCode: String,
    val voiceEnabled: Boolean,
    val typingAnimation: Boolean,
    val themeMode: String = "light",
    val notificationsEnabled: Boolean = true,
    val studyReminders: Boolean = false
)

