package de.sofoste.app.ui

import android.graphics.BitmapFactory
import android.content.Context
import android.net.Uri
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalContext
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
import de.sofoste.app.data.model.StudentActivityItem
import de.sofoste.app.data.model.StudentAgendaItem
import de.sofoste.app.data.model.StudentLessonItem
import de.sofoste.app.data.model.StudentPaymentItem
import de.sofoste.app.ui.theme.Aurora
import de.sofoste.app.ui.theme.DeepSpace
import de.sofoste.app.ui.theme.Nebula
import de.sofoste.app.ui.theme.Orbit
import de.sofoste.app.ui.theme.Panel
import de.sofoste.app.ui.theme.Solar
import de.sofoste.app.ui.theme.Starlight
import de.sofoste.app.ui.theme.Void
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.io.ByteArrayOutputStream
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
    val admin: String,
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
        admin = "Admin",
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
        admin = "Admin",
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
        admin = "Admin",
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
    val upcoming: String,
    val overviewTab: String,
    val agendaTab: String,
    val lessonsTab: String,
    val activityTab: String,
    val paymentsTab: String,
    val profileTab: String,
    val emptyAgenda: String,
    val emptyLessons: String,
    val emptyActivity: String,
    val emptyPayments: String,
    val planned: String,
    val completed: String,
    val cancelled: String,
    val flexibleTime: String,
    val duration: String,
    val summary: String,
    val progress: String,
    val practice: String,
    val lessonPublished: String,
    val unreadLabel: String,
    val readLabel: String,
    val markRead: String,
    val reschedule: String,
    val cancelAppointment: String,
    val saveAppointment: String,
    val cancelConfirm: String,
    val appointmentClosed: String,
    val appointmentUnavailable: String,
    val personalTerms: String,
    val perLesson: String,
    val everyTwoWeeks: String,
    val monthly: String,
    val paymentMethod: String,
    val cash: String,
    val bankTransfer: String,
    val otherMethod: String,
    val dueOn: String,
    val due: String,
    val paid: String,
    val waived: String,
    val payPal: String,
    val displayName: String,
    val profileIntro: String,
    val saveProfile: String,
    val choosePhoto: String,
    val removePhoto: String,
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String,
    val changePassword: String,
    val passwordMismatch: String,
    val currentPasswordFailed: String,
    val avatarInvalid: String,
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
        upcoming = "Upcoming sessions",
        overviewTab = "Overview",
        agendaTab = "Agenda",
        lessonsTab = "Lessons",
        activityTab = "Activity",
        paymentsTab = "Payments",
        profileTab = "Profile",
        emptyAgenda = "No session is scheduled in this orbit.",
        emptyLessons = "No lesson note has been shared yet.",
        emptyActivity = "No new classroom activity.",
        emptyPayments = "No payment reminder.",
        planned = "Planned",
        completed = "Completed",
        cancelled = "Cancelled",
        flexibleTime = "Time to be agreed",
        duration = "min",
        summary = "Session notes",
        progress = "Progress",
        practice = "Practice path",
        lessonPublished = "A lesson note was shared",
        unreadLabel = "New",
        readLabel = "Read",
        markRead = "Mark as read",
        reschedule = "Reschedule",
        cancelAppointment = "Cancel appointment",
        saveAppointment = "Save new time",
        cancelConfirm = "Cancel this appointment?",
        appointmentClosed = "This appointment can no longer be changed.",
        appointmentUnavailable = "This time is no longer available. Choose another one.",
        personalTerms = "Personal terms",
        perLesson = "Per lesson",
        everyTwoWeeks = "Every two weeks",
        monthly = "Monthly",
        paymentMethod = "Payment method",
        cash = "Cash",
        bankTransfer = "Bank transfer",
        otherMethod = "Other",
        dueOn = "Due",
        due = "Due",
        paid = "Paid",
        waived = "Waived",
        payPal = "Open PayPal",
        displayName = "Display name",
        profileIntro = "Manage your identity and access without leaving the app.",
        saveProfile = "Save profile",
        choosePhoto = "Choose a profile picture",
        removePhoto = "Remove picture",
        currentPassword = "Current password",
        newPassword = "New password",
        confirmPassword = "Confirm new password",
        changePassword = "Change password",
        passwordMismatch = "The new passwords do not match.",
        currentPasswordFailed = "The current password is incorrect.",
        avatarInvalid = "Choose a valid JPG, PNG or WebP image up to 5 MB.",
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
        upcoming = "Séances à venir",
        overviewTab = "Vue d’ensemble",
        agendaTab = "Agenda",
        lessonsTab = "Cours",
        activityTab = "Activités",
        paymentsTab = "Règlements",
        profileTab = "Profil",
        emptyAgenda = "Aucune séance n’est programmée dans cette orbite.",
        emptyLessons = "Aucune note de cours n’a encore été partagée.",
        emptyActivity = "Aucune nouvelle activité dans la salle de classe.",
        emptyPayments = "Aucun rappel de règlement.",
        planned = "Planifiée",
        completed = "Terminée",
        cancelled = "Annulée",
        flexibleTime = "Heure à convenir",
        duration = "min",
        summary = "Notes de séance",
        progress = "Progression",
        practice = "Piste de travail",
        lessonPublished = "Une note de cours a été partagée",
        unreadLabel = "Nouveau",
        readLabel = "Lu",
        markRead = "Marquer comme lu",
        reschedule = "Modifier le rendez-vous",
        cancelAppointment = "Annuler le rendez-vous",
        saveAppointment = "Enregistrer le nouveau créneau",
        cancelConfirm = "Annuler ce rendez-vous ?",
        appointmentClosed = "Ce rendez-vous ne peut plus être modifié.",
        appointmentUnavailable = "Ce créneau n’est plus disponible. Choisis-en un autre.",
        personalTerms = "Conditions personnelles",
        perLesson = "Par séance",
        everyTwoWeeks = "Toutes les deux semaines",
        monthly = "Par mois",
        paymentMethod = "Mode de règlement",
        cash = "Espèces",
        bankTransfer = "Virement bancaire",
        otherMethod = "Autre",
        dueOn = "Échéance",
        due = "À régler",
        paid = "Réglé",
        waived = "Dispensé",
        payPal = "Ouvrir PayPal",
        displayName = "Nom affiché",
        profileIntro = "Gère ton identité et ton accès sans quitter l’application.",
        saveProfile = "Enregistrer le profil",
        choosePhoto = "Choisir une photo de profil",
        removePhoto = "Supprimer la photo",
        currentPassword = "Mot de passe actuel",
        newPassword = "Nouveau mot de passe",
        confirmPassword = "Confirmer le nouveau mot de passe",
        changePassword = "Modifier le mot de passe",
        passwordMismatch = "Les nouveaux mots de passe ne correspondent pas.",
        currentPasswordFailed = "Le mot de passe actuel est incorrect.",
        avatarInvalid = "Choisis une image JPG, PNG ou WebP valide de 5 Mo maximum.",
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
        upcoming = "Kommende Termine",
        overviewTab = "Übersicht",
        agendaTab = "Agenda",
        lessonsTab = "Lektionen",
        activityTab = "Aktivitäten",
        paymentsTab = "Zahlungen",
        profileTab = "Profil",
        emptyAgenda = "In dieser Umlaufbahn ist kein Termin geplant.",
        emptyLessons = "Noch wurden keine Unterrichtsnotizen geteilt.",
        emptyActivity = "Keine neue Aktivität im Lernraum.",
        emptyPayments = "Keine Zahlungserinnerung.",
        planned = "Geplant",
        completed = "Abgeschlossen",
        cancelled = "Abgesagt",
        flexibleTime = "Uhrzeit nach Absprache",
        duration = "Min.",
        summary = "Unterrichtsnotizen",
        progress = "Fortschritt",
        practice = "Übungsweg",
        lessonPublished = "Eine Unterrichtsnotiz wurde geteilt",
        unreadLabel = "Neu",
        readLabel = "Gelesen",
        markRead = "Als gelesen markieren",
        reschedule = "Termin verschieben",
        cancelAppointment = "Termin absagen",
        saveAppointment = "Neuen Termin speichern",
        cancelConfirm = "Diesen Termin absagen?",
        appointmentClosed = "Dieser Termin kann nicht mehr geändert werden.",
        appointmentUnavailable = "Dieser Termin ist nicht mehr verfügbar. Wähle einen anderen.",
        personalTerms = "Persönliche Konditionen",
        perLesson = "Pro Unterricht",
        everyTwoWeeks = "Alle zwei Wochen",
        monthly = "Monatlich",
        paymentMethod = "Zahlungsart",
        cash = "Bargeld",
        bankTransfer = "Überweisung",
        otherMethod = "Andere",
        dueOn = "Fällig",
        due = "Offen",
        paid = "Bezahlt",
        waived = "Erlassen",
        payPal = "PayPal öffnen",
        displayName = "Anzeigename",
        profileIntro = "Verwalte deine Identität und deinen Zugang direkt in der App.",
        saveProfile = "Profil speichern",
        choosePhoto = "Profilbild auswählen",
        removePhoto = "Bild entfernen",
        currentPassword = "Aktuelles Passwort",
        newPassword = "Neues Passwort",
        confirmPassword = "Neues Passwort bestätigen",
        changePassword = "Passwort ändern",
        passwordMismatch = "Die neuen Passwörter stimmen nicht überein.",
        currentPasswordFailed = "Das aktuelle Passwort ist falsch.",
        avatarInvalid = "Wähle ein gültiges JPG-, PNG- oder WebP-Bild bis 5 MB.",
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

