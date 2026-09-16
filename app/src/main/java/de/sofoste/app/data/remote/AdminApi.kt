package de.sofoste.app.data.remote

import android.content.Context
import de.sofoste.app.BuildConfig
import de.sofoste.app.data.model.AdminLoginRequest
import de.sofoste.app.data.model.AdminMePayload
import de.sofoste.app.data.model.AdminProfile
import de.sofoste.app.data.model.AdminProfilePayload
import de.sofoste.app.data.model.AdminProfileRequest
import de.sofoste.app.data.model.ApiErrorEnvelope
import de.sofoste.app.data.model.PasswordChangePayload
import de.sofoste.app.data.model.PasswordChangeRequest
import de.sofoste.app.data.model.StudentAuthPayload
import de.sofoste.app.data.model.StudentCsrfPayload
import de.sofoste.app.data.model.StudentEnvelope
import de.sofoste.app.data.model.UploadPayload
import de.sofoste.app.data.session.SecureAdminCookieStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.accept
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class AdminApi(context: Context, baseUrl: String = BuildConfig.API_BASE_URL) : AutoCloseable {
    private val apiRoot = baseUrl.trimEnd('/')
    private val cookies = SecureAdminCookieStorage(context.applicationContext)
    private var csrfToken: String? = null
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; explicitNulls = false })
        }
        install(HttpCookies) { storage = cookies }
    }

    suspend fun restore(language: String): AdminMePayload {
        refreshCsrf(language)
        return me(language)
    }

    suspend fun login(language: String, username: String, password: String): AdminMePayload {
        csrfPost<AdminLoginRequest, StudentAuthPayload>(
            "login", language, AdminLoginRequest(username.trim(), password),
        )
        return me(language)
    }

    suspend fun me(language: String): AdminMePayload {
        val payload = get<AdminMePayload>("me", language)
        csrfToken = payload.csrfToken
        return payload
    }

    suspend fun avatar(language: String): ByteArray {
        val response = client.get("$apiRoot/admin/avatar") {
            accept(ContentType.Image.Any)
            header(HttpHeaders.CacheControl, "no-store")
            parameter("lang", language)
        }
        ensureSuccess(response.status.value) { response.body<ApiErrorEnvelope>() }
        return response.body()
    }

    suspend fun saveProfile(
        language: String,
        displayName: String,
        email: String,
        preferredLanguage: String,
    ): AdminProfile = csrfPost<AdminProfileRequest, AdminProfilePayload>(
        "profile", language, AdminProfileRequest(displayName.trim(), email.trim(), preferredLanguage),
    ).profile

    suspend fun changePassword(language: String, currentPassword: String, newPassword: String): Boolean {
        val payload = csrfPost<PasswordChangeRequest, PasswordChangePayload>(
            "password", language, PasswordChangeRequest(currentPassword, newPassword),
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
            val response = client.post("$apiRoot/admin/avatar/upload") {
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
            csrfPost<EmptyBody, StudentAuthPayload>("logout", language, EmptyBody)
        } finally {
            clearLocalSession()
        }
    }

    suspend fun clearLocalSession() {
        csrfToken = null
        cookies.clear()
    }

    private suspend fun refreshCsrf(language: String): String {
        val token = get<StudentCsrfPayload>("csrf", language).csrfToken
        csrfToken = token
        return token
    }

    private suspend inline fun <reified T> get(path: String, language: String): T {
        val response = client.get("$apiRoot/admin/$path") {
            accept(ContentType.Application.Json)
            header(HttpHeaders.CacheControl, "no-store")
            parameter("lang", language)
        }
        ensureSuccess(response.status.value) { response.body<ApiErrorEnvelope>() }
        return response.body<StudentEnvelope<T>>().data
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

    private suspend inline fun <reified Request : Any, reified Response> post(
        path: String,
        language: String,
        token: String,
        body: Request,
    ): Response {
        val response = client.post("$apiRoot/admin/$path") {
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
        throw SofosteApiException(status, apiError?.code ?: "http_error", apiError?.message)
    }

    override fun close() = client.close()

    @kotlinx.serialization.Serializable
    private data object EmptyBody
}
