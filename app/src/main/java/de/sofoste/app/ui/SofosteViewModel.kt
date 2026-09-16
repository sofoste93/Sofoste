package de.sofoste.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.sofoste.app.data.model.PublicContent
import de.sofoste.app.data.model.AdminDashboard
import de.sofoste.app.data.model.AdminMePayload
import de.sofoste.app.data.model.AdminProfile
import de.sofoste.app.data.model.StudentMePayload
import de.sofoste.app.data.model.StudentActivityItem
import de.sofoste.app.data.model.StudentAgendaItem
import de.sofoste.app.data.model.StudentBillingPayload
import de.sofoste.app.data.model.StudentLessonItem
import de.sofoste.app.data.model.StudentOverview
import de.sofoste.app.data.model.StudentProfile
import de.sofoste.app.data.remote.AdminApi
import de.sofoste.app.data.remote.SofosteApi
import de.sofoste.app.data.remote.SofosteApiException
import de.sofoste.app.data.remote.StudentApi
import de.sofoste.app.data.repository.PublicRepository
import de.sofoste.app.data.repository.AdminRepository
import de.sofoste.app.data.repository.StudentRepository
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

enum class AppLanguage(val code: String, val label: String) {
    English("en", "EN"),
    French("fr", "FR"),
    German("de", "DE");

    companion object {
        fun deviceDefault(): AppLanguage = when (Locale.getDefault().language) {
            "fr" -> French
            "de" -> German
            else -> English
        }
    }
}

enum class Destination {
    Home,
    Media,
    Projects,
    Journal,
    Student,
    Admin,
}

enum class StudentSessionStatus {
    Checking,
    SignedOut,
    SignedIn,
}

data class StudentUiState(
    val status: StudentSessionStatus = StudentSessionStatus.Checking,
    val overview: StudentOverview? = null,
    val avatar: ByteArray? = null,
    val agenda: List<StudentAgendaItem> = emptyList(),
    val lessons: List<StudentLessonItem> = emptyList(),
    val activity: List<StudentActivityItem> = emptyList(),
    val billing: StudentBillingPayload? = null,
    val profile: StudentProfile? = null,
    val busy: Boolean = false,
    val errorCode: String? = null,
)

data class AdminUiState(
    val status: StudentSessionStatus = StudentSessionStatus.Checking,
    val profile: AdminProfile? = null,
    val dashboard: AdminDashboard? = null,
    val avatar: ByteArray? = null,
    val busy: Boolean = false,
    val errorCode: String? = null,
)

data class SofosteUiState(
    val language: AppLanguage = AppLanguage.deviceDefault(),
    val destination: Destination = Destination.Home,
    val content: PublicContent? = null,
    val loading: Boolean = true,
    val failed: Boolean = false,
    val student: StudentUiState = StudentUiState(),
    val admin: AdminUiState = AdminUiState(),
)

class SofosteViewModel(application: Application) : AndroidViewModel(application) {
    private val publicApi = SofosteApi()
    private val publicRepository = PublicRepository(publicApi)
    private val studentApi = StudentApi(application)
    private val studentRepository = StudentRepository(studentApi)
    private val adminApi = AdminApi(application)
    private val adminRepository = AdminRepository(adminApi)
    private var publicJob: Job? = null
    private var studentJob: Job? = null
    private var adminJob: Job? = null

    var state by mutableStateOf(SofosteUiState())
        private set

    init {
        refresh()
        restoreIdentity()
    }

    fun select(destination: Destination) {
        state = state.copy(destination = destination)
    }

    fun selectLanguage(language: AppLanguage) {
        if (language == state.language) return
        state = state.copy(language = language)
        refresh()
        restoreIdentity()
    }

    fun refresh() {
        publicJob?.cancel()
        publicJob = viewModelScope.launch {
            state = state.copy(loading = true, failed = false)
            runCatching { publicRepository.load(state.language.code) }
                .onSuccess { state = state.copy(content = it, loading = false) }
                .onFailure { state = state.copy(loading = false, failed = true) }
        }
    }

    fun loginStudent(email: String, password: String) {
        authenticateStudent { studentRepository.login(state.language.code, email, password) }
    }

    fun activateStudent(email: String, code: String, password: String) {
        authenticateStudent { studentRepository.activate(state.language.code, email, code, password) }
    }

    fun refreshStudent() {
        if (state.student.status != StudentSessionStatus.SignedIn) return
        studentJob?.cancel()
        studentJob = viewModelScope.launch {
            state = state.copy(student = state.student.copy(busy = true, errorCode = null))
            runCatching { studentRepository.refresh(state.language.code) }
                .onSuccess { applyStudentSession(it) }
                .onFailure { handleStudentFailure(it, keepDashboard = true) }
        }
    }

