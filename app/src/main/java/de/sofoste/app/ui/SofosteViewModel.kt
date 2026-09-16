package de.sofoste.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.sofoste.app.data.model.PublicContent
import de.sofoste.app.data.remote.SofosteApi
import de.sofoste.app.data.repository.PublicRepository
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
}

data class SofosteUiState(
    val language: AppLanguage = AppLanguage.deviceDefault(),
    val destination: Destination = Destination.Home,
    val content: PublicContent? = null,
    val loading: Boolean = true,
    val failed: Boolean = false,
)

class SofosteViewModel : ViewModel() {
    private val api = SofosteApi()
    private val repository = PublicRepository(api)
    private var loadJob: Job? = null

    var state by mutableStateOf(SofosteUiState())
        private set

    init {
        refresh()
    }

    fun select(destination: Destination) {
        state = state.copy(destination = destination)
    }

    fun selectLanguage(language: AppLanguage) {
        if (language == state.language) return
        state = state.copy(language = language)
        refresh()
    }

    fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            state = state.copy(loading = true, failed = false)
            runCatching { repository.load(state.language.code) }
                .onSuccess { state = state.copy(content = it, loading = false) }
                .onFailure { state = state.copy(loading = false, failed = true) }
        }
    }

    override fun onCleared() {
        api.close()
    }
}
