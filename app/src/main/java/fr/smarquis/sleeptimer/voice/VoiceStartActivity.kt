package fr.smarquis.sleeptimer.voice

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import fr.smarquis.sleeptimer.SleepNotification.startTimer
import fr.smarquis.sleeptimer.SleepTileService.Companion.requestTileUpdate
import fr.smarquis.sleeptimer.media.NotificationPermissionHelper
import java.util.concurrent.TimeUnit.MINUTES

class VoiceStartActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        handleIntent()
        finish()
    }

    private fun handleIntent() {
        // Check for notification access permission (needed for reliable media control)
        if (!NotificationPermissionHelper.hasNotificationAccess(this)) {
            // If permission not granted, request it and still start the timer
            // Timer will work with audio focus fallback, but may be limited when screen is locked
            NotificationPermissionHelper.requestNotificationAccess(this)
        }
        
        // Check if launched via deep link with custom duration
        val data: Uri? = intent.data
        val timeoutMillis = if (data != null) {
            // Parse minutes parameter from deep link: sleeptimer://start?minutes=30
            val minutesStr = data.getQueryParameter("minutes")
            val minutes = minutesStr?.toLongOrNull()
            if (minutes != null) {
                // Validate minutes (clamp to 1-180)
                val validMinutes = minutes.coerceIn(1L, 180L)
                MINUTES.toMillis(validMinutes)
            } else {
                // Default to 2 hours if deep link has no valid minutes parameter
                MINUTES.toMillis(120L)
            }
        } else {
            // Default to 2 hours if opened normally (not via deep link)
            MINUTES.toMillis(120L)
        }
        
        // Start the timer using existing notification logic
        startTimer(timeoutMillis)
        
        // Update the Quick Settings tile to reflect the active timer
        requestTileUpdate()
    }
}