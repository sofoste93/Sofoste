package de.sofoste.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StudentEnvelope<T>(val data: T)

@Serializable
data class StudentCsrfPayload(
    @SerialName("csrf_token") val csrfToken: String,
)

@Serializable
data class StudentAuthPayload(
    @SerialName("signed_in") val signedIn: Boolean = false,
    @SerialName("signed_out") val signedOut: Boolean = false,
    @SerialName("csrf_token") val csrfToken: String,
)

@Serializable
data class StudentLoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class StudentActivationRequest(
    val email: String,
    val code: String,
    val password: String,
)

@Serializable
data class StudentMePayload(
    @SerialName("signed_in") val signedIn: Boolean,
    @SerialName("csrf_token") val csrfToken: String,
    val overview: StudentOverview,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)

@Serializable
data class StudentOverview(
    val lessons: Int,
    val unread: Int,
    @SerialName("payments_due") val paymentsDue: Int,
    @SerialName("amount_due_cents") val amountDueCents: Int,
    @SerialName("has_avatar") val hasAvatar: Boolean,
)
