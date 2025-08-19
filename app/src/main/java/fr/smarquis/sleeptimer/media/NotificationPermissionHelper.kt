package fr.smarquis.sleeptimer.media

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService

object NotificationPermissionHelper {
    
    /**
     * Check if the app has notification listener permission
     */
    fun hasNotificationAccess(context: Context): Boolean {
        val componentName = ComponentName(context, SessionControllerService::class.java)
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return enabledServices?.contains(componentName.flattenToString()) == true
    }
    
    /**
     * Open notification access settings for the user to grant permission
     */
    fun requestNotificationAccess(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
    
    /**
     * Check permission and request if needed
     */
    fun ensureNotificationAccess(context: Context): Boolean {
        return if (hasNotificationAccess(context)) {
            true
        } else {
            requestNotificationAccess(context)
            false
        }
    }
}