package com.quill.editor.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** User-configurable settings, persisted in DataStore. */
data class QuillSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val fontSize: Int = 14,
    val wordWrap: Boolean = true,
    val autoSaveIntervalSeconds: Int = 10,
    val encoding: String = "UTF-8",
    val showLineNumbers: Boolean = true,
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "quill_settings")

class SettingsRepository(context: Context) {

    private val appContext = context.applicationContext

    private object Keys {
        val THEME = intPreferencesKey("theme_mode")
        val DYNAMIC = booleanPreferencesKey("dynamic_color")
        val FONT = intPreferencesKey("font_size")
        val WRAP = booleanPreferencesKey("word_wrap")
        val AUTOSAVE = intPreferencesKey("autosave_seconds")
        val ENCODING = stringPreferencesKey("encoding")
        val LINES = booleanPreferencesKey("show_line_numbers")
    }

    val settings: Flow<QuillSettings> = appContext.dataStore.data.map { prefs ->
        QuillSettings(
            themeMode = ThemeMode.entries.getOrElse(prefs[Keys.THEME] ?: 0) { ThemeMode.SYSTEM },
            dynamicColor = prefs[Keys.DYNAMIC] ?: true,
            fontSize = prefs[Keys.FONT] ?: 14,
            wordWrap = prefs[Keys.WRAP] ?: true,
            autoSaveIntervalSeconds = prefs[Keys.AUTOSAVE] ?: 10,
            encoding = prefs[Keys.ENCODING] ?: "UTF-8",
            showLineNumbers = prefs[Keys.LINES] ?: true,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) = edit { it[Keys.THEME] = mode.ordinal }
    suspend fun setDynamicColor(enabled: Boolean) = edit { it[Keys.DYNAMIC] = enabled }
    suspend fun setFontSize(size: Int) = edit { it[Keys.FONT] = size.coerceIn(10, 24) }
    suspend fun setWordWrap(enabled: Boolean) = edit { it[Keys.WRAP] = enabled }
    suspend fun setAutoSaveInterval(seconds: Int) = edit { it[Keys.AUTOSAVE] = seconds.coerceIn(3, 120) }
    suspend fun setEncoding(encoding: String) = edit { it[Keys.ENCODING] = encoding }
    suspend fun setShowLineNumbers(enabled: Boolean) = edit { it[Keys.LINES] = enabled }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        appContext.dataStore.edit(block)
    }
}
