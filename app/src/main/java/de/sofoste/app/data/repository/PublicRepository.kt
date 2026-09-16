package de.sofoste.app.data.repository

import de.sofoste.app.data.model.PublicContent
import de.sofoste.app.data.remote.SofosteApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class PublicRepository(
    private val api: SofosteApi,
) {
    suspend fun load(language: String): PublicContent = coroutineScope {
        val home = async { api.home(language) }
        val media = async { api.media(language) }
        val projects = async { api.projects(language) }
        val articles = async { api.journal(language) }

        PublicContent(
            home = home.await(),
            media = media.await(),
            projects = projects.await(),
            articles = articles.await(),
        )
    }
}
