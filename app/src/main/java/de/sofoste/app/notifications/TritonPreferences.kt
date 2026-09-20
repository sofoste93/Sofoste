package de.sofoste.app.notifications

import android.content.Context
import androidx.core.content.edit
import java.security.MessageDigest

data class TritonSettings(val enabled: Boolean, val activity: Boolean, val agenda: Boolean, val language: String)

class TritonPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun settings() = TritonSettings(
        preferences.getBoolean(KEY_ENABLED, false),
        preferences.getBoolean(KEY_ACTIVITY, true),
        preferences.getBoolean(KEY_AGENDA, true),
        preferences.getString(KEY_LANGUAGE, "en").orEmpty().takeIf { it in LANGUAGES } ?: "en",
    )

    fun save(enabled: Boolean, activity: Boolean, agenda: Boolean, language: String) = preferences.edit {
        putBoolean(KEY_ENABLED, enabled)
        putBoolean(KEY_ACTIVITY, activity)
        putBoolean(KEY_AGENDA, agenda)
        putString(KEY_LANGUAGE, language.takeIf { it in LANGUAGES } ?: "en")
    }

    fun latestActivityId(): Long? = preferences.getString(KEY_ACTIVITY_ID, null)?.toLongOrNull()
    fun latestActivityId(value: String?) = preferences.edit {
        if (value == null) remove(KEY_ACTIVITY_ID) else putString(KEY_ACTIVITY_ID, value)
    }
    fun lastAppointmentKey(): String? = preferences.getString(KEY_APPOINTMENT, null)
    fun lastAppointmentKey(value: String) = preferences.edit { putString(KEY_APPOINTMENT, value) }
    fun matchesIdentity(email: String): Boolean =
        preferences.getString(KEY_IDENTITY, null) == identityFingerprint(email)
    fun bindIdentity(email: String) = preferences.edit {
        putString(KEY_IDENTITY, identityFingerprint(email))
    }
    fun disable() = preferences.edit { putBoolean(KEY_ENABLED, false) }

    private fun identityFingerprint(email: String): String = MessageDigest.getInstance("SHA-256")
        .digest(email.trim().lowercase().toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    private companion object {
        const val PREFERENCES_NAME = "sofoste_triton_preferences"
        const val KEY_ENABLED = "enabled"
        const val KEY_ACTIVITY = "activity"
        const val KEY_AGENDA = "agenda"
        const val KEY_LANGUAGE = "language"
        const val KEY_ACTIVITY_ID = "latest_activity_id"
        const val KEY_APPOINTMENT = "last_appointment_key"
        const val KEY_IDENTITY = "student_identity_hash"
        val LANGUAGES = setOf("en", "fr", "de")
    }
}
