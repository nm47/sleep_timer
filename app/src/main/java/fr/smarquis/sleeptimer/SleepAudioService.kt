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
import android.media.session.MediaSessionManager
import android.os.Build
import fr.smarquis.sleeptimer.SleepTileService.Companion.requestTileUpdate
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
        val mediaSessionManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSystemService(MediaSessionManager::class.java)
        } else null

        // 1. Directly pause active media sessions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mediaSessionManager != null) {
            try {
                val activeSessions = mediaSessionManager.getActiveSessions(null)
                activeSessions.forEach { controller ->
                    try {
                        controller.transportControls?.pause()
                    } catch (e: Exception) {
                        // Some controllers may not allow external pause
                    }
                }
            } catch (e: SecurityException) {
                // Need notification access permission for this to work
            }
        }

        // 2. Request audio focus to force other apps to pause/duck
        val attributes = AudioAttributes.Builder()
            .setUsage(USAGE_MEDIA)
            .setContentType(CONTENT_TYPE_MUSIC)
            .build()
        val focusRequest = AudioFocusRequest.Builder(AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(attributes)
            .setOnAudioFocusChangeListener {}
            .setAcceptsDelayedFocusGain(false)
            .build()
        audioManager.requestAudioFocus(focusRequest)

        // 3. Brief pause to let everything settle
        Thread.sleep(SETTLE_TIME_MILLIS)

        // 4. Keep audio focus indefinitely to prevent apps from resuming
        // Focus will be released when another app requests it or device restarts

        // Update tile
        requestTileUpdate()
    }

}