package com.example.final_project_pam.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.final_project_pam.MainActivity
import com.example.final_project_pam.R
import com.example.final_project_pam.repository.AppSelectRepository
import kotlinx.coroutines.*

class GatewayTimerService : Service() {

    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var repository: AppSelectRepository

    companion object {
        const val ACTION_START = "START_TIMER"
        const val ACTION_STOP = "STOP_TIMER"
        const val EXTRA_DURATION = "duration_minutes"
        const val EXTRA_PACKAGE = "target_package"
        const val CHANNEL_ID = "unscroll_timer"
        const val NOTIF_ID = 1
        const val NOTIF_ID_WARNING = 2
        const val NOTIF_ID_FINISHED = 3

        val monitoredPackages = mutableSetOf<String>()
    }

    override fun onCreate() {
        super.onCreate()
        repository = AppSelectRepository(applicationContext)
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

        // Simpan lock ke repository (15 menit) agar app tidak bisa dibuka lagi
        val lockUntil = System.currentTimeMillis() + (15L * 60 * 1000)
        repository.setLockForApp(targetPackage, lockUntil)

        // Kirim notifikasi sesi selesai
        sendTimerFinishedNotification(targetPackage)

        withContext(Dispatchers.Main) {
            AppMonitorService.instance?.forceOpenUnscroll()
        }
        stopSelf()
    }

    private fun sendTimerFinishedNotification(targetPackage: String) {
        val appLabel = try {
            val info = packageManager.getApplicationInfo(targetPackage, 0)
            packageManager.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            targetPackage
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("⏰ Waktu akses habis")
            .setContentText("Akses ke $appLabel telah selesai. Terkunci selama 15 menit.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        getSystemService(NotificationManager::class.java).notify(NOTIF_ID_FINISHED, notif)
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
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(content: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIF_ID, buildNotification(content))
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Unscroll Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Timer untuk monitoring screen time" }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        timerJob?.cancel()
        scope.cancel()
        AppMonitorService.isTimerRunning = false
        super.onDestroy()
    }
}