    fun logoutStudent() {
        studentJob?.cancel()
        studentJob = viewModelScope.launch {
            state = state.copy(student = state.student.copy(busy = true, errorCode = null))
            try {
                studentRepository.logout(state.language.code)
            } finally {
                state = state.copy(student = StudentUiState(status = StudentSessionStatus.SignedOut))
            }
        }
    }

    fun clearStudentError() {
        state = state.copy(student = state.student.copy(errorCode = null))
    }

    fun markStudentActivityRead(id: String) {
        if (state.student.status != StudentSessionStatus.SignedIn) return
        studentJob?.cancel()
        studentJob = viewModelScope.launch {
            state = state.copy(student = state.student.copy(busy = true, errorCode = null))
            runCatching {
                studentRepository.markActivityRead(state.language.code, id)
                studentRepository.refresh(state.language.code)
            }
                .onSuccess { applyStudentSession(it) }
                .onFailure { handleStudentFailure(it, keepDashboard = true) }
        }
    }

    fun rescheduleStudentAppointment(id: String, date: String, time: String) {
        mutateStudent { studentRepository.rescheduleAppointment(state.language.code, id, date, time) }
    }

    fun cancelStudentAppointment(id: String) {
        mutateStudent { studentRepository.cancelAppointment(state.language.code, id) }
    }

    fun saveStudentProfile(displayName: String, preferredLanguage: String) {
        mutateStudent {
            studentRepository.saveProfile(state.language.code, displayName, preferredLanguage)
        }
    }

    fun changeStudentPassword(currentPassword: String, newPassword: String) {
        mutateStudent { studentRepository.changePassword(state.language.code, currentPassword, newPassword) }
    }

    fun uploadStudentAvatar(bytes: ByteArray, mimeType: String) {
        if (!validAvatar(bytes, mimeType)) {
            state = state.copy(student = state.student.copy(errorCode = "avatar_invalid"))
            return
        }
        mutateStudent { studentRepository.uploadAvatar(state.language.code, bytes, mimeType) }
    }

    fun removeStudentAvatar() {
        mutateStudent { studentRepository.removeAvatar(state.language.code) }
    }

    fun loginAdmin(username: String, password: String) {
        adminJob?.cancel()
        adminJob = viewModelScope.launch {
            state = state.copy(admin = state.admin.copy(busy = true, errorCode = null))
            runCatching { adminRepository.login(state.language.code, username, password) }
                .onSuccess {
                    studentRepository.clearLocalSession()
                    state = state.copy(student = StudentUiState(status = StudentSessionStatus.SignedOut))
                    applyAdminSession(it)
                }
                .onFailure { handleAdminFailure(it, false) }
        }
    }

    fun refreshAdmin() {
        if (state.admin.status != StudentSessionStatus.SignedIn) return
        adminJob?.cancel()
        adminJob = viewModelScope.launch {
            state = state.copy(admin = state.admin.copy(busy = true, errorCode = null))
            runCatching { adminRepository.refresh(state.language.code) }
                .onSuccess { applyAdminSession(it) }
                .onFailure { handleAdminFailure(it, true) }
        }
    }

    fun saveAdminProfile(displayName: String, email: String, preferredLanguage: String) {
        mutateAdmin { adminRepository.saveProfile(state.language.code, displayName, email, preferredLanguage) }
    }

    fun changeAdminPassword(currentPassword: String, newPassword: String) {
        mutateAdmin { adminRepository.changePassword(state.language.code, currentPassword, newPassword) }
    }

    fun uploadAdminAvatar(bytes: ByteArray, mimeType: String) {
        if (!validAvatar(bytes, mimeType)) {
            state = state.copy(admin = state.admin.copy(errorCode = "avatar_invalid"))
            return
        }
        mutateAdmin { adminRepository.uploadAvatar(state.language.code, bytes, mimeType) }
    }

    fun removeAdminAvatar() {
        mutateAdmin { adminRepository.removeAvatar(state.language.code) }
    }

    fun logoutAdmin() {
        adminJob?.cancel()
        adminJob = viewModelScope.launch {
            state = state.copy(admin = state.admin.copy(busy = true, errorCode = null))
            try {
                adminRepository.logout(state.language.code)
            } finally {
                state = state.copy(admin = AdminUiState(status = StudentSessionStatus.SignedOut))
            }
        }
    }

