package de.sofoste.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiEnvelope<T>(
    val data: T,
    val meta: ApiMeta,
)

@Serializable
data class ApiMeta(
    @SerialName("api_version") val apiVersion: String,
    val language: String,
    val count: Int? = null,
    val pagination: Pagination? = null,
)

@Serializable
data class Pagination(
    val page: Int,
    @SerialName("per_page") val perPage: Int,
    @SerialName("has_more") val hasMore: Boolean,
)

@Serializable
data class ApiErrorEnvelope(val error: ApiError)

@Serializable
data class ApiError(
    val code: String,
    val message: String,
    val status: Int,
)

@Serializable
data class HomePayload(
    @SerialName("featured_media") val featuredMedia: MediaItem? = null,
    @SerialName("featured_project") val featuredProject: ProjectItem? = null,
    @SerialName("latest_article") val latestArticle: ArticleItem? = null,
)

@Serializable
data class MediaItem(
    val slug: String,
    val kind: String,
    val title: String,
    val description: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("image_alt") val imageAlt: String,
    @SerialName("release_date") val releaseDate: String? = null,
    val featured: Boolean = false,
    val sources: List<MediaSource> = emptyList(),
    @SerialName("web_url") val webUrl: String,
)

@Serializable
data class MediaSource(
    val provider: String,
    val label: String? = null,
    val url: String,
    val playback: String,
    val primary: Boolean,
)

@Serializable
data class ProjectItem(
    val slug: String,
    val kind: String,
    val title: String,
    val excerpt: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("image_alt") val imageAlt: String,
    @SerialName("start_date") val startDate: String? = null,
    val featured: Boolean = false,
    @SerialName("web_url") val webUrl: String,
)

@Serializable
data class ArticleItem(
    val slug: String,
    val language: String,
    val topic: String,
    val title: String,
    val excerpt: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("image_alt") val imageAlt: String,
    @SerialName("published_at") val publishedAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("reading_minutes") val readingMinutes: Int,
    @SerialName("web_url") val webUrl: String,
)

data class PublicContent(
    val home: HomePayload,
    val media: List<MediaItem>,
    val projects: List<ProjectItem>,
    val articles: List<ArticleItem>,
)
