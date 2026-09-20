package de.sofoste.app.data.remote

import android.content.Context
import de.sofoste.app.BuildConfig
import de.sofoste.app.data.model.ApiErrorEnvelope
import de.sofoste.app.data.model.StudentActivationRequest
import de.sofoste.app.data.model.StudentActivityPayload
import de.sofoste.app.data.model.StudentAgendaPayload
import de.sofoste.app.data.model.StudentAppointmentPayload
import de.sofoste.app.data.model.StudentAppointmentRequest
import de.sofoste.app.data.model.StudentAuthPayload
import de.sofoste.app.data.model.StudentBillingPayload
import de.sofoste.app.data.model.StudentCsrfPayload
import de.sofoste.app.data.model.StudentEnvelope
import de.sofoste.app.data.model.StudentLoginRequest
import de.sofoste.app.data.model.StudentLessonPayload
import de.sofoste.app.data.model.StudentMePayload
import de.sofoste.app.data.model.StudentNotificationReadPayload
import de.sofoste.app.data.model.StudentNotificationReadRequest
import de.sofoste.app.data.model.StudentDeviceRegistrationRequest
import de.sofoste.app.data.model.StudentDeviceTokenPayload
import de.sofoste.app.data.model.StudentDeviceRevocationRequest
import de.sofoste.app.data.model.StudentDeviceRevocationPayload
import de.sofoste.app.data.model.PasswordChangePayload
import de.sofoste.app.data.model.PasswordChangeRequest
import de.sofoste.app.data.model.StudentProfile
import de.sofoste.app.data.model.StudentProfilePayload
import de.sofoste.app.data.model.StudentProfileRequest
import de.sofoste.app.data.model.UploadPayload
import de.sofoste.app.data.session.SecureStudentCookieStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Headers
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class StudentApi(
    context: Context,
    baseUrl: String = BuildConfig.API_BASE_URL,
) : AutoCloseable {
    private val apiRoot = baseUrl.trimEnd('/')
    private val cookies = SecureStudentCookieStorage(context.applicationContext)
    private var csrfToken: String? = null
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; explicitNulls = false })
        }
        install(HttpCookies) {
            storage = cookies
        }
    }

    suspend fun restore(language: String): StudentMePayload {
        refreshCsrf(language)
        return me(language)
    }

    suspend fun login(language: String, email: String, password: String): StudentMePayload =
        authenticate(language) { token ->
            post<StudentLoginRequest, StudentAuthPayload>(
                path = "login",
                language = language,
                token = token,
                body = StudentLoginRequest(email.trim(), password),
            )
        }

    suspend fun activate(
        language: String,
        email: String,
        code: String,
        password: String,
    ): StudentMePayload = authenticate(language) { token ->
        post<StudentActivationRequest, StudentAuthPayload>(
            path = "activate",
            language = language,
            token = token,
            body = StudentActivationRequest(email.trim(), code.trim(), password),
        )
    }

    suspend fun me(language: String): StudentMePayload {
        val payload = get<StudentMePayload>("me", language)
        csrfToken = payload.csrfToken
        return payload
    }

    suspend fun avatar(language: String): ByteArray {
        val response = client.get("$apiRoot/student/avatar") {
            accept(ContentType.Image.Any)
            header(HttpHeaders.CacheControl, "no-store")
            parameter("lang", language)
        }
        ensureSuccess(response.status.value) { response.body<ApiErrorEnvelope>() }
        return response.body()
    }

    suspend fun agenda(language: String): StudentAgendaPayload = get("agenda", language)

    suspend fun rescheduleAppointment(language: String, id: String, date: String, time: String, note: String): Boolean =
        csrfPost<StudentAppointmentRequest, StudentAppointmentPayload>(
            "agenda/reschedule", language, StudentAppointmentRequest(id, date, time, note),
        ).rescheduled

    suspend fun cancelAppointment(language: String, id: String, note: String): Boolean =
        csrfPost<StudentAppointmentRequest, StudentAppointmentPayload>(
            "agenda/cancel", language, StudentAppointmentRequest(id, note = note),
        ).cancelled

    suspend fun lessons(language: String): StudentLessonPayload = get("lessons", language)

    suspend fun activity(language: String): StudentActivityPayload = get("notifications", language)

    suspend fun billing(language: String): StudentBillingPayload = get("billing", language)

    suspend fun markActivityRead(language: String, id: String): Boolean {
        val token = csrfToken ?: refreshCsrf(language)
        val payload = post<StudentNotificationReadRequest, StudentNotificationReadPayload>(
            path = "notifications/read",
            language = language,
            token = token,
            body = StudentNotificationReadRequest(id),
        )
        return payload.read
    }

    suspend fun markAllActivityRead(language: String): Boolean =
        csrfPost<StudentNotificationReadRequest, StudentNotificationReadPayload>(
            path = "notifications/read-all",
            language = language,
            body = StudentNotificationReadRequest("all"),
        ).read

    suspend fun registerDevice(language: String, deviceName: String): StudentDeviceTokenPayload =
        csrfPost<StudentDeviceRegistrationRequest, StudentDeviceTokenPayload>(
            path = "devices/register",
            language = language,
            body = StudentDeviceRegistrationRequest(deviceName, language),
        )

    suspend fun revokeDevice(language: String, token: String): Boolean =
        csrfPost<StudentDeviceRevocationRequest, StudentDeviceRevocationPayload>(
            path = "devices/revoke",
            language = language,
            body = StudentDeviceRevocationRequest(token),
        ).revoked

    suspend fun saveProfile(language: String, displayName: String, preferredLanguage: String): StudentProfile =
        csrfPost<StudentProfileRequest, StudentProfilePayload>(
            path = "profile",
            language = language,
            body = StudentProfileRequest(displayName.trim(), preferredLanguage),
        ).profile

    suspend fun changePassword(language: String, currentPassword: String, newPassword: String): Boolean {
        val payload = csrfPost<PasswordChangeRequest, PasswordChangePayload>(
            path = "password",
            language = language,
            body = PasswordChangeRequest(currentPassword, newPassword),
        )
        csrfToken = payload.csrfToken
        return payload.changed
    }

    suspend fun uploadAvatar(language: String, bytes: ByteArray, mimeType: String): Boolean {
        suspend fun request(token: String): UploadPayload {
            val extension = when (mimeType) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }
            val response = client.post("$apiRoot/student/avatar/upload") {
                accept(ContentType.Application.Json)
                header(HttpHeaders.CacheControl, "no-store")
                header("X-CSRF-Token", token)
                parameter("lang", language)
                setBody(MultiPartFormDataContent(formData {
                    append("avatar", bytes, Headers.build {
                        append(HttpHeaders.ContentType, mimeType)
                        append(HttpHeaders.ContentDisposition, "filename=\"avatar.$extension\"")
                    })
                }))
            }
            ensureSuccess(response.status.value) { response.body<ApiErrorEnvelope>() }
            return response.body<StudentEnvelope<UploadPayload>>().data
        }
        var token = csrfToken ?: refreshCsrf(language)
        val payload = try {
            request(token)
        } catch (exception: SofosteApiException) {
            if (exception.status != 403) throw exception
            token = refreshCsrf(language)
            request(token)
        }
        return payload.uploaded
    }

    suspend fun removeAvatar(language: String): Boolean =
        csrfPost<EmptyBody, UploadPayload>("avatar/remove", language, EmptyBody).removed

    suspend fun logout(language: String) {
        try {
            val token = csrfToken ?: refreshCsrf(language)
            val response = post<EmptyBody, StudentAuthPayload>(
                path = "logout",
                language = language,
                token = token,
                body = EmptyBody,
            )
            csrfToken = response.csrfToken
        } finally {
            clearLocalSession()
        }
    }

    suspend fun clearLocalSession() {
        csrfToken = null
        cookies.clear()
    }

    private suspend fun authenticate(
        language: String,
        request: suspend (String) -> StudentAuthPayload,
    ): StudentMePayload {
        var token = csrfToken ?: refreshCsrf(language)
        val authentication = try {
            request(token)
        } catch (exception: SofosteApiException) {
            if (exception.status != 403) throw exception
            token = refreshCsrf(language)
            request(token)
        }
        csrfToken = authentication.csrfToken
        return me(language)
    }

    private suspend inline fun <reified Request : Any, reified Response> csrfPost(
        path: String,
        language: String,
        body: Request,
    ): Response {
        var token = csrfToken ?: refreshCsrf(language)
        return try {
            post(path, language, token, body)
        } catch (exception: SofosteApiException) {
            if (exception.status != 403) throw exception
            token = refreshCsrf(language)
            post(path, language, token, body)
        }
    }

    private suspend fun refreshCsrf(language: String): String {
        val token = get<StudentCsrfPayload>("csrf", language).csrfToken
        csrfToken = token
        return token
    }

    private suspend inline fun <reified T> get(path: String, language: String): T {
        val response = client.get("$apiRoot/student/$path") {
            accept(ContentType.Application.Json)
            header(HttpHeaders.CacheControl, "no-store")
            parameter("lang", language)
        }
        ensureSuccess(response.status.value) { response.body<ApiErrorEnvelope>() }
        return response.body<StudentEnvelope<T>>().data
    }

    private suspend inline fun <reified Request : Any, reified Response> post(
        path: String,
        language: String,
        token: String,
        body: Request,
    ): Response {
        val response = client.post("$apiRoot/student/$path") {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            header(HttpHeaders.CacheControl, "no-store")
            parameter("lang", language)
            header("X-CSRF-Token", token)
            setBody(body)
        }
        ensureSuccess(response.status.value) { response.body<ApiErrorEnvelope>() }
        return response.body<StudentEnvelope<Response>>().data
    }

    private suspend fun ensureSuccess(status: Int, error: suspend () -> ApiErrorEnvelope) {
        if (status in 200..299) return
        val apiError = runCatching { error().error }.getOrNull()
        throw SofosteApiException(
            status = status,
            code = apiError?.code ?: "http_error",
            apiMessage = apiError?.message,
        )
    }

    override fun close() {
        client.close()
    }

    @kotlinx.serialization.Serializable
    private data object EmptyBody
}
