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
    val profile: StudentProfile? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)

@Serializable
data class StudentProfile(
    val email: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("preferred_language") val preferredLanguage: String,
)

@Serializable
data class StudentProfileRequest(
    @SerialName("display_name") val displayName: String,
    @SerialName("preferred_language") val preferredLanguage: String,
)

@Serializable
data class StudentProfilePayload(val profile: StudentProfile)

@Serializable
data class PasswordChangeRequest(
    @SerialName("current_password") val currentPassword: String,
    @SerialName("new_password") val newPassword: String,
)

@Serializable
data class PasswordChangePayload(
    val changed: Boolean,
    @SerialName("csrf_token") val csrfToken: String,
)

@Serializable
data class UploadPayload(val uploaded: Boolean = false, val removed: Boolean = false)

@Serializable
data class StudentOverview(
    val lessons: Int,
    val upcoming: Int = 0,
    val unread: Int,
    @SerialName("payments_due") val paymentsDue: Int,
    @SerialName("amount_due_cents") val amountDueCents: Int,
    @SerialName("has_avatar") val hasAvatar: Boolean,
)

@Serializable
data class StudentAgendaPayload(
    val items: List<StudentAgendaItem>,
    val from: String,
    val to: String,
    val truncated: Boolean = false,
)

@Serializable
data class StudentAgendaItem(
    val id: String,
    @SerialName("session_date") val sessionDate: String,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("duration_minutes") val durationMinutes: Int,
    val status: String,
    @SerialName("can_manage") val canManage: Boolean = false,
)

@Serializable
data class StudentAppointmentRequest(
    val id: String,
    val date: String? = null,
    val time: String? = null,
)

@Serializable
data class StudentAppointmentPayload(
    val rescheduled: Boolean = false,
    val cancelled: Boolean = false,
)

@Serializable
data class StudentLessonPayload(
    val items: List<StudentLessonItem>,
    @SerialName("next_before") val nextBefore: String? = null,
)

@Serializable
data class StudentLessonItem(
    val id: String,
    val title: String,
    val summary: String,
    val progress: String,
    val practice: String,
    val revision: Int,
    @SerialName("published_at") val publishedAt: String,
    @SerialName("session_date") val sessionDate: String,
)

@Serializable
data class StudentActivityPayload(
    val items: List<StudentActivityItem>,
    @SerialName("next_before") val nextBefore: String? = null,
)

@Serializable
data class StudentActivityItem(
    val id: String,
    @SerialName("publication_id") val publicationId: String,
    @SerialName("read_at") val readAt: String? = null,
    @SerialName("created_at") val createdAt: String,
    val type: String,
    val title: String,
    val summary: String,
    val progress: String,
    val practice: String,
    @SerialName("session_date") val sessionDate: String,
)

@Serializable
data class StudentBillingPayload(
    val profile: StudentBillingProfile? = null,
    val items: List<StudentPaymentItem>,
    @SerialName("next_before") val nextBefore: String? = null,
    @SerialName("payment_url") val paymentUrl: String? = null,
)

@Serializable
data class StudentBillingProfile(
    @SerialName("amount_cents") val amountCents: Int,
    val currency: String,
    @SerialName("billing_cycle") val billingCycle: String,
    @SerialName("session_minutes") val sessionMinutes: Int? = null,
    @SerialName("public_note") val publicNote: String? = null,
)

@Serializable
data class StudentPaymentItem(
    val id: String,
    val label: String,
    @SerialName("amount_cents") val amountCents: Int,
    val currency: String,
    @SerialName("due_on") val dueOn: String,
    val status: String,
    @SerialName("payment_method") val paymentMethod: String? = null,
    @SerialName("paid_at") val paidAt: String? = null,
    @SerialName("public_note") val publicNote: String? = null,
)

data class StudentPrivateData(
    val agenda: StudentAgendaPayload,
    val lessons: StudentLessonPayload,
    val activity: StudentActivityPayload,
    val billing: StudentBillingPayload,
)

@Serializable
data class StudentNotificationReadRequest(val id: String)

@Serializable
data class StudentNotificationReadPayload(val read: Boolean)