private data class AdminCopy(
    val title: String,
    val intro: String,
    val login: String,
    val username: String,
    val password: String,
    val enter: String,
    val restoring: String,
    val secureSession: String,
    val overviewTab: String,
    val profileTab: String,
    val messages: String,
    val reservations: String,
    val comments: String,
    val students: String,
    val reviews: String,
    val nextLesson: String,
    val noLesson: String,
    val readOnly: String,
    val refresh: String,
    val logout: String,
    val displayName: String,
    val email: String,
    val saveProfile: String,
    val choosePhoto: String,
    val removePhoto: String,
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String,
    val passwordRule: String,
    val changePassword: String,
    val passwordMismatch: String,
    val loginFailed: String,
    val currentPasswordFailed: String,
    val avatarInvalid: String,
    val throttled: String,
    val invalidInput: String,
    val unavailable: String,
)

private fun adminCopy(language: AppLanguage): AdminCopy = when (language) {
    AppLanguage.English -> AdminCopy(
        title = "Mission Control", intro = "Private mobile bridge for authorized crew.", login = "Crew sign in",
        username = "Crew identifier", password = "Password", enter = "Enter Mission Control",
        restoring = "Checking the command channel…", secureSession = "Four-hour encrypted crew session",
        overviewTab = "Overview", profileTab = "Profile", messages = "New messages",
        reservations = "Pending lessons", comments = "Comments to review", students = "Active students",
        reviews = "Reviews due", nextLesson = "Next trajectory", noLesson = "No upcoming trajectory",
        readOnly = "Operational telemetry is read-only in this orbit.", refresh = "Refresh", logout = "Sign out",
        displayName = "Display name", email = "Email address", saveProfile = "Save crew profile",
        choosePhoto = "Choose profile picture", removePhoto = "Remove picture",
        currentPassword = "Current password", newPassword = "New password",
        confirmPassword = "Confirm new password", passwordRule = "Use at least 12 characters.",
        changePassword = "Change password", passwordMismatch = "The new passwords do not match.",
        loginFailed = "The crew identifier or password is incorrect.",
        currentPasswordFailed = "The current password is incorrect.",
        avatarInvalid = "Choose a valid JPG, PNG or WebP image up to 5 MB.",
        throttled = "Too many attempts. Wait 15 minutes.", invalidInput = "Check the information entered.",
        unavailable = "Mission Control is temporarily unavailable.",
    )
    AppLanguage.French -> AdminCopy(
        title = "Mission Control", intro = "Passerelle mobile privée pour l’équipage autorisé.", login = "Connexion équipage",
        username = "Identifiant équipage", password = "Mot de passe", enter = "Entrer dans Mission Control",
        restoring = "Vérification du canal de commandement…", secureSession = "Session équipage chiffrée de quatre heures",
        overviewTab = "Vue d’ensemble", profileTab = "Profil", messages = "Nouveaux messages",
        reservations = "Cours en attente", comments = "Commentaires à examiner", students = "Élèves actifs",
        reviews = "Bilans à préparer", nextLesson = "Prochaine trajectoire", noLesson = "Aucune trajectoire à venir",
        readOnly = "La télémétrie opérationnelle est en lecture seule dans cette orbite.", refresh = "Actualiser", logout = "Déconnexion",
        displayName = "Nom affiché", email = "Adresse email", saveProfile = "Enregistrer le profil équipage",
        choosePhoto = "Choisir une photo de profil", removePhoto = "Supprimer la photo",
        currentPassword = "Mot de passe actuel", newPassword = "Nouveau mot de passe",
        confirmPassword = "Confirmer le nouveau mot de passe", passwordRule = "Utilise au moins 12 caractères.",
        changePassword = "Modifier le mot de passe", passwordMismatch = "Les nouveaux mots de passe ne correspondent pas.",
        loginFailed = "L’identifiant équipage ou le mot de passe est incorrect.",
        currentPasswordFailed = "Le mot de passe actuel est incorrect.",
        avatarInvalid = "Choisis une image JPG, PNG ou WebP valide de 5 Mo maximum.",
        throttled = "Trop de tentatives. Patiente 15 minutes.", invalidInput = "Vérifie les informations saisies.",
        unavailable = "Mission Control est momentanément indisponible.",
    )
    AppLanguage.German -> AdminCopy(
        title = "Mission Control", intro = "Private mobile Brücke für autorisierte Crew.", login = "Crew-Anmeldung",
        username = "Crew-Kennung", password = "Passwort", enter = "Mission Control betreten",
        restoring = "Kommandokanal wird geprüft…", secureSession = "Vierstündige verschlüsselte Crew-Sitzung",
        overviewTab = "Übersicht", profileTab = "Profil", messages = "Neue Nachrichten",
        reservations = "Offene Unterrichtsanfragen", comments = "Kommentare zur Prüfung", students = "Aktive Lernende",
        reviews = "Fällige Rückblicke", nextLesson = "Nächste Flugbahn", noLesson = "Keine kommende Flugbahn",
        readOnly = "Die operative Telemetrie ist in diesem Orbit schreibgeschützt.", refresh = "Aktualisieren", logout = "Abmelden",
        displayName = "Anzeigename", email = "E-Mail-Adresse", saveProfile = "Crew-Profil speichern",
        choosePhoto = "Profilbild auswählen", removePhoto = "Bild entfernen",
        currentPassword = "Aktuelles Passwort", newPassword = "Neues Passwort",
        confirmPassword = "Neues Passwort bestätigen", passwordRule = "Verwende mindestens 12 Zeichen.",
        changePassword = "Passwort ändern", passwordMismatch = "Die neuen Passwörter stimmen nicht überein.",
        loginFailed = "Crew-Kennung oder Passwort ist falsch.", currentPasswordFailed = "Das aktuelle Passwort ist falsch.",
        avatarInvalid = "Wähle ein gültiges JPG-, PNG- oder WebP-Bild bis 5 MB.",
        throttled = "Zu viele Versuche. Bitte 15 Minuten warten.", invalidInput = "Bitte prüfe die eingegebenen Daten.",
        unavailable = "Mission Control ist vorübergehend nicht erreichbar.",
    )
}

