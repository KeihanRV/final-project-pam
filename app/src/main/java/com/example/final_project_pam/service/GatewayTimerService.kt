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
        
        // Channel khusus untuk yang melayang (High Importance)
        const val CHANNEL_ALERTS = "unscroll_alerts_v2" 
        // Channel khusus untuk timer yang jalan terus (Low Importance - BIAR GA BUNYI)
        const val CHANNEL_SILENT = "unscroll_silent" 
        
        const val NOTIF_ID = 1
        const val NOTIF_ID_WARNING = 2
        const val NOTIF_ID_FINISHED = 3
        const val NOTIF_ID_FIVE_MIN_WARNING = 4

        val monitoredPackages = mutableSetOf<String>()
    }

    override fun onCreate() {
        super.onCreate()
        repository = AppSelectRepository(applicationContext)
        createNotificationChannels()
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
        val fiveMinMs = 5 * 60 * 1000L
        val warningMs = 30 * 1000L

        // PENTING: Gunakan CHANNEL_SILENT untuk timer agar tidak bunyi tiap 5 detik
        startForeground(NOTIF_ID, buildForegroundNotification("Timer berjalan: ${minutes}m 0s"))

        AppMonitorService.allowedPackage = targetPackage
        AppMonitorService.isTimerRunning = true

        timerJob = scope.launch {
            var remaining = totalMs
            var fiveMinWarningSent = false
            var warningSent = false

            while (remaining > 0) {
                delay(1000)
                remaining -= 1000

                if (remaining % 5000L == 0L) {
                    val min = remaining / 60000
                    val sec = (remaining % 60000) / 1000
                    updateForegroundNotification("Sisa waktu: ${min}m ${sec}s")
                }

                if (remaining <= fiveMinMs && !fiveMinWarningSent && totalMs > fiveMinMs) {
                    fiveMinWarningSent = true
                    sendFiveMinuteWarningNotification()
                }

                if (remaining <= warningMs && !warningSent) {
                    warningSent = true
                    sendWarningNotification()
                }
            }
            onTimerFinished(targetPackage, minutes)
        }
    }

    private suspend fun onTimerFinished(targetPackage: String, durationMinutes: Int) {
        AppMonitorService.isTimerRunning = false
        AppMonitorService.allowedPackage = null
        val lockUntil = System.currentTimeMillis() + (15L * 60 * 1000)
        repository.setLockForApp(targetPackage, lockUntil)

        val appLabel = try {
            val info = packageManager.getApplicationInfo(targetPackage, 0)
            packageManager.getApplicationLabel(info).toString()
        } catch (_: Exception) { targetPackage }

        val userId = repository.getUserId()
        if (userId != null) {
            repository.insertAppUsage(
                userId = userId,
                packageName = targetPackage,
                appName = appLabel,
                timeSpentMinutes = durationMinutes.toLong(),
                maxLimitMinutes = durationMinutes.toLong()
            )
        }

        sendTimerFinishedNotification(targetPackage)

        // Coba redirect via AppMonitorService (accessibility service)
        withContext(Dispatchers.Main) {
            if (AppMonitorService.instance != null) {
                AppMonitorService.instance!!.forceOpenUnscroll()
            } else {
                // Fallback: redirect langsung dari sini jika accessibility service tidak aktif
                forceOpenUnscrollDirect()
            }
        }

        // Beri waktu untuk sistem memproses intent redirect sebelum service mati
        delay(2000)
        stopSelf()
    }

    private fun forceOpenUnscrollDirect() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }

    private fun sendTimerFinishedNotification(targetPackage: String) {
        val appLabel = try {
            val info = packageManager.getApplicationInfo(targetPackage, 0)
            packageManager.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            targetPackage
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val notif = NotificationCompat.Builder(this, CHANNEL_ALERTS)
            .setContentTitle("⏰ Waktu akses habis")
            .setContentText("Akses ke $appLabel telah selesai.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        getSystemService(NotificationManager::class.java).notify(NOTIF_ID_FINISHED, notif)
    }

    private fun buildForegroundNotification(content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_SILENT)
            .setContentTitle("Unscroll aktif")
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true) // Tambahan biar makin sunyi
            .build()
    }

    private fun updateForegroundNotification(content: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIF_ID, buildForegroundNotification(content))
    }

    private fun sendFiveMinuteWarningNotification() {
        val notif = NotificationCompat.Builder(this, CHANNEL_ALERTS)
            .setContentTitle("⚠️ Peringatan Waktu")
            .setContentText("5 menit lagi akan berakhir")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        getSystemService(NotificationManager::class.java).notify(NOTIF_ID_FIVE_MIN_WARNING, notif)
    }

    private fun sendWarningNotification() {
        val notif = NotificationCompat.Builder(this, CHANNEL_ALERTS)
            .setContentTitle("⚠️ Waktu hampir habis!")
            .setContentText("Sisa 30 detik.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        getSystemService(NotificationManager::class.java).notify(NOTIF_ID_WARNING, notif)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            // 1. Channel Silent (Low Importance) -> Untuk Timer yang jalan terus
            val silentChannel = NotificationChannel(
                CHANNEL_SILENT, 
                "Timer Aktif", 
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Menampilkan sisa waktu tanpa suara"
                setShowBadge(false)
            }
            manager.createNotificationChannel(silentChannel)

            // 2. Channel Alerts (High Importance) -> Untuk Peringatan yang Melayang
            val alertChannel = NotificationChannel(
                CHANNEL_ALERTS, 
                "Peringatan Penting", 
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi yang muncul melayang saat waktu mau habis"
                enableVibration(true)
            }
            manager.createNotificationChannel(alertChannel)
        }
    }

    override fun onBind(intent: Intent?) = null
    override fun onDestroy() {
        timerJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }
}