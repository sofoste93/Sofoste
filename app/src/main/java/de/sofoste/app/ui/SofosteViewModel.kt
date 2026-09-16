package de.sofoste.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.sofoste.app.data.model.PublicContent
import de.sofoste.app.data.model.StudentMePayload
import de.sofoste.app.data.model.StudentOverview
import de.sofoste.app.data.remote.SofosteApi
import de.sofoste.app.data.remote.SofosteApiException
import de.sofoste.app.data.remote.StudentApi
import de.sofoste.app.data.repository.PublicRepository
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
)

class SofosteViewModel(application: Application) : AndroidViewModel(application) {
    private val publicApi = SofosteApi()
    private val publicRepository = PublicRepository(publicApi)
    private val studentApi = StudentApi(application)
    private val studentRepository = StudentRepository(studentApi)
    private var publicJob: Job? = null
    private var studentJob: Job? = null

    var state by mutableStateOf(SofosteUiState())
        private set

    init {
        refresh()
        restoreStudent()
    }

    fun select(destination: Destination) {
        state = state.copy(destination = destination)
    }

    fun selectLanguage(language: AppLanguage) {
        if (language == state.language) return
        state = state.copy(language = language)
        refresh()
        restoreStudent()
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

    private fun restoreStudent() {
        studentJob?.cancel()
        studentJob = viewModelScope.launch {
            state = state.copy(student = StudentUiState(status = StudentSessionStatus.Checking))
            runCatching { studentRepository.restore(state.language.code) }
                .onSuccess { applyStudentSession(it) }
                .onFailure { handleStudentFailure(it, keepDashboard = false) }
        }
    }

    private fun authenticateStudent(request: suspend () -> StudentMePayload) {
        studentJob?.cancel()
        studentJob = viewModelScope.launch {
            state = state.copy(student = state.student.copy(busy = true, errorCode = null))
            runCatching { request() }
                .onSuccess { applyStudentSession(it) }
                .onFailure { handleStudentFailure(it, keepDashboard = false) }
        }
    }

    private suspend fun applyStudentSession(session: StudentMePayload) {
        val avatar = if (session.overview.hasAvatar) {
            runCatching { studentRepository.avatar(state.language.code) }.getOrNull()
        } else {
            null
        }
        state = state.copy(
            student = StudentUiState(
                status = StudentSessionStatus.SignedIn,
                overview = session.overview,
                avatar = avatar,
            ),
        )
    }

    private suspend fun handleStudentFailure(error: Throwable, keepDashboard: Boolean) {
        val apiError = error as? SofosteApiException
        if (apiError?.status == 401) {
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
    }
}
