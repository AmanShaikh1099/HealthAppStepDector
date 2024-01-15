package Services
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.R.drawable.notification_icon_background
import androidx.core.app.NotificationCompat

class BackgroundService: Service() {

    private val CHANNEL_ID = "health_app_channel"
    private val NOTIFICATION_ID = 1

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // TODO: Implement your background tasks here

        // Optional: If your tasks are long-running, start the service as a foreground service
        startForegroundService()

        return START_STICKY // This makes the service restart if it gets terminated by the system
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

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
