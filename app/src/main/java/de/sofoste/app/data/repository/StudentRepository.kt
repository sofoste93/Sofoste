package de.sofoste.app.data.repository

import de.sofoste.app.data.model.StudentMePayload
import de.sofoste.app.data.remote.StudentApi

class StudentRepository(
    private val api: StudentApi,
) {
    suspend fun restore(language: String): StudentMePayload = api.restore(language)

    suspend fun login(language: String, email: String, password: String): StudentMePayload =
        api.login(language, email, password)

    suspend fun activate(
        language: String,
        email: String,
        code: String,
        password: String,
    ): StudentMePayload = api.activate(language, email, code, password)

    suspend fun avatar(language: String): ByteArray = api.avatar(language)

    suspend fun refresh(language: String): StudentMePayload = api.me(language)

    suspend fun logout(language: String) = api.logout(language)

    suspend fun clearLocalSession() = api.clearLocalSession()
}
