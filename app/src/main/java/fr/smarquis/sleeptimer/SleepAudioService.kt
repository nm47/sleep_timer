package fr.smarquis.sleeptimer

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_MUSIC
import android.media.AudioAttributes.USAGE_MEDIA
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
import android.os.Build
import android.os.Handler
import android.os.Looper
import fr.smarquis.sleeptimer.SleepTileService.Companion.requestTileUpdate
import fr.smarquis.sleeptimer.media.SessionController
import java.util.concurrent.TimeUnit.SECONDS

@Suppress("DEPRECATION")
class SleepAudioService : android.app.IntentService("SleepAudioService") {

    companion object {
        private val SETTLE_TIME_MILLIS = SECONDS.toMillis(2)

        private fun intent(context: Context) = Intent(context, SleepAudioService::class.java)
        fun pendingIntent(context: Context): PendingIntent? = PendingIntent.getService(context, 0, intent(context), FLAG_IMMUTABLE)
    }

    @Deprecated("Deprecated in Java")
    override fun onHandleIntent(intent: Intent?) {
        val audioManager = getSystemService(AudioManager::class.java) ?: return
        
        // 1. Try to pause active media sessions using NotificationListenerService
        val sessionController = SessionController.instance
        var paused = false
        if (sessionController != null) {
            paused = sessionController.pauseAllActiveSessions()
            
            // If sessions are still playing after initial pause, try aggressive stop
            Handler(Looper.getMainLooper()).postDelayed({
                if (sessionController.anySessionStillPlaying()) {
                    sessionController.pauseAllActiveSessions(aggressiveStop = true)
                }
            }, 600)
        }

        // 2. Fallback: Request audio focus to force other apps to pause/duck
        // This may be blocked on newer Android versions when screen is locked
        val attributes = AudioAttributes.Builder()
            .setUsage(USAGE_MEDIA)
            .setContentType(CONTENT_TYPE_MUSIC)
            .build()
        val focusRequest = AudioFocusRequest.Builder(AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(attributes)
            .setOnAudioFocusChangeListener {}
            .setAcceptsDelayedFocusGain(false)
            .build()
        
        try {
            audioManager.requestAudioFocus(focusRequest)
        } catch (e: Exception) {
            // Audio focus request may be blocked by Android security
        }

        // 3. Brief pause to let everything settle
        Thread.sleep(SETTLE_TIME_MILLIS)

        // Update tile
        requestTileUpdate()
    }

}