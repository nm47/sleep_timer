package fr.smarquis.sleeptimer.media

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService

class SessionControllerService : NotificationListenerService() {

    private val msm by lazy {
        getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
    }

    override fun onCreate() {
        super.onCreate()
        SessionController.instance = this
    }

    override fun onDestroy() {
        SessionController.instance = null
        super.onDestroy()
    }

    fun pauseAllActiveSessions(
        denylist: Set<String> = DEFAULT_DENYLIST,
        aggressiveStop: Boolean = false
    ): Boolean {
        val sessions = msm.getActiveSessions(ComponentName(this, javaClass))
        var paused = false
        for (mc in sessions) {
            if (mc.packageName !in denylist &&
                mc.playbackState?.state == PlaybackState.STATE_PLAYING) {
                mc.transportControls.pause()
                if (aggressiveStop) mc.transportControls.stop()
                paused = true
            }
        }
        return paused
    }

    fun anySessionStillPlaying(denylist: Set<String> = DEFAULT_DENYLIST): Boolean {
        val sessions = msm.getActiveSessions(ComponentName(this, javaClass))
        return sessions.any { mc ->
            mc.packageName !in denylist &&
            mc.playbackState?.state == PlaybackState.STATE_PLAYING
        }
    }

    fun getActiveSessions(): List<MediaController> {
        return msm.getActiveSessions(ComponentName(this, javaClass))
    }

    companion object {
        val DEFAULT_DENYLIST = setOf(
            "com.android.server.telecom",
            "com.google.android.dialer",
            "fr.smarquis.sleeptimer" // Don't control our own app
        )
    }
}

object SessionController {
    @Volatile var instance: SessionControllerService? = null
}