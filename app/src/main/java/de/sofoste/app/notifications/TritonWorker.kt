package de.sofoste.app.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class TritonWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        val tokenStorage = SecureDeviceTokenStorage(applicationContext)
        val preferences = TritonPreferences(applicationContext)
        val settings = preferences.settings()
        val token = tokenStorage.get()
        if (!settings.enabled || token == null) return Result.success()
        return try {
            StudentSignalApi().use { api ->
                val signal = api.load(token)
                TritonNotifications.ensureChannel(applicationContext, settings.language)
                val latestId = signal.latestActivityId?.toLongOrNull()
                val previousId = preferences.latestActivityId()
                if (settings.activity && latestId != null && previousId != null && latestId > previousId) {
                    TritonNotifications.activity(applicationContext, settings.language, signal.unreadCount)
                }
                preferences.latestActivityId(signal.latestActivityId)
                signal.nextAppointment?.let { appointment ->
                    val time = appointment.startTime ?: return@let
                    val key = "${appointment.id}:${appointment.sessionDate}:$time"
                    if (settings.agenda && key != preferences.lastAppointmentKey() &&
                        appointmentWithinDay(appointment.sessionDate, time)
                    ) {
                        TritonNotifications.agenda(applicationContext, settings.language)
                        preferences.lastAppointmentKey(key)
                    }
                }
            }
            Result.success()
        } catch (_: SignalAuthorizationException) {
            tokenStorage.clear(); preferences.disable(); Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private fun appointmentWithinDay(date: String, time: String): Boolean {
        val appointment = runCatching {
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply { isLenient = false }.parse("$date $time")
        }.getOrNull() ?: return false
        return appointment.time - System.currentTimeMillis() in 0..TimeUnit.HOURS.toMillis(24)
    }
}

object TritonScheduler {
    private const val UNIQUE_WORK = "sofoste-triton-private-signals"
    fun schedule(context: Context) {
        val work = PeriodicWorkRequestBuilder<TritonWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK, ExistingPeriodicWorkPolicy.UPDATE, work,
        )
    }
    fun cancel(context: Context) = WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK)
}