@Composable
fun SofosteApp(viewModel: SofosteViewModel = viewModel()) {
    val state = viewModel.state
    val labels = copy(state.language)
    val studentLabels = studentCopy(state.language)
    val adminLabels = adminCopy(state.language)

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
                    onAdmin = { viewModel.select(Destination.Admin) },
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
                state.destination == Destination.Admin -> AdminOrbit(
                    state = state.admin,
                    language = state.language,
                    labels = adminLabels,
                    onLogin = viewModel::loginAdmin,
                    onRefresh = viewModel::refreshAdmin,
                    onSaveProfile = viewModel::saveAdminProfile,
                    onChangePassword = viewModel::changeAdminPassword,
                    onUploadAvatar = viewModel::uploadAdminAvatar,
                    onRemoveAvatar = viewModel::removeAdminAvatar,
                    onLogout = viewModel::logoutAdmin,
                    modifier = Modifier.padding(padding),
                )
                state.destination == Destination.Student -> StudentOrbit(
                    state = state.student,
                    language = state.language,
                    labels = studentLabels,
                    onLogin = viewModel::loginStudent,
                    onActivate = viewModel::activateStudent,
                    onRefresh = viewModel::refreshStudent,
                    onLogout = viewModel::logoutStudent,
                    onMarkActivityRead = viewModel::markStudentActivityRead,
                    onRescheduleAppointment = viewModel::rescheduleStudentAppointment,
                    onCancelAppointment = viewModel::cancelStudentAppointment,
                    onSaveProfile = viewModel::saveStudentProfile,
                    onChangePassword = viewModel::changeStudentPassword,
                    onUploadAvatar = viewModel::uploadStudentAvatar,
                    onRemoveAvatar = viewModel::removeStudentAvatar,
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
    onAdmin: () -> Unit,
) {
    Surface(color = Void.copy(alpha = 0.9f)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onAdmin) {
                    Icon(
                        painter = painterResource(R.drawable.ic_crew),
                        contentDescription = labels.admin,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
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
        listOf(Destination.Home, Destination.Media, Destination.Projects, Destination.Journal, Destination.Student)
            .forEach { destination ->
            NavigationBarItem(
                selected = destination == selected,
                onClick = { onSelect(destination) },
                icon = {
                    BadgedBox(badge = {
                        if (destination == Destination.Student && (student.overview?.unread ?: 0) > 0) {
                            Badge { Text((student.overview?.unread ?: 0).coerceAtMost(99).toString()) }
                        }
                    }) {
                        Icon(
                            painter = painterResource(destinationIcon(destination)),
                            contentDescription = null,
                            tint = if (destination == selected) Aurora else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
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

private fun destinationIcon(destination: Destination): Int = when (destination) {
    Destination.Home -> R.drawable.ic_home
    Destination.Media -> R.drawable.ic_media
    Destination.Projects -> R.drawable.ic_projects
    Destination.Journal -> R.drawable.ic_journal
    Destination.Student -> R.drawable.ic_student
    Destination.Admin -> R.drawable.ic_crew
}

private enum class AdminSection { Overview, Profile }

@Composable
private fun AdminOrbit(
    state: AdminUiState,
    language: AppLanguage,
    labels: AdminCopy,
    onLogin: (String, String) -> Unit,
    onRefresh: () -> Unit,
    onSaveProfile: (String, String, String) -> Unit,
    onChangePassword: (String, String) -> Unit,
    onUploadAvatar: (ByteArray, String) -> Unit,
    onRemoveAvatar: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier,
) {
    when (state.status) {
        StudentSessionStatus.Checking -> Column(
            modifier = modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(color = Solar)
            Spacer(Modifier.height(18.dp))
            Text(labels.restoring, color = Starlight)
        }
        StudentSessionStatus.SignedOut -> AdminLoginScreen(state, labels, onLogin, modifier)
        StudentSessionStatus.SignedIn -> AdminDashboardScreen(
            state, language, labels, onRefresh, onSaveProfile, onChangePassword,
            onUploadAvatar, onRemoveAvatar, onLogout, modifier,
        )
    }
}

@Composable
private fun AdminLoginScreen(
    state: AdminUiState,
    labels: AdminCopy,
    onLogin: (String, String) -> Unit,
    modifier: Modifier,
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ScreenTitle(labels.title)
            Text(labels.intro, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Surface(color = Panel.copy(alpha = 0.94f), shape = RoundedCornerShape(26.dp)) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(labels.login, color = Starlight, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it.take(100) },
                        label = { Text(labels.username) },
                        singleLine = true,
                        enabled = !state.busy,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it.take(72) },
                        label = { Text(labels.password) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        enabled = !state.busy,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (username.isNotBlank() && password.isNotEmpty()) {
                                focusManager.clearFocus()
                                onLogin(username, password)
                            }
                        }),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    state.errorCode?.let {
                        Text(adminErrorMessage(it, labels), color = MaterialTheme.colorScheme.error)
                    }
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onLogin(username, password)
                        },
                        enabled = username.isNotBlank() && password.isNotEmpty() && !state.busy,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (state.busy) CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp),
                        ) else Text(labels.enter)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminDashboardScreen(
    state: AdminUiState,
    language: AppLanguage,
    labels: AdminCopy,
    onRefresh: () -> Unit,
    onSaveProfile: (String, String, String) -> Unit,
    onChangePassword: (String, String) -> Unit,
    onUploadAvatar: (ByteArray, String) -> Unit,
    onRemoveAvatar: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier,
) {
    val profile = state.profile ?: return
    val dashboard = state.dashboard ?: return
    var section by remember { mutableStateOf(AdminSection.Overview) }
    val avatar = remember(state.avatar) {
        state.avatar?.let { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() }
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(color = DeepSpace, shape = CircleShape, modifier = Modifier.size(88.dp)) {
                    if (avatar != null) Image(
                        bitmap = avatar,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                    ) else Image(
                        painter = painterResource(R.drawable.sofoste_s_planet),
                        contentDescription = null,
                        modifier = Modifier.padding(12.dp),
                    )
                }
                Column {
                    ScreenTitle(profile.displayName)
                    Text(labels.secureSession, color = Solar, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        if (state.busy) item { LinearSignal() }
        state.errorCode?.let { item { Text(adminErrorMessage(it, labels), color = MaterialTheme.colorScheme.error) } }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminSection.entries.forEach { option ->
                    FilterChip(
                        selected = section == option,
                        onClick = { section = option },
                        label = { Text(if (option == AdminSection.Overview) labels.overviewTab else labels.profileTab) },
                    )
                }
            }
        }
        when (section) {
            AdminSection.Overview -> {
                item { StudentMetric(labels.messages, dashboard.newMessages.toString(), Nebula) }
                item { StudentMetric(labels.reservations, dashboard.pendingReservations.toString(), Orbit) }
                item { StudentMetric(labels.comments, dashboard.pendingComments.toString(), Solar) }
                item { StudentMetric(labels.students, dashboard.activeStudents.toString(), Aurora) }
                item { StudentMetric(labels.reviews, dashboard.reviewsDue.toString(), Solar) }
                item {
                    val lesson = dashboard.nextLesson
                    StudentInfoCard(
                        title = labels.nextLesson,
                        body = if (lesson == null) labels.noLesson else
                            "${localizedDate(lesson.date, language)} · ${lesson.time}\n${lesson.name} · ${lesson.service}",
                        accent = Orbit,
                    )
                }
                item { Text(labels.readOnly, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp) }
            }
            AdminSection.Profile -> item {
                AdminProfilePanel(
                    state, labels, onSaveProfile, onChangePassword, onUploadAvatar, onRemoveAvatar,
                )
            }
        }
        item { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }
        item {
            Button(onClick = onRefresh, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
                Text(labels.refresh)
            }
        }
        item {
            TextButton(onClick = onLogout, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
                Text(labels.logout)
            }
        }
    }
}

@Composable
private fun AdminProfilePanel(
    state: AdminUiState,
    labels: AdminCopy,
    onSaveProfile: (String, String, String) -> Unit,
    onChangePassword: (String, String) -> Unit,
    onUploadAvatar: (ByteArray, String) -> Unit,
    onRemoveAvatar: () -> Unit,
) {
    val profile = state.profile ?: return
    var displayName by remember(profile.displayName) { mutableStateOf(profile.displayName) }
    var email by remember(profile.email) { mutableStateOf(profile.email.orEmpty()) }
    var preferredLanguage by remember(profile.preferredLanguage) { mutableStateOf(profile.preferredLanguage) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val mime = normalizedImageMime(context.contentResolver.getType(uri).orEmpty())
            onUploadAvatar(readAvatarBytes(context, uri) ?: ByteArray(0), mime)
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("@${profile.username}", color = Solar, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = displayName, onValueChange = { displayName = it.take(120) },
            label = { Text(labels.displayName) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = email, onValueChange = { email = it.take(190) },
            label = { Text(labels.email) }, enabled = !state.busy,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppLanguage.entries.forEach { option ->
                FilterChip(
                    selected = preferredLanguage == option.code,
                    onClick = { preferredLanguage = option.code },
                    label = { Text(option.label) },
                )
            }
        }
        Button(
            onClick = { onSaveProfile(displayName, email, preferredLanguage) },
            enabled = displayName.isNotBlank() && !state.busy,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(labels.saveProfile) }
        Button(
            onClick = { picker.launch("image/*") }, enabled = !state.busy, modifier = Modifier.fillMaxWidth(),
        ) { Text(labels.choosePhoto) }
        if (state.avatar != null) TextButton(
            onClick = onRemoveAvatar, enabled = !state.busy, modifier = Modifier.fillMaxWidth(),
        ) { Text(labels.removePhoto) }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        OutlinedTextField(
            value = currentPassword, onValueChange = { currentPassword = it.take(72) },
            label = { Text(labels.currentPassword) }, visualTransformation = PasswordVisualTransformation(),
            enabled = !state.busy, modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = newPassword, onValueChange = { newPassword = it.take(72) },
            label = { Text(labels.newPassword) }, visualTransformation = PasswordVisualTransformation(),
            supportingText = { Text(labels.passwordRule) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = confirmation, onValueChange = { confirmation = it.take(72) },
            label = { Text(labels.confirmPassword) }, visualTransformation = PasswordVisualTransformation(),
            isError = confirmation.isNotEmpty() && confirmation != newPassword,
            supportingText = if (confirmation.isNotEmpty() && confirmation != newPassword) {
                { Text(labels.passwordMismatch) }
            } else null,
            enabled = !state.busy, modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = {
                onChangePassword(currentPassword, newPassword)
                currentPassword = ""
                newPassword = ""
                confirmation = ""
            },
            enabled = currentPassword.isNotEmpty() && newPassword.length >= 12 &&
                newPassword == confirmation && !state.busy,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(labels.changePassword) }
    }
}

private fun adminErrorMessage(code: String, labels: AdminCopy): String = when (code) {
    "login_failed" -> labels.loginFailed
    "current_password_failed" -> labels.currentPasswordFailed
    "avatar_invalid" -> labels.avatarInvalid
    "too_many_attempts" -> labels.throttled
    "invalid_input" -> labels.invalidInput
    else -> labels.unavailable
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
    onMarkActivityRead: (String) -> Unit,
    onRescheduleAppointment: (String, String, String) -> Unit,
    onCancelAppointment: (String) -> Unit,
    onSaveProfile: (String, String) -> Unit,
    onChangePassword: (String, String) -> Unit,
    onUploadAvatar: (ByteArray, String) -> Unit,
    onRemoveAvatar: () -> Unit,
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
            onMarkActivityRead = onMarkActivityRead,
            onRescheduleAppointment = onRescheduleAppointment,
            onCancelAppointment = onCancelAppointment,
            onSaveProfile = onSaveProfile,
            onChangePassword = onChangePassword,
            onUploadAvatar = onUploadAvatar,
            onRemoveAvatar = onRemoveAvatar,
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
    onMarkActivityRead: (String) -> Unit,
    onRescheduleAppointment: (String, String, String) -> Unit,
    onCancelAppointment: (String) -> Unit,
    onSaveProfile: (String, String) -> Unit,
    onChangePassword: (String, String) -> Unit,
    onUploadAvatar: (ByteArray, String) -> Unit,
    onRemoveAvatar: () -> Unit,
    modifier: Modifier,
) {
    val overview = state.overview ?: return
    val uriHandler = LocalUriHandler.current
    val avatar = remember(state.avatar) {
        state.avatar?.let { bytes ->
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }
    }
    var section by remember { mutableStateOf(StudentSection.Overview) }

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
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StudentSection.entries.forEach { option ->
                    FilterChip(
                        selected = option == section,
                        onClick = { section = option },
                        label = { Text(studentSectionLabel(option, labels)) },
                    )
                }
            }
        }
        when (section) {
            StudentSection.Overview -> {
                item { StudentMetric(labels.upcoming, overview.upcoming.toString(), Aurora) }
                item { StudentMetric(labels.lessons, overview.lessons.toString(), Aurora) }
                item { StudentMetric(labels.unread, overview.unread.toString(), Nebula) }
                item { StudentMetric(labels.payments, overview.paymentsDue.toString(), Solar) }
                item { StudentMetric(labels.amountDue, formatEuros(overview.amountDueCents, language), Orbit) }
            }
            StudentSection.Agenda -> {
                if (state.agenda.isEmpty()) item { EmptyStudentSignal(labels.emptyAgenda) }
                items(state.agenda, key = { it.id }) {
                    AgendaCard(it, language, labels, state.busy, onRescheduleAppointment, onCancelAppointment)
                }
            }
            StudentSection.Lessons -> {
                if (state.lessons.isEmpty()) item { EmptyStudentSignal(labels.emptyLessons) }
                items(state.lessons, key = { it.id }) { LessonCard(it, language, labels) }
            }
            StudentSection.Activity -> {
                if (state.activity.isEmpty()) item { EmptyStudentSignal(labels.emptyActivity) }
                items(state.activity, key = { it.id }) {
                    ActivityCard(it, language, labels, onMarkActivityRead)
                }
            }
            StudentSection.Payments -> {
                state.billing?.profile?.let { profile ->
                    item {
                        StudentInfoCard(
                            title = labels.personalTerms,
                            body = buildString {
                                append(formatEuros(profile.amountCents, language))
                                append(" · ${billingCycleLabel(profile.billingCycle, labels)}")
                                profile.sessionMinutes?.let { append(" · $it ${labels.duration}") }
                                profile.publicNote?.takeIf(String::isNotBlank)?.let { append("\n$it") }
                            },
                            accent = Solar,
                        )
                    }
                }
                val payments = state.billing?.items.orEmpty()
                if (payments.isEmpty()) item { EmptyStudentSignal(labels.emptyPayments) }
                items(payments, key = { it.id }) { PaymentCard(it, language, labels) }
                state.billing?.paymentUrl?.let { paymentUrl ->
                    item {
                        Button(
                            onClick = { uriHandler.openUri(paymentUrl) },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(labels.payPal) }
                    }
                }
            }
            StudentSection.Profile -> item {
                StudentProfilePanel(
                    state = state,
                    labels = labels,
                    onSaveProfile = onSaveProfile,
                    onChangePassword = onChangePassword,
                    onUploadAvatar = onUploadAvatar,
                    onRemoveAvatar = onRemoveAvatar,
                )
            }
        }
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

private enum class StudentSection {
    Overview,
    Agenda,
    Lessons,
    Activity,
    Payments,
    Profile,
}

private fun studentSectionLabel(section: StudentSection, labels: StudentCopy): String = when (section) {
    StudentSection.Overview -> labels.overviewTab
    StudentSection.Agenda -> labels.agendaTab
    StudentSection.Lessons -> labels.lessonsTab
    StudentSection.Activity -> labels.activityTab
    StudentSection.Payments -> labels.paymentsTab
    StudentSection.Profile -> labels.profileTab
}

@Composable
private fun StudentProfilePanel(
    state: StudentUiState,
    labels: StudentCopy,
    onSaveProfile: (String, String) -> Unit,
    onChangePassword: (String, String) -> Unit,
    onUploadAvatar: (ByteArray, String) -> Unit,
    onRemoveAvatar: () -> Unit,
) {
    val profile = state.profile ?: return
    var displayName by remember(profile.displayName) { mutableStateOf(profile.displayName) }
    var preferredLanguage by remember(profile.preferredLanguage) { mutableStateOf(profile.preferredLanguage) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val mime = normalizedImageMime(context.contentResolver.getType(uri).orEmpty())
            onUploadAvatar(readAvatarBytes(context, uri) ?: ByteArray(0), mime)
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(labels.profileIntro, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it.take(120) },
            label = { Text(labels.displayName) },
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = profile.email,
            onValueChange = {},
            label = { Text(labels.email) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppLanguage.entries.forEach { option ->
                FilterChip(
                    selected = preferredLanguage == option.code,
                    onClick = { preferredLanguage = option.code },
                    label = { Text(option.label) },
                )
            }
        }
        Button(
            onClick = { onSaveProfile(displayName, preferredLanguage) },
            enabled = displayName.isNotBlank() && !state.busy,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(labels.saveProfile) }
        Button(
            onClick = { picker.launch("image/*") },
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(labels.choosePhoto) }
        if (state.avatar != null) {
            TextButton(
                onClick = onRemoveAvatar,
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(labels.removePhoto) }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        OutlinedTextField(
            value = currentPassword,
            onValueChange = { currentPassword = it.take(72) },
            label = { Text(labels.currentPassword) },
            visualTransformation = PasswordVisualTransformation(),
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it.take(72) },
            label = { Text(labels.newPassword) },
            visualTransformation = PasswordVisualTransformation(),
            supportingText = { Text(labels.passwordRule) },
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = confirmation,
            onValueChange = { confirmation = it.take(72) },
            label = { Text(labels.confirmPassword) },
            visualTransformation = PasswordVisualTransformation(),
            isError = confirmation.isNotEmpty() && confirmation != newPassword,
            supportingText = if (confirmation.isNotEmpty() && confirmation != newPassword) {
                { Text(labels.passwordMismatch) }
            } else null,
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = {
                onChangePassword(currentPassword, newPassword)
                currentPassword = ""
                newPassword = ""
                confirmation = ""
            },
            enabled = currentPassword.isNotEmpty() && newPassword.length >= 12 &&
                newPassword == confirmation && !state.busy,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(labels.changePassword) }
    }
}

@Composable
private fun AgendaCard(
    item: StudentAgendaItem,
    language: AppLanguage,
    labels: StudentCopy,
    busy: Boolean,
    onReschedule: (String, String, String) -> Unit,
    onCancel: (String) -> Unit,
) {
    val status = when (item.status) {
        "completed" -> labels.completed
        "cancelled" -> labels.cancelled
        else -> labels.planned
    }
    val accent = when (item.status) {
        "completed" -> Aurora
        "cancelled" -> MaterialTheme.colorScheme.error
        else -> Orbit
    }
    var editing by remember(item.id) { mutableStateOf(false) }
    var confirmCancel by remember(item.id) { mutableStateOf(false) }
    var date by remember(item.sessionDate) { mutableStateOf(item.sessionDate) }
    var time by remember(item.startTime) { mutableStateOf(item.startTime ?: "12:00") }
    val context = LocalContext.current
    Surface(
        color = Panel.copy(alpha = 0.9f),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(localizedDate(item.sessionDate, language), color = accent, fontWeight = FontWeight.Bold)
            Text("${item.startTime ?: labels.flexibleTime} · ${item.durationMinutes} ${labels.duration}\n$status")
            if (item.canManage) {
                if (editing) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = {
                            val parts = date.split("-").mapNotNull(String::toIntOrNull)
                            DatePickerDialog(context, { _, year, month, day ->
                                date = "%04d-%02d-%02d".format(year, month + 1, day)
                            }, parts.getOrElse(0) { 2026 }, parts.getOrElse(1) { 1 } - 1,
                                parts.getOrElse(2) { 1 }).show()
                        }) { Text("📅 ${localizedDate(date, language)}") }
                        TextButton(onClick = {
                            val parts = time.split(":").mapNotNull(String::toIntOrNull)
                            TimePickerDialog(context, { _, hour, minute ->
                                time = "%02d:%02d".format(hour, minute)
                            }, parts.getOrElse(0) { 12 }, parts.getOrElse(1) { 0 }, true).show()
                        }) { Text("🕒 $time") }
                    }
                    Button(
                        onClick = { onReschedule(item.id, date, time); editing = false },
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(labels.saveAppointment) }
                } else {
                    TextButton(onClick = { editing = true }, enabled = !busy) { Text(labels.reschedule) }
                }
                TextButton(onClick = { confirmCancel = true }, enabled = !busy) {
                    Text(labels.cancelAppointment, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
    if (confirmCancel) {
        AlertDialog(
            onDismissRequest = { confirmCancel = false },
            title = { Text(labels.cancelConfirm) },
            confirmButton = {
                TextButton(onClick = { confirmCancel = false; onCancel(item.id) }) {
                    Text(labels.cancelAppointment, color = MaterialTheme.colorScheme.error)
                }
            },
        )
    }
}

@Composable
private fun LessonCard(item: StudentLessonItem, language: AppLanguage, labels: StudentCopy) {
    Surface(
        color = Panel.copy(alpha = 0.9f),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(item.title, color = Starlight, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(localizedDate(item.sessionDate, language), color = Aurora, fontSize = 13.sp)
            LessonField(labels.summary, item.summary)
            LessonField(labels.progress, item.progress)
            LessonField(labels.practice, item.practice)
        }
    }
}

@Composable
private fun LessonField(label: String, value: String) {
    if (value.isBlank()) return
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label.uppercase(), color = Orbit, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ActivityCard(
    item: StudentActivityItem,
    language: AppLanguage,
    labels: StudentCopy,
    onMarkRead: (String) -> Unit,
) {
    val unread = item.readAt == null
    Surface(
        color = if (unread) Nebula.copy(alpha = 0.2f) else Panel.copy(alpha = 0.86f),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(labels.lessonPublished, color = Starlight, fontWeight = FontWeight.Bold)
            Text(item.title, color = Aurora, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(localizedDate(item.sessionDate, language), color = MaterialTheme.colorScheme.onSurfaceVariant)
            LessonField(labels.summary, item.summary)
            LessonField(labels.progress, item.progress)
            LessonField(labels.practice, item.practice)
            Text(
                localizedDate(item.createdAt.take(10), language),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
            Text(
                if (unread) labels.unreadLabel else labels.readLabel,
                color = if (unread) Nebula else Aurora,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
            if (unread) {
                TextButton(onClick = { onMarkRead(item.id) }) { Text(labels.markRead) }
            }
        }
    }
}

@Composable
private fun PaymentCard(item: StudentPaymentItem, language: AppLanguage, labels: StudentCopy) {
    val status = when (item.status) {
        "paid" -> labels.paid
        "waived" -> labels.waived
        else -> labels.due
    }
    val accent = if (item.status == "due") Solar else Aurora
    Surface(
        color = Panel.copy(alpha = 0.9f),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(item.label, color = Starlight, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(formatEuros(item.amountCents, language), color = accent, fontWeight = FontWeight.Black)
            }
            Text("${labels.dueOn} · ${localizedDate(item.dueOn, language)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(status, color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            item.paymentMethod?.let {
                Text(
                    "${labels.paymentMethod} · ${paymentMethodLabel(it, labels)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                )
            }
            item.publicNote?.takeIf(String::isNotBlank)?.let {
                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun StudentInfoCard(title: String, body: String, accent: Color) {
    Surface(
        color = Panel.copy(alpha = 0.9f),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(title, color = Starlight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(body, color = accent)
        }
    }
}

@Composable
private fun EmptyStudentSignal(message: String) {
    Surface(
        color = Panel.copy(alpha = 0.72f),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(22.dp),
        )
    }
}

private fun localizedDate(value: String, language: AppLanguage): String {
    val locale = when (language) {
        AppLanguage.English -> Locale.UK
        AppLanguage.French -> Locale.FRANCE
        AppLanguage.German -> Locale.GERMANY
    }
    return runCatching {
        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { isLenient = false }.parse(value)
            ?: return@runCatching value
        SimpleDateFormat("EEE d MMM yyyy", locale).format(parsed)
    }.getOrDefault(value)
}

private fun readAvatarBytes(context: Context, uri: Uri): ByteArray? = runCatching {
    context.contentResolver.openInputStream(uri)?.use { input ->
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var total = 0
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            total += read
            if (total > 5 * 1024 * 1024) return@runCatching null
            output.write(buffer, 0, read)
        }
        output.toByteArray()
    }
}.getOrNull()

private fun normalizedImageMime(value: String): String = if (value == "image/jpg") "image/jpeg" else value

private fun billingCycleLabel(value: String, labels: StudentCopy): String = when (value) {
    "fortnightly" -> labels.everyTwoWeeks
    "monthly" -> labels.monthly
    else -> labels.perLesson
}

private fun paymentMethodLabel(value: String, labels: StudentCopy): String = when (value) {
    "paypal" -> "PayPal"
    "cash" -> labels.cash
    "bank_transfer" -> labels.bankTransfer
    else -> labels.otherMethod
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
    "current_password_failed" -> labels.currentPasswordFailed
    "avatar_invalid" -> labels.avatarInvalid
    "appointment_closed" -> labels.appointmentClosed
    "appointment_unavailable" -> labels.appointmentUnavailable
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
        Destination.Admin -> Unit
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