    private fun restoreIdentity() {
        studentJob?.cancel()
        studentJob = viewModelScope.launch {
            state = state.copy(student = StudentUiState(status = StudentSessionStatus.Checking))
            runCatching { studentRepository.restore(state.language.code) }
                .onSuccess {
                    adminRepository.clearLocalSession()
                    state = state.copy(admin = AdminUiState(status = StudentSessionStatus.SignedOut))
                    applyStudentSession(it)
                }
                .onFailure {
                    handleStudentFailure(it, keepDashboard = false)
                    state = state.copy(admin = AdminUiState(status = StudentSessionStatus.Checking))
                    runCatching { adminRepository.restore(state.language.code) }
                        .onSuccess { applyAdminSession(it) }
                        .onFailure { error -> handleAdminFailure(error, false) }
                }
        }
    }

    private fun authenticateStudent(request: suspend () -> StudentMePayload) {
        studentJob?.cancel()
        studentJob = viewModelScope.launch {
            state = state.copy(student = state.student.copy(busy = true, errorCode = null))
            runCatching { request() }
                .onSuccess {
                    adminRepository.clearLocalSession()
                    state = state.copy(admin = AdminUiState(status = StudentSessionStatus.SignedOut))
                    applyStudentSession(it)
                }
                .onFailure { handleStudentFailure(it, keepDashboard = false) }
        }
    }

    private suspend fun applyStudentSession(session: StudentMePayload) {
        val language = state.language.code
        val avatar = if (session.overview.hasAvatar) runCatching {
            studentRepository.avatar(language)
        }.getOrNull() else null
        val privateData = studentRepository.privateData(language)
        state = state.copy(
            student = StudentUiState(
                status = StudentSessionStatus.SignedIn,
                overview = session.overview,
                avatar = avatar,
                agenda = privateData.agenda.items,
                lessons = privateData.lessons.items,
                activity = privateData.activity.items,
                billing = privateData.billing,
                profile = session.profile,
            ),
        )
    }

    private fun mutateStudent(request: suspend () -> Any) {
        if (state.student.status != StudentSessionStatus.SignedIn) return
        studentJob?.cancel()
        studentJob = viewModelScope.launch {
            state = state.copy(student = state.student.copy(busy = true, errorCode = null))
            runCatching {
                request()
                studentRepository.refresh(state.language.code)
            }.onSuccess { applyStudentSession(it) }
                .onFailure { handleStudentFailure(it, true) }
        }
    }

    private suspend fun applyAdminSession(session: AdminMePayload) {
        val avatar = if (session.profile.hasAvatar) runCatching {
            adminRepository.avatar(state.language.code)
        }.getOrNull() else null
        state = state.copy(
            admin = AdminUiState(
                status = StudentSessionStatus.SignedIn,
                profile = session.profile,
                dashboard = session.dashboard,
                avatar = avatar,
            ),
        )
    }

    private fun mutateAdmin(request: suspend () -> Any) {
        if (state.admin.status != StudentSessionStatus.SignedIn) return
        adminJob?.cancel()
        adminJob = viewModelScope.launch {
            state = state.copy(admin = state.admin.copy(busy = true, errorCode = null))
            runCatching {
                request()
                adminRepository.refresh(state.language.code)
            }.onSuccess { applyAdminSession(it) }
                .onFailure { handleAdminFailure(it, true) }
        }
    }

    private suspend fun handleAdminFailure(error: Throwable, keepDashboard: Boolean) {
        val apiError = error as? SofosteApiException
        if (apiError?.status == 401 && apiError.code == "unauthorized") {
            adminRepository.clearLocalSession()
            state = state.copy(admin = AdminUiState(status = StudentSessionStatus.SignedOut))
            return
        }
        val previous = state.admin
        state = state.copy(
            admin = if (keepDashboard && previous.dashboard != null) {
                previous.copy(busy = false, errorCode = apiError?.code ?: "unavailable")
            } else {
                AdminUiState(
                    status = StudentSessionStatus.SignedOut,
                    errorCode = apiError?.code ?: "unavailable",
                )
            },
        )
    }

    private fun validAvatar(bytes: ByteArray, mimeType: String): Boolean =
        bytes.isNotEmpty() && bytes.size <= 5 * 1024 * 1024 &&
            mimeType in setOf("image/jpeg", "image/png", "image/webp")

    private suspend fun handleStudentFailure(error: Throwable, keepDashboard: Boolean) {
        val apiError = error as? SofosteApiException
        if (apiError?.status == 401 && apiError.code == "unauthorized") {
            studentRepository.clearLocalSession()
            state = state.copy(student = StudentUiState(status = StudentSessionStatus.SignedOut))
            return
        }
        val previous = state.student
        state = state.copy(
            student = if (keepDashboard && previous.overview != null) {
                previous.copy(busy = false, errorCode = apiError?.code ?: "unavailable")
            } else {
                StudentUiState(
                    status = StudentSessionStatus.SignedOut,
                    errorCode = apiError?.code ?: "unavailable",
                )
            },
        )
    }

    override fun onCleared() {
        publicApi.close()
        studentApi.close()
        adminApi.close()
    }
}
