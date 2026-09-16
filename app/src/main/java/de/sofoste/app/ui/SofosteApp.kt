package de.sofoste.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.sofoste.app.R
import de.sofoste.app.data.model.ArticleItem
import de.sofoste.app.data.model.MediaItem
import de.sofoste.app.data.model.ProjectItem
import de.sofoste.app.data.model.PublicContent
import de.sofoste.app.ui.theme.Aurora
import de.sofoste.app.ui.theme.DeepSpace
import de.sofoste.app.ui.theme.Nebula
import de.sofoste.app.ui.theme.Orbit
import de.sofoste.app.ui.theme.Panel
import de.sofoste.app.ui.theme.Solar
import de.sofoste.app.ui.theme.Starlight
import de.sofoste.app.ui.theme.Void

private data class UiCopy(
    val eyebrow: String,
    val subtitle: String,
    val home: String,
    val media: String,
    val projects: String,
    val journal: String,
    val featuredMedia: String,
    val featuredProject: String,
    val latestArticle: String,
    val explore: String,
    val retry: String,
    val unavailable: String,
    val empty: String,
    val minutes: String,
)

private fun copy(language: AppLanguage): UiCopy = when (language) {
    AppLanguage.English -> UiCopy(
        eyebrow = "SOFOSTE MUSIC · MOBILE ORBIT",
        subtitle = "Music, stories and projects in motion.",
        home = "Home",
        media = "Media",
        projects = "Projects",
        journal = "Journal",
        featuredMedia = "Featured signal",
        featuredProject = "Project in orbit",
        latestArticle = "Latest transmission",
        explore = "Explore",
        retry = "Try again",
        unavailable = "The Sofoste signal is temporarily unavailable.",
        empty = "No published signal in this orbit yet.",
        minutes = "min read",
    )
    AppLanguage.French -> UiCopy(
        eyebrow = "SOFOSTE MUSIC · ORBITE MOBILE",
        subtitle = "Musique, récits et projets en mouvement.",
        home = "Accueil",
        media = "Médias",
        projects = "Projets",
        journal = "Journal",
        featuredMedia = "Signal à la une",
        featuredProject = "Projet en orbite",
        latestArticle = "Dernière transmission",
        explore = "Explorer",
        retry = "Réessayer",
        unavailable = "Le signal Sofoste est momentanément indisponible.",
        empty = "Aucun signal publié dans cette orbite pour le moment.",
        minutes = "min de lecture",
    )
    AppLanguage.German -> UiCopy(
        eyebrow = "SOFOSTE MUSIC · MOBILE UMLAUFBAHN",
        subtitle = "Musik, Geschichten und Projekte in Bewegung.",
        home = "Start",
        media = "Medien",
        projects = "Projekte",
        journal = "Journal",
        featuredMedia = "Signal im Fokus",
        featuredProject = "Projekt im Orbit",
        latestArticle = "Neueste Übertragung",
        explore = "Entdecken",
        retry = "Erneut versuchen",
        unavailable = "Das Sofoste-Signal ist vorübergehend nicht erreichbar.",
        empty = "In diesem Orbit gibt es noch kein veröffentlichtes Signal.",
        minutes = "Min. Lesezeit",
    )
}

