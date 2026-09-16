package de.sofoste.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminLoginRequest(val username: String, val password: String)

@Serializable
data class AdminProfile(
    val username: String,
    @SerialName("display_name") val displayName: String,
    val email: String? = null,
    @SerialName("preferred_language") val preferredLanguage: String,
    @SerialName("has_avatar") val hasAvatar: Boolean,
)

@Serializable
data class AdminProfileRequest(
    @SerialName("display_name") val displayName: String,
    val email: String,
    @SerialName("preferred_language") val preferredLanguage: String,
)

@Serializable
data class AdminProfilePayload(val profile: AdminProfile)

@Serializable
data class AdminDashboard(
    @SerialName("new_messages") val newMessages: Int,
    @SerialName("pending_reservations") val pendingReservations: Int,
    @SerialName("pending_comments") val pendingComments: Int,
    @SerialName("active_students") val activeStudents: Int,
    @SerialName("reviews_due") val reviewsDue: Int,
    @SerialName("next_lesson") val nextLesson: AdminNextLesson? = null,
)

@Serializable
data class AdminNextLesson(
    val name: String,
    val service: String,
    val date: String,
    val time: String,
    val status: String,
)

@Serializable
data class AdminMePayload(
    @SerialName("signed_in") val signedIn: Boolean,
    @SerialName("csrf_token") val csrfToken: String,
    val profile: AdminProfile,
    val dashboard: AdminDashboard,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)
