package com.example.final_project_pam.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.final_project_pam.MainActivity
import com.example.final_project_pam.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GatewayTimerService : Service() {

    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    companion object {
        const val ACTION_START = "START_TIMER"
        const val ACTION_STOP = "STOP_TIMER"
        const val EXTRA_DURATION = "duration_minutes"
        const val EXTRA_PACKAGE = "target_package"
        const val CHANNEL_ID = "unscroll_timer"
        const val NOTIF_ID = 1
        const val NOTIF_ID_WARNING = 2

        // Set package yang sedang dimonitor (diisi dari Dashboard/ViewModel)
        val monitoredPackages = mutableSetOf<String>()
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val minutes = intent.getIntExtra(EXTRA_DURATION, 5)
                val pkg = intent.getStringExtra(EXTRA_PACKAGE) ?: return START_NOT_STICKY
                startTimer(minutes, pkg)
            }
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    private fun startTimer(minutes: Int, targetPackage: String) {
        val totalMs = minutes * 60 * 1000L
        val warningMs = 30 * 1000L

        startForeground(NOTIF_ID, buildNotification("Timer berjalan: ${minutes}m 0s"))

        AppMonitorService.allowedPackage = targetPackage
        AppMonitorService.isTimerRunning = true

        timerJob = scope.launch {
            var remaining = totalMs
            var warningSent = false

            while (remaining > 0) {
                delay(1000)
                remaining -= 1000

                if (remaining % 5000L == 0L) {
                    val min = remaining / 60000
                    val sec = (remaining % 60000) / 1000
                    updateNotification("Sisa waktu: ${min}m ${sec}s")
                }

                if (remaining <= warningMs && !warningSent) {
                    warningSent = true
                    sendWarningNotification()
                }
            }

            onTimerFinished(targetPackage)
        }
    }

    private suspend fun onTimerFinished(targetPackage: String) {
        AppMonitorService.isTimerRunning = false
        AppMonitorService.allowedPackage = null

        withContext(Dispatchers.Main) {
            AppMonitorService.instance?.forceOpenUnscroll()
        }

        stopSelf()
    }

    private fun buildNotification(content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Unscroll aktif")
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(content: String) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIF_ID, buildNotification(content))
    }

    private fun sendWarningNotification() {
        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("⚠️ Waktu hampir habis!")
            .setContentText("Sisa 30 detik. Unscroll akan segera aktif.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        getSystemService(NotificationManager::class.java).notify(NOTIF_ID_WARNING, notif)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Unscroll Timer",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Timer untuk monitoring screen time" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        timerJob?.cancel()
        scope.cancel()
        AppMonitorService.isTimerRunning = false
        super.onDestroy()
    }
}