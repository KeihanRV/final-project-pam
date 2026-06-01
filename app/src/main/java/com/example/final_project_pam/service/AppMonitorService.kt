package com.example.final_project_pam.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.final_project_pam.MainActivity
import com.example.final_project_pam.repository.AppSelectRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppMonitorService : AccessibilityService() {

    companion object {
        var instance: AppMonitorService? = null
        var allowedPackage: String? = null
        var isTimerRunning: Boolean = false
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var repository: AppSelectRepository

    override fun onServiceConnected() {
        instance = this
        repository = AppSelectRepository(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName || pkg == "com.android.systemui") return

        if (GatewayTimerService.monitoredPackages.contains(pkg)) {
            // Block jika timer tidak berjalan untuk app ini
            if (allowedPackage != pkg || !isTimerRunning) {
                // Cek apakah app sedang dalam status lock
                scope.launch {
                    val apps = try { repository.getCachedApps() } catch (e: Exception) { emptyList() }
                    val app = apps.find { it.packageName == pkg }
                    val isLocked = app != null && app.lockUntilTimestamp > System.currentTimeMillis()
                    if (isLocked || allowedPackage != pkg || !isTimerRunning) {
                        kotlinx.coroutines.withContext(Dispatchers.Main) {
                            forceOpenUnscroll()
                        }
                    }
                }
            }
        }
    }

    fun forceOpenUnscroll() {
        allowedPackage = null
        isTimerRunning = false
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("from_monitor", true)
        }
        try {
            startActivity(intent)
        } catch (_: Exception) { }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}