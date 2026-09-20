package de.sofoste.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import de.sofoste.app.MainActivity
import de.sofoste.app.R

object TritonNotifications {
    const val OPEN_STUDENT_ACTIVITY = "de.sofoste.app.OPEN_STUDENT_ACTIVITY"
    private const val CHANNEL_ID = "sofoste_private_signals"

    fun ensureChannel(context: Context, language: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val labels = copy(language)
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL_ID, labels.channel, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = labels.channelDescription
                lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
            },
        )
    }

    fun activity(context: Context, language: String, count: Int) {
        val labels = copy(language)
        post(context, 4101, labels.activityTitle, labels.activityBody(count))
    }
    fun agenda(context: Context, language: String) {
        val labels = copy(language)
        post(context, 4102, labels.agendaTitle, labels.agendaBody)
    }

    private fun post(context: Context, id: Int, title: String, body: String) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) return
        val intent = Intent(context, MainActivity::class.java).apply {
            action = OPEN_STUDENT_ACTIVITY
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    private fun copy(language: String): NotificationCopy = when (language) {
        "fr" -> NotificationCopy("Signaux privés Sofoste", "Activité et rappels sans contenu privé.",
            "Nouveau signal Sofoste", { if (it > 1) "$it nouvelles activités t’attendent." else "Une nouvelle activité t’attend." },
            "Rappel d’orbite Sofoste", "Une séance approche. Ouvre ton agenda pour les détails.")
        "de" -> NotificationCopy("Private Sofoste-Signale", "Aktivität und Erinnerungen ohne private Inhalte.",
            "Neues Sofoste-Signal", { if (it > 1) "$it neue Aktivitäten warten auf dich." else "Eine neue Aktivität wartet auf dich." },
            "Sofoste-Umlaufbahnerinnerung", "Eine Unterrichtsstunde rückt näher. Details findest du in deiner Agenda.")
        else -> NotificationCopy("Private Sofoste signals", "Activity and reminders without private content.",
            "New Sofoste signal", { if (it > 1) "$it new activities are waiting." else "A new activity is waiting." },
            "Sofoste orbit reminder", "A lesson is approaching. Open your agenda for details.")
    }

    private data class NotificationCopy(
        val channel: String, val channelDescription: String, val activityTitle: String,
        val activityBody: (Int) -> String, val agendaTitle: String, val agendaBody: String,
    )
}
