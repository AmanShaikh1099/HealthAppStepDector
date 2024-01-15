package Services
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.R.drawable.notification_icon_background
import androidx.core.app.NotificationCompat
/**
 * A service that runs background tasks for a health application.
 *
 * This service is designed to perform operations in the background. When running long-term tasks,
 * it can be promoted to a foreground service, showing a notification to the user. This ensures that
 * the service is less likely to be killed by the system.
 *
 * Methods:
 * onStartCommand: Called when the service is started, to initiate background tasks.
 * onBind: Binds the service with a component that interacts with it.
 * startForegroundService: Starts the service as a foreground service with a notification.
 */
class BackgroundService: Service() {

    private val CHANNEL_ID = "health_app_channel"
    private val NOTIFICATION_ID = 1
    /**
     * Called by the system when the service is started.
     *
     * @param intent The Intent supplied to `startService()`, used for passing data to the service.
     * @param flags Additional data about this start request.
     * @param startId A unique integer representing this specific request to start.
     * @return Int The return value `START_STICKY` indicates that the service will be restarted
     *             if it's killed by the system.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // TODO: Implement your background tasks here

        // Optional: If your tasks are long-running, start the service as a foreground service
        startForegroundService()

        return START_STICKY // This makes the service restart if it gets terminated by the system
    }

    /**
     * Called when a client binds to the service using `bindService()`.
     *
     * @param intent The Intent used to bind to this service.
     * @return IBinder? Returns `null` as binding is not allowed.
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    /**
     * Starts the service as a foreground service.
     *
     * Sets up a notification channel and builds a notification, then starts the service in the
     * foreground. This is required for services running long-term operations in the background,
     * especially for Android Oreo and above.
     */
    private fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Health App Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Health App is Running")
                .setContentText("Background tasks are active")
                .setSmallIcon(notification_icon_background)
                .build()

            startForeground(NOTIFICATION_ID, notification)
        }
    }
}
