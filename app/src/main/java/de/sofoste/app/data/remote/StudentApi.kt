package de.sofoste.app.data.remote

import android.content.Context
import de.sofoste.app.BuildConfig
import de.sofoste.app.data.model.ApiErrorEnvelope
import de.sofoste.app.data.model.StudentActivationRequest
import de.sofoste.app.data.model.StudentAuthPayload
import de.sofoste.app.data.model.StudentCsrfPayload
import de.sofoste.app.data.model.StudentEnvelope
import de.sofoste.app.data.model.StudentLoginRequest
import de.sofoste.app.data.model.StudentMePayload
import de.sofoste.app.data.session.SecureStudentCookieStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
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
