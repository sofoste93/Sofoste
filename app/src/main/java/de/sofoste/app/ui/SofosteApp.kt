package de.sofoste.app.ui

import android.graphics.BitmapFactory
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.sofoste.app.R
import de.sofoste.app.data.model.ArticleItem
import de.sofoste.app.data.model.MediaItem
import de.sofoste.app.data.model.ProjectItem
import de.sofoste.app.data.model.PublicContent
import de.sofoste.app.data.model.StudentOverview
import de.sofoste.app.ui.theme.Aurora
import de.sofoste.app.ui.theme.DeepSpace
import de.sofoste.app.ui.theme.Nebula
import de.sofoste.app.ui.theme.Orbit
import de.sofoste.app.ui.theme.Panel
import de.sofoste.app.ui.theme.Solar
import de.sofoste.app.ui.theme.Starlight
import de.sofoste.app.ui.theme.Void
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

private data class UiCopy(
    val eyebrow: String,
    val subtitle: String,
    val home: String,
    val media: String,
    val projects: String,
    val journal: String,
    val student: String,
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
        student = "Student",
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
        student = "Élève",
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
        student = "Lernen",
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

private data class StudentCopy(
    val title: String,
    val intro: String,
    val login: String,
    val activate: String,
    val email: String,
    val password: String,
    val invitationCode: String,
    val passwordRule: String,
    val submitLogin: String,
    val submitActivation: String,
    val needActivation: String,
    val alreadyActivated: String,
    val forgotPassword: String,
    val restoring: String,
    val secureSession: String,
    val lessons: String,
    val unread: String,
    val payments: String,
    val amountDue: String,
    val refresh: String,
    val logout: String,
    val manageProfile: String,
    val privateNotice: String,
    val loginFailed: String,
    val activationFailed: String,
    val throttled: String,
    val invalidInput: String,
    val unavailable: String,
)

private fun studentCopy(language: AppLanguage): StudentCopy = when (language) {
    AppLanguage.English -> StudentCopy(
        title = "Student space",
        intro = "Find your lesson journey in a private orbit.",
        login = "Sign in",
        activate = "Activate access",
        email = "Email address",
        password = "Password",
        invitationCode = "Invitation code",
        passwordRule = "Use at least 12 characters for a new password.",
        submitLogin = "Enter the classroom",
        submitActivation = "Activate my account",
        needActivation = "I have an invitation code",
        alreadyActivated = "I already have an account",
        forgotPassword = "Forgot password?",
        restoring = "Checking your private orbit…",
        secureSession = "Private session protected on this device",
        lessons = "Shared lessons",
        unread = "New activities",
        payments = "Payment reminders",
        amountDue = "Amount due",
        refresh = "Refresh",
        logout = "Sign out",
        manageProfile = "Manage profile and password",
        privateNotice = "Your private data is never stored in the public catalogue cache.",
        loginFailed = "The email or password is incorrect.",
        activationFailed = "The invitation, email or password is not valid.",
        throttled = "Too many attempts. Please wait 15 minutes.",
        invalidInput = "Please check the information entered.",
        unavailable = "The student signal is temporarily unavailable.",
    )
    AppLanguage.French -> StudentCopy(
        title = "Espace élève",
        intro = "Retrouve ton parcours de cours dans une orbite privée.",
        login = "Connexion",
        activate = "Activer l’accès",
        email = "Adresse email",
        password = "Mot de passe",
        invitationCode = "Code d’invitation",
        passwordRule = "Utilise au moins 12 caractères pour un nouveau mot de passe.",
        submitLogin = "Entrer dans la salle de classe",
        submitActivation = "Activer mon compte",
        needActivation = "J’ai un code d’invitation",
        alreadyActivated = "J’ai déjà un compte",
        forgotPassword = "Mot de passe oublié ?",
        restoring = "Vérification de ton orbite privée…",
        secureSession = "Session privée protégée sur cet appareil",
        lessons = "Cours partagés",
        unread = "Nouvelles activités",
        payments = "Rappels de règlement",
        amountDue = "Montant à régler",
        refresh = "Actualiser",
        logout = "Déconnexion",
        manageProfile = "Gérer le profil et le mot de passe",
        privateNotice = "Tes données privées ne sont jamais stockées dans le cache du catalogue public.",
        loginFailed = "L’adresse email ou le mot de passe est incorrect.",
        activationFailed = "L’invitation, l’adresse email ou le mot de passe n’est pas valide.",
        throttled = "Trop de tentatives. Patiente 15 minutes.",
        invalidInput = "Vérifie les informations saisies.",
        unavailable = "Le signal de l’espace élève est momentanément indisponible.",
    )
    AppLanguage.German -> StudentCopy(
        title = "Lernraum",
        intro = "Dein Unterrichtsweg in einer privaten Umlaufbahn.",
        login = "Anmelden",
        activate = "Zugang aktivieren",
        email = "E-Mail-Adresse",
        password = "Passwort",
        invitationCode = "Einladungscode",
        passwordRule = "Ein neues Passwort benötigt mindestens 12 Zeichen.",
        submitLogin = "Klassenzimmer betreten",
        submitActivation = "Konto aktivieren",
        needActivation = "Ich habe einen Einladungscode",
        alreadyActivated = "Ich habe bereits ein Konto",
        forgotPassword = "Passwort vergessen?",
        restoring = "Private Umlaufbahn wird geprüft…",
        secureSession = "Private Sitzung auf diesem Gerät geschützt",
        lessons = "Geteilte Lektionen",
        unread = "Neue Aktivitäten",
        payments = "Zahlungserinnerungen",
        amountDue = "Offener Betrag",
        refresh = "Aktualisieren",
        logout = "Abmelden",
        manageProfile = "Profil und Passwort verwalten",
        privateNotice = "Private Daten werden nie im öffentlichen Katalog-Cache gespeichert.",
        loginFailed = "E-Mail-Adresse oder Passwort ist falsch.",
        activationFailed = "Einladung, E-Mail-Adresse oder Passwort ist ungültig.",
        throttled = "Zu viele Versuche. Bitte 15 Minuten warten.",
        invalidInput = "Bitte prüfe die eingegebenen Daten.",
        unavailable = "Das Signal des Lernraums ist vorübergehend nicht erreichbar.",
    )
}

@Composable
fun SofosteApp(viewModel: SofosteViewModel = viewModel()) {
    val state = viewModel.state
    val labels = copy(state.language)
    val studentLabels = studentCopy(state.language)

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
                    student = state.student,
                    onSelect = viewModel::select,
                )
            },
        ) { padding ->
            when {
                state.destination == Destination.Student -> StudentOrbit(
                    state = state.student,
                    language = state.language,
                    labels = studentLabels,
                    onLogin = viewModel::loginStudent,
                    onActivate = viewModel::activateStudent,
                    onRefresh = viewModel::refreshStudent,
                    onLogout = viewModel::logoutStudent,
                    onClearError = viewModel::clearStudentError,
                    modifier = Modifier.padding(padding),
                )
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
    student: StudentUiState,
    onSelect: (Destination) -> Unit,
) {
    val names = mapOf(
        Destination.Home to labels.home,
        Destination.Media to labels.media,
        Destination.Projects to labels.projects,
        Destination.Journal to labels.journal,
        Destination.Student to labels.student,
    )
    NavigationBar(containerColor = DeepSpace) {
        Destination.entries.forEach { destination ->
            NavigationBarItem(
                selected = destination == selected,
                onClick = { onSelect(destination) },
                icon = {
                    Text(
                        text = if (destination == Destination.Student && (student.overview?.unread ?: 0) > 0) {
                            (student.overview?.unread ?: 0).coerceAtMost(99).toString()
                        } else if (destination == selected) {
                            "●"
                        } else {
                            "○"
                        },
                        color = if (destination == selected) Aurora else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
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

private enum class StudentEntryMode {
    Login,
    Activation,
}

@Composable
private fun StudentOrbit(
    state: StudentUiState,
    language: AppLanguage,
    labels: StudentCopy,
    onLogin: (String, String) -> Unit,
    onActivate: (String, String, String) -> Unit,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state.status) {
        StudentSessionStatus.Checking -> Column(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(color = Aurora)
            Spacer(Modifier.height(18.dp))
            Text(labels.restoring, color = Starlight)
        }
        StudentSessionStatus.SignedOut -> StudentAuthScreen(
            state = state,
            language = language,
            labels = labels,
            onLogin = onLogin,
            onActivate = onActivate,
            onClearError = onClearError,
            modifier = modifier,
        )
        StudentSessionStatus.SignedIn -> StudentDashboard(
            state = state,
            language = language,
            labels = labels,
            onRefresh = onRefresh,
            onLogout = onLogout,
            modifier = modifier,
        )
    }
}

@Composable
private fun StudentAuthScreen(
    state: StudentUiState,
    language: AppLanguage,
    labels: StudentCopy,
    onLogin: (String, String) -> Unit,
    onActivate: (String, String, String) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier,
) {
    var mode by remember { mutableStateOf(StudentEntryMode.Login) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var invitationCode by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val uriHandler = LocalUriHandler.current
    val passwordBytes = password.toByteArray(Charsets.UTF_8).size
    val canSubmit = email.contains('@') && password.isNotEmpty() && passwordBytes <= 72 &&
        (mode == StudentEntryMode.Login ||
            (password.length >= 12 && invitationCode.matches(Regex("^[a-fA-F0-9]{64}$"))))
    val submit = {
        focusManager.clearFocus()
        if (mode == StudentEntryMode.Login) {
            onLogin(email, password)
        } else {
            onActivate(email, invitationCode.lowercase(), password)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ScreenTitle(labels.title)
            Spacer(Modifier.height(6.dp))
            Text(labels.intro, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Surface(
                color = Panel.copy(alpha = 0.92f),
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = if (mode == StudentEntryMode.Login) labels.login else labels.activate,
                        color = Starlight,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it.take(190) },
                        label = { Text(labels.email) },
                        singleLine = true,
                        enabled = !state.busy,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (mode == StudentEntryMode.Activation) {
                        OutlinedTextField(
                            value = invitationCode,
                            onValueChange = { value ->
                                invitationCode = value.filter { it.isLetterOrDigit() }.take(64)
                            },
                            label = { Text(labels.invitationCode) },
                            singleLine = true,
                            enabled = !state.busy,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it.take(72) },
                        label = { Text(labels.password) },
                        singleLine = true,
                        enabled = !state.busy,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { if (canSubmit && !state.busy) submit() }),
                        supportingText = if (mode == StudentEntryMode.Activation) {
                            { Text(labels.passwordRule) }
                        } else {
                            null
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    state.errorCode?.let { code ->
                        Text(
                            text = studentErrorMessage(code, labels),
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Button(
                        onClick = submit,
                        enabled = canSubmit && !state.busy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .sizeIn(minHeight = 52.dp),
                    ) {
                        if (state.busy) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp),
                            )
                        } else {
                            Text(if (mode == StudentEntryMode.Login) labels.submitLogin else labels.submitActivation)
                        }
                    }
                    TextButton(
                        onClick = {
                            mode = if (mode == StudentEntryMode.Login) {
                                StudentEntryMode.Activation
                            } else {
                                StudentEntryMode.Login
                            }
                            password = ""
                            invitationCode = ""
                            onClearError()
                        },
                        enabled = !state.busy,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (mode == StudentEntryMode.Login) labels.needActivation else labels.alreadyActivated)
                    }
                    if (mode == StudentEntryMode.Login) {
                        TextButton(
                            onClick = {
                                uriHandler.openUri("https://sofoste.de/${language.code}/student?forgot=1")
                            },
                            enabled = !state.busy,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(labels.forgotPassword)
                        }
                    }
                }
            }
        }
        item {
            Text(
                text = labels.privateNotice,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun StudentDashboard(
    state: StudentUiState,
    language: AppLanguage,
    labels: StudentCopy,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier,
) {
    val overview = state.overview ?: return
    val uriHandler = LocalUriHandler.current
    val avatar = remember(state.avatar) {
        state.avatar?.let { bytes ->
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Surface(
                    color = DeepSpace,
                    shape = CircleShape,
                    modifier = Modifier.size(88.dp),
                ) {
                    if (avatar != null) {
                        Image(
                            bitmap = avatar,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.sofoste_s_planet),
                            contentDescription = null,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }
                Column {
                    ScreenTitle(labels.title)
                    Text(
                        labels.secureSession,
                        color = Aurora,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        if (state.busy) item { LinearSignal() }
        state.errorCode?.let { code ->
            item {
                Text(studentErrorMessage(code, labels), color = MaterialTheme.colorScheme.error)
            }
        }
        item { StudentMetric(labels.lessons, overview.lessons.toString(), Aurora) }
        item { StudentMetric(labels.unread, overview.unread.toString(), Nebula) }
        item { StudentMetric(labels.payments, overview.paymentsDue.toString(), Solar) }
        item { StudentMetric(labels.amountDue, formatEuros(overview.amountDueCents, language), Orbit) }
        item {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
        item {
            Button(
                onClick = onRefresh,
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(labels.refresh)
            }
        }
        item {
            TextButton(
                onClick = { uriHandler.openUri("https://sofoste.de/${language.code}/student") },
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(labels.manageProfile)
            }
        }
        item {
            TextButton(
                onClick = onLogout,
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(labels.logout)
            }
        }
        item {
            Text(
                labels.privateNotice,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun StudentMetric(label: String, value: String, accent: Color) {
    Surface(
        color = Panel.copy(alpha = 0.88f),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, color = accent, fontSize = 24.sp, fontWeight = FontWeight.Black)
        }
    }
}

private fun formatEuros(amountCents: Int, language: AppLanguage): String {
    val locale = when (language) {
        AppLanguage.English -> Locale.UK
        AppLanguage.French -> Locale.FRANCE
        AppLanguage.German -> Locale.GERMANY
    }
    return NumberFormat.getCurrencyInstance(locale).apply {
        currency = Currency.getInstance("EUR")
    }.format(amountCents / 100.0)
}

private fun studentErrorMessage(code: String, labels: StudentCopy): String = when (code) {
    "login_failed" -> labels.loginFailed
    "activation_failed" -> labels.activationFailed
    "too_many_attempts" -> labels.throttled
    "invalid_input" -> labels.invalidInput
    else -> labels.unavailable
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
        Destination.Student -> Unit
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
