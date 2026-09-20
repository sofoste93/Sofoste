package de.sofoste.app.notifications

import de.sofoste.app.BuildConfig
import de.sofoste.app.data.model.StudentEnvelope
import de.sofoste.app.data.model.StudentSignalPayload
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class StudentSignalApi(baseUrl: String = BuildConfig.API_BASE_URL) : AutoCloseable {
    private val endpoint = baseUrl.trimEnd('/') + "/student/signals"
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; explicitNulls = false }) }
    }
    suspend fun load(token: String): StudentSignalPayload {
        val response = client.get(endpoint) {
            accept(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.CacheControl, "no-store")
        }
        if (response.status.value == 401) throw SignalAuthorizationException()
        if (response.status.value !in 200..299) throw SignalUnavailableException()
        return response.body<StudentEnvelope<StudentSignalPayload>>().data
    }
    override fun close() = client.close()
}

class SignalAuthorizationException : RuntimeException()
class SignalUnavailableException : RuntimeException()
