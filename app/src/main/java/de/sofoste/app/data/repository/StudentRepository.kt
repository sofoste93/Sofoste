package de.sofoste.app.data.repository

import de.sofoste.app.data.model.StudentMePayload
import de.sofoste.app.data.model.StudentActivityPayload
import de.sofoste.app.data.model.StudentAgendaPayload
import de.sofoste.app.data.model.StudentBillingPayload
import de.sofoste.app.data.model.StudentLessonPayload
import de.sofoste.app.data.model.StudentPrivateData
import de.sofoste.app.data.remote.SofosteApiException
import de.sofoste.app.data.remote.StudentApi
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope

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

    suspend fun privateData(language: String): StudentPrivateData = supervisorScope {
        val agenda = async {
            privateCall(StudentAgendaPayload(emptyList(), "", "")) { api.agenda(language) }
        }
        val lessons = async {
            privateCall(StudentLessonPayload(emptyList())) { api.lessons(language) }
        }
        val activity = async {
            privateCall(StudentActivityPayload(emptyList())) { api.activity(language) }
        }
        val billing = async {
            privateCall(StudentBillingPayload(items = emptyList())) { api.billing(language) }
        }
        StudentPrivateData(
            agenda = agenda.await(),
            lessons = lessons.await(),
            activity = activity.await(),
            billing = billing.await(),
        )
    }

    suspend fun markActivityRead(language: String, id: String): Boolean =
        api.markActivityRead(language, id)

    private suspend fun <T> privateCall(fallback: T, request: suspend () -> T): T = try {
        request()
    } catch (exception: SofosteApiException) {
        if (exception.status == 401) throw exception
        fallback
    }

    suspend fun logout(language: String) = api.logout(language)

    suspend fun clearLocalSession() = api.clearLocalSession()
}
