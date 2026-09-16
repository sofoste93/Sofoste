package de.sofoste.app.data.remote

import de.sofoste.app.BuildConfig
import de.sofoste.app.data.model.ApiEnvelope
import de.sofoste.app.data.model.ApiErrorEnvelope
import de.sofoste.app.data.model.ArticleItem
import de.sofoste.app.data.model.HomePayload
import de.sofoste.app.data.model.MediaItem
import de.sofoste.app.data.model.ProjectItem
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class SofosteApi(
    baseUrl: String = BuildConfig.API_BASE_URL,
) : AutoCloseable {
    private val apiRoot = baseUrl.trimEnd('/')
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                },
            )
        }
    }

    suspend fun home(language: String): HomePayload =
        request<HomePayload>("home", language).data

    suspend fun media(language: String): List<MediaItem> =
        request<List<MediaItem>>("media", language).data

    suspend fun projects(language: String): List<ProjectItem> =
        request<List<ProjectItem>>("projects", language).data

    suspend fun journal(language: String): List<ArticleItem> =
        request<List<ArticleItem>>("journal", language, mapOf("per_page" to "30")).data

    private suspend inline fun <reified T> request(
        path: String,
        language: String,
        query: Map<String, String> = emptyMap(),
    ): ApiEnvelope<T> {
        val response = client.get("$apiRoot/$path") {
            accept(ContentType.Application.Json)
            parameter("lang", language)
            query.forEach { (name, value) -> parameter(name, value) }
        }
        if (!response.status.isSuccess()) {
            val apiError = runCatching { response.body<ApiErrorEnvelope>().error }.getOrNull()
            throw SofosteApiException(
                status = response.status.value,
                code = apiError?.code ?: "http_error",
                apiMessage = apiError?.message,
            )
        }
        return response.body()
    }

    override fun close() {
        client.close()
    }
}

class SofosteApiException(
    val status: Int,
    val code: String,
    val apiMessage: String?,
) : Exception(apiMessage ?: "Sofoste API request failed with HTTP $status")