@Composable
fun SofosteApp(viewModel: SofosteViewModel = viewModel()) {
    val state = viewModel.state
    val labels = copy(state.language)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Orbit.copy(alpha = 0.22f), Void, DeepSpace),
                    radius = 1400f,
                ),
            ),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                MissionHeader(
                    language = state.language,
                    labels = labels,
                    onLanguage = viewModel::selectLanguage,
                )
            },
            bottomBar = {
                MissionNavigation(
                    selected = state.destination,
                    labels = labels,
                    onSelect = viewModel::select,
                )
            },
        ) { padding ->
            when {
                state.loading && state.content == null -> LoadingSignal(Modifier.padding(padding))
                state.failed && state.content == null -> ErrorSignal(
                    labels = labels,
                    onRetry = viewModel::refresh,
                    modifier = Modifier.padding(padding),
                )
                else -> PublicOrbit(
                    destination = state.destination,
                    content = state.content,
                    labels = labels,
                    refreshing = state.loading,
                    onRetry = viewModel::refresh,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}

@Composable
private fun MissionHeader(
    language: AppLanguage,
    labels: UiCopy,
    onLanguage: (AppLanguage) -> Unit,
) {
    Surface(color = Void.copy(alpha = 0.9f)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.sofoste_s_planet),
                    contentDescription = null,
                    modifier = Modifier.size(62.dp),
                )
                Column {
                    Text(
                        text = labels.eyebrow,
                        color = Aurora,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.7.sp,
                    )
                    Text(
                        text = "Sofoste",
                        color = Starlight,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = labels.subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppLanguage.entries.forEach { option ->
                    FilterChip(
                        selected = option == language,
                        onClick = { onLanguage(option) },
                        label = { Text(option.label) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MissionNavigation(
    selected: Destination,
    labels: UiCopy,
    onSelect: (Destination) -> Unit,
) {
    val names = mapOf(
        Destination.Home to labels.home,
        Destination.Media to labels.media,
        Destination.Projects to labels.projects,
        Destination.Journal to labels.journal,
    )
    NavigationBar(containerColor = DeepSpace) {
        Destination.entries.forEach { destination ->
            NavigationBarItem(
                selected = destination == selected,
                onClick = { onSelect(destination) },
                icon = {
                    Text(
                        text = if (destination == selected) "●" else "○",
                        color = if (destination == selected) Aurora else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                label = {
                    Text(
                        text = names.getValue(destination),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}

@Composable
private fun PublicOrbit(
    destination: Destination,
    content: PublicContent?,
    labels: UiCopy,
    refreshing: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (content == null) {
        ErrorSignal(labels, onRetry, modifier)
        return
    }

    when (destination) {
        Destination.Home -> HomeScreen(content, labels, refreshing, modifier)
        Destination.Media -> MediaScreen(content.media, labels, refreshing, modifier)
        Destination.Projects -> ProjectScreen(content.projects, labels, refreshing, modifier)
        Destination.Journal -> JournalScreen(content.articles, labels, refreshing, modifier)
    }
}

@Composable
private fun HomeScreen(
    content: PublicContent,
    labels: UiCopy,
    refreshing: Boolean,
    modifier: Modifier,
) {
    val uriHandler = LocalUriHandler.current
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (refreshing) item { LinearSignal() }
        content.home.featuredMedia?.let { item { SectionLabel(labels.featuredMedia, Aurora) } }
        content.home.featuredMedia?.let { item { MediaCard(it) { uriHandler.openUri(it.webUrl) } } }
        content.home.featuredProject?.let { item { SectionLabel(labels.featuredProject, Orbit) } }
        content.home.featuredProject?.let { item { ProjectCard(it) { uriHandler.openUri(it.webUrl) } } }
        content.home.latestArticle?.let { item { SectionLabel(labels.latestArticle, Nebula) } }
        content.home.latestArticle?.let { item { ArticleCard(it, labels) { uriHandler.openUri(it.webUrl) } } }
        if (content.home.featuredMedia == null && content.home.featuredProject == null && content.home.latestArticle == null) {
            item { EmptySignal(labels.empty) }
        }
    }
}

@Composable
private fun MediaScreen(items: List<MediaItem>, labels: UiCopy, refreshing: Boolean, modifier: Modifier) {
    val uriHandler = LocalUriHandler.current
    OrbitList(modifier, labels.media, refreshing, items, labels.empty) { item ->
        MediaCard(item) { uriHandler.openUri(item.webUrl) }
    }
}

@Composable
private fun ProjectScreen(items: List<ProjectItem>, labels: UiCopy, refreshing: Boolean, modifier: Modifier) {
    val uriHandler = LocalUriHandler.current
    OrbitList(modifier, labels.projects, refreshing, items, labels.empty) { item ->
        ProjectCard(item) { uriHandler.openUri(item.webUrl) }
    }
}

@Composable
private fun JournalScreen(items: List<ArticleItem>, labels: UiCopy, refreshing: Boolean, modifier: Modifier) {
    val uriHandler = LocalUriHandler.current
    OrbitList(modifier, labels.journal, refreshing, items, labels.empty) { item ->
        ArticleCard(item, labels) { uriHandler.openUri(item.webUrl) }
    }
}

@Composable
private fun <T> OrbitList(
    modifier: Modifier,
    title: String,
    refreshing: Boolean,
    items: List<T>,
    empty: String,
    card: @Composable (T) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { ScreenTitle(title) }
        if (refreshing) item { LinearSignal() }
        if (items.isEmpty()) item { EmptySignal(empty) }
        items(items) { item -> card(item) }
    }
}

@Composable
private fun MediaCard(item: MediaItem, onOpen: () -> Unit) {
    SignalCard(
        tag = item.kind.uppercase(),
        title = item.title,
        body = item.description,
        meta = listOfNotNull(item.releaseDate, item.sources.firstOrNull()?.provider).joinToString(" · "),
        accent = Aurora,
        onOpen = onOpen,
    )
}

@Composable
private fun ProjectCard(item: ProjectItem, onOpen: () -> Unit) {
    SignalCard(
        tag = item.kind.uppercase(),
        title = item.title,
        body = item.excerpt,
        meta = item.startDate.orEmpty(),
        accent = Orbit,
        onOpen = onOpen,
    )
}

@Composable
private fun ArticleCard(item: ArticleItem, labels: UiCopy, onOpen: () -> Unit) {
    SignalCard(
        tag = item.topic.uppercase(),
        title = item.title,
        body = item.excerpt,
        meta = "${item.readingMinutes} ${labels.minutes}",
        accent = Nebula,
        onOpen = onOpen,
    )
}

@Composable
private fun SignalCard(
    tag: String,
    title: String,
    body: String?,
    meta: String,
    accent: Color,
    onOpen: () -> Unit,
) {
    Surface(
        color = Panel.copy(alpha = 0.86f),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onOpen),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(tag, color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
            Spacer(Modifier.height(8.dp))
            Text(title, color = Starlight, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            if (!body.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 4, overflow = TextOverflow.Ellipsis)
            }
            if (meta.isNotBlank()) {
                Spacer(Modifier.height(14.dp))
                Text(meta, color = Solar, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, color: Color) {
    Text(text.uppercase(), color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
}

@Composable
private fun ScreenTitle(text: String) {
    Text(text, color = Starlight, fontSize = 32.sp, fontWeight = FontWeight.Black)
}

@Composable
private fun LoadingSignal(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Aurora)
    }
}

@Composable
private fun ErrorSignal(labels: UiCopy, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(labels.unavailable, color = Starlight, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(18.dp))
        Button(onClick = onRetry) { Text(labels.retry) }
    }
}

@Composable
private fun EmptySignal(text: String) {
    Surface(color = Panel, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(20.dp))
    }
}

@Composable
private fun LinearSignal() {
    Surface(color = Aurora.copy(alpha = 0.16f), shape = RoundedCornerShape(100.dp), modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(3.dp))
    }
}
