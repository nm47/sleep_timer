package fr.smarquis.sleeptimer

import android.content.BroadcastReceiver
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
import android.os.PowerManager
import fr.smarquis.sleeptimer.SleepTileService.Companion.requestTileUpdate
import java.util.concurrent.TimeUnit.SECONDS

class SleepAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_SLEEP_TIMER_ALARM = "fr.smarquis.sleeptimer.SLEEP_TIMER_ALARM"
        private val SETTLE_TIME_MILLIS = SECONDS.toMillis(2)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SLEEP_TIMER_ALARM) return

        // Acquire wake lock to ensure we can complete the audio stopping
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SleepTimer::AudioStopWakeLock"
        )
        wakeLock.acquire(10000) // 10 second timeout

        try {
            stopAudio(context)
        } finally {
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }

    private fun stopAudio(context: Context) {
        val audioManager = context.getSystemService(AudioManager::class.java) ?: return
        val mediaSessionManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.getSystemService(MediaSessionManager::class.java)
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

        // 5. Update tile and cancel notification
        context.requestTileUpdate()
        
        // Cancel the notification since timer has expired
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.cancel(R.id.notification_id)
    }
}