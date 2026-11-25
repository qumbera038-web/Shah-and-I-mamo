package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.ShahgeeModelCatalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.studioDataStore by preferencesDataStore(name = "shahgee_v200_settings")

data class StudioSettings(
    val autoSpeakReplies: Boolean = true,
    val hologramPulseEnabled: Boolean = true,
    val preferredLocaleTag: String = "en-US",
    val customBrainDirective: String = "",
    val selectedModelId: String = ShahgeeModelCatalog.MERGED_ID,
    val mergedMemberIds: Set<String> = ShahgeeModelCatalog.defaultMergedMemberIds,
    val localServerUrl: String = ""
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val AUTO_SPEAK = booleanPreferencesKey("auto_speak_replies")
        val HOLOGRAM_PULSE = booleanPreferencesKey("hologram_pulse_enabled")
        val LOCALE_TAG = stringPreferencesKey("preferred_locale_tag")
        val BRAIN_DIRECTIVE = stringPreferencesKey("custom_brain_directive")
        val SELECTED_MODEL_ID = stringPreferencesKey("selected_model_id")
        val MERGED_MEMBER_IDS = stringSetPreferencesKey("merged_member_ids")
        val LOCAL_SERVER_URL = stringPreferencesKey("local_server_url")
    }

    val settings: Flow<StudioSettings> = context.studioDataStore.data.map { prefs ->
        val rawMembers = prefs[Keys.MERGED_MEMBER_IDS]
        StudioSettings(
            autoSpeakReplies = prefs[Keys.AUTO_SPEAK] ?: true,
            hologramPulseEnabled = prefs[Keys.HOLOGRAM_PULSE] ?: true,
            preferredLocaleTag = prefs[Keys.LOCALE_TAG] ?: "en-US",
            customBrainDirective = prefs[Keys.BRAIN_DIRECTIVE] ?: "",
            selectedModelId = prefs[Keys.SELECTED_MODEL_ID] ?: ShahgeeModelCatalog.MERGED_ID,
            mergedMemberIds = if (rawMembers.isNullOrEmpty()) {
                ShahgeeModelCatalog.defaultMergedMemberIds
            } else {
                rawMembers
            },
            localServerUrl = prefs[Keys.LOCAL_SERVER_URL] ?: ""
        )
    }

    suspend fun setAutoSpeak(enabled: Boolean) {
        context.studioDataStore.edit { it[Keys.AUTO_SPEAK] = enabled }
    }

    suspend fun setHologramPulse(enabled: Boolean) {
        context.studioDataStore.edit { it[Keys.HOLOGRAM_PULSE] = enabled }
    }

    suspend fun setPreferredLocale(localeTag: String) {
        context.studioDataStore.edit { it[Keys.LOCALE_TAG] = localeTag }
    }

    suspend fun setCustomBrainDirective(directive: String) {
        context.studioDataStore.edit { it[Keys.BRAIN_DIRECTIVE] = directive.trim() }
    }

    suspend fun setSelectedModelId(modelId: String) {
        context.studioDataStore.edit { it[Keys.SELECTED_MODEL_ID] = modelId }
    }

    suspend fun setMergedMemberIds(memberIds: Set<String>) {
        val safeSet = memberIds.ifEmpty { ShahgeeModelCatalog.defaultMergedMemberIds }
        context.studioDataStore.edit { it[Keys.MERGED_MEMBER_IDS] = safeSet }
    }

    suspend fun setLocalServerUrl(url: String) {
        context.studioDataStore.edit { it[Keys.LOCAL_SERVER_URL] = url.trim().trimEnd('/') }
    }
}
