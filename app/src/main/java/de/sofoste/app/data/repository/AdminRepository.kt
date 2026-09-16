package de.sofoste.app.data.repository

import de.sofoste.app.data.model.AdminMePayload
import de.sofoste.app.data.model.AdminProfile
import de.sofoste.app.data.remote.AdminApi

class AdminRepository(private val api: AdminApi) {
    suspend fun restore(language: String): AdminMePayload = api.restore(language)
    suspend fun login(language: String, username: String, password: String): AdminMePayload =
        api.login(language, username, password)
    suspend fun refresh(language: String): AdminMePayload = api.me(language)
    suspend fun avatar(language: String): ByteArray = api.avatar(language)
    suspend fun saveProfile(language: String, name: String, email: String, preferredLanguage: String): AdminProfile =
        api.saveProfile(language, name, email, preferredLanguage)
    suspend fun changePassword(language: String, current: String, replacement: String): Boolean =
        api.changePassword(language, current, replacement)
    suspend fun uploadAvatar(language: String, bytes: ByteArray, mimeType: String): Boolean =
        api.uploadAvatar(language, bytes, mimeType)
    suspend fun removeAvatar(language: String): Boolean = api.removeAvatar(language)
    suspend fun logout(language: String) = api.logout(language)
    suspend fun clearLocalSession() = api.clearLocalSession()
}
